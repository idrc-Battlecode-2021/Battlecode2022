package firstBot.robots;

import battlecode.common.*;
import firstBot.util.Constants;

public abstract class Robot {
    protected RobotController rc;
    protected Team myTeam;
    protected MapLocation myLocation;

    protected Direction initDirection;
    protected Direction[] directions;
    //OLD Movement Method Fields

    public Robot(RobotController rc){
        this.rc = rc;
        myTeam = rc.getTeam();
        myLocation = rc.getLocation();
    }

    public abstract void init() throws GameActionException;
    public abstract void run() throws GameActionException;

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


    //TODO: Old Movement Methods, Update to use BFS
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

    public void intermediateMove(MapLocation target) throws GameActionException {
        int x = target.x-myLocation.x, y = target.y-myLocation.y;
        if (x == y) {
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
}
