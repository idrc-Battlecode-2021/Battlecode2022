package firstBot.robots;

import battlecode.common.*;

public abstract class Robot {
    protected RobotController rc;
    protected Team myTeam;
    public Robot(RobotController rc){
        this.rc = rc;
        myTeam = rc.getTeam();
    }

    public abstract void init() throws GameActionException;
    public abstract void run() throws GameActionException;

    public void move(MapLocation target){
        
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
