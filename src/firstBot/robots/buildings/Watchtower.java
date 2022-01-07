package firstBot.robots.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;
import battlecode.common.MapLocation;

import java.util.Map;

public class Watchtower extends Building {
    private boolean isDefensive = false;
    private MapLocation archon = null;
    private MapLocation latticeCenter = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
    private boolean inPosition = false;
    private MapLocation[] latticePositions = new MapLocation[14];

    public Watchtower(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        int[] dx = {0, 6, -6, 12, -12, 18, -18};
        int[] dy = {3, -3};
        int counter = -1;
        for (int i : dx) {
            counter = counter + 1;
            for (int j : dy) {
                latticePositions[counter] = latticeCenter.translate(i, j);
            }
        }
    }

    //TODO: fix targeting
    @Override
    public void run() throws GameActionException {
        broadcast();
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : enemies) {
            if (r.getType() == RobotType.SAGE || r.getType()==RobotType.ARCHON) {
                if (rc.canAttack(r.getLocation())) {
                    rc.attack(r.getLocation());
                }
            }
        }
        if (enemies.length > 0 && rc.canAttack(enemies[0].getLocation())) {
            rc.attack(enemies[0].getLocation());
        }
        if (hasMapLocation()){
            if (rc.getMode()==RobotMode.TURRET){
                if(rc.canTransform()){
                    rc.transform();
                }
            }
            if (attackArchon()){
                if (rc.getLocation().isWithinDistanceSquared(decode(),rc.getType().actionRadiusSquared)){
                    if(rc.getMode() == RobotMode.PORTABLE && rc.canTransform()){
                        rc.transform();
                    }
                }
                else{
                    intermediateMove(decode());
                }
            }
            else if(rc.getLocation().isWithinDistanceSquared(decode(), 5)){
                if(rc.canTransform() && rc.getMode() == RobotMode.PORTABLE) {
                    rc.transform();
                }
            }
            else{
                intermediateMove(decode());
            }
        }

    }
    public boolean attackArchon() throws GameActionException{
        if (hasMapLocation()){
            if (rc.readSharedArray(55)/4096>0){
                return true;
            }
        }
        return false;
    }
}