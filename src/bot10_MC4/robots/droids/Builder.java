package bot10_MC4.robots.droids;

import battlecode.common.*;

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
        indicatorString+=" repair";
        int bytecode = Clock.getBytecodeNum();
        if (!rc.canSenseRobotAtLocation(target)){
            builderMove(target);
            return true;
        }
        RobotInfo building = rc.senseRobotAtLocation(target);
        if (building.getLevel()==1 && building.getHealth()>=building.getType().health || building.getLevel()==2 && building.getHealth()>=180){
            return false;
        }
        MapLocation best_location = rc.getLocation();
        int lowest_rubble = rc.senseRubble(best_location);

        MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(target, RobotType.BUILDER.actionRadiusSquared);

        if (building.getType()!=RobotType.ARCHON){
            Direction targetToArchon = target.directionTo(archonLoc);
            Direction mineToArchon = rc.getLocation().directionTo(archonLoc);
            if (!mineToArchon.equals(targetToArchon) && !mineToArchon.equals(targetToArchon.rotateLeft()) && !mineToArchon.equals(targetToArchon.rotateRight())){
                best_location = null;
            }
            MapLocation[] new_locations = {
                target.add(targetToArchon),
                target.add(targetToArchon.rotateLeft()),
                target.add(targetToArchon.rotateRight()),
                target.add(targetToArchon).add(targetToArchon),
                target.add(targetToArchon.rotateLeft()).add(targetToArchon.rotateLeft()),
                target.add(targetToArchon.rotateRight()).add(targetToArchon.rotateRight())
            };
            locations = new_locations;
        }
        
        for (MapLocation loc:locations){
            if (!rc.canSenseLocation(loc) || rc.canSenseRobotAtLocation(loc))continue;
            int rubble = rc.senseRubble(loc);
            if (best_location==null){
                best_location = loc;
                lowest_rubble = rubble;
                continue;
            }
            if (!best_location.isWithinDistanceSquared(target, RobotType.BUILDER.actionRadiusSquared) || rubble<lowest_rubble){
                lowest_rubble = rubble;
                best_location = loc;
            }
        }
        if (rc.isMovementReady() && !rc.getLocation().equals(best_location)){
            builderMove(best_location);
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
        labThreshold = Math.min(180,(globalLabCount+1)*180);

        //retreat if detecting enemies
        //TODO: discuss priority over repair and also change to better kite function
        if (checkEnemy()){
            if (!rc.getLocation().isWithinDistanceSquared(archonLoc, RobotType.BUILDER.actionRadiusSquared)){
                builderMove(archonLoc);
            }
            else{
                kite();
            }
        }

        //first repair prototype
        indicatorString = "prototype";
        if (finishPrototype!=null && !rc.getLocation().isWithinDistanceSquared(finishPrototype, RobotType.BUILDER.actionRadiusSquared)){
            builderMove(finishPrototype);
        }
        if (finishPrototype!=null && rc.canSenseRobotAtLocation(finishPrototype)){
            if (repair(finishPrototype)){
                return;
            }
            else{
                finishPrototype = null;
            }
        }
        //heal buildings
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
        RobotInfo[] robots = rc.senseNearbyRobots(20,rc.getTeam());
        int leastHealth = RobotType.ARCHON.health;
        for (int i = robots.length; --i>=0;){
            if (robots[i].getMode()==RobotMode.DROID){
                continue;
            }
            //TODO: hardcoded for labs, change for watchtowers
            if (robots[i].getLevel()==1 && robots[i].getType().health-robots[i].getHealth()==0 || robots[i].getLevel()==2 && 180-robots[i].getHealth()==0){
                continue;
            }
            if (robots[i].getHealth()<leastHealth){
                leastHealth = robots[i].getHealth();
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

        indicatorString = "mutate";
        if (rc.getTeamLeadAmount(rc.getTeam())>=150){
            if (mutate()){
                return;
            }
        }
        // prepare for building lab by going to the best lab spot
        indicatorString="finding ";
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
        if (rc.getTeamLeadAmount(rc.getTeam())>=labThreshold){
            buildLab();
        }
        rc.setIndicatorString(indicatorString);

    }

    public MapLocation goToLabSpot(MapLocation target) throws GameActionException{
        MapLocation best_location = target;
        if (!rc.getLocation().isWithinDistanceSquared(target, 2)){
            //indicatorString = "going to: "+target;
            builderMove(target);
        }
        else{
            Direction targetToArchon = target.directionTo(archonLoc);
            Direction mineToArchon = rc.getLocation().directionTo(archonLoc);
            best_location = rc.getLocation();
            if (rc.getLocation().equals(target)){
                best_location = null;
            }
            else if (!mineToArchon.equals(targetToArchon) && !mineToArchon.equals(targetToArchon.rotateLeft()) && !mineToArchon.equals(targetToArchon.rotateRight())){
                best_location = null;
            }
            int lowest_rubble = rc.senseRubble(rc.getLocation());
            MapLocation[] locations = {
                target.add(targetToArchon),
                target.add(targetToArchon.rotateLeft()),
                target.add(targetToArchon.rotateRight()),
            };
            //MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(target, 2);
            for (MapLocation loc:locations){
                if (!rc.canSenseLocation(loc) || rc.canSenseRobotAtLocation(loc) || loc.equals(target) || !loc.isWithinDistanceSquared(target, 2))continue;
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
                builderMove(best_location);
            }
            //indicatorString += "best location: "+best_location;
        }
        return best_location;
    }

    public MapLocation findBestLabSpot() throws GameActionException{
        MapLocation target = bestLabSpot==null?rc.getLocation():bestLabSpot;
        if (!rc.canSenseLocation(target)){
            return target;
        }
        int rubble = rc.senseRubble(target);
        int xCheck = Math.min(Math.abs(-target.x),Math.abs(mapWidth-1-target.x));
        int yCheck = Math.min(Math.abs(-target.y),Math.abs(mapHeight-1-target.y));
        boolean isFirstLab = globalLabCount==0;
        int minRubble = 99;
        Direction toArchon = target.directionTo(archonLoc);
        if (rc.canSenseLocation(target.add(toArchon))) minRubble = Math.min(minRubble, rc.senseRubble(target.add(toArchon)));
        if (rc.canSenseLocation(target.add(toArchon.rotateLeft()))) minRubble = Math.min(minRubble, rc.senseRubble(target.add(toArchon.rotateLeft())));
        if (rc.canSenseLocation(target.add(toArchon.rotateRight()))) minRubble = Math.min(minRubble, rc.senseRubble(target.add(toArchon.rotateRight())));
        //if builder isn't within archon radius come back
        if (bestLabSpot==null && !rc.getLocation().isWithinDistanceSquared(archonLoc, RobotType.ARCHON.visionRadiusSquared)){
            builderMove(archonLoc);
            //indicatorString+="going to archon: "+archonLoc;
            return null;
        }
        for (MapLocation m: rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), RobotType.MINER.visionRadiusSquared)){
            if (!(rc.canSenseLocation(m) && !rc.canSenseRobotAtLocation(m) && !m.isWithinDistanceSquared(archonLoc, 2) && m.isWithinDistanceSquared(archonLoc, RobotType.ARCHON.visionRadiusSquared))){ //TODO: can try another threshold
                continue;
            }
            int r=rc.senseRubble(m);
            if (r>rubble)continue;
            int tempMin = 99;
            int xTemp = Math.min(Math.abs(-m.x),Math.abs(mapWidth-1-m.x));
            int yTemp = Math.min(Math.abs(-m.y),Math.abs(mapHeight-1-m.y));
            if (isFirstLab){
                toArchon = m.directionTo(archonLoc);
                if (rc.canSenseLocation(m.add(toArchon))) tempMin = Math.min(tempMin, rc.senseRubble(m.add(toArchon)));
                if (rc.canSenseLocation(m.add(toArchon.rotateLeft()))) tempMin = Math.min(tempMin, rc.senseRubble(m.add(toArchon.rotateLeft())));
                if (rc.canSenseLocation(m.add(toArchon.rotateRight()))) tempMin = Math.min(tempMin, rc.senseRubble(m.add(toArchon.rotateRight())));
                if (tempMin>minRubble)continue;
            }
            if(r<rubble){
                rubble=r;
                target=m;
                xCheck = xTemp;
                yCheck = yTemp;
            }
            else {
                if (isFirstLab){
                    if (tempMin<minRubble){
                        rubble=r;
                        target=m;
                        xCheck = xTemp;
                        yCheck = yTemp;
                        minRubble = tempMin;
                    }
                    if(xTemp+yTemp < xCheck+yCheck){
                        rubble=r;
                        target=m;
                        xCheck = xTemp;
                        yCheck = yTemp;
                    }
                }
                else{
                    if(xTemp+yTemp < xCheck+yCheck){
                        rubble=r;
                        target=m;
                        xCheck = xTemp;
                        yCheck = yTemp;
                    }
                }
            }
        }
        //indicatorString+="best lab: "+target;
        return target;
    }

    public boolean builderMove(MapLocation target) throws GameActionException{
        if (!rc.isMovementReady()){
            return false;
        }
        MapLocation lowest = rc.getLocation();
        int lowest_rubble = 99;
        myLocation = rc.getLocation();
        for (Direction d: Direction.allDirections()){
            if (d==Direction.CENTER)continue;
            MapLocation adjacent=rc.adjacentLocation(d);
            if(rc.canMove(d)){
                int rubbleAtLoc = rc.senseRubble(adjacent);
                if (adjacent.distanceSquaredTo(target) >= myLocation.distanceSquaredTo(target))continue;
                if(rubbleAtLoc < lowest_rubble || rubbleAtLoc == lowest_rubble && adjacent.distanceSquaredTo(target) < lowest.distanceSquaredTo(target)){
                    lowest = adjacent;
                    lowest_rubble = rubbleAtLoc;
                }
            }
        }
        Direction direction = rc.getLocation().directionTo(lowest);
        indicatorString+=direction.toString();
        if(rc.canMove(direction)){
            rc.move(direction);
            myLocation = rc.getLocation();
            return true;
        }
        return false;
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

    public void kite() throws GameActionException{
        if (!rc.isMovementReady()){
            return;
        }
        int average_x=0,average_y=0;
        myLocation = rc.getLocation();
        int enemyCount = 0;
        RobotInfo[] enemyBotsInVision = rc.senseNearbyRobots(RobotType.SAGE.visionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo robot:enemyBotsInVision){
            if (robot.getType()!=RobotType.SOLDIER && robot.getType()!=RobotType.SAGE && robot.getType()!=RobotType.ARCHON){
                continue;
            }
            enemyCount++;
            MapLocation location = robot.getLocation();
            average_x+=location.x;
            average_y+=location.y;
        }
        if (enemyCount==0){
            builderMove(archonLoc);
            return;
        }
        average_x/=enemyCount;
        average_y/=enemyCount;
        MapLocation enemy = new MapLocation(average_x,average_y);
        Direction lowest = Direction.CENTER;
        //int lowest_rubble = rc.senseRubble(rc.getLocation());
        int lowest_rubble = 99;
        for (Direction d: Direction.allDirections()){
            MapLocation adjacent=rc.adjacentLocation(d);
            if(adjacent.distanceSquaredTo(enemy) < myLocation.distanceSquaredTo(enemy))continue;
            if(rc.canMove(d)){
                int rubbleAtLoc = rc.senseRubble(adjacent);
                if(rubbleAtLoc < lowest_rubble || rubbleAtLoc == lowest_rubble && adjacent.distanceSquaredTo(enemy) > myLocation.distanceSquaredTo(enemy)){
                    lowest = d;
                    lowest_rubble = rubbleAtLoc;
                }
            }
        }
        if(rc.canMove(lowest)){
            rc.move(lowest);
            myLocation = rc.getLocation();
        }
        
    }

    public void addLabs() throws GameActionException{
        int labCount = rc.readSharedArray(4);
        rc.writeSharedArray(4, labCount + 1);
        //rc.writeSharedArray(58, rc.readSharedArray(58) - (int)Math.pow(2,startingBit));
    }

    public boolean mutate() throws GameActionException{
        RobotInfo[] robots = rc.senseNearbyRobots(20,rc.getTeam());
        MapLocation target = null;
        for (RobotInfo robot:robots){
            if (robot.getType()!=RobotType.LABORATORY)continue;
            if (robot.getLevel()==1){
                target = robot.getLocation();
            }
        }
        if (target==null){
            return false;
        }
        indicatorString = target.toString();
        if (!rc.getLocation().isWithinDistanceSquared(target, 2)){
            indicatorString = "going to: "+target;
            builderMove(target);
        }
        else{
            MapLocation best_location;
            Direction targetToArchon = target.directionTo(archonLoc);
            Direction mineToArchon = rc.getLocation().directionTo(archonLoc);
            best_location = rc.getLocation();
            if (rc.getLocation().equals(target)){
                best_location = null;
            }
            else if (!mineToArchon.equals(targetToArchon) && !mineToArchon.equals(targetToArchon.rotateLeft()) && !mineToArchon.equals(targetToArchon.rotateRight())){
                best_location = null;
            }
            int lowest_rubble = rc.senseRubble(rc.getLocation());
            MapLocation[] locations = {
                target.add(targetToArchon),
                target.add(targetToArchon.rotateLeft()),
                target.add(targetToArchon.rotateRight()),
                target.add(targetToArchon).add(targetToArchon),
                target.add(targetToArchon.rotateLeft()).add(targetToArchon.rotateLeft()),
                target.add(targetToArchon.rotateRight()).add(targetToArchon.rotateRight())
            };
            //MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(target, 2);
            for (MapLocation loc:locations){
                if (!rc.canSenseLocation(loc) || rc.canSenseRobotAtLocation(loc) || loc.equals(target) || !loc.isWithinDistanceSquared(target, 2))continue;
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
                builderMove(best_location);
            }
            if (best_location.equals(rc.getLocation())){
                if (rc.canMutate(target)){
                    rc.mutate(target);
                }
            }
        }
        return true;
    }

    public boolean buildLab() throws GameActionException{
        RobotType r = RobotType.LABORATORY;
        //indicatorString+=" target: "+target.toString()+" mytarget: "+myTarget.toString();
        if (!rc.getLocation().equals(bestBuildSpot)){
            return false;
        }
        Direction dir = rc.getLocation().directionTo(bestLabSpot);
        if (rc.canBuildRobot(r, dir)){
            rc.buildRobot(r, dir);
            bestLabSpot = null;
            bestBuildSpot = null;
            finishPrototype = rc.getLocation().add(dir);
            if (r == RobotType.WATCHTOWER) addTowers();
            else addLabs();
            return true;
        }
        return false;
    }
    public boolean checkEnemy() throws GameActionException {
        //detect enemy muckraker
        RobotInfo[] robots=rc.senseNearbyRobots(20,myTeam.opponent());
        if(robots.length > 0){
            rc.writeSharedArray(42,1);
            for (RobotInfo robot : robots){
                switch(robot.getType()){
                    case SAGE:
                    case SOLDIER:
                    case WATCHTOWER:
                        return true;
                }
            }

        }
        return false;
    }

}