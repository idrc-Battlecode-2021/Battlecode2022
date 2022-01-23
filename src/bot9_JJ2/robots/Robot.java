package bot9_JJ2.robots;

import battlecode.common.*;
import bot9_JJ2.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Robot {
    protected RobotController rc;
    protected Team myTeam;
    protected RobotType myType;
    protected MapLocation myLocation;
    protected int myArchonID;
    protected int myArchonOrder;
    protected int mapWidth,mapHeight;
    protected int initialArchons;
    protected boolean archonWait = false;
    protected ArrayList <MapLocation> enemyArchons = new ArrayList<MapLocation>();
    protected ArrayList <MapLocation> myArchons = new ArrayList<MapLocation>();
    protected Direction initDirection;
    protected Direction[] directions;
    //OLD Movement Method Fields
    protected int[][] internalMap;
    protected HashSet<MapLocation> prevLocs = new HashSet<>();
    protected ArrayList<MapLocation> myPath = new ArrayList<>();
    // -1 = unknown, otherwise amount of rubble

    public Robot( RobotController rc){
        this.rc = rc;
        myTeam = rc.getTeam();
        myLocation = rc.getLocation();
        myType = rc.getType();
        
        mapWidth = rc.getMapWidth(); mapHeight = rc.getMapHeight();
        initialArchons = rc.getArchonCount();
        updateDirection(myLocation.directionTo(new MapLocation(mapWidth/2,mapHeight/2)));
        //Too Much Bytecode, 5000
        /*for(int i = mapWidth; --i>=0;){
            for(int j = mapHeight; --j>=0;){
                internalMap[i][j] = -1;
            }
        }*/
        //updateInternalMap();
    }
    
    public abstract void init() throws GameActionException;
    public abstract void run() throws GameActionException;

    public void reassignArchon() throws GameActionException{ //reassigns archon order if an archon has died
        if (initialArchons==rc.getArchonCount()){
            return;
        }
        if (!archonWait){ //wait 1 turn to let archons refresh array
            archonWait = true;
            return;
        }
        initialArchons = rc.getArchonCount();
        archonWait = false;
        boolean archonFound = false;
        for(int i=63; i>59; i--){
            if (rc.readSharedArray(i)==myArchonID+1){
                archonFound = true;
                myArchonOrder=63-i;
            }
        }
        if (!archonFound){
            myArchonOrder = (int)(Math.random()*rc.getArchonCount());
        }
        
    }

    public void detectArchon() throws GameActionException{
        // detect and assign archon ID at spawn
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for (int i = robots.length; --i>=0;){
            if (robots[i].getType() == RobotType.ARCHON){
                myArchonID = robots[i].getID();
                break;
            }
        }
        for(int i=63; i>59; i--){
            if (rc.readSharedArray(i)==myArchonID+1){
                myArchonOrder=63-i;
                break;
            }
        }
    }
    public static int movementTileDistance(MapLocation a, MapLocation b){
        return Math.max(Math.abs(a.x-b.x),Math.abs(a.y-b.y));
    }
    public static int movementXYDistance(MapLocation a, MapLocation b){return Math.abs(a.x-b.x)+Math.abs(a.y-b.y);}

    
    public Direction getDirection(int x, int y){
        if(x <= -1){
            if(y <= -1){
                return Direction.SOUTHWEST;
            }else if(y == 0){
                return Direction.WEST;
            }else{//y >= 1
                return Direction.NORTHWEST;
            }
        }else if(x == 0){
            if(y <= -1){
                return Direction.SOUTH;
            }else if(y == 0){
                return Direction.CENTER;
            }else{//y >= 1
                return Direction.NORTH;
            }
        }else{ //x >= 1
            if(y <= -1){
                return Direction.SOUTHEAST;
            }else if(y == 0){
                return Direction.EAST;
            }else{//y >= 1
                return Direction.NORTHEAST;
            }
        }
    }

    public int getDirectionNumber(int x, int y){
        //return the index associated with the direction in the constant direction array
        if(x <= -1){
            if(y <= -1){
                return 5;
            }else if(y == 0){
                return 6;
            }else{//y >= 1
                return 7;
            }
        }else if(x == 0){
            if(y <= -1){
                return 4;
            }else if(y == 0){
                return 8;
            }else{//y >= 1
                return 0;
            }
        }else{ //x >= 1
            if(y <= -1){
                return 3;
            }else if(y == 0){
                return 2;
            }else{//y >= 1
                return 1;
            }
        }
    }


    public static Direction selectDirection(int x, int y){
        if(x == 0){
            if(y > 0){
                return Direction.NORTH;
            }else if(y < 0){
                return Direction.SOUTH;
            }
        }else if(x > 0){
            if(y > 0){
                return Direction.NORTHEAST;
            }else if(y < 0){
                return Direction.SOUTHEAST;
            }else{
                return Direction.EAST;
            }
        }else {
            if(y > 0){
                return Direction.NORTHWEST;
            }else if(y < 0){
                return Direction.SOUTHWEST;
            }else{
                return Direction.WEST;
            }
        }
        return Direction.CENTER;
    }

    //May not be needed
    public MapLocation closestPointinVision(MapLocation target){
        int vX = target.x-myLocation.x, vY = target.y-myLocation.y;
        double magV = Math.sqrt(vX*vX + vY*vY);
        int radius = rc.getType().visionRadiusSquared;
        return new MapLocation((int)(myLocation.x+vX/magV*radius),(int)(myLocation.x+vX/magV*radius));
    }

    //TODO: Old Movement Methods, Update to use basic path finding
    //Greedy pathfinding against adjacent tiles
    public void intermediateMove(MapLocation target) throws GameActionException {
        int x = target.x-myLocation.x, y = target.y-myLocation.y;
        Direction primaryDir = selectDirection(x,y);
        if (x == y) {
            if(x == 0) return;
            tryMoveMultiple(primaryDir);
        } else if (y == 0) {
            if(Math.abs(x) == 1){
                if(rc.canMove(primaryDir)){
                    rc.move(primaryDir);
                    myLocation = rc.getLocation();
                }
                return;
            }
            double pass1 = 101, pass2 = 101;
			double pass3 = rc.senseRubble(rc.adjacentLocation(primaryDir));
            Direction dir1 = selectDirection(x, 1), dir2 = selectDirection(x, -1);
            if (rc.onTheMap(rc.adjacentLocation(dir1))) {
                pass1 = rc.senseRubble(rc.adjacentLocation(dir1));
            } else if (rc.onTheMap(rc.adjacentLocation(dir2))) {
                pass2 = rc.senseRubble(rc.adjacentLocation(dir2));
            }
            if (pass3 < pass2 && pass3 < pass1) {
                tryMoveMultiple(selectDirection(x, 0));
            } else if (pass2 < pass1) {
                tryMoveMultiple(dir2);
            } else {
                tryMoveMultiple(dir1);
            }
        } else if (x == 0) {
            if(Math.abs(y) == 1){
                if(rc.canMove(primaryDir)){
                    rc.move(primaryDir);
                    myLocation = rc.getLocation();
                }
                return;
            }
            double minRubble = 0;
            MapLocation move = null;
            MapLocation d1 = rc.adjacentLocation(primaryDir);
            MapLocation d2 = d1.subtract(Direction.EAST);
            MapLocation d3 = d1.subtract(Direction.WEST);
            if (rc.onTheMap(d1) && rc.senseRubble(d1) < minRubble) {
                minRubble = rc.senseRubble(d1);
                move = d1;
            }
            if (rc.onTheMap(d2) && rc.senseRubble(d2) < minRubble) {
                minRubble = rc.senseRubble(d2);
                move = d2;
            }
            if (rc.onTheMap(d3) && rc.senseRubble(d3) < minRubble) {
                move = d3;
            }
            tryMoveMultiple(rc.getLocation().directionTo(move));
        } else if (Math.abs(x) > Math.abs(y)) {
            int rubble1 = 101, rubble2 = 101;
            if(rc.onTheMap(rc.adjacentLocation(selectDirection(x, 0)))){
                rubble1 = rc.senseRubble(rc.adjacentLocation(selectDirection(x, 0)));
            }
            if(rc.onTheMap(rc.adjacentLocation(selectDirection(x, y)))){
                rubble2 = rc.senseRubble(rc.adjacentLocation(selectDirection(x, y)));
            }
            if (rubble1 < rubble2) {
                tryMoveMultiple(selectDirection(x, 0));
            } else {
                tryMoveMultiple(selectDirection(x, y));
            }
        } else {
            int rubble1 = 101, rubble2 = 101;
            if(rc.onTheMap(rc.adjacentLocation(selectDirection(0, y)))){
                rubble1 = rc.senseRubble(rc.adjacentLocation(selectDirection(0, y)));
            }
            if(rc.onTheMap(rc.adjacentLocation(selectDirection(x, y)))){
                rubble2 = rc.senseRubble(rc.adjacentLocation(selectDirection(x, y)));
            }
            if (rubble1 < rubble2) {
                tryMoveMultiple(selectDirection(0, y));
            } else {
                tryMoveMultiple(selectDirection(x, y));
            }
        }
    }
    public void priorityMove(int dirIndex) throws GameActionException {
        Direction[] directions = Constants.DIRECTIONS;
        MapLocation loc1 = rc.adjacentLocation(directions[dirIndex]);
        while(!rc.onTheMap(loc1)){
            dirIndex = (dirIndex+4)%8;
            loc1 = rc.adjacentLocation(directions[dirIndex]);
        }
        int rub1 = rc.senseRubble(loc1);
		int rub2 = 101, rub3 = 101;
        MapLocation loc2 = rc.adjacentLocation(directions[(dirIndex+1)%8]),
                loc3 = rc.adjacentLocation(directions[(dirIndex+7)%8]);
        if(rc.onTheMap(loc2)){
            rub2 = rc.senseRubble(loc2);
        }
        if(rc.onTheMap(loc3)){
            rub3 = rc.senseRubble(loc3);
        }
        if(rub1 <= rub2 && rub1 <= rub3){
            tryMoveMultiple(directions[dirIndex]);
        }else if(rub2 <= rub3){
            tryMoveMultiple(directions[(dirIndex+1)%8]);
        }else{
            tryMoveMultiple(directions[(dirIndex+7)%8]);
        }

    }

    public void updateDirection(Direction d){
        initDirection = d;
        directions =  new Direction[]{
                initDirection,
                initDirection.rotateLeft(),
                initDirection.rotateRight(),
                initDirection.rotateLeft().rotateLeft(),
                initDirection.rotateRight().rotateRight(),
                initDirection.rotateLeft().rotateLeft().rotateLeft(),
                initDirection.rotateRight().rotateRight().rotateRight(),
                initDirection.opposite().rotateRight(),
                initDirection.opposite().rotateLeft()

        };
    }
    public boolean tryMoveMultiple(Direction dir) throws GameActionException{ //tries to move in direction, followed by adjacent directions
        if(initDirection == null && (dir == null || dir.equals(Direction.CENTER))){
            updateDirection(Constants.DIRECTIONS[(int) (Math.random()*8)]);
        }else if(dir != null && !dir.equals(Direction.CENTER)){
            updateDirection(dir);
        }
        for(int i = 0; i < directions.length; i++){
            if(rc.canMove(directions[i])){
                rc.move(directions[i]);
                myLocation = rc.getLocation();
                updateInternalMap();
                return true;
            }
        }
        return false;
    }
    public boolean hasMapLocation() throws GameActionException{
        if (rc.readSharedArray(55)==0){
            return false;
        }
        return true;
    }
    public boolean hasMapLocation(int i) throws GameActionException{
        if (rc.readSharedArray(i)==0){
            return false;
        }
        return true;
    }
    public MapLocation decode() throws GameActionException{
        int loc = rc.readSharedArray(55);
        int x = (loc/64)%64;
        int y = loc%64;
        return new MapLocation(x, y);
    }
    public MapLocation decode(int i) throws GameActionException{
        int loc = rc.readSharedArray(i);
        int x = (loc/64)%64;
        int y = loc%64;
        return new MapLocation(x, y);
    }
    public void broadcast() throws GameActionException{
        // broadcasts location of multiple enemies and enemy archon
        RobotInfo [] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int num_enemies = enemies.length;
        if (enemies.length>0){
            int bytecode = Clock.getBytecodeNum();
            int locs1 = rc.readSharedArray(12);
            int locs2 = rc.readSharedArray(13);
            MapLocation one = new MapLocation(locs1%16*4, locs1%256/16*4);
            MapLocation two = new MapLocation(locs1%4096/256*4, locs1/4096*4);
            MapLocation three = new MapLocation(locs2%16*4, locs2%256/16*4);
            MapLocation four = new MapLocation(locs2%4096/256*4, locs2/4096*4);
            if (one.x==one.y && one.x==0){
                int x = rc.getLocation().x/4;
                int y = rc.getLocation().y/4;
                rc.writeSharedArray(12, rc.readSharedArray(12)+x+y*16);
            }
            else if (two.x==two.y && two.x==0){
                int x = rc.getLocation().x/4;
                int y = rc.getLocation().y/4;
                if (movementTileDistance(rc.getLocation(), one)>rc.readSharedArray(14)/2){
                    rc.writeSharedArray(12, rc.readSharedArray(12)+x*256+y*4096);
                }
            }
            else if (three.x==three.y && three.x==0){
                int x = rc.getLocation().x/4;
                int y = rc.getLocation().y/4;
                if (movementTileDistance(rc.getLocation(), one)>rc.readSharedArray(14)/2 && movementTileDistance(rc.getLocation(), two)>rc.readSharedArray(14)/2){
                    rc.writeSharedArray(13, rc.readSharedArray(13)+y*16);
                }
            }
            else if (four.x==four.y && four.x==0){
                if (movementTileDistance(rc.getLocation(), one)>rc.readSharedArray(14)/2 && movementTileDistance(rc.getLocation(), two)>rc.readSharedArray(14)/2 && movementTileDistance(rc.getLocation(), three)>rc.readSharedArray(14)/2){
                    int x = rc.getLocation().x/4;
                    int y = rc.getLocation().y/4;
                    rc.writeSharedArray(13, rc.readSharedArray(13)+x*256+y*4096);
                }
            }
            if (Clock.getBytecodeNum()-bytecode>1000){
                System.out.println(" bytecode of broadcast "+(Clock.getBytecodeNum()-bytecode));
            }

        }
        int k=0;
        boolean seesArchon = false, seesAttacker = false;
        loop: for (RobotInfo r:enemies){
            switch(r.getType()){
                case ARCHON:
                    seesArchon=true;
                    k=64*r.getLocation().x+r.getLocation().y;
                    break loop;
                case SOLDIER:
                case WATCHTOWER:
                case SAGE:
                    seesAttacker=true;
                    k=64*r.getLocation().x+r.getLocation().y;
                    break;
                case MINER:
                case BUILDER:
                    if(!seesAttacker){
                        k=64*r.getLocation().x+r.getLocation().y;
                    }
                    break;
            }
        }
        if (seesArchon){
            rc.writeSharedArray(43, k);
            if(rc.readSharedArray(35) == 0){
                rc.writeSharedArray(35,k);
            }
        }
        if (seesAttacker){
            rc.writeSharedArray(55,k);
        }else if(num_enemies>0){
            rc.writeSharedArray(41,k);
        }

    }
    public boolean attackArchon() throws GameActionException{
        if (hasMapLocation()){
            if (rc.readSharedArray(55)/4096>0){
                return true;
            }
        }
        return false;
    }

    public int turnPenalty(int rubble){ //check if works
        return (int) Math.ceil(Math.floor((1+rubble/(double)10)*myType.movementCooldown)/(double) 10);
    }

    private void tryAttack(MapLocation Loc) throws GameActionException {
        if (rc.canAttack(Loc)){
            rc.attack(Loc);
        }
    }

    public MapLocation selectPriorityTarget() throws GameActionException {
        //returns location of target
        //returns own location if none
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        RobotInfo[] myRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        RobotInfo archon=null, sage=null, lab=null, watchtower=null, soldier=null, miner=null, builder=null;
        int[] damages = {0,0,0,0,0}; //order corresponds with order of variables above
        ArrayList<MapLocation> targets = new ArrayList<MapLocation>();
        int bytecode = Clock.getBytecodeNum();
        MapLocation target = rc.getLocation();
        for (RobotInfo r : enemyRobots) {
            RobotType type = r.getType();
            switch (type){
                case ARCHON:
                    if (archon == null || archon.getHealth() > r.getHealth()) {
                        archon = r;
                        damages[0]=rc.getType().getDamage(rc.getLevel());
                        for (RobotInfo robot: myRobots){
                            if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                                int cooldown = 1+rc.senseRubble(robot.getLocation())/10;
                                damages[0]+=(robot.getType().getDamage(robot.getLevel()))/cooldown;
                            }
                        }
                    }
                    break;
                case SAGE:
                    if (sage == null || sage.getHealth() > r.getHealth()) {
                        sage = r;
                        damages[1]=rc.getType().getDamage(rc.getLevel());
                        for (RobotInfo robot: myRobots){
                            if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                                int cooldown = 1+rc.senseRubble(robot.getLocation())/10;
                                damages[1]+=robot.getType().getDamage(robot.getLevel())/cooldown;
                            }
                        }
                    }
                    break;
                case LABORATORY:
                    if (lab == null || lab.getHealth() > r.getHealth()) {
                        lab = r;
                        damages[2]=rc.getType().getDamage(rc.getLevel());
                        for (RobotInfo robot: myRobots){
                            if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                                int cooldown = 1+rc.senseRubble(robot.getLocation())/10;
                                damages[2]+=robot.getType().getDamage(robot.getLevel())/cooldown;
                            }
                        }
                    }
                    break;
                case WATCHTOWER:
                    if (watchtower == null || watchtower.getHealth() > r.getHealth()) {
                        watchtower = r;
                        damages[3]=rc.getType().getDamage(rc.getLevel());
                        for (RobotInfo robot: myRobots){
                            if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                                int cooldown = 1+rc.senseRubble(robot.getLocation())/10;
                                damages[3]+=robot.getType().getDamage(robot.getLevel())/cooldown;
                            }
                        }
                    }
                    break;
                case SOLDIER:
                    if (soldier == null || soldier.getHealth() > r.getHealth()) {
                        soldier = r;
                        damages[4]=rc.getType().getDamage(rc.getLevel());
                        for (RobotInfo robot: myRobots){
                            if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                                int cooldown = 1+rc.senseRubble(robot.getLocation())/10;
                                damages[4]+=robot.getType().getDamage(robot.getLevel())/cooldown;
                            }
                        }
                    }
                    break;
                case MINER:
                    if (miner == null || miner.getHealth() > r.getHealth()) {
                        miner = r;
                    }
                    break;
                case BUILDER:
                    if (builder == null || builder.getHealth() > r.getHealth()) {
                        builder = r;
                    }
                    break;
            }
        }
        int archonTurns = Integer.MAX_VALUE, sageTurns = Integer.MAX_VALUE, labTurns = Integer.MAX_VALUE,
                watchtowerTurns = Integer.MAX_VALUE, soldierTurns = Integer.MAX_VALUE;
        int[] turns = {archonTurns, sageTurns, labTurns, watchtowerTurns, soldierTurns};
        //MapLocation[] locations = {archon.getLocation(), sage.getLocation(), lab.getLocation(), watchtower.getLocation(),soldier.getLocation()};
        if (archon!=null){
            turns[0] = archon.getHealth()/damages[0];
        }
        if (sage!=null){
            turns[1] = sage.getHealth()/damages[1];
        }
        if (lab!=null){
            turns[2] = lab.getHealth()/damages[2];
        }
        if (watchtower!=null){
            turns[3] = watchtower.getHealth()/damages[3];
        }
        if (soldier!=null){
            turns[4] = soldier.getHealth()/damages[4];
        }
        if (archonTurns<=10){
            targets.add(archon.getLocation());
            target = archon.getLocation();
        }
        else if (labTurns<=5){
            targets.add(lab.getLocation());
            target = lab.getLocation();
        }
        else if (sageTurns<=5){
            targets.add(sage.getLocation());
            target = sage.getLocation();
        }
        else if (watchtowerTurns<=5){
            targets.add(watchtower.getLocation());
            target = watchtower.getLocation();
        }
        else if (soldierTurns<=5){
            targets.add(soldier.getLocation());
            target = soldier.getLocation();
        }
        else{
            int minIndex = 0;
            for (int i=1;i<5;i++){
                if (turns[i]<turns[minIndex]){
                    minIndex = i;
                }
            }
            if (turns[minIndex]<Integer.MAX_VALUE){
                if (turns[minIndex]>25){
                    if (miner!=null){
                        target = miner.getLocation();
                        moveToLowPassability(target);
                        tryAttack(target);
                        //rc.setIndicatorString("target: "+target);
                        return target;
                    }
                    else if (builder!=null){
                        target = builder.getLocation();
                        moveToLowPassability(target);
                        tryAttack(target);
                        //rc.setIndicatorString("target: "+target);
                        return target;
                    }
                }
                switch(minIndex){
                    case 0:
                        target = archon.getLocation();
                        break;
                    case 1:
                        target = sage.getLocation();
                        break;
                    case 2:
                        target = lab.getLocation();
                        break;
                    case 3:
                        target = watchtower.getLocation();
                        break;
                    case 4:
                        target = soldier.getLocation();
                        break;
                }
            }
            else{
                if (miner!=null){
                    target = miner.getLocation();
                }
                else if (builder!=null){
                    target = builder.getLocation();
                }
            }
        }
        //Maximum bytecode seems to be ~2000 on maptestsmall
        if (target==null){
            target=rc.getLocation();
        }
        if (target!=rc.getLocation()){
            moveToLowPassability(target);
            tryAttack(target);
        }
        //rc.setIndicatorString("target: "+target);
        return target;
        
    }

    public boolean moveToLowPassability(MapLocation target) throws GameActionException{
        //TODO: doesn't guarantee that the target will be attacked
        //moves to lowest passability nearby
        if (!rc.isMovementReady()){
            return false;
        }
        Direction lowest = Direction.CENTER;
        int lowest_rubble = rc.senseRubble(rc.getLocation());
        if(rc.canSenseRobotAtLocation(target)){
            RobotInfo robot = rc.senseRobotAtLocation(target);
            if(rubbleActionTurnDiff(myLocation) < rubbleActionTurnDiff(target) || robot.getType() != RobotType.SOLDIER){
                for (Direction d: Direction.allDirections()){
                    MapLocation adjacent=rc.adjacentLocation(d);
                    if(adjacent.distanceSquaredTo(target) > rc.getType().actionRadiusSquared)continue;
                    if(rc.onTheMap(adjacent) && rc.canMove(d)){
                        int rubbleAtLoc = rc.senseRubble(adjacent);
                        if(rubbleAtLoc <lowest_rubble){
                            lowest = d;
                            lowest_rubble = rubbleAtLoc;
                        }
                    }
                }
                if (lowest==Direction.CENTER){
                    return false;
                }
                if(rc.canMove(lowest)){
                    rc.move(lowest);
                    myLocation = rc.getLocation();
                }
                return true;
            }
        }
        Direction escape = Direction.CENTER;
        int escape_Rubble = 101;
        for (Direction d: Direction.allDirections()){
            MapLocation adjacent=rc.adjacentLocation(d);
            if (rc.onTheMap(adjacent) && rc.canMove(d)){
                int rubble = rc.senseRubble(adjacent);
                if (rubble<lowest_rubble){
                    lowest = d;
                    lowest_rubble = rubble;
                }
                if(rubble<escape_Rubble && target.distanceSquaredTo(adjacent) > target.distanceSquaredTo(myLocation)){
                    escape = d;
                    escape_Rubble = rubble;
                }
            }
        }
        if (lowest==Direction.CENTER){
            //Moves away from soldier if the soldier is on a better tile and it
            if(escape != Direction.CENTER && rc.canMove(escape) && rubbleActionTurnDiff(myLocation) > rubbleActionTurnDiff(target)){
                rc.move(escape);
                myLocation = rc.getLocation();
            }else{
                return false;
            }
        }
        if(rc.canMove(lowest)){
            rc.move(lowest);
            myLocation = rc.getLocation();
        }
        return true;
    }
    private int rubbleActionTurnDiff(MapLocation loc) throws GameActionException {
        int rubble = rc.senseRubble(loc);
        switch (rc.getType()){
            case SOLDIER:
                switch (rubble){
                    case 0: return 1;
                    case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: return 2;
                    case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18: case 19: case 20: return 3;
                    case 21: case 22: case 23: case 24: case 25: case 26: case 27: case 28: case 29: case 30: return 4;
                    case 31: case 32: case 33: case 34: case 35: case 36: case 37: case 38: case 39: case 40: return 5;
                    case 41: case 42: case 43: case 44: case 45: case 46: case 47: case 48: case 49: case 50: return 6;
                    case 51: case 52: case 53: case 54: case 55: case 56: case 57: case 58: case 59: case 60: return 7;
                    case 61: case 62: case 63: case 64: case 65: case 66: case 67: case 68: case 69: case 70: return 8;
                    case 71: case 72: case 73: case 74: case 75: case 76: case 77: case 78: case 79: case 80: return 9;
                    case 81: case 82: case 83: case 84: case 85: case 86: case 87: case 88: case 89: case 90: return 10;
                    default: return 11;
                }
        }
        return 100;
    }
    
    private void updateInternalMap(){}

    public MapLocation readSymmetry() throws GameActionException {
        int n = rc.readSharedArray(48);
        if (n == 0) {
            return null;
        }
        int y = n % 64;
        int x = (n / 64) % 64;
        if (n > 4096) {
            enemyArchons.remove(new MapLocation(x, y));
            return null;
        }
        return new MapLocation(x, y);
    }
    public void checkSymmetry() throws GameActionException {
        for (MapLocation m : enemyArchons) {
            if (rc.canSenseLocation(m)) {
                for (int i = 0; i < enemyArchons.size(); i++) {
                    if (rc.canSenseLocation(enemyArchons.get(i))) {
                        for (RobotInfo ro : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam.opponent())) {
                            if (ro.getType() == RobotType.ARCHON) {
                                rc.writeSharedArray(48, m.x * 64 + m.y);
                                return;
                            }
                        }
                        rc.writeSharedArray(48, 4096 + m.x * 64 + m.y);
                        return;
                    }
                }
            }
        }
    }
    public MapLocation[] getArchonLocs() throws GameActionException{
        int array1 = rc.readSharedArray(49);
        int array2 = rc.readSharedArray(50);
        int array3 = rc.readSharedArray(15);
        int array4 = rc.readSharedArray(16);
        int archons = rc.getArchonCount();
        MapLocation[] locs = {
                new MapLocation(array1%256,array1/256),
                new MapLocation(array2%256,array2/256),
                new MapLocation(array3%256,array3/256),
                new MapLocation(array4%256,array4/256),
            };
        MapLocation[] returnLocs = new MapLocation[4];
        MapLocation zeroPos = new MapLocation(0,0);
        for(int i = 0; i < locs.length; i++){ //needs to start at 0
            if(!locs[i].equals(zeroPos) || archons > i){
                returnLocs[i] = locs[i];
            }else{
                break;
            }
        }
        return returnLocs;
    }

    public void possibleArchonLocs() throws GameActionException{
        MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
        int centerX = center.x, centerY=center.y;
        ArrayList<MapLocation> enemyLocations = new ArrayList<MapLocation>();
        for (MapLocation m: myArchons){
            int x = m.x;
            int y = m.y;
            if (!enemyLocations.contains(new MapLocation(2*centerX-x, y))){
                enemyArchons.add(new MapLocation(2*centerX -x, y));
            }
            if (!enemyLocations.contains(new MapLocation(x, 2*centerY-y))){
                enemyArchons.add(new MapLocation( x, 2*centerY-y));
            }
            if (!enemyLocations.contains(new MapLocation(2*centerX-x, 2*centerY-y))){
                enemyArchons.add(new MapLocation(2*centerX -x, 2*centerY-y));
            }
        }
        Set<MapLocation> s = new LinkedHashSet<MapLocation>();
        s.addAll(enemyArchons);
        enemyArchons.clear();
        enemyArchons.addAll(s);
        Collections.sort(enemyArchons);
        for (MapLocation m: myArchons){
            enemyArchons.remove(m);
        }
    }
}
