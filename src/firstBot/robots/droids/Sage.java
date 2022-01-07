package firstBot.robots.droids;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Sage extends Droid {
    private MapLocation target;
    
    public Sage(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        
    }

    @Override
    public void run() throws GameActionException {
        // update shared array
        if (rc.getRoundNum() % 3 == 2) {
            rc.writeSharedArray(2, rc.readSharedArray(2) + 1);
        }
        broadcast();
        target = null;

        RobotInfo[] nearbyBots = rc.senseNearbyRobots(20, rc.getTeam().opponent());
        if (nearbyBots.length >= 1) {
            target = nearbyBots[nearbyBots.length - 1].getLocation();
            for (int i = nearbyBots.length - 1; --i >= 0; ) {
                MapLocation temp = nearbyBots[i].getLocation();
                if (movementTileDistance(target, myLocation) > movementTileDistance(temp, myLocation)) target = temp;
            }
        }
        if (target != null) {
            intermediateMove(target);
            if (rc.canAttack(target)) rc.attack(target);
        } else if (hasMapLocation()) {
            MapLocation target = decode();
            if (rc.getLocation().distanceSquaredTo(target) < 20) {
                if (nearbyBots.length < 5) {
                    rc.writeSharedArray(55, 0);
                }
            }
            intermediateMove(target);
        } else tryMoveMultipleNew();
    }
}
