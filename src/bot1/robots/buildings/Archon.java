package bot1.robots.buildings;

import battlecode.common.*;

public class Archon extends Building{
    private static int minerCount, builderCount, sageCount, soldierCount, labCount, watchtowerCount;
    private static int globalMinerCount, globalBuilderCount, globalSageCount, globalSoldierCount, globalWatchtowerCount, globalLabCount;
    private static int targetMinerCount; //target # of miners to build across all archons

    private static MapLocation myLocation;
    private static int minerBuild = 5; //miners to build
    private static int soldierBuild = 10; //soldiers to build
    private static int builderBuild = 3; //builders to build
    private static int watchtowerBuild = 5; //watchtowers to build; *currently not in use*
    private static int labBuild = 2; //labs to build

    private static int minerIndex = 0; //spawning miners
    private static int soldierIndex = 0;
    private static int archonOrder = 0; //reverse position of archonID in shared array
    private static int power = 0; // power of 16 that corresponds with archonOrder 

    private static int spawnPhase = 0;

    private static int buildType = 0;
    private static boolean minerDone, builderDone, labDone;

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
        parseAnomalies();
        myLocation = rc.getLocation();
        // write Archon ID to shared array
        if (rc.getArchonCount()==1){
            archonOrder = 0;
        }
        else {
            while(rc.readSharedArray(63-archonOrder)>0){
                archonOrder++;
            }   
            rc.writeSharedArray(63-archonOrder,rc.getID());
        }
        power = (int)Math.pow(16,archonOrder);
        // Choose # of miners to build based on lead in surroundings
        int leadTiles = rc.senseNearbyLocationsWithLead(34).length;
        
        minerBuild = Math.max(minerBuild, (int)(60*((double)leadTiles/rc.getAllLocationsWithinRadiusSquared(myLocation,34).length)));
        soldierBuild = minerBuild;
        myArchonID = rc.getID();
        myArchonOrder = archonOrder;
        labBuild = rc.getMapHeight()/40+1;
    }

    public boolean canProceed(int n) throws GameActionException {
        // First check if other archons are trying to defend
        int defenseStatus = rc.readSharedArray(56);
        for (int i=0;i<16;i+=4){
            if (rc.readSharedArray(63-i/4)==0){ // don't check archons that don't exist
                break;
            }
            int temp = (int)Math.pow(2,i);
            int thisDefense = (defenseStatus % (temp*16))/temp;
            if (thisDefense > 0){
                return false;
            }
        }
        // Check if other archons have built enough miners
        int minerStatus;
        for (int i=0;i<4;i++){
            if (rc.readSharedArray(63-i)==0){ // don't check archons that don't exist
                break;
            }
            if(i<=1){
                minerStatus = rc.readSharedArray(0);
            }
            else{
                minerStatus = rc.readSharedArray(10);
            }
            if (i%2==0){
                if (minerStatus%256<minerCount){
                    return false;
                }
            }
            else{
                if (minerStatus/256<minerCount){
                    return false;
                }
            }
        }
        // Check if all archons have passed phase n
        // 0 - spawn 1/3 of minerBuild, 1 - spawn 2/3 of minerBuild, etc.
        int archonStatus = rc.readSharedArray(57);
        for (int i=0;i<16;i+=4){
            if (rc.readSharedArray(63-i/4)==0){ // don't check archons that don't exist
                continue;
            }
            int temp = (int)Math.pow(2,i);
            int thisPhase = (archonStatus % (temp*16))/temp;
            if (thisPhase < n){
                return false;
            }
        }
        return true;
    }
     
    public void updateLabConstraints() throws GameActionException {
        //TODO: update constraints for lab based on current lead amounts and needs
        int minLead = 300; //placeholder
        int maxRate = 20; //placeholder, 5 to 20
        rc.writeSharedArray(9, (maxRate-5)*4096+minLead); //minLead may need to be divided by a factor to fit in 12 bits if minLead is large
        
    }
    
    public boolean defense() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int current = rc.readSharedArray(56);
        int myValue = (current % (power*16))/power;
        if (enemies.length>0){
            rc.writeSharedArray(56, current - myValue*power + power);
        }
        else{
            rc.writeSharedArray(56, current - myValue*power);
            return false;
        }
        if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.SOLDIER.buildCostLead){
            Direction directions[] = closestDirections(rc.getLocation().directionTo(enemies[0].getLocation()));
            int i=0;
            while (!rc.canBuildRobot(RobotType.SOLDIER,directions[i]) && i<8){
                i++;
            }
            if (rc.canBuildRobot(RobotType.SOLDIER,directions[i])){
                rc.buildRobot(RobotType.SOLDIER,directions[i]);
            }
        }
        return true;
    }
    
    @Override
    public void run() throws GameActionException {
        minerBuild = Math.max(minerBuild, (int)(60*((double)rc.senseNearbyLocationsWithLead(34).length/rc.getAllLocationsWithinRadiusSquared(myLocation,34).length)));
        if (defense()){
            return;
        }
        avoidFury();
        retransform();
        updateLabConstraints();
        // read total/global # of robots
        if (rc.getRoundNum()%3==0){
            if (archonOrder<=1){
                minerCount = (rc.readSharedArray(0)%((int)Math.pow(256,archonOrder+1)))/(int)Math.pow(256,archonOrder);
            }
            else{
                minerCount = (rc.readSharedArray(10)%((int)Math.pow(256,archonOrder-1)))/(int)Math.pow(256,archonOrder-2);
            }
            builderCount = (rc.readSharedArray(1)%(power*16))/(power);
            globalSageCount = rc.readSharedArray(2);
            globalSoldierCount = rc.readSharedArray(3);
            globalWatchtowerCount = rc.readSharedArray(5) + rc.readSharedArray(6) + rc.readSharedArray(7) + rc.readSharedArray(8);
            int lc = rc.readSharedArray(4);
            globalLabCount = lc % 16 + (lc % 256)/16 + (lc % 4096)/256 + lc / 4096;
        }

        // reset total # of troops in shared array
        if (rc.getRoundNum()%3==1){
            if (archonOrder<=1){
                rc.writeSharedArray(0, 0);
            }
            else{
                rc.writeSharedArray(10, 0);
            }
            for (int i=1;i<4;i++){
                rc.writeSharedArray(i, 0);
            }
        }
        // find individual watchtower count
        if (archonOrder==0){
            watchtowerCount = rc.readSharedArray(5);
        }
        if (archonOrder==1){
            watchtowerCount = rc.readSharedArray(6);
        }
        if (archonOrder==2){
            watchtowerCount = rc.readSharedArray(7);
        }
        if (archonOrder==3){
            watchtowerCount = rc.readSharedArray(8);
        }
        // find individual lab count
        labCount = (rc.readSharedArray(4) % (power*16))/power;
        rc.setIndicatorString("minerCount: "+minerCount+" minerBuild: "+minerBuild);
        if (!canProceed(spawnPhase)){ // shouldn't continue if other archons haven't caught up with spawning
            return;
        }
        if (minerCount<minerBuild){
            if (buildType % 3 == 0){
                if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.SOLDIER.buildCostLead){
                    Direction directions[] = Direction.allDirections();
                    int i=0;
                    while (!rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8]) && i<8){
                        i++;
                    }
                    if (rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8])){
                        soldierIndex = (soldierIndex+i)%8;
                        buildType++;
                        rc.buildRobot(RobotType.SOLDIER,directions[soldierIndex]);
                        soldierIndex++;
                        soldierCount++;
                    }
                }
            }
            else{
                if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.MINER.buildCostLead){
                    Direction directions[] = Direction.allDirections();
                    int i=0;
                    while (!rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8]) && i<8){
                        i++;
                    }
                    if (rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8])){
                        minerIndex = (minerIndex+i)%8;
                        rc.buildRobot(RobotType.MINER,directions[minerIndex]);
                        buildType++;
                        minerIndex++;
                        minerCount++; // OK to manually increment minerCount because it will take <=3 rounds to reset minerCount again
                        if (!minerDone && minerCount == minerBuild){
                            minerDone = true;
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            spawnPhase++;
                            buildType = 0;
                        }
                        /*
                        if (minerCount == minerBuild/3){
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            spawnPhase++;
                        }
                        else if (minerCount == minerBuild*2/3){
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            spawnPhase++;
                        }
                        else if (minerCount == minerBuild){
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            spawnPhase++;
                            buildType = 0;
                        }
                        */
                    }
                }
            }
        }
        else if (builderCount<builderBuild){
            if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.BUILDER.buildCostLead){
                Direction directions[] = Direction.allDirections();
                int i = 0;
                while (!rc.canBuildRobot(RobotType.BUILDER,directions[i]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.BUILDER,directions[i])){
                    rc.buildRobot(RobotType.BUILDER,directions[i]);
                    builderCount++;
                    if (!builderDone && builderCount == builderBuild){
                        builderDone = true;
                        rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                        spawnPhase++;
                    }
                }
            }
        }
        else {
            if (globalSoldierCount+globalWatchtowerCount>40 && rc.getTeamGoldAmount(rc.getTeam())>=RobotType.SAGE.buildCostGold){
                Direction[] directions = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(RobotType.SAGE,directions[i]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.SAGE,directions[i])){
                    rc.buildRobot(RobotType.SAGE,directions[i]);
                    sageCount++;
                }
            }
            else if (labCount<labBuild){
                int mod;
                if (globalLabCount==0 || labCount>0){
                    mod=0;
                }
                else{
                    mod=2;
                }
                if (buildType%3==mod){
                    if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.LABORATORY.buildCostLead){
                        //if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.LABORATORY.buildCostLead){
                        int temp = (int)Math.pow(4,archonOrder);
                        int currentValue = rc.readSharedArray(58);
                        int previousBuildCommand = (currentValue % (temp*4))/temp;
                        int buildCommand = currentValue - previousBuildCommand * temp + temp;
                        rc.writeSharedArray(58, buildCommand);
                        buildType++;
                        /*
                        if (labCount+1==labBuild){
                            buildType = 0;
                        }
                        */
                        if (!labDone && labCount==labBuild && buildType==3){
                            labDone = true;
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            buildType = 0;
                            spawnPhase++;
                        }
                    }
                }
                else{
                    if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.WATCHTOWER.buildCostLead){
                        int temp = (int)Math.pow(4,archonOrder); // power corresponding to this Archon's bits
                        int currentValue = rc.readSharedArray(58); 
                        int previousBuildCommand = (currentValue % (temp * 4))/temp; // previous two-bit build command
                        int buildCommand = currentValue - previousBuildCommand * temp + temp * 2; // subtract previous command and add new command
                        rc.writeSharedArray(58, buildCommand);
                        buildType++;
                    }
                }
            }
            else{
                if (buildType%2==1 && rc.getTeamLeadAmount(rc.getTeam())>=RobotType.WATCHTOWER.buildCostLead){
                    int temp = (int)Math.pow(4,archonOrder); // power corresponding to this Archon's bits
                    int currentValue = rc.readSharedArray(58); 
                    int previousBuildCommand = (currentValue % (temp * 4))/temp; // previous two-bit build command
                    int buildCommand = currentValue - previousBuildCommand * temp + temp * 2; // subtract previous command and add new command
                    watchtowerCount++;
                    buildType++;
                    rc.writeSharedArray(58, buildCommand);
                }
                else if (buildType%2==0 && rc.getTeamLeadAmount(rc.getTeam())>=RobotType.SOLDIER.buildCostLead){
                    Direction directions[] = Direction.allDirections();
                    int i=0;
                    while (!rc.canBuildRobot(RobotType.SOLDIER,directions[i]) && i<8){
                        i++;
                    }
                    if (rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8])){
                        soldierIndex = (soldierIndex+i)%8;
                        buildType++;
                        rc.buildRobot(RobotType.SOLDIER,directions[soldierIndex]);
                        soldierCount++;
                    }
                }
                else { //repair robots if can't build anything
                    RobotInfo[] robots = rc.senseNearbyRobots(RobotType.ARCHON.actionRadiusSquared,rc.getTeam()); 
                    int lowestHealth = 99999;
                    MapLocation location = null;
                    for (RobotInfo robot : robots){
                        if (robot.getMode() == RobotMode.DROID && robot.getHealth()<lowestHealth){
                            lowestHealth = robot.getHealth();
                            location = robot.getLocation();
                        }
                    }
                    if (location!=null && rc.canRepair(location)){
                        rc.repair(location);
                    }
                }
            }
        }
        /*
        else if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.SOLDIER.buildCostLead){
            Direction directions[] = Direction.allDirections();
            int i=0;
            while (!rc.canBuildRobot(RobotType.SOLDIER,directions[i]) && i<8){
                i++;
            }
            if (rc.canBuildRobot(RobotType.SOLDIER,directions[i])){
                rc.buildRobot(RobotType.SOLDIER,directions[i]);
                soldierCount++;
            }
        }
        */
    }

}
