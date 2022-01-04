package firstBot.robots.droids;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Soldier extends Droid{
    public Soldier(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        
    }

    @Override
    public void run() throws GameActionException {
        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(3, rc.readSharedArray(3)+1);
        }
    }
}
