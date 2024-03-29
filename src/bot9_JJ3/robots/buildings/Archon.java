package bot9_JJ3.robots.buildings;

import battlecode.common.*;
import java.util.*;

public class Archon extends Building{
    private static Integer minerCount = 0, minerFoundCount = 0, builderCount = 0, sageCount = 0, soldierCount = 0, labCount = 0, watchtowerCount = 0, minerCountMax = 0;
    private static int count = 0;
    private static int globalMinerCount = 0, globalBuilderCount, globalSageCount, globalSoldierCount, globalWatchtowerCount, globalLabCount;
    private static int targetMinerCount; //target # of miners to build across all archons
    private static int minersForNearbyLead;

    private static Integer minerIndex = 0; //spawning miners
    private static Integer soldierIndex = 0;
    private static int archonOrder = 0; //reverse position of archonID in shared array
    private static int power = 0; // power of 16 that corresponds with archonOrder
    private MapLocation target = null;
    private int leadLocs = 0;
    private static final int SURPLUS_THRESHOLD = 500;

    private static ArrayList<Direction> passableDirections = new ArrayList<Direction>();
    private boolean hasUpdatedDirections = false;

    private static HashMap<Integer, Integer> soldierHealth = new HashMap<Integer,Integer>();
    private static int soldierToHeal = 0;

    private static String indicatorString = "";

    public Archon(RobotController rc) {
        super(rc);
    }

    public Direction[] closestDirections(Direction d){
		return new Direction[]{
							d,
							d.rotateLeft(),
							d.rotateRight(),
							d.rotateLeft().rotateLeft(),
							d.rotateRight().rotateRight(),
							d.rotateLeft().rotateLeft().rotateLeft(),
							d.rotateRight().rotateRight().rotateRight(),
							d.opposite().rotateRight(),
							d.opposite().rotateLeft()
						};
	}

    @Override
    public void init() throws GameActionException {
        initialArchons = rc.getArchonCount();
        parseAnomalies();
        // write Archon ID to shared array
        if (rc.getArchonCount()==1){
            archonOrder = 0;
        }
        else {
            while(rc.readSharedArray(63-archonOrder)>0){
                archonOrder++;
            }
        }
        //write archon location to array
        MapLocation myLocation = rc.getLocation();
        int x = myLocation.x;
        int y = myLocation.y;
        switch (archonOrder){
            case 0:
                rc.writeSharedArray(15, x+y*256);
                break;
            case 1:
                rc.writeSharedArray(16, x+y*256);
                break;
            case 2:
                rc.writeSharedArray(49, x+y*256);
                break;
            case 3:
                rc.writeSharedArray(50, x+y*256);
                break;
        }
        rc.writeSharedArray(63-archonOrder,rc.getID()+1);
        power = (int)Math.pow(16,archonOrder);
        // Choose # of miners to build based on lead in surroundings
        leadLocs = rc.senseNearbyRobots(34).length;
        myArchonID = rc.getID();
        myArchonOrder = archonOrder;
        //labBuild = rc.getMapHeight()/40+1;
        setPassableDirections();
    }
    public void setPassableDirections() throws GameActionException{
        for (Direction d:directions){
            if (!rc.onTheMap(rc.getLocation().add(d)) || d==Direction.CENTER){
                continue;
            }
            if (passableDirections.size()==0){
                passableDirections.add(d);
                continue;
            }
            MapLocation location = rc.getLocation().add(d);
            int rubble = rc.senseRubble(location);
            for (int i=0;i<=passableDirections.size();i++){
                if (i==passableDirections.size()){
                    passableDirections.add(i,d);
                    break;
                }
                if (rubble<rc.senseRubble(rc.getLocation().add(passableDirections.get(i)))){
                    passableDirections.add(i,d);
                    break;
                }
            }
        }
    }
    public void setTargetLocation() throws GameActionException{
        int rubble = rc.senseRubble(rc.getLocation());
        ArrayList<MapLocation> lowPass = new ArrayList<>();
        int minDist=rc.getType().visionRadiusSquared+1;
        target=myLocation;
        for (MapLocation m: rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)){
            int dist = rc.getLocation().distanceSquaredTo(m);
            int r=rc.senseRubble(m);
            if(r<rubble){
                minDist = dist;
                lowPass.clear();
                lowPass.add(m);
                rubble=r;
                target=m;
            }
            if (r==rubble){
                lowPass.add(m);
                if (dist<minDist){
                    minDist = dist;
                    target = m;
                }
            }
        }
        if(!target.equals(rc.getLocation())){
            if(rc.getMode()==RobotMode.TURRET && rc.canTransform() && freeToTransform()){
                rc.transform();
                setTransformStatus();
            }
        }

    }
    public void writeLocationToArray() throws GameActionException{
        //write archon location to array
        myLocation = rc.getLocation();
        int x = myLocation.x;
        int y = myLocation.y;
        switch (archonOrder){
            case 0:
                rc.writeSharedArray(15, x+y*256);
                break;
            case 1:
                rc.writeSharedArray(16, x+y*256);
                break;
            case 2:
                rc.writeSharedArray(49, x+y*256);
                break;
            case 3:
                rc.writeSharedArray(50, x+y*256);
                break;
        }
    }
    public void move() throws GameActionException{
        if(!target.equals(rc.getLocation())){
            if(rc.getMode()==RobotMode.TURRET && rc.canTransform() && freeToTransform()) {
                rc.transform();
                setTransformStatus();
            }
            if (rc.isMovementReady()){
                intermediateMove(target);
                passableDirections.clear();
                setPassableDirections();
                writeLocationToArray();
            }
            indicatorString=rc.getLocation().toString()+target.toString()+(target.equals(rc.getLocation()));
        }
        else{
            if(rc.getLocation().equals(target) && rc.getMode()==RobotMode.PORTABLE && rc.canTransform()){
                rc.transform();
                setTransformStatus();
            }
        }
    }
    public void setTransformStatus() throws GameActionException{
        int currentStatus = rc.readSharedArray(17);
        int power = (int)Math.pow(2,archonOrder);
        int myStatus = (currentStatus%(power*2))/power;
        if (rc.getMode()==RobotMode.PORTABLE){
            rc.writeSharedArray(17,currentStatus-myStatus*power+power);
        }
        else{
            rc.writeSharedArray(17,currentStatus-myStatus*power);
        }
    }

    private int transforms = 0;
    public boolean freeToTransform() throws GameActionException{
        if (rc.getArchonCount()==1){
            return false;
        }
        int currentStatus = rc.readSharedArray(17);
        indicatorString+= " transformStatus: "+Integer.toBinaryString(currentStatus);
        int transformed = 0;
        for (int i=0;i<rc.getArchonCount();i++){
            int value = (int)Math.pow(2,i);
            transformed += (currentStatus%(value*2))/value;
        }
        //System.out.println(" transformStatus: "+Integer.toBinaryString(currentStatus)+" transformed: "+transformed);
        indicatorString+=" transformed: "+transformed;
        transforms = transformed;
        if (transformed>=rc.getArchonCount()-1){
            return false;
        }
        return true;
    }
    private boolean isArchon = false;
    // if enemies are near archon, spawn soldiers and inform other archons
    public boolean defense() throws GameActionException {
        isArchon = false;
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int current = rc.readSharedArray(56);
        int myValue = (current % (power*16))/power;
        boolean isEnemy = false;
        loop1: for (RobotInfo r:enemies){
            switch (r.getType()){
                case SOLDIER:
                case SAGE:
                case WATCHTOWER:
                    isEnemy = true; break loop1;
                case ARCHON:
                    isArchon = true;
            }
            if (r.getType()==RobotType.SOLDIER || r.getType()==RobotType.SAGE || r.getType()==RobotType.WATCHTOWER){
                isEnemy = true;
                break;
            }
        }
        if (isEnemy){
            rc.writeSharedArray(56, current - myValue*power + power);
        }
        else{
            rc.writeSharedArray(56, current - myValue*power);
            return false;
        }
        if (rc.getMode()==RobotMode.PORTABLE && rc.canTransform()){
            rc.transform();
            setTransformStatus();
        }
        if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.SOLDIER.buildCostLead){
            int i=0;
            while (i<passableDirections.size()-1 && !rc.canBuildRobot(RobotType.SOLDIER,passableDirections.get(i))){
                i++;
            }
            if (rc.canBuildRobot(RobotType.SOLDIER,passableDirections.get(i))){
                rc.buildRobot(RobotType.SOLDIER,passableDirections.get(i));
            }
        }
        return true;
    }

    public void buildWatchtower() throws GameActionException {
        int temp = (int)Math.pow(4,archonOrder);
        int currentValue = rc.readSharedArray(58);
        int previousBuildCommand = (currentValue % (temp * 4))/temp;
        int buildCommand = currentValue - previousBuildCommand * temp + temp * 2;
        watchtowerCount++;
        rc.writeSharedArray(58, buildCommand);
    }

    // Checks is all archons are alive; if not, refactor information in shared arrays
    public void checkArchonsAlive() throws GameActionException{
        if (rc.getArchonCount()<initialArchons){
            // check if the archon IDs in the shared array have been deleted (i.e. if another archon has already updated the IDs)
            int i=0;
            while (rc.readSharedArray(63-i)>0 && i<=initialArchons-1){
                i++;
            }
            if (i==initialArchons){ //this is the first archon to update the array
                //resets the archon IDs
                for(int j=0;j<4;j++){
                    rc.writeSharedArray(63-j, 0);
                }
                archonOrder = 0;
                //reset relevant arrays
                rc.writeSharedArray(63,rc.getID()+1);
                rc.writeSharedArray(56,0);
                rc.writeSharedArray(11,0);
            }
            else{
                //set appropriate archonOrder
                archonOrder = i;
                rc.writeSharedArray(63-archonOrder,rc.getID()+1);
            }
            power = (int)Math.pow(16,archonOrder);
            initialArchons = rc.getArchonCount();
        }
    }

    // update troop count variables from shared array
    public void updateTroopCount() throws GameActionException{
         if (roundNum%3==1){ //Reset Troop Counts
            rc.writeSharedArray(10, 0);
            for (int i=0;i<4;i++){
                rc.writeSharedArray(i, 0);
            }
            rc.writeSharedArray(44,0);
         }else if (rc.getRoundNum()%3==0){ //Create New Values
            if (archonOrder<=1){
                minerCount = (rc.readSharedArray(0)%((int)Math.pow(256,archonOrder+1)))/(int)Math.pow(256,archonOrder);
            }
            else{
                minerCount = (rc.readSharedArray(10)%((int)Math.pow(256,archonOrder-1)))/(int)Math.pow(256,archonOrder-2);
            }
            if(minerCount > minerCountMax)minerCountMax = minerCount;
            builderCount = (rc.readSharedArray(1)%(power*16))/(power);
            globalSageCount = rc.readSharedArray(2);
            globalMinerCount = rc.readSharedArray(44);
            globalSoldierCount = rc.readSharedArray(3);
            globalWatchtowerCount = rc.readSharedArray(5) + rc.readSharedArray(6) + rc.readSharedArray(7) + rc.readSharedArray(8);
            int lc = rc.readSharedArray(4);
            globalLabCount = lc % 16 + (lc % 256)/16 + (lc % 4096)/256 + lc / 4096;
            watchtowerCount = rc.readSharedArray(archonOrder+5);
            labCount = (rc.readSharedArray(4) % (power*16))/power;
        }
    }

    public boolean checkBuildStatus(int diff, int cost) throws GameActionException{ //controls the order of archons building troops so troops are spawned evenly
        // diff is the difference between the next archon to spawn and this archon's ID
        // if diff!=0, the archon is later in the order. Check if there are enough resources for this archon to build troops before the
        // next archon to spawn, otherwise don't spawn anything (return false).
        if (diff<0){
            if (rc.getTeamLeadAmount(rc.getTeam())-cost*-1*diff<cost){
                rc.setIndicatorString(indicatorString);
                return false;
            }
        }
        if (diff>0){
            int space = rc.getArchonCount()-diff;
            if (rc.getTeamLeadAmount(rc.getTeam())-cost*space<cost){
                rc.setIndicatorString(indicatorString);
                return false;
            }
        }
        return true;
    }
    
    private int roundNum = 0;

    public void repair() throws GameActionException {
        if (!rc.isActionReady()){
            return;
        }
        int leastAttackedHealth = RobotType.SOLDIER.health;
        MapLocation attackedLocation = null;

        MapLocation continueHealLocation = null;

        int leastSoldierHealth = RobotType.SOLDIER.health;
        MapLocation soldierLocation = null;
        int soldierID = 0;

        int leastMinerHealth = RobotType.MINER.health;
        MapLocation minerLocation = null;

        HashMap<Integer, Integer> newSoldierHealth = new HashMap<Integer, Integer>();

        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.ARCHON.actionRadiusSquared,rc.getTeam());
        for (RobotInfo robot: robots){
            MapLocation thisLocation = robot.getLocation();
            if (robot.type==RobotType.MINER){
                if (robot.getHealth()<leastMinerHealth && rc.canRepair(thisLocation)){
                    leastMinerHealth = robot.getHealth();
                    minerLocation = thisLocation;
                }
                continue;
            }
            if (soldierHealth.containsKey(robot.ID) && soldierHealth.get(robot.ID)>robot.getHealth() && robot.getHealth()<leastAttackedHealth && rc.canRepair(thisLocation)){
                //soldier is being attacked
                attackedLocation = thisLocation;
                leastAttackedHealth=robot.getHealth();
            }
            if (soldierToHeal == robot.getID() && robot.getHealth()<49 && rc.canRepair(thisLocation)){
                //soldier that is currently being healed
                continueHealLocation = thisLocation;
            }
            if (robot.getHealth()<leastSoldierHealth && rc.canRepair(thisLocation)){
                //find soldier to heal
                leastSoldierHealth = robot.getHealth();
                soldierLocation = thisLocation;
                soldierID = robot.ID;
            }
            newSoldierHealth.put(robot.ID, robot.getHealth());
        }
        soldierHealth = newSoldierHealth;
        if (attackedLocation!=null && rc.canRepair(attackedLocation)){
            indicatorString+=" healing attacked soldier";
            //repair soldier being attacked
            rc.repair(attackedLocation);
            return;
        }
        if (continueHealLocation!=null && rc.canRepair(continueHealLocation)){
            indicatorString+=" continue to heal soldier";
            rc.repair(continueHealLocation);
            return;
        }
        if (soldierLocation!=null && rc.canRepair(soldierLocation)){
            indicatorString+=" healing soldier";
            soldierToHeal = soldierID;
            rc.repair(soldierLocation);
            return;
        }
        if (minerLocation!=null && rc.canRepair(minerLocation)){
            indicatorString+=" healing miner";
            rc.repair(minerLocation);
        }
        indicatorString+=" healing none";
    }
    private void checkEnemies() throws GameActionException{
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(34,myTeam.opponent());
        int counter = 0;
        for(int i = nearbyEnemies.length; --i>=0;){
            switch (nearbyEnemies[i].getType()){
                case SAGE:
                case SOLDIER:
                case WATCHTOWER:
                    counter++;
            }
        }
        if(counter > 3 && rc.readSharedArray(45) == 0 && rc.getHealth() > 60){
            rc.writeSharedArray(45,64*myLocation.x+myLocation.y);
        }else if(rc.readSharedArray(45)== 64*myLocation.x+myLocation.y){
            rc.writeSharedArray(45,0);
        }
    }

    @Override
    public void run() throws GameActionException {
        indicatorString = "";
        checkEnemies();
        checkArchonsAlive();
        updateTroopCount();
        roundNum = rc.getRoundNum();
        if (defense()){
            repair();
            indicatorString += " defense";
            rc.setIndicatorString(indicatorString);
            return;
        }
        //add check here for miners
        if (globalMinerCount>=6){
            setTargetLocation();
            move();
        } else if (rc.getMode()==RobotMode.PORTABLE && rc.canTransform()){
            rc.transform();
            setTransformStatus();
        }
        /*
        if(!hasUpdatedDirections && target.equals(rc.getLocation())){
            passableDirections.clear();
            setPassableDirections();
        }
        */
        // START SPAWNING
        int archonBuildStatus = rc.readSharedArray(11);
        int diff = archonBuildStatus - archonOrder;
        int cost = RobotType.MINER.buildCostLead;
        RobotType type = RobotType.MINER;
        indicatorString+=" miners";
        if (!checkBuildStatus(diff, cost)) {
            repair();
            return;
        }
        int mod = 4;
        if(rc.readSharedArray(40) !=0){
            mod = 1;
        }else if (rc.readSharedArray(42)!= 0){ // if a miner has been sighted
            mod = 2;
        }
        if ((globalMinerCount < 6 || count%mod == 1)&&!isArchon){
            if (rc.getTeamLeadAmount(rc.getTeam())>=cost){
                int i=0;
                while (i<passableDirections.size()-1 && !rc.canBuildRobot(type,passableDirections.get(i))){
                    i++;
                }
                if (rc.canBuildRobot(type,passableDirections.get(i))){
                    if (diff==0){
                        if (archonBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,archonBuildStatus+1);
                        }
                    }
                    rc.buildRobot(type,passableDirections.get(i));
                    minerCount++;
                    globalMinerCount++;
                    count++;
                }
            }
        }
        else /*if (count %mod==1)*/{
            cost = RobotType.SOLDIER.buildCostLead;
            type = RobotType.SOLDIER;
            indicatorString += " soldiers";
            if (!checkBuildStatus(diff, cost)){
                repair();
                return;
            }
            if (rc.getTeamLeadAmount(rc.getTeam())>=cost){
                int i=0;
                while (i<passableDirections.size()-1 && !rc.canBuildRobot(type,passableDirections.get(i))){
                    i++;
                }
                if (rc.canBuildRobot(type,passableDirections.get(i))){
                    if (diff==0){
                        if (archonBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,archonBuildStatus+1);
                        }
                    }
                    rc.buildRobot(type,passableDirections.get(i));
                    soldierCount++;
                    count++;
                }
            }
        }
        repair();
        rc.setIndicatorString(indicatorString);
    }
}
