package bot3_JJ4.robots.droids;

import battlecode.common.*;

import java.util.*;

//Work in Progress, maybe one day will supercede other miner
public class Miner2 extends Droid{
    private final HashMap<MapLocation,Integer> gold = new HashMap<>();
    private final HashMap<MapLocation,Integer> lead = new HashMap<>();
    private MapLocation target;
    private boolean isTargetLead = false;

    public Miner2(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        parseAnomalies();
        target = null;
        viewResources();

    }

    @Override
    public void run() throws GameActionException {
        broadcast();
        reassignArchon();
        avoidCharge();
        // update shared array
        if(myLocation.equals(target)){
            rc.writeSharedArray(31+myArchonOrder,rc.readSharedArray(31+myArchonOrder)+1);
        }
        MapLocation prev = myLocation;
        if (rc.getRoundNum() % 3 == 2) {
            if (myArchonOrder <= 1) {
                rc.writeSharedArray(0, rc.readSharedArray(0) + (int) Math.pow(256, myArchonOrder));
            } else {
                rc.writeSharedArray(10, rc.readSharedArray(10) + (int) Math.pow(256, myArchonOrder - 2));
            }
        }
        if(checkEnemy()){ //if bot encounters enemies
            MapLocation[] local = rc.senseNearbyLocationsWithGold(2);
            for(int i = local.length; --i >= 0;){
                if(rc.canMineGold(local[i])){
                    rc.mineGold(local[i]);
                    target = local[i];
                    isTargetLead = false;
                    break;
                }
            }
            local = rc.senseNearbyLocationsWithLead(2,2);
            for(int i = local.length; --i >= 0;){
                if(rc.senseLead(local[i]) > 1 && rc.canMineLead(local[i])){
                    rc.mineLead(local[i]);
                    target = local[i];
                    isTargetLead = true;
                    break;
                }
            }
        }else{
            if(target!=null){
                if(isTargetLead){ //if robot found lead
                    MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold(20);
                    if(nearbyGold.length > 0){
                        target = nearbyGold[0];
                        isTargetLead = false;
                        intermediateMove(target);
                        while(rc.canMineGold(target)){
                            rc.mineGold(target);
                        }
                        int amount = rc.senseGold(target);
                        if(amount == 0){
                            target = null;
                        }else{
                            gold.put(target,amount);
                        }
                    }else{
                        if(rc.canSenseLocation(target)){
                            int amount = rc.senseLead(target);
                            if(amount > 1){
                                if(rc.canMineLead(target))rc.mineLead(target);
                            }else{
                                lead.remove(target);
                                target = null;
                            }
                        }else{
                            intermediateMove(target);
                        }
                    }
                }else{ //if robot found gold

                }
            }
            if (target == null){//if robot has no target
                if(!tryMoveMultipleNew()){
                   tryMoveMultiple(initDirection);
                }
                if(!prev.equals(myLocation)) viewResources();
            }
        }

        //Mines nearbyLead if able to
        if(rc.isActionReady()){
            MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(2);
            if(nearbyLead.length > 0){
                for(int i = nearbyLead.length; --i>=0;){
                    int amount = rc.senseLead(nearbyLead[i]);
                    if(amount > 1){
                        if(rc.canMineLead(nearbyLead[i])){
                            rc.mineLead(nearbyLead[i]);
                            amount = rc.senseLead(nearbyLead[i]);
                        }
                        if(amount > 1){
                            lead.put(nearbyLead[i],amount);
                        }
                    }
                }
            }
        }
    }

    public void viewResources() throws GameActionException {
        //TODO: Method currently doesn't consider if a previously checked location still has resources
        //TODO: Could also check rubble amount
        //Maybe change so that the closest location above the threshold is chosen as the target rather than
        MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold(20),
            nearbyLead = rc.senseNearbyLocationsWithLead(20,6); //change to (20,6)
        searchBlock:{
            if(nearbyGold.length > 0){
                gold.put(nearbyGold[0],rc.senseGold(nearbyGold[0]));
                break searchBlock;
            }
            loop1: for(int i = nearbyLead.length; --i>=0;){
                RobotInfo robot;
                MapLocation[] nearbyLocs = rc.getAllLocationsWithinRadiusSquared(nearbyLead[i],1);
                for(int j = nearbyLocs.length; --j>=0;){
                    if(!myLocation.equals(nearbyLocs[j]) && rc.canSenseRobotAtLocation(nearbyLocs[j]) && (robot = rc.senseRobotAtLocation(nearbyLocs[j]))!=null){
                        if(robot.getTeam() == myTeam && robot.getType() == myType){
                            continue loop1;
                        }
                    }
                }
                int amount = rc.senseLead(nearbyLead[i]);
                if(amount > 5){
                    lead.put(nearbyLead[i],amount);
                    break searchBlock;
                }
            }
        }
        if(!gold.isEmpty()){
            target = getMaxGold();
            isTargetLead = false;
        }else if(!lead.isEmpty()){
            target = getMaxLead();
            if(target != null) isTargetLead = true;
        }
    }

    public MapLocation getMaxGold() throws GameActionException { //location with Max amount of resources
        //Currently choosing closest Location over Amount, may want to change.
        MapLocation loc = null;
        List<Map.Entry<MapLocation, Integer>> entries = new ArrayList<>(gold.entrySet());
        /*Collections.sort(entries, new Comparator<Map.Entry<MapLocation, Integer>>() {
            public int compare(
                    Map.Entry<MapLocation, Integer> entry1, Map.Entry<MapLocation, Integer> entry2) {
                return ((Integer)movementTileDistance(entry1.getKey(),myLocation)).compareTo(movementTileDistance(entry2.getKey(),myLocation));
            }
        });*/
        for(Map.Entry<MapLocation, Integer> entry : entries){
            MapLocation location = entry.getKey();
            if(rc.canSenseLocation(location)){
                if(rc.senseGold(location) == 0){
                    gold.remove(location);
                }else{
                    loc = location;break;
                }
            }else{
                loc = location;break;
            }
        }

        return loc;
    }
    public MapLocation getMaxLead() throws GameActionException { //location with Max amount of resources
        //Currently choosing closest Location over Amount, may want to change.
        MapLocation loc = null;
        List<Map.Entry<MapLocation, Integer>> entries = new ArrayList<>(lead.entrySet());
        /*Collections.sort(entries, new Comparator<Map.Entry<MapLocation, Integer>>() {
            public int compare(
                    Map.Entry<MapLocation, Integer> entry1, Map.Entry<MapLocation, Integer> entry2) {
                return ((Integer)movementTileDistance(entry1.getKey(),myLocation)).compareTo(movementTileDistance(entry2.getKey(),myLocation));
            }
        });*/
        loop1: for(Map.Entry<MapLocation, Integer> entry : entries){
            MapLocation location = entry.getKey();
            if(rc.canSenseLocation(location)){
                RobotInfo robot;
                MapLocation[] nearbyLocs = rc.getAllLocationsWithinRadiusSquared(location,1);
                for(int j = nearbyLocs.length; --j>=0;){
                    if(!myLocation.equals(nearbyLocs[j]) && rc.canSenseRobotAtLocation(nearbyLocs[j]) && (robot = rc.senseRobotAtLocation(nearbyLocs[j]))!=null){
                        if(robot.getTeam() == myTeam && robot.getType() == myType){
                            lead.remove(location);
                            continue loop1;
                        }
                    }
                }
                if(rc.senseLead(location) < 6){
                    lead.remove(location);
                }else{
                    loc = location;break;
                }
            }else{
                loc = location;break;
            }
        }
        return loc;
    }

    public boolean checkEnemy() throws GameActionException {
        //detect enemy muckraker
        RobotInfo[] robots=rc.senseNearbyRobots(20,myTeam.opponent());
        if(robots.length > 0){
            rc.writeSharedArray(42,1);
            int xMove = 0, yMove = 0;
            for (RobotInfo robot : robots){
                switch(robot.getType()){
                    case SAGE:
                    case ARCHON:
                    case SOLDIER:
                    case WATCHTOWER:
                        switch (myLocation.directionTo(robot.getLocation())){
                            case EAST:      xMove--;                break;
                            case WEST:      xMove++;                break;
                            case NORTH:                 yMove--;    break;
                            case SOUTH:                 yMove++;    break;
                            case NORTHEAST: xMove--;    yMove--;    break;
                            case NORTHWEST: xMove++;    yMove--;    break;
                            case SOUTHEAST: xMove--;    yMove++;    break;
                            case SOUTHWEST: xMove++;    yMove++;    break;
                        }
                }
            }
            if(xMove != 0 || yMove != 0){
                rc.writeSharedArray(40,1);
                target = null;
                return tryMoveMultiple(selectDirection(xMove,yMove));
            }

        }
        return false;
    }
}
