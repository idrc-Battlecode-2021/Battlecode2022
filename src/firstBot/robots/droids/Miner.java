package firstBot.robots.droids;

import battlecode.common.*;
import firstBot.util.Constants;

import java.util.HashMap;

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
        target = null;
        //exploreTarget = new MapLocation((int)(rc.getMapWidth()*Math.random()),(int)(rc.getMapHeight()*Math.random()));
        //exploreDirIndex = (int)(8*Math.random());
        tryMoveMultipleNew();
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
            if(targetType == 1){
                if(!gold.isEmpty()){ //prioritize gold over Lead
                    MapLocation temp = getMax(gold);
                    if(temp != null){
                        targetType = 2;
                        target = temp;
                    }
                }
                if(targetType == 1 && rc.canSenseLocation(target) && rc.senseLead(target) < 2){
                    lead.remove(target); //checks if there is any lead left
                    if(lead.isEmpty()) target = null;
                    else target = getMax(lead);
                    if(target == null)targetType = 2;
                    else targetType = 1;
                }
                if(target != null){ //actions
                    intermediateMove(target);
                    if(rc.canMineLead(target)){
                        rc.mineLead(target);
                    }
                }
            }
            if(target != null && targetType == 2){
                if(rc.canSenseLocation(target) && rc.senseGold(target) == 0){ //checks if there is any gold left
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
                if(target != null && targetType == 2){ //actions
                    intermediateMove(target);
                    if (rc.canMineGold(target)) rc.mineGold(target);
                }
            }
        }
        if(rc.getMovementCooldownTurns() == 0 && target == null){
            tryMoveMultipleNew();
        }
        if(!prev.equals(myLocation)) viewResources(false);
    }

    public void viewResources(boolean start) throws GameActionException {
        //TODO: Method currently doesn't consider if a previously checked location still has resources
        //TODO: Could also check rubble amount
        //Maybe change so that the closest location above the threshold is chosen as the target rather than
        MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold(20),
            nearbyLead = rc.senseNearbyLocationsWithLead(20);
        for(int i = nearbyGold.length; --i>=0;){
            gold.put(nearbyGold[i],rc.senseGold(nearbyGold[i]));
        }
        for(int i = nearbyLead.length; --i>=0;){
            if(rc.canSenseRobotAtLocation(nearbyLead[i])){
                RobotInfo robot = rc.senseRobotAtLocation(nearbyLead[i]);
                if(robot.getTeam() != myTeam || robot.getType == myType){
                    continue;
                }
            }
            int amount = rc.senseLead(nearbyLead[i]);
            if(amount > 5){
                lead.put(nearbyLead[i],amount);
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
                    movementTileDistance(entry1.getKey(),myLocation) <
                    movementTileDistance(entry2.getKey(),myLocation) && entry1.getValue() > 1 ? 1 : -1).get().getKey();
            if(rc.canSenseLocation(loc)){
                if(rc.senseGold(loc) == 0 && rc.senseLead(loc) < 2){
                    map.remove(loc);
                    loc = null;
                }
            }
        }
        return loc;
    }

}
