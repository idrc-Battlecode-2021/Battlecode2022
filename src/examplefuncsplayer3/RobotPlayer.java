package examplefuncsplayer3;

import battlecode.common.*;
import bot3_JJ3.util.Constants;

import java.util.HashSet;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {
    private static MapLocation target, myLocation;
    private static Direction initDirection;
    private static int mapWidth,mapHeight;
    private static Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };;
    private static HashSet<MapLocation> prevLocs = new HashSet<>();

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST,};

    /** Array containing all the possible movement directions. */

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        //System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");
        myLocation = rc.getLocation();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!
            //System.out.println("Age: " + turnCount + "; Location: " + rc.getLocation());

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case ARCHON:     runArchon(rc);  break;
                    case MINER:      runMiner(rc);   break;
                    case SAGE:
                    case SOLDIER:    runSoldier(rc); break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: // You might want to give them a try!
                    case BUILDER:
                        break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                //System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                //System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runArchon(RobotController rc) throws GameActionException {
        // Pick a direction to build in.
        Direction dir = null;
        for(int i = directions.length; --i>=0;){
            MapLocation adj = rc.adjacentLocation(directions[i]);
            if(!rc.canSenseRobotAtLocation(adj) && rc.onTheMap(adj)){
                dir = directions[i];
                break;
            }
        }
        if(dir == null)return;
        if(rc.canBuildRobot(RobotType.SAGE,dir)){
            rc.buildRobot(RobotType.SAGE,dir);
        }else{
            if (rng.nextBoolean()) {
                // Let's try to build a miner.
                rc.setIndicatorString("Trying to build a miner");
                if (rc.canBuildRobot(RobotType.MINER, dir)) {
                    rc.buildRobot(RobotType.MINER, dir);
                }
            } else {
                // Let's try to build a soldier.
                rc.setIndicatorString("Trying to build a soldier");
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
            }
        }
    }

    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static boolean isGold;
    static void runMiner(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        myLocation = rc.getLocation();

        if (target != null) {
            if(isGold){
                intermediateMove(target,rc);
                if(rc.canSenseLocation(target) && rc.senseGold(target) == 0)target = null;
                else{
                    if(rc.canMineGold(target))rc.mineGold(target); return;
                }

            }else{
                intermediateMove(target,rc);
                if(rc.canSenseLocation(target) && rc.senseLead(target) == 0)target = null;
                else{
                    if(rc.canMineLead(target))rc.mineLead(target); return;
                }

            }
        }else{
            if (!tryMoveMultipleNew(rc))tryMoveMultiple(initDirection,rc);
            checkLocs:{
                MapLocation[] gold = rc.senseNearbyLocationsWithGold(20);
                if(gold.length > 0){
                    target = gold[0];
                    isGold = true;
                    break checkLocs;
                }
                MapLocation[] lead = rc.senseNearbyLocationsWithLead(20);
                if(lead.length > 0){
                    target = lead[0];
                    isGold = false;
                    break checkLocs;
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(myLocation.x + dx, myLocation.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                    target = mineLocation;
                    isGold = true;
                }
                while (rc.canMineLead(mineLocation)) {
                    rc.mineLead(mineLocation);
                    target = mineLocation;
                    isGold = false;
                }
            }
        }
    }

    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runSoldier(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        boolean foundEnemies = false;
        if (enemies.length > 0) {
            foundEnemies = true;
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
                target = toAttack;
            }
        }else{
            foundEnemies = false;
            target = null;
        }
        if(target != null){
            intermediateMove(target,rc);
        }else{
            if (!tryMoveMultipleNew(rc))tryMoveMultiple(initDirection,rc);
        }

    }

    public static void intermediateMove(MapLocation target, RobotController rc) throws GameActionException {
        int x = target.x-myLocation.x, y = target.y-myLocation.y;
        if (x == y) {
            if(x == 0) return;
            tryMoveMultiple(selectDirection(x, y),rc);
        } else if (y == 0) {
            double pass1 = 101, pass2 = 101, pass3 = rc.senseRubble(rc.adjacentLocation(selectDirection(x, 0)));
            Direction dir1 = selectDirection(x, 1), dir2 = selectDirection(x, -1);
            if (rc.onTheMap(rc.adjacentLocation(dir1))) {
                pass1 = rc.senseRubble(rc.adjacentLocation(dir1));
            } else if (rc.onTheMap(rc.adjacentLocation(dir2))) {
                pass2 = rc.senseRubble(rc.adjacentLocation(dir2));
            }
            if (pass3 < pass2 && pass3 < pass1) {
                tryMoveMultiple(selectDirection(x, 0),rc);
            } else if (pass2 < pass1) {
                tryMoveMultiple(dir2,rc);
            } else {
                tryMoveMultiple(dir1,rc);
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
            tryMoveMultiple(rc.getLocation().directionTo(move),rc);
        } else if (Math.abs(x) > Math.abs(y)) {
            int rubble1 = 101, rubble2 = 101;
            if(rc.onTheMap(rc.adjacentLocation(selectDirection(x, 0)))){
                rubble1 = rc.senseRubble(rc.adjacentLocation(selectDirection(x, 0)));
            }
            if(rc.onTheMap(rc.adjacentLocation(selectDirection(x, y)))){
                rubble2 = rc.senseRubble(rc.adjacentLocation(selectDirection(x, y)));
            }
            if (rubble1 < rubble2) {
                tryMoveMultiple(selectDirection(x, 0),rc);
            } else {
                tryMoveMultiple(selectDirection(x, y),rc);
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
                tryMoveMultiple(selectDirection(0, y),rc);
            } else {
                tryMoveMultiple(selectDirection(x, y),rc);
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
    public static boolean tryMoveMultiple(Direction dir, RobotController rc) throws GameActionException{ //tries to move in direction, followed by adjacent directions
        if(initDirection == null && (dir == null || dir.equals(Direction.CENTER))){
            updateDirection(DIRECTIONS[(int) (Math.random()*8)]);
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
    public static void updateDirection(Direction d){
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
    private static boolean priorityMoveNew2(RobotController rc) throws GameActionException{
        Direction dir2 = initDirection.rotateLeft(), dir3 = initDirection.rotateRight();
        int rubble1 = rc.senseRubble(rc.adjacentLocation(initDirection)),
                rubble2 = rc.senseRubble(rc.adjacentLocation(dir2)),
                rubble3 = rc.senseRubble(rc.adjacentLocation(dir3));
        if(rubble1 <= rubble2 && rubble1 <= rubble3 && rc.canMove(initDirection)){
            rc.move(initDirection);
            myLocation = rc.getLocation();
            prevLocs.add(myLocation);
            return true;
        }else if(rubble2 <= rubble3 && rc.canMove(dir2)){
            rc.move(dir2);
            myLocation = rc.getLocation();
            prevLocs.add(myLocation);
            return true;
        }else if(rc.canMove(dir3)){
            rc.move(dir3);
            myLocation = rc.getLocation();
            prevLocs.add(myLocation);
            return true;
        }
        return false;
    }

    private static boolean tryMoveMultipleNew(RobotController rc) throws GameActionException {
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
                    if(x == 0){
                        if(y == 0){
                            updateDirection(Direction.NORTHEAST);
                        }else{
                            updateDirection(Direction.SOUTHEAST);
                        }
                    }else{
                        if(y == 0){
                            updateDirection(Direction.NORTHWEST);
                        }
                    }break;
                case NORTHEAST:
                    if(x == mapWidth-1){
                        if(y == mapHeight-1){
                            updateDirection(Direction.SOUTHWEST);
                        }else{
                            updateDirection(Direction.NORTHWEST);
                        }
                    }else{
                        if(y == mapHeight-1){
                            updateDirection(Direction.SOUTHEAST);
                        }
                    }break;
                case NORTHWEST:
                    if(x == 0){
                        if(y == mapHeight-1){
                            updateDirection(Direction.SOUTHEAST);
                        }else{
                            updateDirection(Direction.NORTHEAST);
                        }
                    }else{
                        if(y == mapHeight-1){
                            updateDirection(Direction.SOUTHWEST);
                        }
                    }break;
                case SOUTHEAST:
                    if(x == mapWidth-1){
                        if(y == 0){
                            updateDirection(Direction.NORTHWEST);
                        }else{
                            updateDirection(Direction.SOUTHWEST);
                        }
                    }else{
                        if(y == 0){
                            updateDirection(Direction.NORTHEAST);
                        }
                    }break;
            }
        }
        if(priorityMoveNew2(rc)) return true;
        for(int i = 0; i < directions.length; i++){
            int[] offsets = getDirectionOffsets(directions[i]);
            int xVal = offsets[0]+myLocation.x, yVal = offsets[1]+myLocation.y;
            if(xVal >= 0 && xVal < rc.getMapWidth() && yVal >= 0 && yVal < rc.getMapHeight() &&
                    !prevLocs.contains(myLocation) && rc.canMove(directions[i])){
                if(tryMoveMultiple(directions[i],rc)){
                    prevLocs.add(myLocation);
                    return true;
                }
            }
        }
        return false;
    }

    private static int[] getDirectionOffsets(Direction dir){
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
}
