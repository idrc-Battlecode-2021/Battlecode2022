package bot9_SL.robots.droids;

import battlecode.common.*;
import bot9_SL.robots.Robot;
import bot9_SL.util.Constants;

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
    public Builder(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        parseAnomalies();
        int archonID=0;
        detectArchon();
        archonLoc = rc.senseRobot(myArchonID).getLocation();
        startingBit = 2*myArchonOrder;
        //System.out.println("init: "+ Clock.getBytecodesLeft());
    }

    @Override
    public void run() throws GameActionException {
        avoidCharge();
        int builderCount = 1;
        reassignArchon();
       startingBit = 2*myArchonOrder;
        // update shared array
        //build();
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(1, rc.readSharedArray(1)+(int)Math.pow(16,myArchonOrder));
        }
        if(rc.getLocation().distanceSquaredTo(archonLoc)<2){
            tryMoveMultiple(rc.getLocation().directionTo(archonLoc).opposite());
        }
        if(rc.getLocation().distanceSquaredTo(archonLoc)>rc.getType().actionRadiusSquared){
            intermediateMove(archonLoc);
        }
        RobotInfo[] friends = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, myTeam);
        for (RobotInfo r: friends){
            if(r.getType()==RobotType.ARCHON){
                if (r.getHealth()!=RobotType.ARCHON.health){
                    if(rc.canRepair(r.getLocation())){
                        rc.repair(r.getLocation());
                    }
                }
            }
        }
        RobotInfo [] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam.opponent());
        if(enemies.length==0) return;
        for (RobotInfo r: enemies){
            if(r.getType()==RobotType.SOLDIER || rc.getType()==RobotType.SAGE || rc.getType()==RobotType.WATCHTOWER){
                if(rc.canMove(r.getLocation().directionTo(rc.getLocation()).opposite())){
                    rc.move(r.getLocation().directionTo(rc.getLocation()).opposite());
                }
            }
        }
    }
    public void build() throws GameActionException{
        if(rc.getTeamLeadAmount(myTeam)<RobotType.WATCHTOWER.buildCostLead){
            return;
        }
        for (Direction d: Direction.allDirections()){
            if(rc.canBuildRobot(RobotType.LABORATORY, d)){
                rc.buildRobot(RobotType.LABORATORY, d);
            }
            else if(rc.canBuildRobot(RobotType.WATCHTOWER, d)){
                rc.buildRobot(RobotType.WATCHTOWER, d);
            }
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
        //System.out.println("Subtracting 800 from "+rc.readSharedArray(11));
        //rc.writeSharedArray(11, rc.readSharedArray(11)-RobotType.LABORATORY.buildCostLead);
        int labCount = rc.readSharedArray(4);
        rc.writeSharedArray(4, labCount + (int)Math.pow(2,myArchonOrder*4));
        rc.writeSharedArray(58, rc.readSharedArray(58) - (int)Math.pow(2,startingBit));
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