package agrobot.robots;

import battlecode.common.*;
import agrobot.util.Constants;

public abstract class Robot {
    protected RobotController rc;
    protected Team myTeam;
    protected MapLocation myLocation;

    protected Direction initDirection;
    protected Direction[] directions;
    //OLD Movement Method Fields
    protected int[][] internalMap;
    // 0 = new space, 1 = traveled space

    public Robot(RobotController rc){
        this.rc = rc;
        myTeam = rc.getTeam();
        myLocation = rc.getLocation();
        internalMap = new int[rc.getMapWidth()][rc.getMapHeight()];
        internalMap[myLocation.x][myLocation.y] = 1;
    }

    public abstract void init() throws GameActionException;
    public abstract void run() throws GameActionException;
    
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

    public void move(MapLocation target) throws GameActionException {
        Direction dir = selectDirection(target.x-myLocation.x,target.y-myLocation.y);
        switch(dir){
            case EAST:

            case WEST:

            case NORTH:

            case SOUTH:

            case NORTHEAST:

            case NORTHWEST:

            case SOUTHEAST:

            case SOUTHWEST:

        }
    }

    //May not be needed
    public MapLocation closestPointinVision(MapLocation target){
        int vX = target.x-myLocation.x, vY = target.y-myLocation.y;
        double magV = Math.sqrt(vX*vX + vY*vY);
        int radius = rc.getType().visionRadiusSquared;
        return new MapLocation((int)(myLocation.x+vX/magV*radius),(int)(myLocation.x+vX/magV*radius));
    }

    //TODO: Old Movement Methods, Update to use basic path finding
    public void intermediateMove(MapLocation target) throws GameActionException {
        int x = target.x-myLocation.x, y = target.y-myLocation.y;
        if (x == y) {
            if(x == 0) return;
            tryMoveMultiple(selectDirection(x, y));
        } else if (y == 0) {
            double pass1 = 101, pass2 = 101, pass3 = rc.senseRubble(rc.adjacentLocation(selectDirection(x, 0)));
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
            double minRubble = 0;
            MapLocation move = null;
            MapLocation d1 = rc.adjacentLocation(selectDirection(0, y));
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
        int rub1 = rc.senseRubble(loc1), rub2 = 101, rub3 = 101;
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
        for(Direction d : directions){
            if(rc.canMove(d)){
                rc.move(d);
                myLocation = rc.getLocation();
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
    public MapLocation decode() throws GameActionException{
        int loc = rc.readSharedArray(55);
        int x = (loc/64)%64;
        int y = loc%64;
        return new MapLocation(x, y);
    }
    public void broadcast() throws GameActionException{
        RobotInfo [] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int num_enemies =enemies.length;
        int k=0;
        boolean seesArchon = false;
        for (RobotInfo r:enemies){
            if (r.getType()== RobotType.ARCHON){
                seesArchon=true;
                k=4096+64*r.getLocation().x+r.getLocation().y;
            }
        }
        if (num_enemies>5 && !seesArchon){
            MapLocation m = rc.getLocation();
            int x = m.x, y=m.y;
            k=x*64+y;
            rc.writeSharedArray(55,k);
        }
        if (seesArchon){
            rc.writeSharedArray(55, k);
        }

    }
}
