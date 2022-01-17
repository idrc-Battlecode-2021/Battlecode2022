package bot5_JJ8.robots.droids;

import battlecode.common.*;

import java.util.*;

public class Miner extends Droid{
    private HashMap<MapLocation,Integer> gold = new HashMap<>();
    private HashMap<MapLocation,Integer> lead = new HashMap<>();
    private MapLocation target;
    private int exploreDirIndex;
    private int targetType = 0;
    //0 = exploreTarget, 1 = lead, 2 = null/gold

    public Miner(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        parseAnomalies();
        target = null;
        //exploreTarget = new MapLocation((int)(rc.getMapWidth()*Math.random()),(int)(rc.getMapHeight()*Math.random()));
        //exploreDirIndex = (int)(8*Math.random());
        //tryMoveMultipleNew();
        viewResources();
        detectArchon();
    }

    @Override
    public void run() throws GameActionException {
        broadcast();
        reassignArchon();
        avoidCharge();
        // update shared array
        MapLocation prev = myLocation;
        if (rc.getRoundNum() % 3 == 2) {
            if (myArchonOrder <= 1) {
                rc.writeSharedArray(0, rc.readSharedArray(0) + (int) Math.pow(256, myArchonOrder));
            } else {
                rc.writeSharedArray(10, rc.readSharedArray(10) + (int) Math.pow(256, myArchonOrder - 2));
            }
            rc.writeSharedArray(44,rc.readSharedArray(44)+1);
        }
        if(checkEnemy()){
            MapLocation[] local = rc.senseNearbyLocationsWithGold(2);
            for(int i = local.length; --i >= 0;){
                if(rc.canMineGold(local[i])){
                    rc.mineGold(local[i]);
                    target = local[i];
                    targetType = 2;
                    gold.put(target,rc.senseGold(target));
                    break;
                }
            }
            local = rc.senseNearbyLocationsWithLead(2);
            for(int i = local.length; --i >= 0;){
                if(rc.senseLead(local[i]) > 1 && rc.canMineLead(local[i])){
                    rc.mineLead(local[i]);
                    target = local[i];
                    targetType = 1;
                    lead.put(target,rc.senseLead(target));
                    break;
                }
            }
        }else{
            if(targetType == 1 && target != null && rc.canSenseLocation(target) && rc.senseLead(target) < 2){
                target = getMaxLead();
            }
            if(gold.isEmpty()){
                int amount = rc.senseLead(myLocation);
                if(amount > 1){
                    target = myLocation;
                    targetType = 1;
                    lead.put(target,amount);
                    RobotInfo[] robots = rc.senseNearbyRobots(2);
                    for(int i = robots.length; --i>=0;){
                        if(robots[i].getType()==RobotType.ARCHON && rc.canMineLead(target)){
                            rc.mineLead(target);
                        }
                    }
                    if(rc.senseLead(myLocation) > 1 && rc.canMineLead(target))rc.mineLead(myLocation);
                }
            }
            miningBlock:{
                if(target != null){
                    leadBlock:{
                        if(targetType == 1){
                            if(!gold.isEmpty()){ //prioritize gold over Lead
                                MapLocation temp = getMaxGold();
                                if(temp != null){
                                    targetType = 2;
                                    target = temp;
                                    break leadBlock;
                                }
                            }
                            checkTargetBlock:{
                                if(rc.canSenseLocation(target) && rc.senseLead(target) == 0){
                                    lead.remove(target);
                                    if(lead.isEmpty()) target = null;
                                    else target = getMaxLead();
                                    if(target == null){
                                        targetType = 2;
                                        break miningBlock;
                                    }
                                    else{
                                        targetType = 1;
                                    }
                                    break checkTargetBlock;
                                }
                                RobotInfo robot;
                                if(!myLocation.equals(target) && rc.canSenseRobotAtLocation(target) && (robot = rc.senseRobotAtLocation(target))!=null){
                                    if(robot.getTeam() == myTeam && robot.getType() == myType){
                                        lead.remove(target);
                                        if(lead.isEmpty()) target = null;
                                        else target = getMaxLead();
                                        if(target == null){
                                            targetType = 2;
                                            break miningBlock;
                                        }
                                        else{
                                            targetType = 1;
                                        }
                                    }
                                }
                            }
                            if(!myLocation.equals(target) && rc.senseLead(myLocation) > 1){
                                RobotInfo[] localBots = rc.senseNearbyRobots(1,myTeam);
                                int count = 0;
                                for(int i = localBots.length; --i>=0;){
                                    if(localBots[i].getType().equals(RobotType.MINER))count++;
                                }
                                if(count < 3){
                                    target = myLocation;
                                    lead.put(target,rc.senseLead(myLocation));
                                }
                            }
                            if(rc.canMineLead(target)){
                                RobotInfo[] robots = rc.senseNearbyRobots(2);
                                for(int i = robots.length; --i>=0;){
                                    if(robots[i].getType()==RobotType.ARCHON){
                                        rc.mineLead(target);
                                    }
                                }
                                if(!target.equals(myLocation) && rc.canSenseRobotAtLocation(target)){
                                    RobotInfo potentialMiner = rc.senseRobotAtLocation(target);
                                    if(!potentialMiner.getTeam().equals(myTeam) && potentialMiner.getType().equals(RobotType.MINER)){
                                        if(rc.canMineLead(target)) rc.mineLead(target);
                                    }
                                } else if(rc.senseLead(target) > 1 && rc.canMineLead(target)) rc.mineLead(target);
                            }
                            if(myLocation.equals(target)){
                                RobotInfo[] nearbyBots = rc.senseNearbyRobots(2,myTeam);
                                boolean nextToMiner = false;
                                for (int i = nearbyBots.length; --i >= 0; ) {
                                    if (nearbyBots[i].getType() == myType && nearbyBots[i].getID() != rc.getID()) {
                                        nextToMiner = true;
                                        break;
                                    }
                                }
                                MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(2);
                                if (nextToMiner) {
                                    int amount = lead.get(target);
                                    int start = (int) (nearbyLead.length * Math.random());
                                    loop1: for (int i = start; i < start + nearbyLead.length; i++) {
                                        int j = i % nearbyLead.length;
                                        MapLocation[] nextTo = rc.getAllLocationsWithinRadiusSquared(nearbyLead[j], 1);
                                        boolean none = false;
                                        for (int k = nextTo.length; --k >= 0; ) {
                                            if (rc.canSenseRobotAtLocation(nextTo[k]) && rc.senseRobotAtLocation(nextTo[k]).getID() != rc.getID()){
                                                continue loop1;
                                            }
                                        }
                                        Direction dirTo = myLocation.directionTo(nearbyLead[j]);
                                        if (!myLocation.equals(nearbyLead[j]) && rc.canMove(dirTo)){
                                            rc.move(dirTo);
                                            myLocation = rc.getLocation();
                                            target = nearbyLead[j];
                                            lead.put(target, rc.senseLead(nearbyLead[j]));
                                            break loop1;
                                        }
                                    }
                                }else {
                                    Arrays.sort(nearbyLead,(MapLocation o1, MapLocation o2) -> {
                                        try {
                                            return rc.senseLead(o1) - rc.senseLead(o2);
                                        } catch (GameActionException e) {
                                            e.printStackTrace();
                                            return 1;
                                        }
                                    });
                                    for(int i = nearbyLead.length; --i>=0;){
                                        Direction dirTo = myLocation.directionTo(nearbyLead[i]);
                                        if (rc.canMove(dirTo)){
                                            rc.move(dirTo);
                                            myLocation = rc.getLocation();
                                            target = nearbyLead[i];
                                            lead.put(target, rc.senseLead(nearbyLead[i]));
                                            break;
                                        }
                                    }
                                }
                            }
                            intermediateMove(target);
                            boolean mine = true;
                            while(rc.isActionReady() && mine){
                                mine = false;
                                MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(2);
                                for(int i = nearbyLead.length; --i>=0;) {
                                    if (rc.senseLead(nearbyLead[i]) > 1 && rc.canMineLead(nearbyLead[i])){
                                        rc.mineLead(nearbyLead[i]);
                                        mine = true;
                                    }
                                }
                            }

                        }
                    }
                    if(targetType == 2){
                        if(rc.canSenseLocation(target) && rc.senseGold(target) == 0){
                            gold.remove(target);
                            if(gold.isEmpty()) target = null;
                            else{
                                target = getMaxGold();
                                targetType = 2;
                            }
                            if(target == null) {
                                if (lead.isEmpty()) target = null;
                                else target = getMaxLead();
                                if (target == null){
                                    targetType = 2;
                                    break miningBlock;
                                }
                                else {
                                    targetType = 1;
                                    run();
                                    return;
                                }
                            }
                        }
                        if(rc.canMineGold(target)){
                            rc.mineGold(target);
                        }intermediateMove(target);
                        while(rc.isActionReady()){
                            boolean mine = true;
                            while(rc.isActionReady() && mine){
                                mine = false;
                                MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(2);
                                for(int i = nearbyLead.length; --i>=0;) {
                                    if (rc.senseLead(nearbyLead[i]) > 1 && rc.canMineLead(nearbyLead[i])){
                                        rc.mineLead(nearbyLead[i]);
                                        mine = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(target == null){
                checkMiners();
                if(!tryMoveMultipleNew()){
                   tryMoveMultiple(initDirection);
                }
                if(!prev.equals(myLocation)) viewResources();
            }
        }
    }

    public void viewResources() throws GameActionException {
        //TODO: Method currently doesn't consider if a previously checked location still has resources
        //TODO: Could also check rubble amount
        //Maybe change so that the closest location above the threshold is chosen as the target rather than
        MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold(20),
            nearbyLead = rc.senseNearbyLocationsWithLead(20,3); //change to (20,6)
        searchBlock:{
            for(int i = nearbyGold.length; --i>=0;){
                gold.put(nearbyGold[i],rc.senseGold(nearbyGold[i]));
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
            targetType = 2;
        }else if(!lead.isEmpty()){
            target = getMaxLead();
            if(target == null) targetType = 2;
            else targetType = 1;
        }
    }

    public MapLocation getMaxGold() throws GameActionException { //location with Max amount of resources
        //Currently choosing closest Location over Amount, may want to change.
        MapLocation loc = null;
        List<Map.Entry<MapLocation, Integer>> entries = new ArrayList<Map.Entry<MapLocation, Integer>>(gold.entrySet());
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
        List<Map.Entry<MapLocation, Integer>> entries = new ArrayList<Map.Entry<MapLocation, Integer>>(lead.entrySet());
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
                if(rc.senseLead(location) < 2){
                    lead.remove(location);
                    continue loop1;
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
                targetType = 2;
                return tryMoveMultiple(selectDirection(xMove,yMove));
            }

        }
        return false;
    }
    private void checkMiners() { //Repel against other miners
        int momentumVectorX = initDirection.getDeltaX();
        int momentumVectorY = initDirection.getDeltaY();
        RobotInfo[] nearbyBots = rc.senseNearbyRobots(20,myTeam);
        for(int i = nearbyBots.length; --i>=0;){
            if(nearbyBots[i].getType()==RobotType.MINER){
                momentumVectorX -= nearbyBots[i].getLocation().x-myLocation.x;
                momentumVectorY -= nearbyBots[i].getLocation().y-myLocation.y;
            }
        }
        if(momentumVectorX != 0 || momentumVectorY != 0){
            updateDirection(selectDirection(momentumVectorX,momentumVectorY));
        }

    }
}

