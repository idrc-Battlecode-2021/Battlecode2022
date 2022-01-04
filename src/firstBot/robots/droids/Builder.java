package firstBot.robots.droids;

import battlecode.common.*;
public class Builder extends Droid{
    private int startingBit;
    public Builder(RobotController rc) throws GameActionException {
        super(rc);
        int archonID=0;
        for (RobotInfo R : rc.senseNearbyRobots()){
            if (R.getType() == RobotType.ARCHON){
                archonID=R.getID();
            }
        }
        for(int i=63; i>59; i--){
            if (rc.readSharedArray(i)==archonID){
                startingBit=2*(63-i);
            }
        }
    }

    @Override
    public void init() throws GameActionException {
        
    }

    @Override
    public void run() throws GameActionException {
        int toBuild = read();
        build(toBuild);

    }
    public int read() throws GameActionException{
        int build = rc.readSharedArray(6);
        switch (startingBit) {
            case 0: build=build%4;
            case 1: build=(build/4)%4;
            case 2: build=(build/16)%4;
            case 3: build=(build/64)%4;
        }
        return build;
    }

    public boolean build(int id) throws GameActionException{
        RobotType r = RobotType.WATCHTOWER;
        if (id ==0){
            return false;
        }
        else if (id == 1){
            r = RobotType.LABORATORY;
        }
        for(Direction d : Direction.allDirections()){
            if(rc.canBuildRobot(r,d)){
                rc.buildRobot(r, d);
                return true;
            }
        }
        return false;
    }
}