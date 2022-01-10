package bot4.robots.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;
import battlecode.common.MapLocation;

public class Watchtower extends Building {
    private boolean isDefensive = true;
    private MapLocation archon = null;
    public Watchtower(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        RobotInfo [] r = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam);
        for (RobotInfo ro: r) {
            if (ro.getType() == RobotType.ARCHON) {
                archon = ro.getLocation();
            }
        }
        if (archon==null){
            isDefensive=false;
        }
        else if (rc.readSharedArray(5)+rc.readSharedArray(6)+
                rc.readSharedArray(7)+rc.readSharedArray(8)>2*rc.getArchonCount()){
            isDefensive=false;
        }
    }
    @Override
    public void run() throws GameActionException {
        avoidFury();
        retransform();
        if(isDefensive){
            rc.setIndicatorString(Integer.toBinaryString(rc.readSharedArray(57)));
            if (rc.getLocation().isWithinDistanceSquared(archon,2)){
                if (rc.getMode()!=RobotMode.PORTABLE){
                    if (rc.canTransform()){
                        rc.transform();
                    }
                }
                else{
                   MapLocation away =  rc.getLocation().add(rc.getLocation().directionTo(archon).opposite());
                    if (rc.onTheMap(away)){
                        intermediateMove(away);
                    }
                    else{
                        Direction direction = rc.getLocation().directionTo(archon);
                        away = (archon.add(direction)).add(direction);
                        if (rc.onTheMap(away)){
                            intermediateMove(away);
                        }
                        else{
                            if (rc.getMode()==RobotMode.PORTABLE && rc.canTransform()) rc.transform();
                            attackDefensive();
                        }
                    }
                }
            }
            else{
                if (rc.getMode()==RobotMode.PORTABLE && rc.canTransform()) rc.transform();
                attackDefensive();
            }
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
                    if(decode()!=null){
                        System.out.println(decode());
                        intermediateMove(decode());
                    }
                 }
            }
            else{
                broadcastLattice();
                joinLattice();
            }
            rc.setIndicatorString(rc.readSharedArray(54)+" ");
            }

    }
    public void joinLattice() throws GameActionException{
        int loc = rc.readSharedArray(54);
        int x = loc/64;
        int y = loc%64;

        MapLocation target = new MapLocation (x, y);
        if (target != new MapLocation(0,0)){
            if (rc.getLocation().distanceSquaredTo(target)<rc.getType().visionRadiusSquared){
                        return;
                    }
                    intermediateMove(target);
        }
        }
    public void broadcastLattice() throws GameActionException{
        RobotInfo[] friends = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam);
        int count=0;
        int length = friends.length;
        for (int i=length; --i>0;){
            if(friends[i].getType()==RobotType.WATCHTOWER) count=count+1;
        }
        if (count>5){
            MapLocation m = rc.getLocation();
            int x = m.x;
            int y = m.y;
            rc.writeSharedArray(54, 64*x+y);
        }
        else if (rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam.opponent()).length >0){
            rc.writeSharedArray(54, rc.getLocation().x*64+rc.getLocation().y);
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