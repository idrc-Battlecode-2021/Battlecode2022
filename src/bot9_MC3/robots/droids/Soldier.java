package bot9_MC3.robots.droids;
import battlecode.common.*;
import bot9_MC3.util.PathFindingSoldier;

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
    private String indicatorString = "";

    private int targetType = 0;
    private boolean passedAttackPhase = false;

    private MapLocation closestArchon;

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
        int soldierCount = 0;
        for (RobotInfo robot:enemyBotsInVision){
            if (robot.getType()!=RobotType.SOLDIER && robot.getType()!=RobotType.SAGE && robot.getType()!=RobotType.ARCHON){
                continue;
            }
            soldierCount++;
            health+=robot.getHealth();
        }
        for (RobotInfo robot:allyBotsInAction){
            health-=robot.getHealth();
        }
        MapLocation attackTarget = selectVisionTarget();
        if (attackTarget!=rc.getLocation() && rc.canSenseRobotAtLocation(attackTarget)){
            RobotInfo robot = rc.senseRobotAtLocation(attackTarget);
            int enemyHealth = robot.getHealth();
            if((enemyHealth<13 && health<-10*soldierCount) || ((robot.getType()==RobotType.MINER || robot.getType()==RobotType.BUILDER || robot.getType()==RobotType.LABORATORY) && soldierCount==0)){ 
                if (rc.getLocation().distanceSquaredTo(attackTarget) >= RobotType.SOLDIER.actionRadiusSquared-2){
                    soldierMove(attackTarget);
                }
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
            int enemyHealth = robot.getHealth();
            if (attackTarget!=rc.getLocation()){
                if((enemyHealth<13 && health<-10*soldierCount) || ((robot.getType()==RobotType.MINER || robot.getType()==RobotType.BUILDER || robot.getType()==RobotType.LABORATORY) && soldierCount==0)){ //TODO: try buffer between health
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
        if (rc.isActionReady()){
            attackTarget = selectActionTarget();
            tryAttack(attackTarget);
        }
        
        if (rc.isMovementReady() && enemyBotsInVision.length>0){
            moveToLowRubble();
        }
        
    }

    @Override
    public void run() throws GameActionException {
        indicatorString = "";
        reassignArchon();
        setArchonLocation();
        passedAttackPhase = false;
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

        int location = rc.readSharedArray(15);
        closestArchon = new MapLocation(location%256,location/256);
        myLocation = rc.getLocation();
        for (int i=1;i<rc.getArchonCount();i++){
            switch (i){
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
            MapLocation thisArchon = new MapLocation(x,y);
            if (movementTileDistance(myLocation,thisArchon)<movementTileDistance(myLocation,closestArchon)){
                closestArchon = thisArchon;
            }
        }
        archonLocs = getArchonLocs();
        setHealStatus();
        MapLocation attackTarget;
        // Prioritize running away and healing if health<18
        if (rc.getHealth()<=18){
            /* Stay alive method
            if (enemyBotsInVision.length>0 && rc.getLocation().distanceSquaredTo(archonLoc)>4){
                kite();
            }  
            else */
            
            if (!rc.getLocation().isWithinDistanceSquared(archonLoc, RobotType.ARCHON.actionRadiusSquared)){
                soldierMove(archonLoc);
            }
            else if (enemyBotsInVision.length>0 && rc.isMovementReady()){
                if (rc.getLocation().isWithinDistanceSquared(archonLoc, 2)){
                    kite();
                }
                else{
                    soldierMove(archonLoc);
                }
            }
            if (rc.isActionReady()){
                attackTarget = selectActionTarget();
                tryAttack(attackTarget);
            }
            prevHealth = rc.getHealth();
            return;
        }
        // defend if archon is being attacked
        if (enemyBotsInVision.length>0){
            //TODO: prioritize soldier attacking archon?
            attackTarget = selectVisionTarget();
            if (attackTarget.isWithinDistanceSquared(closestArchon, RobotType.ARCHON.visionRadiusSquared) && rc.getLocation().distanceSquaredTo(attackTarget) >= RobotType.SOLDIER.actionRadiusSquared-2){
                rc.setIndicatorString(indicatorString+" defend");
                moveTowardToLowRubble(attackTarget, false);
                attack();
                prevHealth = rc.getHealth();
                return;
            }
        }
        // go to heal if it needs to
        if (shouldHeal){
            if (!rc.getLocation().isWithinDistanceSquared(closestArchon, RobotType.ARCHON.actionRadiusSquared)){
                soldierMove(closestArchon);
            }
            if (rc.isActionReady()){
                attackTarget = selectActionTarget();
                tryAttack(attackTarget);
            }
            prevHealth = rc.getHealth();
            return;
        }
        // attack
        int health = 0;
        int soldierCount = 0;
        for (RobotInfo robot:enemyBotsInVision){
            if (robot.getType()!=RobotType.SOLDIER && robot.getType()!=RobotType.SAGE && robot.getType()!=RobotType.ARCHON){
                continue;
            }
            soldierCount++;
            health+=robot.getHealth();
        }
        for (RobotInfo robot:allyBotsInAction){
            health-=robot.getHealth();
        }
        if(enemyBotsInAction.length >= 1){
            attackTarget = selectActionTarget();
            int enemyHealth = rc.senseRobotAtLocation(attackTarget).getHealth();
            // defend 
            if (attackTarget.isWithinDistanceSquared(closestArchon, RobotType.ARCHON.visionRadiusSquared) && rc.getLocation().distanceSquaredTo(attackTarget) >= RobotType.SOLDIER.actionRadiusSquared-2){
                rc.setIndicatorString(indicatorString+" defend");
                moveTowardToLowRubble(attackTarget, false);
            }
            else if (rc.getHealth()<prevHealth && !(enemyHealth<13 && health<-10*soldierCount)){
                rc.setIndicatorString(indicatorString+" kite");
                if (rc.getLocation().isWithinDistanceSquared(closestArchon, RobotType.ARCHON.actionRadiusSquared)){
                    kite();
                }
                else{
                    soldierMove(archonLoc);
                }
                //kite();
            }
            //New targetting
            attack();
            prevHealth = rc.getHealth();
            return;
        }
        if (enemyBotsInVision.length>0){
            //TODO: prioritize soldier attacking archon?
            attackTarget = selectVisionTarget();
            int enemyHealth = rc.senseRobotAtLocation(attackTarget).getHealth();
            indicatorString+=" vision target: "+attackTarget;
            //defend
            if (attackTarget.isWithinDistanceSquared(closestArchon, RobotType.ARCHON.visionRadiusSquared) && rc.getLocation().distanceSquaredTo(attackTarget) >= RobotType.SOLDIER.actionRadiusSquared-2){
                rc.setIndicatorString(indicatorString+" defend");
                moveTowardToLowRubble(attackTarget, false);
            }
            attack();
            prevHealth = rc.getHealth();
            return;
        }
        passedAttackPhase = true;
        if (hasMapLocation(45)){
            MapLocation target/*temp*/ = decode(45);
            /*if(target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)){
                target = temp;
            }*/
            soldierMove(target);
        }else if(hasMapLocation(43) && globalSoldierCount > 5){
            MapLocation temp = decode(43);
            if((target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)) && targetType <= 2){
                target = temp;
                targetType = 2;
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                if (enemyBotsInVision.length ==0){
                    if(target.equals(temp)) rc.writeSharedArray(43,0);
                    target = null;
                    targetType = 0;
                }
            }
        }else if (hasMapLocation()){
            MapLocation temp = decode();
            if((target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target))&& targetType <= 1){
                target = temp;
                targetType = 1;
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                if (enemyBotsInVision.length == 0){
                    if(target.equals(temp))rc.writeSharedArray(55,0);
                    target = null;
                    targetType = 0;
                }
            }
        }else if (hasMapLocation(41)){
            MapLocation temp = decode(41);
            if((target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)) && targetType <= 0){
                target = temp;
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                if (enemyBotsInVision.length == 0){
                    if(target.equals(temp)) rc.writeSharedArray(41,0);
                    target = null;
                    targetType = 0;
                }
            }
        } else if(target != null){
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

    public void setHealStatus() throws GameActionException{
        if(rc.getHealth()>=49){
            shouldHeal=false;
            return;
        }
        if (rc.getHealth()<=18){
            shouldHeal = true;
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
        for (RobotInfo robot:enemyBotsInAction){
            if (robot.getType()!=RobotType.SOLDIER && robot.getType()!=RobotType.SAGE && robot.getType()!=RobotType.ARCHON){
                continue;
            }
            enemyCount++;
            MapLocation location = robot.getLocation();
            average_x+=location.x;
            average_y+=location.y;
        }
        if (enemyCount==0){
            soldierMove(archonLoc);
            return;
        }
        average_x/=enemyCount;
        average_y/=enemyCount;
        MapLocation enemy = new MapLocation(average_x,average_y);
        moveAwayToLowRubble(enemy, false);
        
    }

    private MapLocation pastTarget = null;
    private HashSet<MapLocation> pastLocations = new HashSet<>();
    private void soldierMove(MapLocation target) throws GameActionException {
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