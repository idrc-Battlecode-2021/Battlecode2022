package firstBot.robots.buildings;

import battlecode.common.*;

public class Archon extends Building{
    int miners, builders, sages, soldiers, labs, watchtowers;
    int minerIndex = 0; //spawning miners
    int archonOrder = 0;
    /* Shared array indices
    0 - # of miners
    1 - # of builders
    2 - # of sages
    3 - # of soldiers
    4 - # of labs
    5 - # of watchtowers
    */
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
        miners = rc.readSharedArray(0);
        builders = rc.readSharedArray(1);
        sages = rc.readSharedArray(2);
        soldiers = rc.readSharedArray(3);
        labs = rc.readSharedArray(4);
        watchtowers = rc.readSharedArray(5);

        if (miners<5){
            if (rc.getTeamLeadAmount(rc.getTeam())>=50){
                Direction directions[] = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8])){
                    minerIndex = (minerIndex+i)%8;
                    rc.buildRobot(RobotType.MINER,directions[(minerIndex+i)%8]);
                    minerIndex++;
                }
            }
        }
        else if (builders<1){
            if (rc.getTeamLeadAmount(rc.getTeam())>=40){
                Direction directions[] = Direction.allDirections();
                int i = 0;
                while (!rc.canBuildRobot(RobotType.BUILDER,directions[i]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.BUILDER,directions[i])){
                    rc.buildRobot(RobotType.BUILDER,directions[i]);
                }
            }
        }
        else if (watchtowers<2){
            if (rc.getTeamLeadAmount(rc.getTeam())>=180){
                int power = (int)Math.pow(2,archonOrder*2); // power corresponding to this Archon's bits
                int currentValue = rc.readSharedArray(59); 
                int previousBuildCommand = (currentValue % (power*4))/power; // previous two-bit build command
                int buildCommand = currentValue - previousBuildCommand + power * 2; // subtract previous command and add new command
                rc.writeSharedArray(59, buildCommand);
            }
        }
        else if (labs<1){
            if (rc.getTeamLeadAmount(rc.getTeam())>=800){
                int power = (int)Math.pow(2,archonOrder*2); 
                int currentValue = rc.readSharedArray(59);
                int previousBuildCommand = (currentValue % (power*4))/power;
                int buildCommand = currentValue - previousBuildCommand + power;
                rc.writeSharedArray(59, buildCommand);
            }
        }
    }

}
