package bot9_JJ.robots.droids;
import battlecode.common.*;
import bot9_JJ.util.PathFindingSoldier;

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
    private boolean reachedArchon;
    private boolean addedToHeal = false;
    public Soldier(RobotController rc) {super(rc);}

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
        detectArchon();
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

    public void setArchonLocation() throws GameActionException{
        int location = 0;
        switch (myArchonOrder){
            case 0:
                location = rc.readSharedArray(15);
                break;
            case 1:
                location = rc.readSharedArray(16);
                break;
            case 2:
                location = rc.readSharedArray(49);
                break;
            case 3:
                location = rc.readSharedArray(50);
                break;
        }
        int x = location%256;
        int y = location/256;
        archonLoc = new MapLocation(x,y);
        rc.setIndicatorString(myArchonOrder+" "+archonLoc.toString());
    }

    @Override
    public void run() throws GameActionException {
        reassignArchon();
        setArchonLocation();
        avoidCharge();
        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(3, rc.readSharedArray(3)+1);
        }else if(rc.getRoundNum()%3 == 0){
            globalSoldierCount = rc.readSharedArray(3);
        }
        broadcast();
        target = null;

        int healCheck = rc.readSharedArray(31+myArchonOrder);
        if(healCheck < 24 || addedToHeal){
            retreat();
            if(shouldHeal){
                if(!addedToHeal){
                    rc.writeSharedArray(31+myArchonOrder,healCheck+1);
                    addedToHeal = true;
                }
                selectPriorityTarget();
                return;
            }
        }
        if(addedToHeal){
            rc.writeSharedArray(31+myArchonOrder,healCheck-1);
            addedToHeal = false;
       }
        RobotInfo[] nearbyBots = rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared,rc.getTeam().opponent());
        if(nearbyBots.length >= 1){
            //New targetting
            target = selectPriorityTarget();
            return;
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
                if(Clock.getBytecodesLeft() > 6000){
                    soldierExplore();
                }else{
                    if (!tryMoveMultipleNew()) {
                        tryMoveMultiple(initDirection);
                    }
                }

            }
        }else if(rc.senseNearbyRobots(2).length>2){
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
        if(rc.isActionReady())selectPriorityTarget();
    }

    public void retreat() throws GameActionException{
        if(rc.getHealth()>=49){
            shouldHeal=false;
            return;
        }
        if (rc.getHealth()<=18){
            shouldHeal = true;
            reachedArchon = false;
        }
        if (shouldHeal){
            if (!rc.getLocation().isWithinDistanceSquared(archonLoc, RobotType.ARCHON.actionRadiusSquared)){
                //rc.setIndicatorString("going to heal");
                soldierMove(archonLoc);
            }
            else{
                reachedArchon = true;

            }

            //TODO: try this code after archon moves to low passability?
            /*
            else if (rc.isMovementReady()){
                RobotInfo[] nearbyBots = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared,rc.getTeam().opponent());
                if (nearbyBots.length>0){
                    tryMoveMultiple(rc.getLocation().directionTo(nearbyBots[0].getLocation()).opposite());
                }
            }
            */
            
        }
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
            intermediateMove(target);
            pastLocations.add(temp);
        }
    }

    private MapLocation pastExploreTarget = null;
    private HashSet<MapLocation> pastExploreLocations = new HashSet<>();
    private void soldierExplore() throws GameActionException{
        MapLocation exploreTarget = pfs.getExploreTarget();
        if(!exploreTarget.equals(pastExploreTarget)){
            pastExploreTarget = exploreTarget;
            pastExploreLocations.clear();
        }
        Direction dir = pfs.getBestDir(exploreTarget);
        MapLocation temp = myLocation;
        if(dir != null && rc.canMove(dir) && !pastExploreLocations.contains(myLocation.add(dir))){
            if(tryMoveMultiple(dir)){
                pastExploreLocations.add(temp);
            }
        }else{
            intermediateMove(exploreTarget);
            pastExploreLocations.add(temp);
        }
    }
}