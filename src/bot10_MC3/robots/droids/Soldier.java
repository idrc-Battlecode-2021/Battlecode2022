package bot10_MC3.robots.droids;
import battlecode.common.*;
import bot10_MC3.util.PathFindingSoldier;

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
    private int targetType = 0;
    //0 = tier 1
    //1 = tier 2
    //2 = tier 3

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
        //avoidCharge();
        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(3, rc.readSharedArray(3)+1);
        }else if(rc.getRoundNum()%3 == 0){
            globalSoldierCount = rc.readSharedArray(3);
        }
        broadcast();
        //target = null;

        int healCheck = rc.readSharedArray(31+myArchonOrder);
        if(healCheck < 18 || addedToHeal){
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
        if(target != null && hasMapLocation(35)){
            if(decode(35).equals(target)){
                target = null;
                targetType = 0;
            }
        }
        if (hasMapLocation(45)){
            MapLocation target/*temp*/ = decode(45);
            /*if(target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)){
                target = temp;
            }*/
            soldierMove(target);
        }
        else if(hasMapLocation(43) && globalSoldierCount > 5){
            MapLocation temp = decode(43);
            if((target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)) && targetType <= 2){
                target = temp;
                targetType = 2;
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                nearbyBots = rc.senseNearbyRobots(20,myTeam.opponent());
                if (nearbyBots.length ==0){
                    if(target.equals(temp)) rc.writeSharedArray(43,0);
                    rc.writeSharedArray(35,64*target.x+target.y);
                    target = null;
                    targetType = 0;
                }
            }
        }else if (hasMapLocation()){
            MapLocation temp = decode();
            if((target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target))&& targetType <= 1){
                target = temp;
                targetType = 1;
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                nearbyBots = rc.senseNearbyRobots(20,myTeam.opponent());
                if (nearbyBots.length == 0){
                    if(target.equals(temp))rc.writeSharedArray(55,0);
                    rc.writeSharedArray(35,64*target.x+target.y);
                    target = null;
                    targetType = 0;
                }
            }
        }else if (hasMapLocation(41)){
            MapLocation temp = decode(41);
            if((target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)) && targetType <= 0){
                target = temp;
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                nearbyBots = rc.senseNearbyRobots(20,myTeam.opponent());
                if (nearbyBots.length == 0){
                    if(target.equals(temp)) rc.writeSharedArray(41,0);
                    rc.writeSharedArray(35,64*target.x+target.y);
                    target = null;
                    targetType = 0;
                }
            }
        } else if(target != null){
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                nearbyBots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared,myTeam.opponent());
                if (nearbyBots.length == 0){
                    rc.writeSharedArray(35,64*target.x+target.y);
                    target = null;
                    targetType = 0;
                }
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
        }else{ //Follows miners. If no miners stay near Archon
            RobotInfo[] nearbyTeam = rc.senseNearbyRobots(20,myTeam);
            MapLocation miner = new MapLocation(10000,10000);
            for(int i = nearbyTeam.length; --i>=0;){
                if(nearbyTeam[i].getType() == RobotType.MINER){
                    if(nearbyTeam[i].location.distanceSquaredTo(centralArchon) < miner.distanceSquaredTo(centralArchon)){ //Try it with center, mylocation, centralArchon, archonLoc
                        miner = nearbyTeam[i].location;
                    }
                }
            }
            if(miner.equals(new MapLocation(10000,10000)) && rc.senseNearbyRobots(2).length>2){
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
            }else{
                soldierMove(miner);
            }
        }
        if(rc.isActionReady()){
            MapLocation temp = selectPriorityTarget();
            if(temp != null && !temp.equals(myLocation)){
                target = temp;
            }
        }
        else if(target != null){ //Retreat If they can't attack
            MapLocation targetRetreat = myLocation;
            for(int i = directions.length; --i>=0;){
                MapLocation adjacent = rc.adjacentLocation(directions[i]);
                if(adjacent.distanceSquaredTo(target) >= myLocation.distanceSquaredTo(target) && rc.canMove(directions[i])){
                    if(targetRetreat == null || rc.senseRubble(adjacent) <= rc.senseRubble(targetRetreat)){
                        targetRetreat = adjacent;
                    }
                }
            }
            if(targetRetreat != null || !targetRetreat.equals(myLocation)) intermediateMove(targetRetreat);
        }
        
    }

    public void retreat() throws GameActionException{
        if(rc.getHealth()>=49){
            shouldHeal=false;
            return;
        }
        if (rc.getHealth()<=8){
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