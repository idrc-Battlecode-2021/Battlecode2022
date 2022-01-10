package bot4.robots.droids;

import java.lang.annotation.Target;

import battlecode.common.*;

public class Soldier extends Droid{
    private MapLocation target;
    private MapLocation archonLoc;
    private MapLocation [] corners = new MapLocation[4];
    private MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    private int globalSoldierCount = 0;
    private boolean defensive = false;
    private boolean reachedLocation = false;
    public Soldier(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        readArchonLocs();
        possibleArchonLocs();
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
        checkSymmetry();
        MapLocation enemyArchon = readSymmetry();
        //rc.setIndicatorString("archon: "+myArchonOrder);
        avoidCharge();
        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(3, rc.readSharedArray(3)+1);
        }else if(rc.getRoundNum()%3 == 0){
            globalSoldierCount = rc.readSharedArray(3);
        }
        broadcast();
        target = null;
        RobotInfo[] nearbyBots = rc.senseNearbyRobots(20,rc.getTeam().opponent());
        int possibleLocation = rc.readSharedArray(12);
        if(nearbyBots.length >= 1){
            //New targetting
            target = selectPriorityTarget();
            if (target!=rc.getLocation()){
                if (rc.canAttack(target)){
                    rc.attack(target);
                }
                else if (rc.isActionReady()){
                    intermediateMove(target);
                }
                return;
            }
        }
        if (globalSoldierCount>10 && possibleLocation>0 && !reachedLocation){
            //Chooses the closest location where an enemy has been sighted
            int bytecode = Clock.getBytecodeNum();
            int locs1 = rc.readSharedArray(12);
            int locs2 = rc.readSharedArray(13);
            MapLocation one = new MapLocation(locs1%16*4, locs1%256/16*4);
            MapLocation two = new MapLocation(locs1%4096/256*4, locs1/4096*4);
            MapLocation three = new MapLocation(locs2%16*4, locs2%256/16*4);
            MapLocation four = new MapLocation(locs2%4096/256*4, locs2/4096*4);
            rc.setIndicatorString(one+ " "+two+" "+three+" "+four);
            target = one;
            if (two.x!=0 && two.y!=0 && movementTileDistance(rc.getLocation(), two)<movementTileDistance(rc.getLocation(), target)){
                target = two;
            }
            if (three.x!=0 && three.y!=0 && movementTileDistance(rc.getLocation(), three)<movementTileDistance(rc.getLocation(), target)){
                target = three;
            }
            if (four.x!=0 && four.y!=0 && movementTileDistance(rc.getLocation(), four)<movementTileDistance(rc.getLocation(), target)){
                target = four;
            }

            if (rc.canSenseLocation(target) && rc.getLocation().isWithinDistanceSquared(target, 8)){
                reachedLocation = true;
            }
            else if (rc.isActionReady()){
                intermediateMove(target);
            }
            if (Clock.getBytecodeNum()-bytecode>1000){
                System.out.println("targetting BC: "+(Clock.getBytecodeNum()-bytecode));
            }
        }
        /*
        else if (defensive){
            if(rc.getLocation().distanceSquaredTo(archonLoc)<2){
                tryMoveMultiple(rc.getLocation().directionTo(archonLoc).opposite());
            }
            else if (rc.getLocation().distanceSquaredTo(archonLoc)>20){
                tryMoveMultiple(rc.getLocation().directionTo(archonLoc));
            }
        }
        */
        else if (hasMapLocation()){
            MapLocation target = decode();
            if (rc.getLocation().distanceSquaredTo(target)<20){
                if (nearbyBots.length <5){
                    rc.writeSharedArray(55,0);
                }
            }
            if (rc.isActionReady()){
                intermediateMove(target);
            }
        }
        else{
            if (!rc.isActionReady()){
                return;
            }
            if(enemyArchon !=null){
                intermediateMove(enemyArchon);
            }
            MapLocation [] all = rc.getAllLocationsWithinRadiusSquared(myLocation, 20);
            for (int i = all.length; --i>=0;){
                for (MapLocation c: corners){
                    if (all[i]==c){
                        Direction d = myLocation.directionTo(c).opposite();
                        tryMoveMultiple(d);
                    }
                }
            }
            /*if (rc.getLocation().distanceSquaredTo(archonLoc)<30){
                Direction d = myLocation.directionTo(center);
                tryMoveMultiple(d);
            }*/
            if(rc.readSharedArray(40) == 1){
                if (rc.getLocation().distanceSquaredTo(archonLoc)<4){
                    Direction d = myLocation.directionTo(center);
                    tryMoveMultiple(d);
                }
                else if(!tryMoveMultipleNew()){
                    tryMoveMultiple(initDirection);
                }
            }else if(rc.senseNearbyRobots(2).length>3){
                //updateDirection(myLocation.directionTo(new MapLocation(mapWidth/2,mapHeight/2)).opposite());
                //tryMoveMultiple(initDirection);
                MapLocation[] local = rc.getAllLocationsWithinRadiusSquared(myLocation,2);
                int start = (int) (local.length * Math.random());
                loop1: for (int i = start; i < start + local.length; i++) {
                    int j = i % local.length;
                    Direction dirTo = myLocation.directionTo(local[j]);
                    if(!myLocation.equals(local[j]) && rc.canMove(dirTo)){
                        rc.move(dirTo);
                        myLocation = rc.getLocation();
                        prevLocs.add(local[j]);
                        break;
                    }
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
