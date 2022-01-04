package firstBot.robots.buildings;

import battlecode.common.*;

public class Archon extends Building{
    //TODO: Archons blow up for some reason ~ turn 700
    private static int minerCount, builderCount, sageCount, soldierCount, labCount, watchtowerCount;
    private static int globalMinerCount, globalBuilderCount, globalSageCount, globalSoldierCount;

    private static int minerIndex = 0; //spawning miners

    private static int archonOrder = 0; //reverse position of archonID in shared array

    public Archon(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        rc.writeSharedArray(4,rc.readSharedArray(4)+1);
        if (rc.getArchonCount()==1){
            archonOrder = 0;
        }
        else {
            while(rc.readSharedArray(63-archonOrder)>0){
                archonOrder++;
            }   
            rc.writeSharedArray(63-archonOrder,rc.getID());
        }
    }

    @Override
    public void run() throws GameActionException {
        rc.setIndicatorString(archonOrder+"");
        // read total # of troops
        if (rc.getRoundNum()%3==0){
            globalMinerCount = rc.readSharedArray(0);
            globalBuilderCount = rc.readSharedArray(1);
            globalSageCount = rc.readSharedArray(2);
            globalSoldierCount = rc.readSharedArray(3);
        }

        // reset total # of troops
        if (archonOrder == 0 && rc.getRoundNum()%3==1){
            /*
                right now only the 1st archon that moves always refreshes the array to save bytecode
                in the future program for the case where it gets destroyed and others are alive
            */
            for (int i=0;i<7;i++){
                rc.writeSharedArray(i, 0);
            }
        }
        int power = 0;
        // individual watchtower count
        if (archonOrder==0 || archonOrder==1){
            power = (int)Math.pow(2,archonOrder*8); // power corresponding to this Archon's bits
            watchtowerCount = (rc.readSharedArray(5) % (power*128))/power;
        }
        else{
            power = (int)Math.pow(2,(archonOrder-2)*8); // power corresponding to this Archon's bits
            watchtowerCount = (rc.readSharedArray(6) % (power*128))/power;
        }

        // individual lab count
        power = (int)Math.pow(2,archonOrder*4); // power corresponding to this Archon's bits
        labCount = (rc.readSharedArray(4) % (power*16))/power;

        //build droids
        if (minerCount<5){
            if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.MINER.buildCostLead){
                Direction directions[] = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8])){
                    minerIndex = (minerIndex+i)%8;
                    rc.buildRobot(RobotType.MINER,directions[(minerIndex+i)%8]);
                    minerIndex++;
                    minerCount++;
                }
            }
        }
        else if (builderCount<1){
            /*
            if (globalMinerCount<rc.getArchonCount()*5){ // wait for other archons to build miners before building a builder 
                return;
            }
            */
            if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.BUILDER.buildCostLead){
                Direction directions[] = Direction.allDirections();
                int i = 0;
                while (!rc.canBuildRobot(RobotType.BUILDER,directions[i]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.BUILDER,directions[i])){
                    rc.buildRobot(RobotType.BUILDER,directions[i]);
                    builderCount++;
                }
            }
        }
        else if (watchtowerCount<2){
            if (globalBuilderCount<rc.getArchonCount()){ // wait for other archons to build builders before building watchtowers
                return;
            }
            if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.WATCHTOWER.buildCostLead){
                power = (int)Math.pow(2,archonOrder*2); // power corresponding to this Archon's bits
                int currentValue = rc.readSharedArray(58); 
                int previousBuildCommand = (currentValue % (power*4))/power; // previous two-bit build command
                int buildCommand = currentValue - previousBuildCommand * power + power * 2; // subtract previous command and add new command
                rc.writeSharedArray(58, buildCommand);
            }
        }
        else if (labCount<1){
            //TODO: fix code to calculate total # of watchtowers instead of individual
            /*
            if (rc.readSharedArray(5)<rc.getArchonCount()*2){ // wait for other archons to build watchtowers before building lab
                return;
            }
            */
            if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.LABORATORY.buildCostLead){
                power = (int)Math.pow(2,archonOrder*2); 
                int currentValue = rc.readSharedArray(58);
                int previousBuildCommand = (currentValue % (power*4))/power;
                int buildCommand = currentValue - previousBuildCommand * power + power;
                rc.writeSharedArray(58, buildCommand);
            }
        }
        else if (rc.getTeamGoldAmount(rc.getTeam())>=RobotType.SAGE.buildCostGold){
            Direction directions[] = Direction.allDirections();
            int i=0;
            while (!rc.canBuildRobot(RobotType.SAGE,directions[i]) && i<8){
                i++;
            }
            if (rc.canBuildRobot(RobotType.SAGE,directions[i])){
                rc.buildRobot(RobotType.SAGE,directions[i]);
                sageCount++;
            }
        }
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
            if (location!=null){
                rc.repair(location);
            }
        }
    }

}
