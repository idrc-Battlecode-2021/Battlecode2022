package bot10_MC3.robots.buildings;

import battlecode.common.*;
import bot10_MC3.util.Constants;
import bot10_MC3.util.PathFinding30;

import java.util.*;

public class Archon extends Building{
    private static Integer  minerFoundCount = 0, builderCount = 0, sageCount = 0, soldierCount = 0, labCount = 0, watchtowerCount = 0, minerCountMax = 0;
    private static int count = 0;
    private static int globalMinerCount = 0, globalBuilderCount, globalSageCount, globalSoldierCount, globalWatchtowerCount, globalLabCount;
    private static int targetMinerCount; //target # of miners to build across all archons
    private static int minersForNearbyLead;
    private static int minerMin, minerMax;
    private static int peakMiner = 0;

    private static Integer minerIndex = 0; //spawning miners
    private static Integer soldierIndex = 0;
    private static int archonOrder = 0; //reverse position of archonID in shared array
    private static int power = 0; // power of 16 that corresponds with archonOrder
    private MapLocation bestTargetLocation = null;
    private static final int SURPLUS_THRESHOLD = 500;

    private static ArrayList<Direction> passableDirections = new ArrayList<Direction>();
    private boolean hasUpdatedDirections = false;

    private static HashMap<Integer, Integer> nearbyTroopHealth = new HashMap<Integer,Integer>();
    private static int soldierToHeal = 0;

    private static int minerThreshold = 200; //TODO: make miner and lab thresholds global

    private static String indicatorString = "";
    private PathFinding30 pfs;

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
        pfs=new PathFinding30(rc);
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
        minersForNearbyLead = (int) Math.ceil(rc.senseNearbyLocationsWithLead(34).length/9.0);
        myArchonID = rc.getID();
        myArchonOrder = archonOrder;
        //labBuild = rc.getMapHeight()/40+1;
        setPassableDirections();
        checkEdge();
        //System.out.println(isEdge);
        targetMinerCount = 3+(Math.min(mapHeight, mapWidth)-20)/3;
        minerMax = (int) Math.sqrt(mapHeight*mapWidth)/2;
        minerMin = minerMax/10*3;
        bestTargetLocation = rc.getLocation();
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
    /*
    public void setTargetLocation() throws GameActionException{
        int rubble = rc.senseRubble(rc.getLocation());
        int minDist=0;
        bestTargetLocation=myLocation;
        for (MapLocation m: rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)){
            if (rc.canSenseRobotAtLocation(m))continue;
            int dist = rc.getLocation().distanceSquaredTo(m);
            int r=rc.senseRubble(m);
            if(r<rubble){
                minDist = dist;
                rubble=r;
                bestTargetLocation=m;
            }
            if (r==rubble){
                if (dist<minDist){
                    minDist = dist;
                    bestTargetLocation = m;
                }
            }
        }
        if(!bestTargetLocation.equals(rc.getLocation())){
            if(rc.getMode()==RobotMode.TURRET && rc.canTransform() && freeToTransform()){
                rc.transform();
                setTransformStatus();
            }
        }
    }
    */

    public void setOffensiveTarget() throws GameActionException{
        int bytecode = Clock.getBytecodeNum();
        int rubble = rc.senseRubble(rc.getLocation());
        if (bestTargetLocation==null){
            bestTargetLocation = rc.getLocation();
        }
        if (!rc.canSenseLocation(bestTargetLocation)){
            System.out.println("CANT SENSE BEST TARGET: "+bestTargetLocation);
            return;
        }
        if (!bestTargetLocation.equals(rc.getLocation()) && rc.canSenseRobotAtLocation(bestTargetLocation) && rc.senseRobotAtLocation(bestTargetLocation).getMode()!=RobotMode.DROID){
            bestTargetLocation = rc.getLocation();
        }
        int xCheck = Math.abs(center.x-bestTargetLocation.x);
        int yCheck = Math.abs(center.y-bestTargetLocation.y);
        //TODO: prioritize number of tiles on the map around it
        int averageSurroundingRubble = 0;
        int count = 0;
        for (Direction d: Constants.DIRECTIONS ){
            MapLocation thisLocation = rc.adjacentLocation(d);
            if (!rc.onTheMap(thisLocation) || !rc.canSenseLocation(thisLocation))continue;
            count++;
            averageSurroundingRubble+=rc.senseRubble(thisLocation);
        }
        averageSurroundingRubble/=count;
        for (MapLocation m: rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared-2)){
            int r=rc.senseRubble(m);
            if (r>rubble)continue;
            int xTemp = Math.abs(center.x-m.x);
            int yTemp = Math.abs(center.y-m.y);
            if (xTemp+yTemp > xCheck+yCheck+2)continue;
            if (rc.canSenseRobotAtLocation(m) && rc.senseRobotAtLocation(m).getMode()!=RobotMode.DROID)continue;
            int tempRubble = 0;
            int tempCount = 0;
            for (Direction d: Constants.DIRECTIONS ){
                MapLocation thisLocation = m.add(d);
                if (!rc.canSenseLocation(thisLocation))continue;
                tempCount++;
                tempRubble+=rc.senseRubble(thisLocation);
            }
            tempRubble/=tempCount;
            if (tempCount<6)continue;
            if (r<rubble){
                rubble = r;
                bestTargetLocation = m;
                xCheck = xTemp;
                yCheck = yTemp;
                averageSurroundingRubble = tempRubble;
                count = tempCount;
            }
            else if (r==rubble){
                if(tempRubble<averageSurroundingRubble){
                    rubble = r;
                    bestTargetLocation = m;
                    xCheck = xTemp;
                    yCheck = yTemp;
                    averageSurroundingRubble = tempRubble;
                    count = tempCount;
                }
                else if (tempRubble==averageSurroundingRubble){
                    if (xTemp+yTemp<xCheck+yCheck){
                        rubble = r;
                        bestTargetLocation = m;
                        xCheck = xTemp;
                        yCheck = yTemp;
                        averageSurroundingRubble = tempRubble;
                        count = tempCount;
                    }
                }
            }
            
        }
        if(!bestTargetLocation.equals(rc.getLocation())){
            if(rc.getMode()==RobotMode.TURRET && rc.canTransform() && freeToTransform()){
                rc.transform();
                pastLocations.clear();
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
        if(!bestTargetLocation.equals(rc.getLocation())){
            if(rc.getMode()==RobotMode.TURRET && rc.canTransform() && freeToTransform()) {
                rc.transform();
                pastLocations.clear();
                setTransformStatus();
            }
            if (rc.isMovementReady()){
                //archonMove(target);
                System.out.println("1: "+Clock.getBytecodesLeft());
                //TODO: Check if Bytecode Permits
                soldierMove(bestTargetLocation);
                System.out.println("2: "+Clock.getBytecodesLeft());
                passableDirections.clear();
                setPassableDirections();
                writeLocationToArray();
            }
            System.out.println(rc.getLocation().toString()+bestTargetLocation.toString()+(bestTargetLocation.equals(rc.getLocation())));
        }
        else{
            if(rc.getLocation().equals(bestTargetLocation) && rc.getMode()==RobotMode.PORTABLE && rc.canTransform()){
                rc.transform();
                pastLocations.clear();
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
        if (rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent()).length>0){
            return false;
        }
        if (rc.getArchonCount()==1){
            return true;
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
    // if enemies are near archon, spawn soldiers and inform other archons
    private boolean nearEnemyArchon = false;
    public boolean defense() throws GameActionException {
        nearEnemyArchon = false;
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int current = rc.readSharedArray(56);
        int myValue = (current % (power*16))/power;
        boolean isEnemy = false;
        boolean isSupport = false;
        loop1: for (RobotInfo r:enemies){
            switch(r.getType()){
                case SOLDIER:
                case SAGE:
                case WATCHTOWER:
                    isEnemy = true; break loop1;
                case ARCHON:
                    nearEnemyArchon = true;
            }
        }
        if (isEnemy){
            rc.writeSharedArray(56, current - myValue*power + power);
        }
        else{
            rc.writeSharedArray(56, current - myValue*power);
            if (nearEnemyArchon && rc.getMode()==RobotMode.PORTABLE && rc.canTransform()){
                rc.transform();
                pastLocations.clear();
                setTransformStatus();
            }
            return false;
        }
        if (rc.getMode()==RobotMode.PORTABLE && rc.canTransform()){
            rc.transform();
            pastLocations.clear();
            setTransformStatus();
        }
        if(rc.getTeamGoldAmount(myTeam)>=RobotType.SAGE.buildCostGold){
            int i=0;
            while (i<passableDirections.size()-1 && !rc.canBuildRobot(RobotType.SAGE,passableDirections.get(i))){
                i++;
            }
            if (rc.canBuildRobot(RobotType.SAGE,passableDirections.get(i))){
                rc.buildRobot(RobotType.SAGE,passableDirections.get(i));
            }
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
                rc.writeSharedArray(10,0);
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
        if (rc.getRoundNum()%3!=2){
            /*
            if (archonOrder<=1){
                minerCount = (rc.readSharedArray(0)%((int)Math.pow(256,archonOrder+1)))/(int)Math.pow(256,archonOrder);
            }
            else{
                minerCount = (rc.readSharedArray(10)%((int)Math.pow(256,archonOrder-1)))/(int)Math.pow(256,archonOrder-2);
            }
            */
            //if(minerCount > minerCountMax)minerCountMax = minerCount;
            builderCount = rc.readSharedArray(1);
            globalBuilderCount = builderCount;
            globalSageCount = rc.readSharedArray(2);
            globalMinerCount = rc.readSharedArray(44);
            peakMiner = Math.max(globalMinerCount, peakMiner);
            globalSoldierCount = rc.readSharedArray(3);
            globalWatchtowerCount = rc.readSharedArray(5) + rc.readSharedArray(6) + rc.readSharedArray(7) + rc.readSharedArray(8);
            globalLabCount = rc.readSharedArray(4);
            watchtowerCount = rc.readSharedArray(archonOrder+5);
            labCount = (rc.readSharedArray(4) % (power*16))/power;
        }
        if (roundNum%3==1 && archonOrder==rc.getArchonCount()-1){ //Reset Troop Counts only if is last archon
            for (int i=0;i<5;i++){
                rc.writeSharedArray(i, 0);
            }
            rc.writeSharedArray(44,0);
        }
    }

    public boolean checkLeadBuildStatus(int diff, int cost) throws GameActionException{ //controls the order of archons building troops so troops are spawned evenly
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

    public boolean checkGoldBuildStatus(int diff, int cost) throws GameActionException{ //controls the order of archons building troops so troops are spawned evenly
        // diff is the difference between the next archon to spawn and this archon's ID
        // if diff!=0, the archon is later in the order. Check if there are enough resources for this archon to build troops before the
        // next archon to spawn, otherwise don't spawn anything (return false).
        if (diff<0){
            if (rc.getTeamGoldAmount(rc.getTeam())-cost*-1*diff<cost){
                rc.setIndicatorString(indicatorString);
                return false;
            }
        }
        if (diff>0){
            int space = rc.getArchonCount()-diff;
            if (rc.getTeamGoldAmount(rc.getTeam())-cost*space<cost){
                rc.setIndicatorString(indicatorString);
                return false;
            }
        }
        return true;
    }
    
    private int roundNum = 0;

    public void repair() throws GameActionException {
        //TODO: optimize code
        //TODO: update for sages
        if (!rc.isActionReady()){
            return;
        }
        int leastAttackedHealth = Integer.MAX_VALUE;
        MapLocation attackedLocation = null;

        MapLocation continueHealLocation = null;
        //TODO: prioritize healing sages no matter what
        int greatestHealthDifference = 0;
        MapLocation troopLocation = null;
        int troopID = 0;

        int greatestSupportHealthDifference = 0; // miner/builder health
        MapLocation supportLocation = null;

        HashMap<Integer, Integer> newTroopHealth = new HashMap<Integer, Integer>();

        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.ARCHON.actionRadiusSquared,rc.getTeam());
        for (RobotInfo robot: robots){
            MapLocation thisLocation = robot.getLocation();
            if (robot.type==RobotType.MINER || robot.type==RobotType.BUILDER){
                if (robot.getType().health-robot.getHealth()>greatestSupportHealthDifference && rc.canRepair(thisLocation)){
                    greatestSupportHealthDifference = robot.getType().health-robot.getHealth();
                    supportLocation = thisLocation;
                }
                continue;
            }
            if (nearbyTroopHealth.containsKey(robot.ID) && nearbyTroopHealth.get(robot.ID)>robot.getHealth() && robot.getHealth()<leastAttackedHealth && rc.canRepair(thisLocation)){
                //soldier is being attacked
                attackedLocation = thisLocation;
                leastAttackedHealth=robot.getHealth();
            }
            if (soldierToHeal == robot.getID() && robot.getHealth()<robot.getType().health-1 && rc.canRepair(thisLocation)){
                //soldier that is currently being healed
                continueHealLocation = thisLocation;
            }
            if (robot.getType().health-robot.getHealth()>greatestHealthDifference && rc.canRepair(thisLocation)){
                //find soldier to heal
                greatestHealthDifference = robot.getType().health-robot.getHealth();
                troopLocation = thisLocation;
                troopID = robot.ID;
            }
            newTroopHealth.put(robot.ID, robot.getHealth());
        }
        nearbyTroopHealth = newTroopHealth;
        if (attackedLocation!=null && rc.canRepair(attackedLocation)){
            //indicatorString+=" healing attacked soldier";
            //repair soldier being attacked
            rc.repair(attackedLocation);
            return;
        }
        if (continueHealLocation!=null && rc.canRepair(continueHealLocation)){
            //indicatorString+=" continue to heal soldier";
            rc.repair(continueHealLocation);
            return;
        }
        if (troopLocation!=null && rc.canRepair(troopLocation)){
            //indicatorString+=" healing soldier";
            soldierToHeal = troopID;
            rc.repair(troopLocation);
            return;
        }
        if (supportLocation!=null && rc.canRepair(supportLocation)){
            //indicatorString+=" healing miner";
            rc.repair(supportLocation);
        }
        //indicatorString+=" healing none";
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
        roundNum = rc.getRoundNum();
        indicatorString = "";
        checkEnemies();
        checkArchonsAlive();
        checkEdge(); //TODO: to optimize bytecode move this to init() and checkarchonsalive only
        updateTroopCount();
        if (defense()){
            repair();
            indicatorString += " defense";
            rc.setIndicatorString(indicatorString);
            return;
        }
        //minerThreshold = Math.max(200, 200+(globalMinerCount-3)/rc.getArchonCount()*90); //TODO: experiment with factor 
        //don't move unless labs are built
        if (globalLabCount>0){ 
            setOffensiveTarget();
            //setTargetLocation();
            move();
        } 
        else if (rc.getMode()==RobotMode.PORTABLE && rc.canTransform()){
            rc.transform();
            pastLocations.clear();
            setTransformStatus();
        }

        // START SPAWNING
        int goldBuildStatus = rc.readSharedArray(10);
        int leadBuildStatus = rc.readSharedArray(11);
        int leadDiff = leadBuildStatus - archonOrder;
        int goldDiff = goldBuildStatus - archonOrder;

        if (rc.getMode()==RobotMode.PORTABLE){
            if (leadDiff==0){
                if (leadBuildStatus == rc.getArchonCount()-1){
                    rc.writeSharedArray(11,0);
                }
                else{
                    rc.writeSharedArray(11,leadBuildStatus+1);
                }
            }
            if (goldDiff==0){
                if (goldBuildStatus == rc.getArchonCount()-1){
                    rc.writeSharedArray(10,0);
                }
                else{
                    rc.writeSharedArray(10,goldBuildStatus+1);
                }
            }
            System.out.println("3: "+Clock.getBytecodesLeft());
            return;
        }

        indicatorString+="gd: "+goldDiff+" gs: "+goldBuildStatus;
        int mod = 4;
        /*
        if(rc.readSharedArray(40)!=0){
            mod = 1;
        }else if (rc.readSharedArray(42)!= 0){ // if a miner has been sighted
            mod = 2;
        }
        */
        if (/*!(rc.readSharedArray(47)>=3 && rc.getRoundNum()<5) && */globalMinerCount < 3/*minerMin*/ && !nearEnemyArchon){
            int cost = RobotType.MINER.buildCostLead;
            RobotType type = RobotType.MINER;
            indicatorString += " miners";
            if (!checkLeadBuildStatus(leadDiff, cost)){
                repair();
                return;
            }
            if (rc.getTeamLeadAmount(rc.getTeam())>=cost){
                int i=0;
                while (i<passableDirections.size()-1 && !rc.canBuildRobot(type,passableDirections.get(i))){
                    i++;
                }
                if (rc.canBuildRobot(type,passableDirections.get(i))){
                    if (leadDiff==0){
                        if (leadBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,leadBuildStatus+1);
                        }
                    }
                    rc.buildRobot(type,passableDirections.get(i));
                    //minerCount++;
                    globalMinerCount++;
                    rc.writeSharedArray(44,rc.readSharedArray(44)+1);
                    rc.writeSharedArray(47,rc.readSharedArray(47)+1);
                    peakMiner = Math.max(globalMinerCount, peakMiner);
                    count++;
                }
            }
        }
        else if (globalBuilderCount < 1 && !nearEnemyArchon){
            int cost = RobotType.BUILDER.buildCostLead;
            RobotType type = RobotType.BUILDER;
            indicatorString += " builders";
            if (!isEdge){ // pass the turn and make the archon closest to the edge build a builder
                if (leadDiff==0){
                    if (leadBuildStatus == rc.getArchonCount()-1){
                        rc.writeSharedArray(11,0);
                    }
                    else{
                        rc.writeSharedArray(11,leadBuildStatus+1);
                    }
                }
            }
            else if (rc.getTeamLeadAmount(rc.getTeam())>=cost){
                int i=0;
                while (i<passableDirections.size()-1 && !rc.canBuildRobot(type,passableDirections.get(i))){
                    i++;
                }
                if (rc.canBuildRobot(type,passableDirections.get(i))){
                    if (leadDiff==0){
                        if (leadBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,leadBuildStatus+1);
                        }
                    }
                    rc.buildRobot(type,passableDirections.get(i));
                    rc.writeSharedArray(1, rc.readSharedArray(1)+1);
                    globalBuilderCount++;
                    builderCount++;
                    count++;
                }
            }
        }

        if (rc.getTeamGoldAmount(rc.getTeam())>RobotType.SAGE.buildCostGold) {
            int cost = RobotType.SAGE.buildCostGold;
            RobotType type = RobotType.SAGE;
            indicatorString += " sages";
            if (!checkGoldBuildStatus(goldDiff, cost)){
                repair();
                return;
            }
            if (rc.getTeamGoldAmount(rc.getTeam())>=cost){
                int i=0;
                while (i<passableDirections.size()-1 && !rc.canBuildRobot(type,passableDirections.get(i))){
                    i++;
                }
                if (rc.canBuildRobot(type,passableDirections.get(i))){
                    if (goldDiff==0){
                        if (goldBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(10,0);
                        }
                        else{
                            rc.writeSharedArray(10,goldBuildStatus+1);
                        }
                    }
                    rc.buildRobot(type,passableDirections.get(i));
                    sageCount++;
                    count++;
                }
            }
        }
        else if (globalLabCount>0 && rc.getTeamLeadAmount(rc.getTeam())>RobotType.MINER.buildCostLead && !nearEnemyArchon && (globalMinerCount<targetMinerCount || peakMiner<targetMinerCount*4/3)/*minerMax*/){
            int cost = RobotType.MINER.buildCostLead;
            RobotType type = RobotType.MINER;
            indicatorString += " miners";
            if (!checkLeadBuildStatus(leadDiff, cost)){
                repair();
                return;
            }
            if (rc.getTeamLeadAmount(rc.getTeam())>=cost){
                int i=0;
                while (i<passableDirections.size()-1 && !rc.canBuildRobot(type,passableDirections.get(i))){
                    i++;
                }
                if (rc.canBuildRobot(type,passableDirections.get(i))){
                    if (leadDiff==0){
                        if (leadBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,leadBuildStatus+1);
                        }
                    }
                    rc.buildRobot(type,passableDirections.get(i));
                    //minerCount++;
                    globalMinerCount++;
                    rc.writeSharedArray(44,rc.readSharedArray(44)+1);
                    peakMiner = Math.max(globalMinerCount, peakMiner);
                    count++;
                }
            }
        }else if(nearEnemyArchon){
            int cost = RobotType.SOLDIER.buildCostLead;
            RobotType type = RobotType.SOLDIER;
            indicatorString += " soldiers";
            if (!checkLeadBuildStatus(leadDiff, cost)){
                repair();
                return;
            }
            if (rc.getTeamLeadAmount(rc.getTeam())>=cost){
                int i=0;
                while (i<passableDirections.size()-1 && !rc.canBuildRobot(type,passableDirections.get(i))){
                    i++;
                }
                if (rc.canBuildRobot(type,passableDirections.get(i))){
                    if (leadDiff==0){
                        if (leadBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,leadBuildStatus+1);
                        }
                    }
                    rc.buildRobot(type,passableDirections.get(i));
                    soldierCount++;
                    count++;
                }
            }
        }else if (rc.getHealth()<RobotType.ARCHON.health){
            boolean builder = false;
            RobotInfo[] allyBots = rc.senseNearbyRobots(RobotType.BUILDER.visionRadiusSquared, rc.getTeam());
            for (RobotInfo robot:allyBots){
                if (robot.getType()==RobotType.BUILDER){
                    builder = true;
                    break;
                }
            }
            if (!builder){
                int cost = RobotType.BUILDER.buildCostLead;
                RobotType type = RobotType.BUILDER;
                indicatorString += " builders";
                if (rc.getTeamLeadAmount(rc.getTeam())>=cost){
                    int i=0;
                    while (i<passableDirections.size()-1 && !rc.canBuildRobot(type,passableDirections.get(i))){
                        i++;
                    }
                    if (rc.canBuildRobot(type,passableDirections.get(i))){
                        rc.buildRobot(type,passableDirections.get(i));
                        rc.writeSharedArray(1, rc.readSharedArray(1)+1);
                        globalBuilderCount++;
                        builderCount++;
                        count++;
                    }
                }
            }
        }
        repair();
        rc.setIndicatorString(indicatorString);
    }

    public boolean archonMove(MapLocation target) throws GameActionException{
        if (!rc.isMovementReady()){
            return false;
        }
        MapLocation lowest = rc.getLocation();
        int lowest_rubble = 99;
        myLocation = rc.getLocation();
        for (Direction d: Direction.allDirections()){
            if (d==Direction.CENTER)continue;
            MapLocation adjacent=rc.adjacentLocation(d);
            if (!rc.onTheMap(adjacent) || rc.canSenseRobotAtLocation(adjacent) && rc.senseRobotAtLocation(adjacent).getMode()!=RobotMode.DROID) continue;
            int rubbleAtLoc = rc.senseRubble(adjacent);
            if (adjacent.distanceSquaredTo(target) >= myLocation.distanceSquaredTo(target))continue;
            if(rubbleAtLoc < lowest_rubble || rubbleAtLoc == lowest_rubble && adjacent.distanceSquaredTo(target) < lowest.distanceSquaredTo(target)){
                lowest = adjacent;
                lowest_rubble = rubbleAtLoc;
            }
        }
        Direction direction = rc.getLocation().directionTo(lowest);
        indicatorString+=direction.toString();
        if(rc.canMove(direction)){
            rc.move(direction);
            myLocation = rc.getLocation();
            return true;
        }
        return false;
    }

    private boolean isEdge = false;
    private void checkEdge() throws GameActionException {
        MapLocation[] archons = getArchonLocs();
        if(archons.length == 0)return;
        myLocation = rc.getLocation();
        MapLocation targetArchon = archons[0]; // if both archons are same distance apart, make sure the first archon always builds
        int xCheck = Math.min(Math.abs(-targetArchon.x),Math.abs(mapWidth-1-targetArchon.x));
        int yCheck = Math.min(Math.abs(-targetArchon.y),Math.abs(mapHeight-1-targetArchon.y));
        for(int i = archons.length; --i>=0;){
            if(archons[i] == null || targetArchon.equals(archons[i]))continue;
            int xTemp = Math.min(Math.abs(-archons[i].x),Math.abs(mapWidth-1-archons[i].x));
            int yTemp = Math.min(Math.abs(-archons[i].y),Math.abs(mapHeight-1-archons[i].y));
            if(xTemp+yTemp < xCheck+yCheck){
                targetArchon = archons[i];
                xCheck = xTemp;
                yCheck = yTemp;
            }
        }
        if(targetArchon.equals(myLocation)) isEdge = true;
    }

    private HashSet<MapLocation> pastLocations = new HashSet<>();
    private void soldierMove(MapLocation target) throws GameActionException {
        Direction dir = pfs.getBestDir(target);
        MapLocation temp = myLocation;
        if(dir != null && rc.canMove(dir) && !pastLocations.contains(myLocation.add(dir))){
            if(tryMoveMultiple(dir)){
                pastLocations.add(temp);
            }
        }else{
            intermediateMove(target);
            pastLocations.add(temp);
        }
    }
}