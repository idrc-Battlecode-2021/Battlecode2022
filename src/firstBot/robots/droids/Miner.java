package firstBot.robots.droids;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
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
        exploreTarget = new MapLocation((int)(rc.getMapWidth()*Math.random()),(int)(rc.getMapHeight()*Math.random()));
    }

    @Override
    public void run() throws GameActionException {
        //TODO: Optimize branching
        if(target != null){
            if(targetType == 1){
                if(!gold.isEmpty()){
                    target = getMax(gold);
                    targetType = 2;
                    run();
                    return;
                }
                else{
                    if(rc.canSenseLocation(target) && rc.senseLead(target) == 0){
                        lead.remove(target);
                        if(lead.isEmpty()) target = null;
                        else target = getMax(lead);
                        if(target == null)targetType = 2;
                        else targetType = 1;
                        run();
                        return;

                    }else if(rc.canMineLead(target))rc.mineLead(target);
                    else move(target);
                }
            }else {
                if(rc.canSenseLocation(target) && rc.senseGold(target) == 0){
                    gold.remove(target);
                    if(!gold.isEmpty()){
                        target = getMax(gold);
                        targetType = 2;
                    }
                    if(target == null){
                        if(lead.isEmpty()) target = null;
                        else target = getMax(lead);
                        if(target == null) targetType = 2;
                        else targetType = 1;
                    }
                    run();
                    return;
                } else if (rc.canMineGold(target)) rc.mineGold(target);
                else move(target);
            }
        } else if(target == null){
            if(gold.isEmpty()){
                if(lead.isEmpty()){
                    if(targetType != 0){
                        exploreTarget = new MapLocation((int)(rc.getMapWidth()*Math.random()),(int)(rc.getMapHeight()*Math.random()));
                        targetType = 0;
                    }
                    explore();
                }else{
                    target = getMax(lead);
                    if(target == null) targetType = 1;
                    else targetType = 2;
                    run();
                    return;
                }
            }else{
                target = getMax(gold);
                targetType = 2;
                run();
                return;
            }
        }
        viewResources();
    }

    public void viewResources() throws GameActionException {
        //TODO: Method currently doesn't consider if a previously checked location still has resources
        //TODO: Could also check rubble amount
        MapLocation current = rc.getLocation();
        for(int[] offsets : Constants.VIEWABLE_TILES_20){
            MapLocation loc = new MapLocation(current.x+offsets[0], current.y+offsets[1]);
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
    }

    public MapLocation getMax(HashMap<MapLocation,Integer> map) throws GameActionException { //location with Max amount of resources
        //Currently choosing amount over location, may want to change.
        MapLocation loc = null;
        while(loc == null && !map.isEmpty()){
            loc = map.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
            if(rc.canSenseLocation(loc)){
                if(rc.senseGold(loc) == 0 && rc.senseLead(loc) == 0){
                    map.remove(loc);
                    loc = null;
                }
            }
        }
        return loc;
    }

    public void explore(){
        move(exploreTarget);
    }


}
