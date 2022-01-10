package bot_MC.robots.droids;

import battlecode.common.*;

public class Soldier extends Droid{
    private MapLocation target;
    private MapLocation archonLoc;
    private MapLocation [] corners = new MapLocation[4];
    private MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    private boolean defensive = false;
    public Soldier(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        parseAnomalies();
        RobotInfo [] r = rc.senseNearbyRobots();
        for (RobotInfo ro : r){
            if(ro.getTeam()==myTeam && ro.getType()==RobotType.ARCHON){
                archonLoc = ro.getLocation();
            }
        }
        corners[0]=new MapLocation (0,0);
        corners[1]=new MapLocation(0,rc.getMapHeight());
        corners[2]=new MapLocation(rc.getMapWidth(),0);
        corners[3]=new MapLocation(rc.getMapWidth(),rc.getMapHeight());
        defensive = isDefensive();
    }

    @Override
    public void run() throws GameActionException {
        reassignArchon();
        rc.setIndicatorString("archon: "+myArchonOrder);
        avoidCharge();
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
        else if (defensive){
            if (rc.getLocation().isWithinDistanceSquared(archonLoc,8)){
                MapLocation away =  rc.getLocation().add(rc.getLocation().directionTo(archonLoc).opposite());
                if (rc.onTheMap(away)){
                    intermediateMove(away);
                    return;
                }
                else{
                    Direction direction = rc.getLocation().directionTo(archonLoc);
                    away = (archonLoc.add(direction)).add(direction);
                    if (rc.onTheMap(away)){
                        intermediateMove(away);
                        return;
                    }
                }
            }
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
        else{
            if (rc.getLocation().isWithinDistanceSquared(archonLoc,18)){
                MapLocation away =  rc.getLocation().add(rc.getLocation().directionTo(archonLoc).opposite());
                if (rc.canSenseLocation(away) && rc.onTheMap(away)){
                    intermediateMove(away);
                    return;
                }
                else{
                    Direction direction = rc.getLocation().directionTo(archonLoc);
                    away = (archonLoc.add(direction)).add(direction);
                    if (rc.canSenseLocation(away) && rc.onTheMap(away)){
                        intermediateMove(away);
                        return;
                    }
                }
            }
            MapLocation [] all = rc.getAllLocationsWithinRadiusSquared(myLocation, 20);
            for (int i = all.length; --i>=0;){
                for (MapLocation c: corners){
                    if (all[i]==c){
                        Direction d = myLocation.directionTo(c);
                        tryMoveMultiple(d);
                    }
                }
            }

            if (rc.getLocation().distanceSquaredTo(archonLoc)<30){
                Direction d = myLocation.directionTo(center);
                tryMoveMultiple(d);
            }
            else{
                if(!tryMoveMultipleNew()){
                    tryMoveMultiple(initDirection);
                }
            }
        }

    }
    public boolean isDefensive() throws GameActionException{
        RobotInfo [] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam.opponent());
        RobotInfo [] friends = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam);
        boolean justSpawned = false;
        for (RobotInfo r: friends){
            if(r.getType()==RobotType.ARCHON){
                justSpawned = true;
            }
        }
        if (enemies.length>0 && justSpawned){
            return true;
        }
        
        return false;
    }

}
