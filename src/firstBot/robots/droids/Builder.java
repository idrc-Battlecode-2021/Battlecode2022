package firstBot.robots.droids;

import battlecode.common.*;
import firstBot.util.Constants;
import java.util.Random;
public class Builder extends Droid{
    private int startingBit;
    private int archon;
    private Random rand = new Random();
    private boolean close_spawn=false;
    private MapLocation archonLoc;
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
                archonID=63-i;
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
        boolean built = build(toBuild);
        if (built){
            for (RobotInfo r:rc.senseNearbyRobots()){
                if (r.getMode()==RobotMode.PROTOTYPE){
                    MapLocation target = r.getLocation();
                    rc.writeSharedArray(58, target.x*64+target.y);
                }
                }
            }
        else{
            MapLocation target = healLocation();
            if (canHeal()){
                if (rc.canRepair(target)){
                    rc.repair(target);
                }
                else{
                     intermediateMove(target);
                 }
               }
            else{
                int numBuilt = readTowers();
                if (numBuilt>=2){
                    int randInt = rand.nextInt(8);
                    if (rc.canMove(Constants.DIRECTIONS[randInt])){
                        rc.move(Constants.DIRECTIONS[randInt]);
                    }
                }
                else{
                //circle the archon
                MapLocation [] p = rc.getAllLocationsWithinRadiusSquared(archonLoc, 20);
                int randInt = rand.nextInt(20);
                intermediateMove(p[randInt]);
                }

             }
            }
    }
    public boolean canHeal() throws GameActionException{
        if(rc.readSharedArray(59)==0){
            return false;
        }
        return true;
    }
    public MapLocation healLocation() throws GameActionException{
        int loc = rc.readSharedArray(59);
        int x = loc/64;
        int y = loc%64;
        return new MapLocation(x, y);
    }
    public int read() throws GameActionException{
        int build = rc.readSharedArray(58);
        switch (startingBit) {
            case 0: build=build%4;
            case 1: build=(build/4)%4;
            case 2: build=(build/16)%4;
            case 3: build=(build/64)%4;
        }
        return build;
    }
    public int readTowers() throws GameActionException{
        int num;
        int index=6;
        if (archon == 0 || archon == 1){
            index=5;
        }
        num = rc.readSharedArray(index);
        if (archon%2==0){
            return num%128;
        }
        return num/128;
    }
    public void addTowers() throws GameActionException{
        int num;
        int index=6;
        if (archon ==0 || archon == 1){
            index=5;
        }
        num=rc.readSharedArray(index);
        int k=0;
        if (archon%2==0){
            k=num%128+1;
            k=128*(num/128)+k;
        }
        else{
            k=num/128+1;
            k=128*k+num%128;
        }
        rc.writeSharedArray(index, k);
    }
    public void addLabs() throws GameActionException{
        int num = rc.readSharedArray(4);
        int k=0;
        String q;
        String bnum=Integer.toBinaryString(num);
        String padding = "0000000000000000".substring(bnum.length()) + bnum;
        switch (archon) {
            case 0:
            q=Integer.toBinaryString(Integer.parseInt(bnum.substring(bnum.length()-3))+1);
            k=Integer.parseInt(bnum.substring(0,bnum.length()-3)+q);
            case 1:
            q=Integer.toBinaryString(Integer.parseInt(bnum.substring(bnum.length()-7,bnum.length()-4))+1);
            k=Integer.parseInt(bnum.substring(0,bnum.length()-7)+q+bnum.substring(bnum.length()-4));
            case 2:
            q=Integer.toBinaryString(Integer.parseInt(bnum.substring(bnum.length()-11,bnum.length()-8))+1);
            k=Integer.parseInt(bnum.substring(0,bnum.length()-11)+q+bnum.substring(bnum.length()-8));
            case 3:
            q=Integer.toBinaryString(Integer.parseInt(bnum.substring(bnum.length()-15,bnum.length()-12))+1);
            k=Integer.parseInt(bnum.substring(0,bnum.length()-15)+q+bnum.substring(bnum.length()-12));
        }
        rc.writeSharedArray(4, k);
    }
    public boolean build(int id) throws GameActionException{
        RobotType r = RobotType.WATCHTOWER;
        if (id ==0){
            return false;
        }
        else if (id == 1){
            r = RobotType.LABORATORY;
        }
        for(Direction d : Direction.allDirections()){
            if(rc.canBuildRobot(r,d)){
                rc.buildRobot(r, d);
                if (r == RobotType.WATCHTOWER) addTowers();
                else addLabs();
                return true;
            }
        }
        return false;
    }
}