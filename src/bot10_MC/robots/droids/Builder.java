package bot10_MC.robots.droids;

import battlecode.common.*;
import bot10_MC.util.Constants;

import java.util.Random;
public class Builder extends Droid{
    private int startingBit;
    private Random rand = new Random();
    private MapLocation archonLoc;
    private boolean isDefensive = true;
    private MapLocation finishPrototype = null;
    private MapLocation bestBuildSpot = null;
    private MapLocation bestLabSpot = null;
    private int builderCount = 0;
    private int globalLabCount = 0;
    private static int labThreshold = 180;
    private String indicatorString = "";
    private MapLocation healLocation = null;
    
    public Builder(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        detectArchon();
        archonLoc = rc.senseRobot(myArchonID).getLocation();
        startingBit = 2*myArchonOrder;
        //System.out.println("init: "+ Clock.getBytecodesLeft());
    }


    //moves toward low rubble location within target and repairs it
    public boolean repair(MapLocation target) throws GameActionException{
        MapLocation best_location = rc.getLocation();
        int lowest_rubble = rc.senseRubble(best_location);
        MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(target, RobotType.BUILDER.actionRadiusSquared);
        for (MapLocation loc:locations){
            if (!rc.canSenseLocation(loc) || rc.canSenseRobotAtLocation(loc))continue;
            int rubble = rc.senseRubble(loc);
            if (rubble<lowest_rubble){
                lowest_rubble = rubble;
                best_location = loc;
            }
        }
        if (rc.isMovementReady() && rc.getLocation()!=best_location){
            intermediateMove(best_location);
        }
        RobotInfo prototype = rc.senseRobotAtLocation(target);
        if (prototype.getHealth()==prototype.getType().health){
            return false;
        }
        else{
            if(rc.canRepair(target)){
                rc.repair(target);
            }
        }
        return true;
    }

    @Override
    public void run() throws GameActionException {
        indicatorString = "";
        reassignArchon();
        startingBit = 2*myArchonOrder;

        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(1, rc.readSharedArray(1)+1);
        }
        else if(rc.getRoundNum()%3 == 0){
            globalLabCount = rc.readSharedArray(4);
            builderCount = rc.readSharedArray(1);
        }
        //TODO: change lab threshold based on income
        labThreshold = (globalLabCount+1)*180;

        //retreat if detecting enemies
        //TODO: discuss priority over repair and also change to better kite function
        RobotInfo[] enemyBotsInVision = rc.senseNearbyRobots(RobotType.BUILDER.visionRadiusSquared, rc.getTeam().opponent());
        if (enemyBotsInVision.length>0){
            intermediateMove(archonLoc);
        }

        //first repair prototype
        indicatorString = "prototype";
        if (finishPrototype!=null && !rc.getLocation().isWithinDistanceSquared(finishPrototype, RobotType.BUILDER.actionRadiusSquared)){
            intermediateMove(finishPrototype);
        }
        if (finishPrototype!=null && rc.canSenseRobotAtLocation(finishPrototype)){
            if (repair(finishPrototype)){
                return;
            }
            else{
                finishPrototype = null;
            }
        }

        indicatorString = "heal";
        //heal buildings
        RobotInfo[] robots = rc.senseNearbyRobots(20,myTeam);
        int leastHealth = RobotType.ARCHON.health;
        //TODO: repair buildings that are missing health
        if (healLocation!=null){
            //TODO: heal as far as possible in case archon needs to spawn (still prioritize low rubble)
            if (repair(healLocation)){
                rc.setIndicatorString(indicatorString);
                return;
            }
            else{
                healLocation = null;
            }
        }
        for (int i = robots.length; --i>=0;){
            if (robots[i].getMode()==RobotMode.DROID || rc.getType().health-rc.getHealth()==0){
                continue;
            }
            if (robots[i].getHealth()<leastHealth){
                healLocation = robots[i].getLocation();
            }
        }
        if (healLocation!=null){
            //TODO: heal as far as possible in case archon needs to spawn (still prioritize low rubble)
            if (repair(healLocation)){
                rc.setIndicatorString(indicatorString);
                return;
            }
            else{
                healLocation = null;
            }
        }

        // prepare for building lab by going to the best lab spot
        indicatorString+="finding ";
        bestLabSpot = findBestLabSpot();
        if (bestLabSpot==null){
            //builder has wandered away and is coming back
            rc.setIndicatorString(indicatorString);
            return;
        }
        bestBuildSpot = goToLabSpot(bestLabSpot);
        if (bestBuildSpot==bestLabSpot){
            //going toward bestlabspot
            rc.setIndicatorString(indicatorString);
            return;
        }
        if (rc.getTeamLeadAmount(rc.getTeam())>labThreshold){
            build(1);
        }
        rc.setIndicatorString(indicatorString);

    }

    public MapLocation goToLabSpot(MapLocation target) throws GameActionException{
        MapLocation best_location = target;
        if (!rc.getLocation().isWithinDistanceSquared(target, 2)){
            indicatorString = "going to: "+target;
            intermediateMove(target);
        }
        else{
            best_location = rc.getLocation();
            if (rc.getLocation().equals(target)){
                best_location = null;
            }
            int lowest_rubble = rc.senseRubble(rc.getLocation());
            MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(target, 2);
            for (MapLocation loc:locations){
                if (!rc.canSenseLocation(loc) || rc.canSenseRobotAtLocation(loc) || loc.equals(target))continue;
                int rubble = rc.senseRubble(loc);
                if (best_location==null){
                    best_location = loc;
                    lowest_rubble = rubble;
                    continue;
                }
                if (rubble<lowest_rubble){
                    lowest_rubble = rubble;
                    best_location = loc;
                }
            }
            if (rc.isMovementReady() && best_location != null && !best_location.equals(target)){
                intermediateMove(best_location);
            }
            indicatorString = "best location: "+best_location;
        }
        return best_location;
    }

    public MapLocation findBestLabSpot() throws GameActionException{
        MapLocation target = rc.getLocation();
        int rubble = rc.senseRubble(target);
        int xCheck = Math.min(Math.abs(-target.x),Math.abs(mapWidth-1-target.x));
        int yCheck = Math.min(Math.abs(-target.y),Math.abs(mapHeight-1-target.y));
        //if builder isn't within archon radius come back
        if (!rc.getLocation().isWithinDistanceSquared(archonLoc, RobotType.ARCHON.visionRadiusSquared)){
            intermediateMove(archonLoc);
            indicatorString+="going to archon: "+archonLoc;
            return null;
        }
        for (MapLocation m: rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), RobotType.MINER.visionRadiusSquared)){
        //for (MapLocation m: rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), RobotType.MINER.visionRadiusSquared)){
            if (!(rc.canSenseLocation(m) && !rc.canSenseRobotAtLocation(m) && !m.isWithinDistanceSquared(archonLoc, 2) && m.isWithinDistanceSquared(archonLoc, RobotType.ARCHON.visionRadiusSquared))){ //TODO: can try another threshold
                continue;
            }
            int r=rc.senseRubble(m);
            if(r<rubble){
                rubble=r;
                target=m;
            }
            else if (r==rubble){
                int xTemp = Math.min(Math.abs(-m.x),Math.abs(mapWidth-1-m.x));
                int yTemp = Math.min(Math.abs(-m.y),Math.abs(mapHeight-1-m.y));
                if(xTemp+yTemp < xCheck+yCheck){
                    target = m;
                    xCheck = xTemp;
                    yCheck = yTemp;
                }
            }
        }
        indicatorString+="best lab: "+target;
        return target;
    }

    public boolean canHeal() throws GameActionException{
        if(rc.readSharedArray(59)==0){
            return false;
        }
        return true;
    }
    public int closestBuilders() throws GameActionException{
        int group = rc.readSharedArray(59);
        group = group/128;
        return group;
    }
    public MapLocation healLocation() throws GameActionException{
        int loc = rc.readSharedArray(59);
        int x = (loc/64)%64;
        int y = loc%64;
        return new MapLocation(x, y);
    }
    public int read() throws GameActionException{
        int build = rc.readSharedArray(58);
        switch (myArchonOrder) {
            case 0: build=build%4;
                break;
            case 1: build=(build/4)%4;
                break;
            case 2: build=(build/16)%4;
                break;
            case 3: build=(build/64)%4;
                break;
        }
        return build;
    }
    public int readTowers() throws GameActionException{
        int index = myArchonOrder+5;
        return rc.readSharedArray(index);
    }
    public void addTowers() throws GameActionException{
        //System.out.println("Subtracting 180 from "+rc.readSharedArray(11));
        try{
            //rc.writeSharedArray(11, rc.readSharedArray(11)-RobotType.WATCHTOWER.buildCostLead);
            int index = myArchonOrder + 5;
            rc.writeSharedArray(index, rc.readSharedArray(index)+1);

            int power = (int)Math.pow(2,startingBit);
            int buildCommand = rc.readSharedArray(58) - 2 * power;
            rc.writeSharedArray(58, buildCommand);
        }
        catch (GameActionException e) {
            //System.out.println(rc.readSharedArray(11)+" "+RobotType.WATCHTOWER.buildCostLead);
        }

    }
    public void addLabs() throws GameActionException{
        int labCount = rc.readSharedArray(4);
        rc.writeSharedArray(4, labCount + 1);
        //rc.writeSharedArray(58, rc.readSharedArray(58) - (int)Math.pow(2,startingBit));
    }
    public boolean build(int id) throws GameActionException{
        RobotType r = RobotType.WATCHTOWER;
        if (id ==0){
            return false;
        }
        else if (id == 1) {
            r = RobotType.LABORATORY;
        }
        //indicatorString+=" target: "+target.toString()+" mytarget: "+myTarget.toString();
        if (!rc.getLocation().equals(bestBuildSpot)){
            return false;
        }
        Direction dir = rc.getLocation().directionTo(bestLabSpot);
        if (rc.canBuildRobot(r, dir)){
            rc.buildRobot(r, dir);
            finishPrototype = rc.getLocation().add(dir);
            if (r == RobotType.WATCHTOWER) addTowers();
            else addLabs();
            return true;
        }
        return false;
    }

}