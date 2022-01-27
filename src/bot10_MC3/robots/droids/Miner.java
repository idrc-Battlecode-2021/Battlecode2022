package bot10_MC3.robots.droids;

import battlecode.common.*;
import bot10_MC3.util.Constants;
import bot10_MC3.util.PathFindingSoldier;

import java.util.*;

public class Miner extends Droid{
    private HashMap<MapLocation,Integer> gold = new HashMap<>();
    private HashMap<MapLocation,Integer> lead = new HashMap<>();
    private MapLocation target;
    private PathFindingSoldier pfs;
    private int targetType = 0;
    private boolean reachedArchon;
    private boolean addedToHeal = false;
    private MapLocation archonLoc;
    private boolean shouldHeal = false;
    //0 = exploreTarget, 1 = lead, 2 = null/gold

    public Miner(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        pfs=new PathFindingSoldier(rc);
        //parseAnomalies();
        RobotInfo [] r = rc.senseNearbyRobots(2,myTeam);
        for (RobotInfo ro : r){
            if(ro.getTeam()==myTeam && ro.getType()==RobotType.ARCHON){
                archonLoc = ro.getLocation();
            }
        }
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
        //avoidCharge();
        // update shared array
        MapLocation prev = myLocation;
        if (rc.getRoundNum() % 3 == 2) {
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
            MapLocation[] golds = rc.senseNearbyLocationsWithGold(2);
            if(golds.length > 0){
                target = golds[0];
                targetType = 2;
                gold.put(target,rc.senseGold(target));
                if(rc.canMineGold(target))rc.mineGold(target);
                if(Clock.getBytecodesLeft()>5200){
                    soldierMove(target);
                }else{
                    intermediateMove(target);
                }
                return;
            }else{
                golds = rc.senseNearbyLocationsWithGold(20);
                if(golds.length > 0){
                    target = golds[golds.length-1];
                    for(int i = golds.length-1; --i>=0;){
                        if(myLocation.distanceSquaredTo(golds[i]) < myLocation.distanceSquaredTo(target)){
                            target = golds[i];
                        }
                    }
                    targetType = 2;
                    gold.put(target,rc.senseGold(target));
                    if(Clock.getBytecodesLeft()>5200){
                        soldierMove(target);
                    }else{
                        intermediateMove(target);
                    }
                    return;
                }/*else{
                    MapLocation[] leads = rc.senseNearbyLocationsWithLead(2,2);
                    if(leads.length > 0){
                        for(int i = leads.length; --i>=0;){
                            int amount = rc.senseLead(leads[i]);
                            if(rc.canMineLead(leads[i]) && amount > 1){
                                rc.mineLead(leads[i]);
                                target = leads[i];
                                targetType = 1;
                                lead.put(target,amount);
                                break;
                            }
                        }
                    }else{
                        leads = rc.senseNearbyLocationsWithLead(20,2);
                        if(leads.length > 0){
                            target = leads[leads.length-1];
                            for(int i = leads.length-1; --i>=0;){
                                if(myLocation.distanceSquaredTo(leads[i]) < myLocation.distanceSquaredTo(target)){
                                    target = leads[i];
                                }
                            }
                            targetType = 1;
                            lead.put(target,rc.senseLead(target));
                            if(Clock.getBytecodesLeft()>5200){
                                soldierMove(target);
                            }else{
                                intermediateMove(target);
                            }
                            return;
                        }else{
                            checkMiners();
                            if(!tryMoveMultipleNew()){
                                tryMoveMultiple(initDirection);
                            }
                        }
                    }
                }*/
            }
            if(targetType == 2 && target != null && rc.canSenseLocation(target) && rc.senseGold(target) == 0){
                target = getMaxLead();
                if(target != null)targetType = 1;
                else targetType = 2;
            }
            if(targetType == 1 && target != null && rc.canSenseLocation(target) && rc.senseLead(target) < 2){
                target = getMaxLead();
                if(target != null)targetType = 1;
                else targetType = 2;
            }
            miningBlock:{
                if(target != null){
                    leadBlock:{
                        if(targetType == 1){
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
                            if(Clock.getBytecodesLeft() > 5200){
                                soldierMove(target);
                            }else{
                                intermediateMove(target);
                            }
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
            }else{
                if(Clock.getBytecodesLeft() > 5200){
                    soldierMove(target);
                }else{
                    intermediateMove(target);
                }
                if(!prev.equals(myLocation)) viewResourcesLite();
            }
        }
        //rc.setIndicatorString(""+target);
    }

    public void viewResources() throws GameActionException {
        //TODO: Method currently doesn't consider if a previously checked location still has resources
        //TODO: Could also check rubble amount
        //Maybe change so that the closest location above the threshold is chosen as the target rather than
        MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold(20),
            nearbyLead = rc.senseNearbyLocationsWithLead(20,3); //change to (20,6)
        searchBlock:{
            loop1: for(int i = nearbyLead.length; --i>=0 && Clock.getBytecodesLeft() > 400;){
                if(lead.containsKey(nearbyLead[i]))continue;
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
                    //break searchBlock;
                }
            }
        }
        if(!lead.isEmpty()){
            target = getMaxLead();
            if(target == null) targetType = 2;
            else targetType = 1;
        }
    }
    public void viewResourcesLite() throws GameActionException {
        //TODO: Method currently doesn't consider if a previously checked location still has resources
        //TODO: Could also check rubble amount
        //Maybe change so that the closest location above the threshold is chosen as the target rather than
        MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold(20),
                nearbyLead = rc.senseNearbyLocationsWithLead(20,3); //change to (20,6)
        searchBlock:{
            loop1: for(int i = nearbyLead.length; --i>=0 && Clock.getBytecodesLeft() > 400;){
                if(lead.containsKey(nearbyLead[i]))continue;
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
                    //break searchBlock;
                }
            }
        }
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
                pfs.newExploreLocation();
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

    private MapLocation pastExploreTarget = null;
    private HashSet<MapLocation> pastExploreLocations = new HashSet<>();
    private void minerExplore() throws GameActionException{
        MapLocation exploreTarget = pfs.getExploreTarget();
        if(!exploreTarget.equals(pastExploreTarget)){
            pastExploreTarget = exploreTarget;
            pastExploreLocations.clear();
        }
        Direction dir = pfs.getBestDirMiner(exploreTarget);
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

    @Override
    public boolean tryMoveMultipleNew() throws GameActionException {
        if(initDirection == null){
            updateDirection(Constants.INTERMEDIATE_DIRECTIONS[(int) (Math.random()*4)]);
        }
        switch(initDirection){
            case SOUTHEAST:
            case NORTHEAST:
            case NORTHWEST:
            case SOUTHWEST: break;
            case SOUTH: updateDirection(Constants.SOUTHERN_DIR[(int) (Math.random()*2)]); break;
            case NORTH: updateDirection(Constants.NORTHERN_DIR[(int) (Math.random()*2)]); break;
            case WEST:  updateDirection(Constants.WESTERN_DIR[(int) (Math.random()*2)]);  break;
            case EAST:  updateDirection(Constants.EASTERN_DIR[(int) (Math.random()*2)]);  break;
        }
        if(!rc.onTheMap(myLocation.add(initDirection))){
            int x = myLocation.x, y = myLocation.y;
            switch (initDirection){
                case SOUTHWEST:
                    if(x < 2){
                        if(y < 2){
                            updateDirection(Direction.NORTHEAST);
                        }else{
                            updateDirection(Direction.SOUTHEAST);
                        }
                    }else{
                        if(y < 2){
                            updateDirection(Direction.NORTHWEST);
                        }
                    }break;
                case NORTHEAST:
                    if(x > mapWidth-3){
                        if(y > mapHeight-3){
                            updateDirection(Direction.SOUTHWEST);
                        }else{
                            updateDirection(Direction.NORTHWEST);
                        }
                    }else{
                        if(y > mapHeight-3){
                            updateDirection(Direction.SOUTHEAST);
                        }
                    }break;
                case NORTHWEST:
                    if(x < 2){
                        if(y > mapHeight-3){
                            updateDirection(Direction.SOUTHEAST);
                        }else{
                            updateDirection(Direction.NORTHEAST);
                        }
                    }else{
                        if(y > mapHeight-3){
                            updateDirection(Direction.SOUTHWEST);
                        }
                    }break;
                case SOUTHEAST:
                    if(x > mapWidth-3){
                        if(y < 2){
                            updateDirection(Direction.NORTHWEST);
                        }else{
                            updateDirection(Direction.SOUTHWEST);
                        }
                    }else{
                        if(y < 2){
                            updateDirection(Direction.NORTHEAST);
                        }
                    }break;
            }
        }
        if(priorityMoveNew2()) return true;
        for(int i = 0; i < directions.length; i++){
            int[] offsets = getDirectionOffsets(directions[i]);
            int xVal = offsets[0]+myLocation.x, yVal = offsets[1]+myLocation.y;
            if(xVal >= 0 && xVal < rc.getMapWidth() && yVal >= 0 && yVal < rc.getMapHeight() &&
                    !prevLocs.contains(myLocation) && rc.canMove(directions[i])){
                updateDirection(directions[i]);
                soldierMove(getExploreTargetFromInitDirection());
                prevLocs.add(myLocation);
            }
        }
        return false;
    }
}

