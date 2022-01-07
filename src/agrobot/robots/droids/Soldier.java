package agrobot.robots.droids;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.Map;

public class Soldier extends Droid{
    private MapLocation target;
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
        broadcast();
        target = null;
        RobotInfo[] nearbyBots = rc.senseNearbyRobots(20,rc.getTeam().opponent());
        if(nearbyBots.length >= 1){
            target = nearbyBots[nearbyBots.length-1].getLocation();
            for(int i=nearbyBots.length-1;--i>=0;){
                MapLocation temp = nearbyBots[i].getLocation();
                if(movementTileDistance(target,myLocation) > movementTileDistance(temp,myLocation)) target = temp;
            }
        }
        if(target != null){
            intermediateMove(target);
            if(rc.canAttack(target))rc.attack(target);
        }
        else if (hasMapLocation()){
            MapLocation target = decode();
            if (rc.getLocation().distanceSquaredTo(target)<20){
                if (nearbyBots.length <5){
                    rc.writeSharedArray(55,0);
                }
            }
            intermediateMove(target);
        }
        else tryMoveMultipleNew();
    }
    public boolean hasMapLocation() throws GameActionException{
        if (rc.readSharedArray(55)==0){
            return false;
        }
        return true;
    }
    public MapLocation decode() throws GameActionException{
        int loc = rc.readSharedArray(55);
        int x = loc/64;
        int y = loc%64;
        return new MapLocation(x, y);
    }
    public void broadcast() throws GameActionException{
        RobotInfo [] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int num_enemies =enemies.length;

        if (num_enemies>5){
            MapLocation m = rc.getLocation();
            int x = m.x, y=m.y;
            int k=x*64+y;
            rc.writeSharedArray(55,k);
        }

    }
}
