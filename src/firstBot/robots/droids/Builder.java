package firstBot.robots.droids;

import battlecode.common.*;
import firstBot.util.Constants;
import java.util.Random;
public class Builder extends Droid{
    private int startingBit;
    private int archon;
    private Random rand = new Random();
    private MapLocation archonLoc;
    private boolean prevBuilder = false;
    private int prevTowers=10;
    public Builder(RobotController rc) throws GameActionException {
        super(rc);
        int archonID=0;
        for (RobotInfo R : rc.senseNearbyRobots()){
            if (R.getType() == RobotType.ARCHON){
                archonID=R.getID();
                archonLoc=R.getLocation();
            }
        }
        for(int i=63; i>59; i--){
            if (rc.readSharedArray(i)==archonID){
                startingBit=2*(63-i);
                archon=63-i;
            }
        }
    }

    @Override
    public void init() throws GameActionException {

    }

    @Override
  public void run() throws GameActionException {
        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(1, rc.readSharedArray(1)+1);
        }
        int toBuild = read();
        rc.setIndicatorString(Integer.toBinaryString(toBuild));
        boolean built = build(toBuild);
        boolean nearPrototype = false;
        MapLocation prototypeLoc = null;
        for (RobotInfo r : rc.senseNearbyRobots()){
            if (r.getMode() == RobotMode.PROTOTYPE){
                if (rc.canRepair(r.getLocation())){
                    nearPrototype=true;
                    prototypeLoc = r.getLocation();
                }
            }
        }
        if (built){
            for (RobotInfo r:rc.senseNearbyRobots()){
                if (r.getMode()==RobotMode.PROTOTYPE){
                    MapLocation target = r.getLocation();
                    rc.writeSharedArray(59, archon*128+target.x*64+target.y);
                    }
                }
            }
        else if (nearPrototype){
            rc.repair(prototypeLoc);
         }
         else{
             int randint = rand.nextInt(8);
             Direction d = Constants.DIRECTIONS[randint];
             if(rc.canMove(d)){
                 rc.move(d);
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
        switch (archon) {
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
        int index = archon+5;
        return rc.readSharedArray(index);
    }
    public void addTowers() throws GameActionException{
        int index = archon + 5;
        rc.writeSharedArray(index, rc.readSharedArray(index)+1);
        prevTowers=rc.readSharedArray(index)+1;
        int power = (int)Math.pow(2,startingBit);
        int buildCommand = rc.readSharedArray(58) - 2 * power;
        rc.writeSharedArray(58, buildCommand);
    }
    public void addLabs() throws GameActionException{
        int labCount = rc.readSharedArray(4);
        rc.writeSharedArray(4, labCount + (int)Math.pow(2,archon*4));
        rc.writeSharedArray(58, rc.readSharedArray(58) - (int)Math.pow(2,startingBit));
    }
    public boolean build(int id) throws GameActionException{
        if (prevBuilder) return false;
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
        for(Direction d : Direction.allDirections()){
            if(rc.canBuildRobot(r,d)){
                if (rc.senseRubble(rc.getLocation().add(d))<minR){
                    k=d; minR=rc.senseRubble(rc.getLocation().add(d));
                }
            }
        }
        if (k!=null) {
            rc.buildRobot(r, k);
            if (r == RobotType.WATCHTOWER) addTowers();
            else addLabs();
            return true;
        }
        return false;
    }
}