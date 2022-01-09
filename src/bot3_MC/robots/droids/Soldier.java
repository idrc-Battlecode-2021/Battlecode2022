package bot3_MC.robots.droids;

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
        rc.setIndicatorString(myArchonOrder+"");
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
            if(rc.getLocation().distanceSquaredTo(archonLoc)<2){
                tryMoveMultiple(rc.getLocation().directionTo(archonLoc).opposite());
            }
            else if (rc.getLocation().distanceSquaredTo(archonLoc)>20){
                tryMoveMultiple(rc.getLocation().directionTo(archonLoc));
            }
        } else if (hasMapLocation(47)){ 
            MapLocation target = decode(47);
            if (rc.getLocation().distanceSquaredTo(target)<20){
        		RobotInfo[] nearbySoldiers = rc.senseNearbyRobots(20,rc.getTeam());
        		int count = 0;
        		for(RobotInfo r : nearbySoldiers){
                    if(r.getType().equals(RobotType.SOLDIER)){
                        count++;
                    }
                }
                defensive = true;
                archonLoc = target;
                if(count > 5) rc.writeSharedArray(47,0);

            }
            intermediateMove(target);
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
            MapLocation [] all = rc.getAllLocationsWithinRadiusSquared(myLocation, 20);
            for (int i = all.length; --i>=0;){
                for (MapLocation c: corners){
                    if (all[i]==c){
                        Direction d = myLocation.directionTo(c).opposite();
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
