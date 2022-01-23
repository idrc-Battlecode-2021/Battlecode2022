package bot9_MC2.robots.droids;
import battlecode.common.*;
import bot9_MC2.util.PathFindingSoldier;

import java.util.HashSet;
import java.util.ArrayList;

public class Soldier extends Droid{
    private MapLocation target = null;
    private MapLocation archonLoc;
    private MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    private int globalSoldierCount = 0;
    private boolean shouldHeal = false;
    private MapLocation[] archonLocs;
    private MapLocation centralArchon;
    private PathFindingSoldier pfs;
    private boolean reachedArchon;

    private int prevHealth = 0;
    private RobotInfo[] enemyBotsInAction;
    private RobotInfo[] enemyBotsInVision;
    private RobotInfo[] allyBotsInAction;
    private RobotInfo[] allyBotsInVision;
    public Soldier(RobotController rc) {super(rc);}

    @Override
    public void init() throws GameActionException {
        pfs=new PathFindingSoldier(rc);
        possibleArchonLocs();
        parseAnomalies();
        RobotInfo [] r = rc.senseNearbyRobots(2,myTeam);
        for (RobotInfo ro : r){
            if(ro.getTeam()==myTeam && ro.getType()==RobotType.ARCHON){
                archonLoc = ro.getLocation();
            }
        }
        detectArchon();
        int archonCount = rc.getArchonCount();
        archonLocs = getArchonLocs();
        int x = 0, y = 0;
        for(int i = 0; i < archonLocs.length; i++){ //needs to start at 0
            if(archonLocs[i] == null)break;
            x += archonLocs[i].x;
            y += archonLocs[i].y;
        }
        centralArchon = new MapLocation(x/archonCount,y/archonCount);
        prevHealth = rc.getHealth();
        enemyBotsInVision = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, rc.getTeam().opponent());
        enemyBotsInAction = rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared, rc.getTeam().opponent());
        allyBotsInAction = rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared,rc.getTeam());
        allyBotsInVision = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared,rc.getTeam());
    }

    public void setArchonLocation() throws GameActionException{
        int location = 0;
        switch (myArchonOrder){
            case 0:
                location = rc.readSharedArray(15);
                break;
            case 1:
                location = rc.readSharedArray(16);
                break;
            case 2:
                location = rc.readSharedArray(49);
                break;
            case 3:
                location = rc.readSharedArray(50);
                break;
        }
        int x = location%256;
        int y = location/256;
        archonLoc = new MapLocation(x,y);
        //rc.setIndicatorString(myArchonOrder+" "+archonLoc.toString());
    }

    public void attack() throws GameActionException{

        int health = 0;
        boolean containsSoldier = false;
        for (RobotInfo robot:enemyBotsInVision){
            if (robot.getType()!=RobotType.SOLDIER && robot.getType()!=RobotType.SAGE){
                continue;
            }
            containsSoldier = true;
            health+=robot.getHealth();
        }
        for (RobotInfo robot:allyBotsInAction){
            health-=robot.getHealth();
        }
        MapLocation attackTarget = selectVisionTarget();
        if (attackTarget!=rc.getLocation()){
            RobotInfo robot = rc.senseRobotAtLocation(attackTarget);
            //rc.setIndicatorString("vision target: "+attackTarget.toString());
            if(robot.getType() != RobotType.SOLDIER && robot.getType()!= RobotType.SAGE && !containsSoldier){ //TODO: try buffer between health
                if (!rc.getLocation().isWithinDistanceSquared(attackTarget, RobotType.SOLDIER.actionRadiusSquared)){
                    soldierMove(attackTarget);
                }
                //TODO: experiment by replacing with alternate mtlp code
                tryAttack(attackTarget);
            }
            else{
                moveToLowRubble();
                tryAttack(attackTarget);
            }
        }
        
        if (rc.isActionReady()){
            attackTarget = selectActionTarget();
            //rc.setIndicatorString("action target: "+attackTarget.toString());
            RobotInfo robot = rc.senseRobotAtLocation(attackTarget);
            if (attackTarget!=rc.getLocation()){
                if(robot.getType() != RobotType.SOLDIER && robot.getType()!=RobotType.SAGE && !containsSoldier){ //TODO: try buffer between health
                    //soldierMove(attackTarget);
                    //TODO: experiment by replacing with alternate movetolowrubble code
                    tryAttack(attackTarget);
                }
                else{
                    moveToLowRubble();
                    //TODO: experiment with soldierMove(archonLoc) or movetolowrubble away from enemy
                    tryAttack(attackTarget);
                }
            }
        }
        if (rc.isMovementReady() && enemyBotsInVision.length>0){
            moveToLowRubble();
        }
    }

    @Override
    public void run() throws GameActionException {
        reassignArchon();
        setArchonLocation();
        enemyBotsInVision = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, rc.getTeam().opponent());
        enemyBotsInAction = rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared, rc.getTeam().opponent());
        allyBotsInAction = rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared,rc.getTeam());
        allyBotsInVision = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared,rc.getTeam());
        //avoidCharge();
        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(3, rc.readSharedArray(3)+1);
        }else if(rc.getRoundNum()%3 == 0){
            globalSoldierCount = rc.readSharedArray(3);
        }
        broadcast();
        target = null;

        retreat();
        MapLocation attackTarget;
        if(shouldHeal){//Adding the if statement does make it lose one more game, but that would be stupid
            attackTarget = selectVisionTarget();
            tryAttack(attackTarget);
            if (rc.isActionReady()){
                attackTarget = selectActionTarget();
                tryAttack(attackTarget);
            }
            prevHealth = rc.getHealth();
            return;
        }
        boolean nearArchon = false;
        for(RobotInfo robot:allyBotsInVision){
            if (robot.getType()==RobotType.ARCHON){
                nearArchon = true;
                break;
            }
        }
        if(enemyBotsInAction.length >= 1){
            if (rc.getHealth()<prevHealth){
                rc.setIndicatorString("kite");
                //soldierMove(archonLoc);
                kite();
            }
            //New targetting
            attack();
            prevHealth = rc.getHealth();
            return;
        }
        if (enemyBotsInVision.length>0){
            attackTarget = selectVisionTarget();
            if (nearArchon){
                rc.setIndicatorString("near archon");
                moveTowardToLowRubble(attackTarget, true);
            }
            if (allyBotsInVision.length>=8){
                rc.setIndicatorString("move toward");
                moveTowardToLowRubble(attackTarget, false);
            }
            else{
                rc.setIndicatorString("regular move");
                moveToLowRubble();
            }
            prevHealth = rc.getHealth();
            return;
        }
        if (hasMapLocation(45)){
            MapLocation target = decode(45);
            soldierMove(target);
        }else if(hasMapLocation(43) && globalSoldierCount > 5){
            MapLocation target = decode(43);
            if (rc.canSenseLocation(target)){
                if (enemyBotsInAction.length ==0){
                    rc.writeSharedArray(43,0);
                }
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
        }else if (hasMapLocation()){
            MapLocation target = decode();
            if (rc.canSenseLocation(target)){
                if (enemyBotsInAction.length == 0){
                    rc.writeSharedArray(55,0);
                }
            }
            if(rc.isActionReady()){
                soldierMove(target);
            };
        }else if (hasMapLocation(41)){
            MapLocation target = decode(41);
            if (rc.canSenseLocation(target)){
                if (enemyBotsInAction.length == 0){
                    rc.writeSharedArray(41,0);
                }
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
        } else if(rc.readSharedArray(40) == 1) {
            if(rc.isActionReady()){
                if(Clock.getBytecodesLeft() > 6000){
                    soldierExplore();
                }else{
                    if (!tryMoveMultipleNew()) {
                        tryMoveMultiple(initDirection);
                    }
                }

            }
        }else if(rc.senseNearbyRobots(2).length>2){
            //updateDirection(myLocation.directionTo(new MapLocation(mapWidth/2,mapHeight/2)).opposite());
            //tryMoveMultiple(initDirection);
            MapLocation[] local = rc.getAllLocationsWithinRadiusSquared(myLocation,2);
            int start = (int) (local.length * Math.random());
            loop1: for (int i = start; i < start + local.length; i++) {
                int j = i % local.length;
                Direction dirTo = myLocation.directionTo(local[j]);
                if(!myLocation.equals(local[j]) && rc.canMove(dirTo)){
                    rc.move(dirTo);
                    myLocation = rc.getLocation();
                    prevLocs.add(local[j]);
                    break;
                }
            }
        }
        if(rc.isActionReady())attack();
        prevHealth = rc.getHealth();
    }

    public void retreat() throws GameActionException{
        if(rc.getHealth()>=49){
            shouldHeal=false;
            return;
        }
        if (rc.getHealth()<=18){
            shouldHeal = true;
            reachedArchon = false;
        }
        if (shouldHeal){
            if (!rc.getLocation().isWithinDistanceSquared(archonLoc, RobotType.ARCHON.actionRadiusSquared)){
                //rc.setIndicatorString("going to heal");
                soldierMove(archonLoc);
            }
            else if (rc.isMovementReady()){
                reachedArchon = true;
                if (enemyBotsInVision.length>1){
                    kite();
                }
            }
            
            
        }
    }
    public void kite() throws GameActionException{
        //rc.setIndicatorString("kite1");
        if (!rc.isMovementReady()){
            return;
        }
        int average_x=0,average_y=0;
        myLocation = rc.getLocation();
        int enemyCount = 0;
        for (RobotInfo robot:enemyBotsInVision){
            if (robot.getType()!=RobotType.SOLDIER && robot.getType()!=RobotType.SAGE){
                continue;
            }
            enemyCount++;
            MapLocation location = robot.getLocation();
            average_x+=location.x;
            average_y+=location.y;
        }
        average_x/=enemyCount;
        average_y/=enemyCount;
        Direction lowest = Direction.CENTER;
        int lowest_rubble = 99;
        MapLocation enemy = new MapLocation(average_x,average_y);
        moveAwayToLowRubble(enemy, true);
        
    }

    private MapLocation pastTarget = null;
    private HashSet<MapLocation> pastLocations = new HashSet<>();
    private void soldierMove(MapLocation target) throws GameActionException {
        if (!rc.isMovementReady()){
            return;
        }
        if(!target.equals(pastTarget)){
            pastTarget = target;
            pastLocations.clear();
        }
        Direction dir = pfs.getBestDir(target);
        MapLocation temp = myLocation;
        if(dir != null && rc.canMove(dir) && !pastLocations.contains(myLocation.add(dir))){
            if(tryMoveMultiple(dir)){
                pastLocations.add(temp);
            }
        }else{
            intermediateMove(target);
            pastLocations.add(temp);
        }
    }

    private MapLocation pastExploreTarget = null;
    private HashSet<MapLocation> pastExploreLocations = new HashSet<>();
    private void soldierExplore() throws GameActionException{
        MapLocation exploreTarget = pfs.getExploreTarget();
        if(!exploreTarget.equals(pastExploreTarget)){
            pastExploreTarget = exploreTarget;
            pastExploreLocations.clear();
        }
        Direction dir = pfs.getBestDir(exploreTarget);
        MapLocation temp = myLocation;
        if(dir != null && rc.canMove(dir) && !pastExploreLocations.contains(myLocation.add(dir))){
            if(tryMoveMultiple(dir)){
                pastExploreLocations.add(temp);
            }
        }else{
            intermediateMove(exploreTarget);
            pastExploreLocations.add(temp);
        }
    }
}