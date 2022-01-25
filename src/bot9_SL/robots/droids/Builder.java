package bot9_SL.robots.droids;

import battlecode.common.*;
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
        //build()
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(1, rc.readSharedArray(1)+(int)Math.pow(16,myArchonOrder));
        }else if(rc.getRoundNum()%3 == 0){
            int power = (int)Math.pow(16,myArchonOrder);
            builderCount = (rc.readSharedArray(1)%(power*16))/(power);
        }
        if(rc.getLocation().distanceSquaredTo(archonLoc)<2){
            tryMoveMultiple(rc.getLocation().directionTo(archonLoc).opposite());
        }
        RobotInfo [] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam.opponent());
        if(enemies.length==0) return;
        for (RobotInfo r: enemies) {
            if (r.getType() == RobotType.SOLDIER || rc.getType() == RobotType.SAGE || rc.getType() == RobotType.WATCHTOWER) {
                if (rc.canMove(r.getLocation().directionTo(rc.getLocation()).opposite())) {
                    rc.move(r.getLocation().directionTo(rc.getLocation()).opposite());
                }
            }
        }
    }
    public void build() throws GameActionException {
        if (rc.getTeamLeadAmount(myTeam) < RobotType.WATCHTOWER.buildCostLead) {
            return;
        }
        for (Direction d : Direction.allDirections()) {
            if (rc.canBuildRobot(RobotType.LABORATORY, d)) {
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
}