package bot3_SL.robots.droids;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.AnomalyType;

public class Sage extends Droid {
    private MapLocation target;
    
    public Sage(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        parseAnomalies();
    }

    @Override
    public void run() throws GameActionException {
        rc.setIndicatorString(myArchonOrder+"");
        avoidCharge();
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
            rc.setIndicatorString("1");
            intermediateMove(target);
            if (rc.canAttack(target)) rc.attack(target);
        }
        else if (attackArchon()){
            rc.setIndicatorString(attackArchon()+" ");
            MapLocation archonLoc = decode();
            if (myLocation.distanceSquaredTo(archonLoc) < 20){
                rc.setIndicatorString("7");
                if(rc.canEnvision(AnomalyType.FURY)){
                    rc.envision(AnomalyType.FURY);
                }
            }else{
                intermediateMove(archonLoc);
            }
        } else if (hasMapLocation()) {
            rc.setIndicatorString("3");
            MapLocation target = decode();
            if (rc.getLocation().distanceSquaredTo(target) < 20) {
                if (nearbyBots.length < 5) {
                    rc.writeSharedArray(55, 0);
                }
            }
            intermediateMove(target);
        } else{
            rc.setIndicatorString("4");
            if(!tryMoveMultipleNew()){
                tryMoveMultiple(initDirection);
            }
        }
    }
}
