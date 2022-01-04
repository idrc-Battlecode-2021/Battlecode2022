package firstBot.robots.droids;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Sage extends Droid {
    public Sage(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        
    }

    @Override
    public void run() throws GameActionException {
        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(2, rc.readSharedArray(2)+1);
        }
    }
}
