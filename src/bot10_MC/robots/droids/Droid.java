package bot10_MC.robots.droids;

import battlecode.common.*;
import bot10_MC.robots.Robot;
import bot10_MC.util.Constants;

import java.util.ArrayList;

public abstract class Droid extends Robot {
    MapLocation exploreTarget;
    private AnomalyScheduleEntry[] anomaly = rc.getAnomalySchedule();
    private ArrayList<AnomalyScheduleEntry> relevantAnomalies = new ArrayList<AnomalyScheduleEntry>();

    public Droid(RobotController rc) {
        super(rc);
        exploreTarget = myLocation;
    }

    private boolean priorityMoveNew2() throws GameActionException{
        Direction dir2 = initDirection.rotateLeft(), dir3 = initDirection.rotateRight(),
            dir4 = dir2.rotateLeft(), dir5 = dir3.rotateRight();
        int rubble1 = rc.senseRubble(rc.adjacentLocation(initDirection)), rubble2 = 101, rubble3 = 101, rubble4 = 101, rubble5 = 101;
        MapLocation loc2 = rc.adjacentLocation(dir2), loc3 = rc.adjacentLocation(dir3), loc4 = rc.adjacentLocation(dir4),
            loc5 = rc.adjacentLocation(dir5);
        if(rc.canSenseLocation(loc2) && !prevLocs.contains(loc2)){
            rubble2 = rc.senseRubble(loc2);
        }
        if(rc.canSenseLocation(loc3) && !prevLocs.contains(loc3)){
            rubble3 = rc.senseRubble(loc3);
        }
        if(rc.canSenseLocation(loc4) && !prevLocs.contains(loc4)){
            rubble4 = rc.senseRubble(loc4);
        }
        if(rc.canSenseLocation(loc5) && !prevLocs.contains(loc5)){
            rubble5 = rc.senseRubble(loc5);
        }
        if(rubble1 <= rubble2 && rubble1 <= rubble3 && rubble1 <= rubble4 && rubble1 <= rubble5 && rc.canMove(initDirection)){
            rc.move(initDirection);
            myLocation = rc.getLocation();
            prevLocs.add(myLocation);
            return true;
        }else if(rubble2 <= rubble3 && rubble2 <= rubble4 && rubble2 <= rubble5 && rc.canMove(dir2)){
            rc.move(dir2);
            myLocation = rc.getLocation();
            prevLocs.add(myLocation);
            return true;
        }else if(rubble3 <= rubble4 && rubble3 <= rubble5 && rc.canMove(dir3)){
            rc.move(dir3);
            myLocation = rc.getLocation();
            prevLocs.add(myLocation);
            return true;
        }else if(rubble4 <= rubble5 && rc.canMove(dir4)){
            rc.move(dir4);
            myLocation = rc.getLocation();
            prevLocs.add(myLocation);
            return true;
        }else if(rc.canMove(dir5)){
            rc.move(dir5);
            myLocation = rc.getLocation();
            prevLocs.add(myLocation);
            return true;
        }
        return false;
    }

    public boolean priorityMoveNew() throws GameActionException {
        if(initDirection == null){
            updateDirection(Constants.INTERMEDIATE_DIRECTIONS[(int) (Math.random()*4)]);
        }
        if(myLocation.x == 0 || myLocation.y == 0 || myLocation.x == rc.getMapWidth()-1 || myLocation.y == rc.getMapHeight()){
            updateDirection(initDirection.rotateLeft().rotateLeft());
        }
        Direction dir = null; int rubble = 101;
        int[] offsets = new int[2];
        if(rc.canMove(initDirection)){
            offsets = getDirectionOffsets(initDirection);
            int xVal = offsets[0]+myLocation.x, yVal = offsets[1]+myLocation.y;
            if(xVal >= 0 && xVal < mapWidth && yVal >= 0 && yVal < mapHeight &&
                    !prevLocs.contains(myLocation)){
                dir = initDirection;
                rubble = rc.senseRubble(rc.adjacentLocation(initDirection));
            }

        }
        int rubble2;
        if(rc.canMove(initDirection.rotateLeft())){
            offsets = getDirectionOffsets(initDirection);
            int xVal = offsets[0]+myLocation.x, yVal = offsets[1]+myLocation.y;
            if(xVal >= 0 && xVal < rc.getMapWidth() && yVal >= 0 && yVal < rc.getMapHeight() &&
                    !prevLocs.contains(myLocation)){
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
                    !prevLocs.contains(myLocation)){
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
            updateDirection(Constants.INTERMEDIATE_DIRECTIONS[(int) (Math.random()*4)]);
        }
        switch(initDirection){
            case SOUTHEAST:
            case NORTHEAST:
            case NORTHWEST:
            case SOUTHWEST: break;
            case SOUTH: updateDirection(Constants.SOUTHERN_DIR[(int) (Math.random()*2)]); break;
            case NORTH: updateDirection(Constants.NORTHERN_DIR[(int) (Math.random()*2)]); break;
            case WEST:  updateDirection(Constants.WESTERN_DIR[(int) (Math.random()*2)]);  break;
            case EAST:  updateDirection(Constants.EASTERN_DIR[(int) (Math.random()*2)]);  break;
        }
        if(!rc.onTheMap(myLocation.add(initDirection))){
            int x = myLocation.x, y = myLocation.y;
            switch (initDirection){
                case SOUTHWEST:
                    if(x < 2){
                        if(y < 2){
                            updateDirection(Direction.NORTHEAST);
                        }else{
                            updateDirection(Direction.SOUTHEAST);
                        }
                    }else{
                        if(y < 2){
                            updateDirection(Direction.NORTHWEST);
                        }
                    }break;
                case NORTHEAST:
                    if(x > mapWidth-3){
                        if(y > mapHeight-3){
                            updateDirection(Direction.SOUTHWEST);
                        }else{
                            updateDirection(Direction.NORTHWEST);
                        }
                    }else{
                        if(y > mapHeight-3){
                            updateDirection(Direction.SOUTHEAST);
                        }
                    }break;
                case NORTHWEST:
                    if(x < 2){
                        if(y > mapHeight-3){
                            updateDirection(Direction.SOUTHEAST);
                        }else{
                            updateDirection(Direction.NORTHEAST);
                        }
                    }else{
                        if(y > mapHeight-3){
                            updateDirection(Direction.SOUTHWEST);
                        }
                    }break;
                case SOUTHEAST:
                    if(x > mapWidth-3){
                        if(y < 2){
                            updateDirection(Direction.NORTHWEST);
                        }else{
                            updateDirection(Direction.SOUTHWEST);
                        }
                    }else{
                        if(y < 2){
                            updateDirection(Direction.NORTHEAST);
                        }
                    }break;
            }
        }
        if(priorityMoveNew2()) return true;
        for(int i = 0; i < directions.length; i++){
            int[] offsets = getDirectionOffsets(directions[i]);
            int xVal = offsets[0]+myLocation.x, yVal = offsets[1]+myLocation.y;
            if(xVal >= 0 && xVal < rc.getMapWidth() && yVal >= 0 && yVal < rc.getMapHeight() &&
                    !prevLocs.contains(myLocation) && rc.canMove(directions[i])){
                if(tryMoveMultiple(directions[i])){
                    prevLocs.add(myLocation);
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
    //Robot wants to move in direction with the least amount of friendly robots
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
        if(y == 0){
            if(myLocation.y == 0){
                y += 4;
            }else if(myLocation.y == mapHeight-1){
                y -=4;
            }
        }
        if(x == 0){
            if(myLocation.x == 0){
                x += 4;
            }else if(myLocation.x == mapWidth-1){
                x -=4;
            }
        }
        if(myLocation.x + x >= mapWidth){
            x = mapWidth-1-myLocation.x;
        }else if(myLocation.x + x < 0){
            x = 0-myLocation.x;
        }
        if(myLocation.y + y >= mapHeight){
            y = mapHeight-1-myLocation.y;
        }else if(myLocation.y + y < 0){
            y = 0-myLocation.y;
        }
        exploreTarget = new MapLocation(x+myLocation.x,y+myLocation.y);
    }
    public void explore() throws GameActionException {
        newLocation();
        intermediateMove(exploreTarget);
    }
    public void parseAnomalies() {
        for (AnomalyScheduleEntry a : anomaly) {
            if (a.anomalyType == AnomalyType.CHARGE) {
                relevantAnomalies.add(a);
            }
        }
    }
    public void avoidCharge() throws GameActionException {
        if(relevantAnomalies.isEmpty())return;
        AnomalyScheduleEntry a = relevantAnomalies.get(0);
        if (rc.getRoundNum()>a.roundNumber){
            relevantAnomalies.remove(a);
            return;
        }
        if(rc.getRoundNum()+10>a.roundNumber){
            int x=0;
            int y=0;
            RobotInfo [] friends = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam);
            for (RobotInfo r: friends){
                switch (rc.getLocation().directionTo(r.getLocation()).opposite()){
                case NORTH:y+=1;break;
                case NORTHEAST: y+=1; x+=1; break;
                case NORTHWEST: y+=1; x-=1; break;
                case SOUTH: y-=1; break;
                case EAST: x+=1; break;
                case WEST: x-=1; break;
                case SOUTHEAST: y-=1; x+=1; break;
                case SOUTHWEST: y-=1; x-=1; break;
                }
            }
            Direction d = getDirection(x, y);
            tryMoveMultiple(d);
        }
    }

}
