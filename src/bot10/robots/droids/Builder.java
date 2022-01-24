package bot10.robots.droids;

import battlecode.common.*;
import bot10.util.Constants;
import bot10.util.PathFindingSoldier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
public class Builder extends Droid{
    private int startingBit;
    private Random rand = new Random();
    private MapLocation archonLoc;
    private boolean isDefensive = true;
    private MapLocation finishPrototype = null;
    private int builderCount = 0;
    private int globalLabCount = 0;
    private PathFindingSoldier pfs;
    private static int labThreshold = 180;
    
    public Builder(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        pfs=new PathFindingSoldier(rc);
        detectArchon();
        archonLoc = rc.senseRobot(myArchonID).getLocation();
        startingBit = 2*myArchonOrder;
        //System.out.println("init: "+ Clock.getBytecodesLeft());
    }

    @Override
    public void run() throws GameActionException {
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
        labThreshold = (globalLabCount+1)*180;
        //first repair prototype if it can
        //TODO: navigate toward prototype location if not there, etc.
        if (finishPrototype!=null && !rc.getLocation().isWithinDistanceSquared(finishPrototype, RobotType.BUILDER.actionRadiusSquared)){
            //shouldn't happen but program in case
        }
        if (finishPrototype!=null && rc.canSenseRobotAtLocation(finishPrototype)){
            MapLocation best_location = rc.getLocation();
            int lowest_rubble = rc.senseRubble(best_location);
            MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(finishPrototype, RobotType.BUILDER.actionRadiusSquared);
            for (MapLocation loc:locations){
                if (!rc.canSenseLocation(loc) || rc.canSenseRobotAtLocation(loc) )continue;
                int rubble = rc.senseRubble(loc);
                if (rubble<lowest_rubble){
                    lowest_rubble = rubble;
                    best_location = loc;
                }
            }
            if (rc.isMovementReady() && rc.getLocation()!=best_location){
                intermediateMove(best_location);
                //TODO: replace with soldierMove?
            }
            RobotInfo prototype = rc.senseRobotAtLocation(finishPrototype);
            if (prototype.getHealth()==prototype.getType().health){
                finishPrototype=null;
            }
            else{
                if(rc.canRepair(finishPrototype)){
                    rc.repair(finishPrototype);
                }
                return;
            }
        }
        //Moves away from archons to not disturb its spawning rates
        RobotInfo[] checkArchons = rc.senseNearbyRobots(Math.max(builderCount,8),myTeam);
        for(int i = checkArchons.length; --i>=0;){
            if(checkArchons[i].getType().equals(RobotType.ARCHON)){
                tryMoveMultiple(myLocation.directionTo(checkArchons[i].getLocation()).opposite());
            }
        }

        boolean built = false;
        if (rc.getTeamLeadAmount(rc.getTeam())>labThreshold){
            built = build(1);
        }
        
        boolean nearPrototype = false;
        MapLocation prototypeLoc = null;
        RobotInfo[] robots = rc.senseNearbyRobots(20,myTeam);
        for (int i = robots.length; --i>=0;){
            if (robots[i].getMode() == RobotMode.PROTOTYPE){
                if (rc.canRepair(robots[i].getLocation())){
                    nearPrototype=true;
                    prototypeLoc = robots[i].getLocation();
                }
            }
        }
        
        
        //System.out.println("After repair: "+Clock.getBytecodesLeft());
        if (built){
            for (int i = robots.length; --i>=0;){
                if (robots[i].getMode()==RobotMode.PROTOTYPE){
                    MapLocation target = robots[i].getLocation();
                    rc.writeSharedArray(59, myArchonOrder*128+target.x*64+target.y);
                    }
                }
                //System.out.println("After built: "+Clock.getBytecodesLeft());
            }
            
        else if (nearPrototype){
            rc.repair(prototypeLoc);
            //System.out.println("After nearPrototype: "+Clock.getBytecodesLeft());
        }
        else if (isDefensive){
            intermediateMove(archonLoc);
            if (rc.getLocation().distanceSquaredTo(archonLoc)<=2){
                intermediateMove(rc.getLocation().add(rc.getLocation().directionTo(archonLoc).opposite()));
            }
            else{
                intermediateMove(archonLoc);
            }
            //System.out.println("After isDefensive: "+Clock.getBytecodesLeft());
        }
            else{
                int randint = rand.nextInt(8);
                Direction d = Constants.DIRECTIONS[randint];
                if(rc.canMove(d)){
                    rc.move(d);
                    myLocation = rc.getLocation();
                }
                //System.out.println("After random: "+Clock.getBytecodesLeft());
            }

    }
    public boolean canHeal() throws GameActionException{
        if(rc.readSharedArray(59)==0){
            return false;
        }
        return true;
    }
    public int closestBuilders() throws GameActionException{
        int group = rc.readSharedArray(59);
        group = group/128;
        return group;
    }
    public MapLocation healLocation() throws GameActionException{
        int loc = rc.readSharedArray(59);
        int x = (loc/64)%64;
        int y = loc%64;
        return new MapLocation(x, y);
    }
    public int read() throws GameActionException{
        int build = rc.readSharedArray(58);
        switch (myArchonOrder) {
            case 0: build=build%4;
                break;
            case 1: build=(build/4)%4;
                break;
            case 2: build=(build/16)%4;
                break;
            case 3: build=(build/64)%4;
                break;
        }
        return build;
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
    public void addLabs() throws GameActionException{
        int labCount = rc.readSharedArray(4);
        rc.writeSharedArray(4, labCount + 1);
        //rc.writeSharedArray(58, rc.readSharedArray(58) - (int)Math.pow(2,startingBit));
    }
    public boolean build(int id) throws GameActionException{
        if(myLocation.x%2 == myLocation.y%2){
            boolean notMoved = true;
            List<Direction> basic = Arrays.asList(Constants.BASIC_DIRECTIONS);
            Collections.shuffle(basic);
            for(Direction d: basic){
                if(rc.canMove(d)){
                    rc.move(d);
                    myLocation = rc.getLocation();
                    notMoved = false;
                    break;
                }
            }
            if(notMoved || myLocation.x%2 == myLocation.y%2)return false;
        }
        RobotType r = RobotType.WATCHTOWER;
        if (id ==0){
            return false;
        }
        else if (id == 1) {
            r = RobotType.LABORATORY;
        }
        // find square with least rubble.
        Direction k = null;
        int minR=1000;
        Direction[] basic = Constants.BASIC_DIRECTIONS;

        for(int i = basic.length; --i>=0;){
            if(rc.canBuildRobot(r,basic[i])){
                if (rc.senseRubble(myLocation.add(basic[i]))<minR){
                    k=basic[i];
                    minR=rc.senseRubble(myLocation.add(basic[i]));
                }
            }
        }
        if (k!=null){
            if(minR < rc.senseRubble(myLocation.add(k))){
                if (rc.canMove(k)){
                    rc.move(k);
                    myLocation = rc.getLocation();
                    k=null;
                    for(int i = basic.length; --i>=0;){
                        if(rc.canBuildRobot(r,basic[i])){
                            if (rc.senseRubble(myLocation.add(basic[i]))<minR){
                                k=basic[i];
                                minR=rc.senseRubble(myLocation.add(basic[i]));
                            }
                        }
                    }
                }
            }
        }
        if (k!=null) {
            rc.buildRobot(r, k);
            finishPrototype = rc.getLocation().add(k);
            //rc.setIndicatorString("prototype");
            if (r == RobotType.WATCHTOWER) addTowers();
            else addLabs();
            return true;
        }
        return false;
    }
}