package bot8.robots.buildings;

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

    private static final int SURPLUS_THRESHOLD = 500;

    private static ArrayList<Direction> passableDirections = new ArrayList<>();

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
        int x = myLocation.x/4;
        int y = myLocation.y/4;
        if (archonOrder<=1){
            int temp = (int)Math.pow(256,archonOrder);
            rc.writeSharedArray(49, rc.readSharedArray(49)+x*temp+y*(temp*16));
        }
        else{
            int temp = (int)Math.pow(256,archonOrder-2);
            rc.writeSharedArray(50, rc.readSharedArray(50)+x*temp+y*(temp*16));
        }
        if (archonOrder==rc.getArchonCount()-1){
            //if this is the last archon, find the average distance between all of the archons
            if(archonOrder==0){
                rc.writeSharedArray(14, (int)Math.pow(2,14));
            }
            else if (archonOrder==1){
                int temp = rc.readSharedArray(49);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation one = new MapLocation(x,y);
                x = temp%4096/256*4;
                y = temp/4096*4;
                MapLocation two = new MapLocation(x,y);
                System.out.println(one);
                System.out.println(two);
                rc.writeSharedArray(14, movementTileDistance(one, two));
                System.out.println("average: "+movementTileDistance(one, two));
            }
            else if (archonOrder==2){
                int temp = rc.readSharedArray(49);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation one = new MapLocation(x,y);
                x = temp%4096/256*4;
                y = temp/4096*4;
                MapLocation two = new MapLocation(x,y);
                temp = rc.readSharedArray(50);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation three = new MapLocation(x,y);
                int average = (movementTileDistance(one, two)+movementTileDistance(one, three)+movementTileDistance(three, two))/3;
                rc.writeSharedArray(14, average);
                System.out.println("average: "+average);
            }
            else{
                int temp = rc.readSharedArray(49);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation one = new MapLocation(x,y);
                x = temp%4096/256*4;
                y = temp/4096*4;
                MapLocation two = new MapLocation(x,y);
                temp = rc.readSharedArray(50);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation three = new MapLocation(x,y);
                x = temp%4096/256*4;
                y = temp/4096*4;
                MapLocation four = new MapLocation(x,y);
                int average = (movementTileDistance(one, two)+movementTileDistance(one, three)+movementTileDistance(one, four)+movementTileDistance(three, two)+movementTileDistance(four, two)+movementTileDistance(three, four))/6;
                rc.writeSharedArray(14, average);
                System.out.println("average: "+average);
            }
        }
        rc.writeSharedArray(63-archonOrder,rc.getID()+1);
        power = (int)Math.pow(16,archonOrder);
        // Choose # of miners to build based on lead in surroundings
        minersForNearbyLead = (int) Math.ceil(rc.senseNearbyLocationsWithLead(34).length/9.0);
        myArchonID = rc.getID();
        myArchonOrder = archonOrder;
        //labBuild = rc.getMapHeight()/40+1;

        for (Direction d:Direction.allDirections()){
            if (!rc.onTheMap(rc.adjacentLocation(d)) || d==Direction.CENTER){
                continue;
            }
            if (passableDirections.size()==0){
                passableDirections.add(d);
                continue;
            }
            MapLocation location = rc.adjacentLocation(d);
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

    // if enemies are near archon, spawn soldiers and inform other archons
    public boolean defense() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int current = rc.readSharedArray(56);
        int myValue = (current % (power*16))/power;
        boolean isEnemy = false;
        for (RobotInfo r:enemies){
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

    // NOT substitute for build watchtower code in run(), only used when surplus of lead is achieved
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
            System.out.println("Reassigning archonOrder "+archonOrder);
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
        if (rc.isActionReady()){
            RobotInfo[] robots = rc.senseNearbyRobots(RobotType.ARCHON.actionRadiusSquared,rc.getTeam());
            int greatestHealthDifference = 0;
            MapLocation location = null;
            for (RobotInfo robot : robots){
                if (robot.getMode() == RobotMode.DROID && robot.getType().health-robot.getHealth()>greatestHealthDifference){
                    greatestHealthDifference=robot.getType().health-robot.getHealth();
                    location = robot.getLocation();
                }
            }
            if (location!=null && rc.canRepair(location)){
                rc.repair(location);
            }
        }
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
        if(counter > 3 && rc.readSharedArray(45) == 0){
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
        avoidFury();
        if (defense()){
            repair();
            indicatorString += " defense";
            rc.setIndicatorString(indicatorString);
            return;
        }
        /*
        if (!canProceed()){
            indicatorString += " can't proceed";
            rc.setIndicatorString(indicatorString);
            return;
        }
        */
        // START SPAWNING
        int mod = 4;
        if (rc.getTeamLeadAmount(rc.getTeam())>1000 && builderCount<7){
            mod = 5;
        }
        if(rc.readSharedArray(40) !=0){
            mod = 1;
        }else if (rc.readSharedArray(42)!= 0){ // if a miner has been sighted
            mod = 2;
        }
        int archonBuildStatus = rc.readSharedArray(11);
        int diff = archonBuildStatus - archonOrder;
        int cost = RobotType.MINER.buildCostLead;
        RobotType type = RobotType.MINER;
        if (globalMinerCount>=6 && count%mod!=1){
            cost = RobotType.SOLDIER.buildCostLead;
            type = RobotType.SOLDIER;
        }
        if (!checkBuildStatus(diff, cost)){
            repair();
            return;
        }
        if (globalMinerCount < 6 || count%mod == 1){
            indicatorString+=" miners";
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
            if (!checkBuildStatus(diff, cost)) {
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
