package agrobot.robots.droids;

import battlecode.common.*;
import agrobot.robots.Robot;
import agrobot.util.Constants;

public abstract class Droid extends Robot {
    MapLocation exploreTarget;
    public Droid(RobotController rc) {
        super(rc);
        exploreTarget = myLocation;
    }

    public boolean priorityMoveNew() throws GameActionException {
        if(initDirection == null){
            updateDirection(Constants.COMPOSITE_DIRECTIONS[(int) (Math.random()*4)]);
        }
        if(myLocation.x == 0 || myLocation.y == 0 || myLocation.x == rc.getMapWidth()-1 || myLocation.y == rc.getMapHeight()){
            updateDirection(initDirection.rotateLeft().rotateLeft());
        }
        Direction dir = null; int rubble = 101;
        int[] offsets = new int[2];
        if(rc.canMove(initDirection)){
            offsets = getDirectionOffsets(initDirection);
            int xVal = offsets[0]+myLocation.x, yVal = offsets[1]+myLocation.y;
            if(xVal >= 0 && xVal < rc.getMapWidth() && yVal >= 0 && yVal < rc.getMapHeight() &&
                    internalMap[xVal][yVal] == 0){
                dir = initDirection;
                rubble = rc.senseRubble(rc.adjacentLocation(initDirection));
            }

        }
        int rubble2;
        if(rc.canMove(initDirection.rotateLeft())){
            offsets = getDirectionOffsets(initDirection);
            int xVal = offsets[0]+myLocation.x, yVal = offsets[1]+myLocation.y;
            if(xVal >= 0 && xVal < rc.getMapWidth() && yVal >= 0 && yVal < rc.getMapHeight() &&
                    internalMap[xVal][yVal] == 0){
                rubble2 = rc.senseRubble(rc.adjacentLocation(initDirection.rotateLeft()));
                if(rubble2 < rubble){
                    dir = initDirection.rotateLeft();
                    rubble = rubble2;
                }
            }
        }
        if(rc.canMove(initDirection.rotateRight())){
            offsets = getDirectionOffsets(initDirection);
            int xVal = offsets[0]+myLocation.x, yVal = offsets[1]+myLocation.y;
            if(xVal >= 0 && xVal < rc.getMapWidth() && yVal >= 0 && yVal < rc.getMapHeight() &&
                    internalMap[xVal][yVal] == 0){
                rubble2 = rc.senseRubble(rc.adjacentLocation(initDirection.rotateRight()));
                if(rubble2 < rubble){
                    dir = initDirection.rotateLeft();
                }
            }
        }
        Direction temp = initDirection;
        if(dir != null && tryMoveMultiple(dir)){
            updateDirection(temp);
            return true;
        }
        return false;
    }

    public boolean tryMoveMultipleNew() throws GameActionException {
        if(initDirection == null){
            updateDirection(Constants.COMPOSITE_DIRECTIONS[(int) (Math.random()*4)]);
        }
        if(priorityMoveNew()) return true;
        //Pair<Direction,Double> pair = new Pair<>(null,0.0);
        for(Direction d : directions){
            int[] offsets = getDirectionOffsets(d);
            int xVal = offsets[0]+myLocation.x, yVal = offsets[1]+myLocation.y;
            if(xVal >= 0 && xVal < rc.getMapWidth() && yVal >= 0 && yVal < rc.getMapHeight() &&
                    internalMap[xVal][yVal] == 0 && rc.canMove(d)){
                if(tryMoveMultiple(d)){
                    internalMap[myLocation.x][myLocation.y] = 1;
                    return true;
                }
            }
        }
        return false;
    }

    public int[] getDirectionOffsets(Direction dir){
        int[] offsets = new int[2];
        switch (dir){
            case EAST:      offsets[0] = 1;                 break;
            case WEST:      offsets[0] = -1;                break;
            case NORTH:                     offsets[1] = 1; break;
            case SOUTH:                     offsets[1] = -1;break;
            case NORTHEAST: offsets[0] = 1; offsets[1] = 1; break;
            case NORTHWEST: offsets[0] = -1;offsets[1] = 1; break;
            case SOUTHEAST: offsets[0] = 1; offsets[1] = -1;break;
            case SOUTHWEST: offsets[0] = -1;offsets[1] = -1;break;
        }

        return offsets;
    }

    //Calculate location to go based on locations of sensed friendly bots
    public void newLocation() throws GameActionException{
            int x = exploreTarget.x-myLocation.x, y = exploreTarget.y-myLocation.y;
            RobotInfo[] sensedBots = rc.senseNearbyRobots(20,myTeam);
            for(int i = sensedBots.length; --i>=0;){
                if(sensedBots[i].getType() == myType || sensedBots[i].getType() == RobotType.ARCHON) {
                    x -= sensedBots[i].location.x-myLocation.x;
                    y -= sensedBots[i].location.y-myLocation.y;
                }
            }
            MapLocation temp1 = new MapLocation(myLocation.x+4,myLocation.y);
            MapLocation temp2 = new MapLocation(myLocation.x-4,myLocation.y);
            if(!rc.onTheMap(temp1) && selectDirection(myLocation.x+4,myLocation.y) == selectDirection(myLocation.x+x,myLocation.y) ||
                !rc.onTheMap(temp2) && selectDirection(myLocation.x-4,myLocation.y) == selectDirection(myLocation.x+x,myLocation.y)){
                x = -x;
            }
            temp1 = new MapLocation(myLocation.x,myLocation.y+4);
            temp2 = new MapLocation(myLocation.x,myLocation.y-4);
            if(!rc.onTheMap(temp1) && selectDirection(myLocation.x,myLocation.y+4) == selectDirection(myLocation.x,myLocation.y+y) ||
                !rc.onTheMap(temp2) && selectDirection(myLocation.x,myLocation.y-4) == selectDirection(myLocation.x,myLocation.y-y)){
                y = -y;
            }
            exploreTarget = new MapLocation(x+myLocation.x,y+myLocation.y);
        }
    public void explore() throws GameActionException {
        newLocation();
        intermediateMove(exploreTarget);
    }

}
