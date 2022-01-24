package bot3_JJ3.robots;

import battlecode.common.*;
import bot3_JJ3.util.Constants;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public abstract class Robot {
    protected RobotController rc;
    protected Team myTeam;
    protected RobotType myType;
    protected MapLocation myLocation;
    protected int myArchonID; //offset by 1
    protected int myArchonOrder;
    protected int mapWidth,mapHeight;
    protected int initialArchons;
    protected boolean archonWait = false;
    protected ArrayList <MapLocation> enemyArchons = new ArrayList<MapLocation>();
    protected Direction initDirection;
    protected Direction[] directions;
    protected ArrayList<MapLocation> xReflect = new ArrayList<MapLocation>();
    protected ArrayList<MapLocation> yReflect = new ArrayList<MapLocation>();
    protected ArrayList<MapLocation> rotate = new ArrayList<MapLocation>();

    //OLD Movement Method Fields
    protected int[][] internalMap;
    protected HashSet<MapLocation> prevLocs = new HashSet<>();
    // -1 = unknown, otherwise amount of rubble

    public Robot( RobotController rc){
        this.rc = rc;
        myTeam = rc.getTeam();
        myLocation = rc.getLocation();
        myType = rc.getType();
        
        mapWidth = rc.getMapWidth(); mapHeight = rc.getMapHeight();
        initialArchons = rc.getArchonCount();
        updateDirection(myLocation.directionTo(new MapLocation(mapWidth/22,mapHeight/2)));
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
        for(int i=63; i>59; i--){
            if (rc.readSharedArray(i)==myArchonID){
                myArchonOrder=63-i;
            }
        }
        
    }

    public void detectArchon() throws GameActionException{
        int startingByteCode = Clock.getBytecodesLeft();
        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (int i = robots.length; --i>=0;){
            if (robots[i].getType() == RobotType.ARCHON){
                myArchonID = robots[i].getID()+1;
                break;
            }
        }
        for(int i=63; i>59; i--){
            if (rc.readSharedArray(i)==myArchonID){
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
    public MapLocation decode() throws GameActionException{
        int loc = rc.readSharedArray(55);
        int x = (loc/64)%64;
        int y = loc%64;
        return new MapLocation(x, y);
    }
    public boolean hasMapLocation(int i) throws GameActionException{
        if (rc.readSharedArray(i)==0){
            return false;
        }
        return true;
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
        int num_enemies =enemies.length;
        int k=0;
        boolean seesArchon = false;
        boolean seesAttackers = false;
        loop: for(int i = enemies.length; --i>=0;){
            switch (enemies[i].getType()){
                case ARCHON:
                    seesArchon=true;
                    k=4096+64*enemies[i].getLocation().x+enemies[i].getLocation().y;
                    break loop;
                case SAGE:
                case BUILDER:
                case SOLDIER:
                case WATCHTOWER:
                    seesAttackers = true;
            }
        }
        if (num_enemies>5 && !seesArchon){
            MapLocation m = rc.getLocation();
            int x = m.x, y=m.y;
            k=x*64+y;
            rc.writeSharedArray(55,k);
        } else if (seesArchon){
            rc.writeSharedArray(55, k);
        }else if(seesAttackers){
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

    protected void pathFind(MapLocation target) throws GameActionException{
        //dijkstra for unknown square
        ArrayList<MapLocation> queue = new ArrayList<MapLocation>();
        HashMap<MapLocation, Integer> dists = new HashMap<>(), movementCosts = new HashMap<>();
        //dist: base distance to target plus rubble penalty, movementCosts: the weighted graph
		MapLocation[] senseLocations = rc.getAllLocationsWithinRadiusSquared(myLocation, 4);
		//Set Radius squared to 15 to try and reduce bytecode
        System.out.println(2+" "+Clock.getBytecodesLeft());
		for (int i = senseLocations.length; --i >= 0;){
		    //Not Tested but could result in less bytecode used at the cost of not checking all tiles
            int distance = movementTileDistance(senseLocations[i],target);
            if(distance < movementTileDistance(myLocation,target)){
                queue.add(senseLocations[i]);
                dists.put(senseLocations[i],distance+turnPenalty(rc.senseRubble(senseLocations[i])));
            }
            //queue.add(senseLocations[i]);
            //dists.put(senseLocations[i], movementTileDistance(senseLocations[i],target)+turnPenalty(rc.senseRubble(senseLocations[i])));

        }
        System.out.println(3+" "+Clock.getBytecodesLeft());
        while (queue.size() > 0){
            MapLocation chosen = null;
			int min= Integer.MAX_VALUE;
			for(int i = queue.size(); --i>=0;){
                MapLocation temp = queue.get(i);
			    if(dists.get(temp) < min){
                    min = dists.get(temp);
                    chosen = temp;
                }
			}
           	queue.remove(chosen);
           	if(!movementCosts.containsKey(chosen)){
                movementCosts.put(chosen,dists.get(chosen));
                //May be more bytecode efficient if this was an if else where one uses chosen's movementCost value and the 
                //other uses chosen's dist value.
           	}
            MapLocation[] temp = rc.getAllLocationsWithinRadiusSquared(chosen, 2);
           	for (int i = temp.length; --i>=0;){
                if(dists.containsKey(temp[i])){
                    int tot_rubble = movementCosts.get(chosen) + turnPenalty(rc.senseRubble(temp[i]));
                    if (!movementCosts.containsKey(temp[i]) || tot_rubble < movementCosts.get(temp[i])){
                        movementCosts.put(temp[i], tot_rubble);
                    }
                }
           	}
           	
        }
        MapLocation[] local = rc.getAllLocationsWithinRadiusSquared(myLocation,2);
        Arrays.sort(local,(MapLocation o1, MapLocation o2) -> movementCosts.get(o2)-movementCosts.get(o1));
        for(int i = local.length; --i>=0;){
            Direction dirTo = myLocation.directionTo(local[i]);
            if(rc.canMove(dirTo)){
                rc.move(dirTo);
                myLocation = rc.getLocation();
                break;
            }
        }

        //dists now contains a dictionary of smallest distance to every square in sight
        
        //if target in sight then pathfind
        	
        
        
        //next step is to pick a decent square in the direction of overall target to pathfind toward if target is not in sight
        
        //note to self: change to include internal map of rubble amounts to lower bytecode instead of resensing
        //note to self: add test to see if target is in internal map already or in sight already
        //note to self: use internal map to find closest known square to target? instead of current
    }

    private void tryAttack(MapLocation Loc) throws GameActionException {
        if (rc.canAttack(Loc)){
            rc.attack(Loc);
        }
    }

    private void selectPriorityTarget() throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        RobotInfo[] myRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        RobotInfo archon=null, sage=null, lab=null, watchtower=null, soldier=null, miner=null, builder=null;
        int[] damages = {0,0,0,0,0}; //order corresponds with order of variables above
        for (RobotInfo r : enemyRobots) {
            if (r.getType() == RobotType.ARCHON) {
                if (archon == null || archon.getHealth() > r.getHealth()) {
                    archon = r;
                    damages[0]=rc.getType().getDamage(rc.getLevel());
                    for (RobotInfo robot: myRobots){
                        if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                            damages[0]+=robot.getType().getDamage(robot.getLevel());
                        }
                    }
                }
            }
            else if (r.getType() == RobotType.SAGE) {
                if (sage == null || sage.getHealth() > r.getHealth()) {
                    sage = r;
                    damages[1]=rc.getType().getDamage(rc.getLevel());
                    for (RobotInfo robot: myRobots){
                        if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                            damages[1]+=robot.getType().getDamage(robot.getLevel());
                        }
                    }
                }
            }
            else if (r.getType() == RobotType.LABORATORY) {
                if (lab == null || lab.getHealth() > r.getHealth()) {
                    lab = r;
                    damages[2]=rc.getType().getDamage(rc.getLevel());
                    for (RobotInfo robot: myRobots){
                        if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                            damages[2]+=robot.getType().getDamage(robot.getLevel());
                        }
                    }
                }
            }
            else if (r.getType() == RobotType.WATCHTOWER) {
                if (watchtower == null || archon.getHealth() > r.getHealth()) {
                    watchtower = r;
                    damages[3]=rc.getType().getDamage(rc.getLevel());
                    for (RobotInfo robot: myRobots){
                        if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                            damages[3]+=robot.getType().getDamage(robot.getLevel());
                        }
                    }
                }
            }
            else if (r.getType() == RobotType.SOLDIER) {
                if (soldier == null || soldier.getHealth() > r.getHealth()) {
                    soldier = r;
                    damages[4]=rc.getType().getDamage(rc.getLevel());
                    for (RobotInfo robot: myRobots){
                        if (robot.getLocation().distanceSquaredTo(r.getLocation())<=robot.getType().actionRadiusSquared){
                            damages[4]+=robot.getType().getDamage(robot.getLevel());
                        }
                    }
                }
            }
            else if (r.getType() == RobotType.MINER) {
                if (miner == null || miner.getHealth() > r.getHealth()) {
                    miner = r;
                }
            }
            else if (r.getType() == RobotType.BUILDER) {
                if (builder == null || builder.getHealth() > r.getHealth()) {
                    builder = r;
                }
            }
        }
        int archonTurns = Integer.MAX_VALUE, sageTurns = Integer.MAX_VALUE, labTurns = Integer.MAX_VALUE,
                watchtowerTurns = Integer.MAX_VALUE, soldierTurns = Integer.MAX_VALUE;
        int[] turns = {archonTurns, sageTurns, labTurns, watchtowerTurns, soldierTurns};
        MapLocation[] locations = {archon.getLocation(), sage.getLocation(), lab.getLocation(), watchtower.getLocation(),soldier.getLocation()};
        if (archon!=null){
            archonTurns = archon.getHealth()/damages[0];
        }
        if (archon!=null){
            sageTurns = sage.getHealth()/damages[1];
        }
        if (lab!=null){
            labTurns = lab.getHealth()/damages[2];
        }
        if (watchtower!=null){
            watchtowerTurns = watchtower.getHealth()/damages[3];
        }
        if (soldier!=null){
            soldierTurns = soldier.getHealth()/damages[4];
        }

        if (archonTurns<=10){
            tryAttack(archon.getLocation());
        }
        else if (labTurns<=5){
            tryAttack(lab.getLocation());
        }
        else if (sageTurns<=5){
            tryAttack(sage.getLocation());
        }
        else if (watchtowerTurns<=5){
            tryAttack(watchtower.getLocation());
        }
        else if (soldierTurns<=5){
            tryAttack(soldier.getLocation());
        }
        else{
            int minIndex = -1;
            for (int i=0;i<5;i++){
                if (minIndex==-1 || turns[i]<turns[minIndex]){
                    minIndex = i;
                }
            }
            if (turns[minIndex]<Integer.MAX_VALUE){
                tryAttack(locations[minIndex]);
            }
            else{
                if (miner!=null){
                    tryAttack(miner.getLocation());
                }
                else if (builder!=null){
                    tryAttack(builder.getLocation());
                }
            }
        }
    }
    
    private void updateInternalMap(){}

    private void targetArchons() throws GameActionException{
        storeEnemyArchons();
        if (enemyArchons.size()>0){
            MapLocation target = enemyArchons.get(0);
            intermediateMove(target);
        }
    }
    private void storeEnemyArchons() throws GameActionException{
        for (RobotInfo r: rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam.opponent())){
            if (r.getType()==RobotType.ARCHON){
                int x = r.getLocation().x, y=r.getLocation().y;
                if(!enemyArchons.contains(new MapLocation(x,y))){
                    enemyArchons.add(new MapLocation(x,y));
                    rc.writeSharedArray(52, 64*x+y);
                }
            }
        }
        int n = rc.readSharedArray(51);
        int x = n/64, y=n%64;
        if (enemyArchons.contains(new MapLocation(x,y))){
            enemyArchons.remove(new MapLocation(x,y));
        }
    }
    private void possibleArchonLocs() throws GameActionException{
        // Get Archon Positions
        // Get Map Size
        // Reflect each archon position over x and over y
        // Rotate 180 degrees
        MapLocation [] myArchonLocs = new MapLocation[4];
        MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
        int centerX = center.x, centerY=center.y;
        ArrayList<MapLocation> enemyLocations = new ArrayList<MapLocation>();
        //(2,36) --> (57, 23)
        for (MapLocation m: myArchonLocs){
            int x = m.x;
            int y = m.y;
            xReflect.add(new MapLocation(2*centerX -x, y));
            yReflect.add(new MapLocation(x, 2*centerY-y));
            rotate.add(new MapLocation(2*centerX-x, 2*centerY-y));
        }
    }
}
