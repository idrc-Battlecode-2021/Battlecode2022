package firstBot.robots.droids;

import battlecode.common.*;
import firstBot.util.Constants;

import java.util.HashMap;
import java.util.HashSet;

public class Miner extends Droid{
    private HashMap<MapLocation,Integer> gold = new HashMap<>();
    private HashMap<MapLocation,Integer> lead = new HashMap<>();
    private HashSet<MapLocation> checkedLocations = new HashSet<>();
    private MapLocation target, exploreTarget;
    private int targetType = 0;
    //0 = exploreTarget, 1 = lead, 2 = null/gold

    public Miner(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        target = null;
        exploreTarget = new MapLocation((int)(rc.getMapWidth()*Math.random()),(int)(rc.getMapHeight()*Math.random()));
        explore();
        viewResources(true);
    }

    @Override
    public void run() throws GameActionException {
        // update shared array
        MapLocation prev = myLocation;
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(0, rc.readSharedArray(0)+1);
        }
        //TODO: Optimize branching
        if(target != null){
            System.out.println(target);
            if(targetType == 1){
                if(!gold.isEmpty()){
                    MapLocation temp = getMax(gold);
                    if(temp != null){
                        targetType = 2;
                        target = temp;
                    }
                }
                if(targetType == 1 && rc.canSenseLocation(target) && rc.senseLead(target) == 0){
                    lead.remove(target);
                    if(lead.isEmpty()) target = null;
                    else target = getMax(lead);
                    if(target == null)targetType = 2;
                    else targetType = 1;
                }
                if(target != null){
                    if(rc.canMineLead(target))rc.mineLead(target);
                    else intermediateMove(target);
                }
            }
            if(target != null && targetType == 2){
                if(rc.canSenseLocation(target) && rc.senseGold(target) == 0){
                    gold.remove(target);
                    if(gold.isEmpty()) target = null;
                    else{
                        target = getMax(gold);
                        targetType = 2;
                    }
                    if(target == null) {
                        if (lead.isEmpty()) target = null;
                        else target = getMax(lead);
                        if (target == null) targetType = 2;
                        else {
                            targetType = 1;
                            run();
                            return;
                        }
                    }
                }
                if(target != null && targetType == 2){
                    if (rc.canMineGold(target)) rc.mineGold(target);
                    else intermediateMove(target);
                }
            }
        }
        if(rc.getMovementCooldownTurns() == 0 && target == null){
            explore();
        }
        if(!prev.equals(myLocation)) viewResources(false);
    }

    public void viewResources(boolean start) throws GameActionException {
        //TODO: Method currently doesn't consider if a previously checked location still has resources
        //TODO: Could also check rubble amount
        MapLocation current = rc.getLocation();
        int[][] vision = null;
        if(start){
            vision = Constants.VIEWABLE_TILES_20;
        }else{
            vision = Constants.OUTTER_TILIES_20;
        }
        for(int i=vision.length; --i>= 0;){
            MapLocation loc = new MapLocation(current.x+vision[i][0], current.y+vision[i][1]);
            if(!checkedLocations.contains(loc)){
                checkedLocations.add(loc);
                if(rc.onTheMap(loc)){
                    int amount = rc.senseGold(loc);
                    if(amount > 0) gold.put(loc,amount);
                    amount = rc.senseLead(loc);
                    if(amount > 0) lead.put(loc,amount);
                }
            }
        }
        if(!gold.isEmpty()){
            target = getMax(gold);
            targetType = 2;
        }else if(!lead.isEmpty()){
            target = getMax(lead);
            if(target == null) targetType = 2;
            else targetType = 1;
        }
    }

    public MapLocation getMax(HashMap<MapLocation,Integer> map) throws GameActionException { //location with Max amount of resources
        //Currently choosing closest Location over Amount, may want to change.
        MapLocation loc = null;
        while(loc == null && !map.isEmpty()){
            loc = map.entrySet().stream().max((entry1, entry2) ->
                    (Math.abs(entry1.getKey().x-myLocation.x)+Math.abs(entry1.getKey().y-myLocation.y)) <
                    ((Math.abs(entry2.getKey().x-myLocation.x)+Math.abs(entry2.getKey().y-myLocation.y))) ? 1 : -1).get().getKey();
            if(rc.canSenseLocation(loc)){
                if(rc.senseGold(loc) == 0 && rc.senseLead(loc) == 0){
                    map.remove(loc);
                    loc = null;
                }
            }
        }
        return loc;
    }

    public void explore() throws GameActionException {
        //TODO: Make ExploreTargets not Repeat or be close to previous locations
        if(exploreTarget == null || targetType != 0 || myLocation.equals(exploreTarget)){
            exploreTarget = new MapLocation((int)(rc.getMapWidth()*Math.random()),(int)(rc.getMapHeight()*Math.random()));
            targetType = 0;
        }
        intermediateMove(exploreTarget);
    }


}
