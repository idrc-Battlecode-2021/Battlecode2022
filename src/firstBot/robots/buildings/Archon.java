package firstBot.robots.buildings;

import firstBot.util.*;
import battlecode.common.*;

public class Archon extends Building{
    private static int minerCount, builderCount, sageCount, soldierCount, labCount, watchtowerCount;
    private static int globalMinerCount, globalBuilderCount, globalSageCount, globalSoldierCount, globalWatchtowerCount, globalLabCount;

    private static MapLocation myLocation;
    private static int minerBuild = 10; //miners to build
    private static int builderBuild = 3; //builders to build
    private static int watchtowerBuild = 5; //watchtowers to build; *currently not in use*
    private static int labBuild = 2; //labs to build

    private static int minerIndex = 0; //spawning miners
    private static int archonOrder = 0; //reverse position of archonID in shared array
    private static int power = 0; // power of 2 that corresponds witha archonOrder

    private static int spawnPhase = 0;

    private static int buildType = 0;


    public Archon(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
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
        power = (int)Math.pow(2,archonOrder*4);
        // Choose # of miners to build based on lead in surroundings
        int leadTiles = 0;
        for (int[] coord : Constants.VIEWABLE_TILES_34){
            //TODO: update based on amount of lead instead of if it exists?
            MapLocation observe = myLocation.translate(coord[0],coord[1]);
            if (rc.onTheMap(observe) && rc.senseLead(observe)>0){
                leadTiles++;
            }
        }
        minerBuild = Math.max(minerBuild, (int)(60*((double)leadTiles/Constants.VIEWABLE_TILES_34.length)));
    }

    public boolean checkArchonPhase(int n) throws GameActionException {
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
        int minLead = 500; //placeholder
        int maxRate = 20; //placeholder, 5 to 20
        rc.writeSharedArray(9, (maxRate-5)*4096+minLead); //minLead may need to be divided by a factor to fit in 12 bits if minLead is large
        
    }

    @Override
    public void run() throws GameActionException {
        updateLabConstraints();
        // read total/global # of robots
        if (rc.getRoundNum()%3==0){
            globalMinerCount = rc.readSharedArray(0);
            globalBuilderCount = rc.readSharedArray(1);
            globalSageCount = rc.readSharedArray(2);
            globalSoldierCount = rc.readSharedArray(3);
            globalWatchtowerCount = rc.readSharedArray(5) + rc.readSharedArray(6) + rc.readSharedArray(7) + rc.readSharedArray(8);
            int lc = rc.readSharedArray(4);
            globalLabCount = lc % 16 + (lc % 256)/16 + (lc % 4096)/256 + lc / 4096;
        }

        // reset total # of troops in shared array
        if (archonOrder == 0 && rc.getRoundNum()%3==1){
            /*
                right now only the 1st archon that moves always refreshes the array to save bytecode.
                in the future program for the case where it gets destroyed and others are alive
            */
            for (int i=0;i<4;i++){
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
        rc.setIndicatorString(labCount+"");
        //build robots
        if (!checkArchonPhase(spawnPhase)){ // shouldn't continue if other archons haven't caught up with spawning
            return;
        }
        if (minerCount<minerBuild){
            if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.MINER.buildCostLead){
                Direction directions[] = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8])){
                    minerIndex = (minerIndex+i)%8;
                    rc.buildRobot(RobotType.MINER,directions[minerIndex]);
                    minerIndex++;
                    minerCount++;
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
                    if (builderCount == builderBuild){
                        rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                        spawnPhase++;
                    }
                }
            }
        }
        else {
            if (rc.getTeamGoldAmount(rc.getTeam())>=RobotType.SAGE.buildCostGold){
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
                if (buildType%3==0){
                    if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.LABORATORY.buildCostLead){
                        //if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.LABORATORY.buildCostLead){
                        int temp = (int)Math.pow(2,archonOrder*2);
                        int currentValue = rc.readSharedArray(58);
                        int previousBuildCommand = (currentValue % (temp*4))/temp;
                        int buildCommand = currentValue - previousBuildCommand * temp + temp;
                        rc.writeSharedArray(58, buildCommand);
                        buildType++;
                        if (buildType>0){
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            spawnPhase++;
                        }
                    }
                }
                else{
                    if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.WATCHTOWER.buildCostLead){
                        int temp = (int)Math.pow(2,archonOrder*2); // power corresponding to this Archon's bits
                        int currentValue = rc.readSharedArray(58); 
                        int previousBuildCommand = (currentValue % (temp * 4))/temp; // previous two-bit build command
                        int buildCommand = currentValue - previousBuildCommand * temp + temp * 2; // subtract previous command and add new command
                        rc.writeSharedArray(58, buildCommand);
                        buildType++;
                    }
                }
            }
            else{
                if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.WATCHTOWER.buildCostLead){
                    int temp = (int)Math.pow(2,archonOrder*2); // power corresponding to this Archon's bits
                    int currentValue = rc.readSharedArray(58); 
                    int previousBuildCommand = (currentValue % (temp * 4))/temp; // previous two-bit build command
                    int buildCommand = currentValue - previousBuildCommand * temp + temp * 2; // subtract previous command and add new command
                    watchtowerCount++;
                    rc.writeSharedArray(58, buildCommand);
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
