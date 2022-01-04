package firstBot.robots;

import battlecode.common.*;

public abstract class Robot {
    protected RobotController rc;
    protected Team myTeam;
    protected MapLocation myLocation;
    public Robot(RobotController rc){
        this.rc = rc;
        myTeam = rc.getTeam();
        myLocation = rc.getLocation();
    }

    public abstract void init() throws GameActionException;
    public abstract void run() throws GameActionException;

    public void move(MapLocation target){
        
    }

    public Direction getDirection(int x, int y){
        if(x == -1){
            if(y == -1){
                return Direction.SOUTHWEST;
            }else if(y == 0){
                return Direction.WEST;
            }else{//y == 1
                return Direction.NORTHWEST;
            }
        }else if(x == 0){
            if(y == -1){
                return Direction.SOUTH;
            }else if(y == 0){
                return Direction.CENTER;
            }else{//y == 1
                return Direction.NORTH;
            }
        }else{ //x == 1
            if(y == -1){
                return Direction.SOUTHEAST;
            }else if(y == 0){
                return Direction.EAST;
            }else{//y == 1
                return Direction.NORTHEAST;
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
}
