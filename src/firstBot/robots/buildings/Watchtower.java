package firstBot.robots.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;
import battlecode.common.MapLocation;

import java.util.Map;

public class Watchtower extends Building {
    private boolean isDefensive = true;
    private MapLocation archon = null;
    public Watchtower(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        RobotInfo [] r = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam.opponent());
        for (RobotInfo ro: r) {
            if (ro.getType() == RobotType.ARCHON) {
                archon = ro.getLocation();
            }
        }
        if (archon==null){
            isDefensive=false;
        }
        else if (rc.readSharedArray(5)+rc.readSharedArray(6)+
                rc.readSharedArray(7)+rc.readSharedArray(8)>8){
            isDefensive=false;
        }
    }
    @Override
    public void run() throws GameActionException {
        avoidFury();
        retransform();
        if(isDefensive){
            rc.setIndicatorString("Defensive");
            if (rc.getLocation().distanceSquaredTo(archon)<2){
                if(rc.getMode()==RobotMode.TURRET && rc.canTransform()){
                    rc.transform();
                }
                else if (rc.canMove(rc.getLocation().directionTo(archon).opposite())){
                    rc.move(rc.getLocation().directionTo(archon).opposite());
                }
            }
            else{
                if(rc.getMode()==RobotMode.TURRET && rc.canTransform()){
                    rc.transform();
                }
            }
            if (rc.getMode()==RobotMode.PORTABLE && rc.canTransform()) rc.transform();
            attackDefensive();
        }
        else{
            broadcast();
            attackDefensive();
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

    }

    public void attackDefensive() throws GameActionException{
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        for (int i = enemies.length; --i>=0;) {
            if (enemies[i].getType() == RobotType.SAGE || enemies[i].getType()==RobotType.ARCHON) {
                if (rc.canAttack(enemies[i].getLocation())) {
                    rc.attack(enemies[i].getLocation());
                }
            }
        }
        if (enemies.length > 0 && rc.canAttack(enemies[0].getLocation())) {
            rc.attack(enemies[0].getLocation());
        }
    }
}