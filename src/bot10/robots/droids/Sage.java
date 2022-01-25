package bot10.robots.droids;
import battlecode.common.*;
import bot10.util.PathFinding30;

import java.util.HashSet;

public class Sage extends Droid{
    private MapLocation target = null;
    private MapLocation archonLoc;
    private MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    private int globalSageCount = 0;
    private boolean shouldHeal = false;
    private MapLocation[] archonLocs;
    private MapLocation centralArchon;
    private PathFinding30 pfs;
    private boolean reachedArchon;
    private boolean addedToHeal = false;
    public Sage(RobotController rc) {super(rc);}
    private int targetType = 0;
    //0 = tier 1
    //1 = tier 2
    //2 = tier 3

    @Override
    public void init() throws GameActionException {
        pfs=new PathFinding30(rc);
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

    @Override
    public void run() throws GameActionException {
        reassignArchon();
        setArchonLocation();
        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(2, rc.readSharedArray(2)+1);
        }else if(rc.getRoundNum()%3 == 0){
            globalSageCount = rc.readSharedArray(2);
        }
        broadcast();
        //target = null;

        int healCheck = rc.readSharedArray(31+myArchonOrder);
        if(healCheck < 24 || addedToHeal){
            rc.setIndicatorString("heal");
            retreat();
            if(shouldHeal){
                if(!addedToHeal){
                    rc.writeSharedArray(31+myArchonOrder,healCheck+1);
                    addedToHeal = true;
                }
                selectTargetKill();
                return;
            }
        }
        if(addedToHeal){
            rc.writeSharedArray(31+myArchonOrder,healCheck-1);
            addedToHeal = false;
       }
        RobotInfo[] nearbyBots = rc.senseNearbyRobots(RobotType.SAGE.actionRadiusSquared,rc.getTeam().opponent());
        if(nearbyBots.length >= 1){
            rc.setIndicatorString(archonLoc+" action radius");
            //New targetting
            if (!rc.isActionReady()){
                soldierMove(archonLoc);
            }
            else{
                target = selectTargetKill();
            }
            return;
        }
        //TODO: Try to reduce times in which units go to previous targets if the enemy there is eliminated (if other stuff is done)
        if (hasMapLocation(45)){
            rc.setIndicatorString("map loc 45");
            MapLocation target/*temp*/ = decode(45);
            /*if(target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)){
                target = temp;
            }*/
            soldierMove(target);
        }else if (rc.senseNearbyRobots(RobotType.SAGE.visionRadiusSquared, rc.getTeam().opponent()).length>0 && !rc.isActionReady()){
            rc.setIndicatorString("vision retreat");
            soldierMove(archonLoc);
        }else if(hasMapLocation(43) && globalSageCount > 5){
            rc.setIndicatorString("43");
            MapLocation temp = decode(43);
            if((target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)) && targetType <= 2){
                target = temp;
                targetType = 2;
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                nearbyBots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared,myTeam.opponent());
                if (nearbyBots.length ==0){
                    if(target.equals(temp)) rc.writeSharedArray(43,0);
                    target = null;
                    targetType = 0;
                }
            }
        }else if (hasMapLocation()){
            rc.setIndicatorString("has map loc");
            MapLocation temp = decode();
            if((target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target))&& targetType <= 1){
                target = temp;
                targetType = 1;
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                nearbyBots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared,myTeam.opponent());
                if (nearbyBots.length == 0){
                    if(target.equals(temp))rc.writeSharedArray(55,0);
                    target = null;
                    targetType = 0;
                }
            }
        }else if (hasMapLocation(41)){
            rc.setIndicatorString("map loc 41");
            MapLocation temp = decode(41);
            if((target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)) && targetType <= 0){
                target = temp;
            }
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                nearbyBots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared,myTeam.opponent());
                if (nearbyBots.length == 0){
                    if(target.equals(temp)) rc.writeSharedArray(41,0);
                    target = null;
                    targetType = 0;
                }
            }
        } else if(target != null){
            rc.setIndicatorString("no target");
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if(rc.canSenseLocation(target)){
                nearbyBots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared,myTeam.opponent());
                if (nearbyBots.length == 0){
                    target = null;
                    targetType = 0;
                }
            }
        } else if(rc.readSharedArray(40) == 1) {
            rc.setIndicatorString("40 == 1");
            if(rc.isActionReady()){
                if(Clock.getBytecodesLeft() > 6000){
                    soldierExplore();
                }else{
                    if (!tryMoveMultipleNew()) {
                        tryMoveMultiple(initDirection);
                    }
                }

            }
        }else{
            rc.setIndicatorString("accompany miner");
            RobotInfo[] nearbyTeam = rc.senseNearbyRobots(20,myTeam);
            MapLocation miner = new MapLocation(10000,10000);
            for(int i = nearbyTeam.length; --i>=0;){
                if(nearbyTeam[i].getType() == RobotType.MINER){
                    if(nearbyTeam[i].location.distanceSquaredTo(centralArchon) < miner.distanceSquaredTo(centralArchon)){ //Try it with center, mylocation, centralArchon, archonLoc
                        miner = nearbyTeam[i].location;
                    }
                }
            }
            if(miner.equals(new MapLocation(10000,10000)) && rc.senseNearbyRobots(2).length>2){
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
            }else{
                soldierMove(miner);
            }
        }
        if(rc.isActionReady()){
            rc.setIndicatorString("action ready kill");
            MapLocation temp = selectTargetKill();
            if(temp != null && !temp.equals(myLocation) /*&& rc.canSenseRobotAtLocation(temp)*/){
                /*switch (rc.senseRobotAtLocation(temp).getType()){
                    case ARCHON:
                        targetType = 2;
                        target = temp;
                        break;
                    case SOLDIER:
                    case WATCHTOWER:
                    case SAGE:
                        if(targetType < 2){
                            targetType = 1;
                            target = temp;
                        }break;
                    case BUILDER:
                    case MINER:
                    case LABORATORY:
                        if(targetType < 1){
                            target = temp;
                        }break;
                }*/
                target = temp;
            }
        }
        else if(target != null){
            rc.setIndicatorString("target retreat "+target.toString());
            MapLocation targetRetreat = myLocation;
            for(int i = directions.length; --i>=0;){
                MapLocation adjacent = rc.adjacentLocation(directions[i]);
                if(adjacent.distanceSquaredTo(target) >= myLocation.distanceSquaredTo(target) && rc.canMove(directions[i])){
                    if(targetRetreat == null || rc.senseRubble(adjacent) <= rc.senseRubble(targetRetreat)){
                        targetRetreat = adjacent;
                    }
                }
            }
            if(targetRetreat != null || !targetRetreat.equals(myLocation)) intermediateMove(targetRetreat);
        }
        
    }

    public void retreat() throws GameActionException{
        if(rc.getHealth()>=rc.getType().health-1){
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
            else{
                reachedArchon = true;

            }
            
        }
    }

    public MapLocation selectTargetKill() throws GameActionException{
        //TODO: experiment with action based on damage dealt or troops killed
        //TODO: experiment with abyss
        moveToLowRubble();
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        RobotInfo[] myRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        RobotInfo archon=null, sage=null, lab=null, watchtower=null, soldier=null, miner=null, builder=null;
        int[] damages = {0,0,0,0,0}; //order corresponds with order of variables above
        MapLocation target = rc.getLocation();
        int chargePotential = 0;
        //TODO: doesn't account for soldier damage if we're spawning soldiers
        for (RobotInfo enemy : enemyRobots) {
            RobotType type = enemy.getType();
            int charge = 22;
            switch (type){
                //TODO: calculate fury damage for buildings
                case ARCHON:
                    if (archon == null || archon.getHealth() > enemy.getHealth()) {
                        archon = enemy;
                        damages[0]=rc.getType().getDamage(rc.getLevel());
                        for (RobotInfo robot: myRobots){
                            if (robot.getLocation().distanceSquaredTo(enemy.getLocation())<=robot.getType().actionRadiusSquared){
                                int cooldown = 1+rc.senseRubble(robot.getLocation())/10;
                                damages[0]+=(robot.getType().getDamage(robot.getLevel()))/cooldown;
                            }
                        }
                    }
                    break;
                case SAGE:
                    //TODO: target sage based on distance away/greater health within one shot range
                    if (sage == null || sage.getHealth() > enemy.getHealth()) {
                        sage = enemy;
                    }
                    for (RobotInfo ally: myRobots){
                        if (ally.getType()!=RobotType.SAGE) continue;
                        if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) charge+=22;
                    }
                    if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+charge>=100) chargePotential++;
                    break;
                //TODO: calculate fury damage for lab and watchtower
                case LABORATORY:
                    if (lab == null || lab.getHealth() > enemy.getHealth()) {
                        lab = enemy;
                        damages[2]=rc.getType().getDamage(rc.getLevel());
                        for (RobotInfo robot: myRobots){
                            if (robot.getLocation().distanceSquaredTo(enemy.getLocation())<=robot.getType().actionRadiusSquared){
                                int cooldown = 1+rc.senseRubble(robot.getLocation())/10;
                                damages[2]+=robot.getType().getDamage(robot.getLevel())/cooldown;
                            }
                        }
                    }
                    break;
                case WATCHTOWER:
                    if (watchtower == null || watchtower.getHealth() > enemy.getHealth()) {
                        watchtower = enemy;
                        damages[3]=rc.getType().getDamage(rc.getLevel());
                        for (RobotInfo robot: myRobots){
                            if (robot.getLocation().distanceSquaredTo(enemy.getLocation())<=robot.getType().actionRadiusSquared){
                                int cooldown = 1+rc.senseRubble(robot.getLocation())/10;
                                damages[3]+=robot.getType().getDamage(robot.getLevel())/cooldown;
                            }
                        }
                    }
                    break;
                case SOLDIER:
                    //TODO: target soldier based on distance away/greater health within one shot range
                    if (soldier == null || soldier.getHealth() > enemy.getHealth()) {
                        soldier = enemy;
                    }
                    for (RobotInfo ally: myRobots){
                        if (ally.getType()!=RobotType.SAGE) continue;
                        if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) charge+=22;
                    }
                    if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+charge>=100) chargePotential++;
                    break;
                case MINER:
                    for (RobotInfo ally: myRobots){
                        if (ally.getType()!=RobotType.SAGE) continue;
                        if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) charge+=22;
                    }
                    if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+charge>=100) chargePotential++;
                    //TODO: target miner based on distance away/greater health since sages can one shot
                    if (miner == null) miner = enemy;
                    break;
                case BUILDER:
                    for (RobotInfo ally: myRobots){
                        if (ally.getType()!=RobotType.SAGE) continue;
                        if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) charge+=22;
                    }
                    if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+charge>=100) chargePotential++;
                    //TODO: target builder based on distance away/greater health since sages can one shot
                    if (builder == null) builder = enemy;
                    break;
            }
        }
        //TODO: change based on turns to kill
        if (sage!=null){
            target = sage.getLocation();
        }
        else if (lab!=null){
            target = lab.getLocation();
        }
        else if (soldier!=null){
            target = soldier.getLocation();
        }
        else if (watchtower!=null){
            target = watchtower.getLocation();
        }
        else if (miner!=null){
            target = miner.getLocation();
        }
        else if (builder!=null){
            target = builder.getLocation();
        }
        else if (archon!=null){
            target = archon.getLocation();
        }
        if (target==null) target = rc.getLocation();

        if (chargePotential>1 && rc.canEnvision(AnomalyType.CHARGE)){
            rc.envision(AnomalyType.CHARGE);
        }
        else if(archon!=null && target.equals(archon.getLocation()) && rc.canEnvision(AnomalyType.FURY)){
            rc.envision(AnomalyType.FURY);
        }
        else{
            tryAttack(target);
        }
        return target;
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