package bot5_JJ9.robots.droids;
import battlecode.common.*;
import bot5_JJ9.util.PathFindingSoldier;

import java.util.HashSet;

public class Soldier extends Droid{
    private MapLocation target = null;
    private MapLocation archonLoc;
    private MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    private int globalSoldierCount = 0;
    private boolean shouldHeal = false;
    private MapLocation[] archonLocs;
    private MapLocation centralArchon;
    private PathFindingSoldier pfs;
    public Soldier(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        pfs=new PathFindingSoldier(rc);
        possibleArchonLocs();
        parseAnomalies();
        RobotInfo [] r = rc.senseNearbyRobots(2,myTeam);
        for (RobotInfo ro : r){
            if(ro.getTeam()==myTeam && ro.getType()==RobotType.ARCHON){
                archonLoc = ro.getLocation();
            }
        }
        int archonCount = rc.getArchonCount();
        archonLocs = getArchonLocs();
        int x = 0, y = 0;
        for(int i = 0; i < archonLocs.length; i++){ //needs to start at 0
            if(archonLocs[i] == null)break;
            x += archonLocs[i].x;
            y += archonLocs[i].y;
        }
        centralArchon = new MapLocation(x/archonCount,y/archonCount);
    }

    static boolean heal = true;
    @Override
    public void run() throws GameActionException {
        reassignArchon();
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
        /*for(int i = nearbyBots.length; --i>=0;){
            if(nearbyBots[i].getType()==RobotType.SOLDIER){
                heal = false;
                break;
            }
        }*/
        int healCheck = rc.readSharedArray(31+myArchonOrder);
        if((healCheck == 0 && heal)|| healCheck == rc.getID()){
            retreat();
            if(shouldHeal){//Adding the if statement does make it lose one more game, but that would be stupid
                selectPriorityTarget();
                return;
            }
        }
        if(nearbyBots.length >= 1){
            //New targetting
            target = selectPriorityTarget();
        }
        if (hasMapLocation(45)){
            MapLocation target = decode(45);
            soldierMove(target);
        }else if(hasMapLocation(43) && globalSoldierCount > 5){
            MapLocation target = decode(43);
            if (rc.canSenseLocation(target)){
                if (nearbyBots.length ==0){
                    rc.writeSharedArray(43,0);
                }
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
        }else if (hasMapLocation()){
            MapLocation target = decode();
            if (rc.canSenseLocation(target)){
                if (nearbyBots.length == 0){
                    rc.writeSharedArray(55,0);
                }
            }
            if(rc.isActionReady()){
                soldierMove(target);
            };
        }else if (hasMapLocation(41)){
            MapLocation target = decode(41);
            if (rc.canSenseLocation(target)){
                if (nearbyBots.length == 0){
                    rc.writeSharedArray(41,0);
                }
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
        } else if(rc.readSharedArray(40) == 1) {
            if(rc.isActionReady()){
                if (!tryMoveMultipleNew()) {
                    tryMoveMultiple(initDirection);
                }
            }
        }else if(rc.senseNearbyRobots(2).length>2){
            MapLocation[] local = rc.getAllLocationsWithinRadiusSquared(myLocation,2);
            int start = (int) (local.length * Math.random());
            for (int i = start; i < start + local.length; i++) {
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
        if(rc.isActionReady())selectPriorityTarget();
    }

    public void retreat() throws GameActionException{
        if(rc.getHealth()>49){
            shouldHeal=false;
            rc.writeSharedArray(31+myArchonOrder,0);
            return;
        }
        if(rc.getHealth()>25)return;
        rc.writeSharedArray(31+myArchonOrder,rc.getID());
        shouldHeal=true;
        soldierMove(archonLoc);
    }

    private MapLocation pastTarget = null;
    private HashSet<MapLocation> pastLocations = new HashSet<>();
    private void soldierMove(MapLocation target) throws GameActionException {
        if(!target.equals(pastTarget)){
            pastTarget = target;
            pastLocations.clear();
        }
        Direction dir = pfs.getBestDir(target);
        MapLocation temp = myLocation;
        if(dir != null && rc.canMove(dir) && !pastLocations.contains(myLocation.add(dir))){
            if(tryMoveMultiple(dir)){
                pastLocations.add(temp);
            }
        }else{
            pastLocations.add(temp);
            intermediateMove(target);
        }
    }
}