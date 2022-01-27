package bot10_MC4.robots.droids;
import battlecode.common.*;
import bot10_MC4.util.PathFinding30Lesser;

import java.util.HashSet;

public class Sage extends Droid{
    private MapLocation target = null;

    private MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    private int globalSageCount = 0;
    private boolean shouldHeal = false;
    private MapLocation[] archonLocs;
    private MapLocation centralArchon;
    private PathFinding30Lesser pfs;
    private boolean reachedArchon;
    private boolean addedToHeal = false;
    public Sage(RobotController rc) {super(rc);}
    private int targetType = 0;
    //0 = tier 1
    //1 = tier 2
    //2 = tier 3

    @Override
    public void init() throws GameActionException {
        pfs=new PathFinding30Lesser(rc);
        //possibleArchonLocs();
        parseAnomalies();
        detectArchon();
    }
    public void setCentralArchon() throws GameActionException {
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
        setCentralArchon();
        // update shared array
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(2, rc.readSharedArray(2)+1);
        }else if(rc.getRoundNum()%3 == 0){
            globalSageCount = rc.readSharedArray(2);
        }
        broadcast();
        //target = null;
        int healCheck = rc.readSharedArray(31+myArchonOrder);
        if(healCheck < 18 || addedToHeal){
            //rc.setIndicatorString("heal");
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
        RobotInfo[] nearbyBots = rc.senseNearbyRobots(25,myTeam.opponent());
        if(nearbyBots.length >= 1){
            //New targetting
            if (!rc.isActionReady()){
                //rc.setIndicatorString("action retreat");
                if (!myLocation.isWithinDistanceSquared(archonLoc, 9)){
                    soldierMove(archonLoc);
                }
                else{
                    kite();
                }
            }
            else{
                target = selectTargetKill();
            }
            return;
        }
        if(target != null && hasMapLocation(35)){
            if(decode(35).equals(target)){
                target = null;
                targetType = 0;
            }
        }
        if (hasMapLocation(45)){
            //rc.setIndicatorString("map loc 45");
            MapLocation temp = decode(45);
            /*if(target == null || myLocation.distanceSquaredTo(temp)<=myLocation.distanceSquaredTo(target)){
                target = temp;
            }*/
            soldierMove(temp);
            if (rc.isActionReady()){
                target = selectTargetKill();
            }
            return;
        }
        RobotInfo[] enemyRobotsInVision = rc.senseNearbyRobots(34, rc.getTeam().opponent());
        RobotInfo[] allyRobotsInVision = rc.senseNearbyRobots(34, rc.getTeam());
        if (enemyRobotsInVision.length>0 && !rc.isActionReady()){
            //rc.setIndicatorString("vision retreat");
            if (!rc.getLocation().isWithinDistanceSquared(archonLoc, 9)){
                soldierMove(archonLoc);
            }
            else{
                kite();
            }
        }
        else if (allyRobotsInVision.length < enemyRobotsInVision.length){
            //rc.setIndicatorString("vision retreat");
            if (!rc.getLocation().isWithinDistanceSquared(archonLoc, 9)){
                soldierMove(archonLoc);
            }
            else{
                kite();
            }
        }
        else if(hasMapLocation(43) && globalSageCount > 5){
            //rc.setIndicatorString("43");
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
                    rc.writeSharedArray(35,64*target.x+target.y);
                    target = null;
                    targetType = 0;
                }
            }
        }else if (hasMapLocation()){
            //rc.setIndicatorString("has map loc");
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
                    rc.writeSharedArray(35,64*target.x+target.y);
                    target = null;
                    targetType = 0;
                }
            }
        }else if (hasMapLocation(41)){
            //rc.setIndicatorString("map loc 41");
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
                    rc.writeSharedArray(35,64*target.x+target.y);
                    target = null;
                    targetType = 0;
                }
            }
        } else if(target != null){
            //rc.setIndicatorString("no target");
            if(rc.isActionReady()){
                soldierMove(target);
            }
            if (rc.canSenseLocation(target)){
                nearbyBots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared,myTeam.opponent());
                if (nearbyBots.length == 0){
                    rc.writeSharedArray(35,64*target.x+target.y);
                    target = null;
                    targetType = 0;
                }
            }
        } else if(rc.readSharedArray(40) == 1) {
            //rc.setIndicatorString("40 == 1");
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
            //rc.setIndicatorString("accompany miner");
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
            //rc.setIndicatorString("action ready kill");
            MapLocation temp = selectTargetKill();
            if(temp != null && !temp.equals(myLocation) /*&& rc.canSenseRobotAtLocation(temp)*/){
                target = temp;
            }
        }
    }

    public void retreat() throws GameActionException{
        if(rc.getHealth()>=rc.getType().health-1){
            shouldHeal=false;
            return;
        }
        if (rc.getHealth()<=26){
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
        moveToLowRubble();
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        RobotInfo[] myRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        RobotInfo archon=null, sage=null, lab=null, watchtower=null, soldier=null, miner=null, builder=null;
        MapLocation target = rc.getLocation();
        int chargePotentialKills = 0;
        boolean offensive = false;
        boolean containsBuilding = false;
        int chargePotentialDamage = 0;
        int furyPotentialKills = 0;
        int furyPotentialDamage = 0;
        for (RobotInfo robot:myRobots){
            if (robot.getMode()==RobotMode.TURRET){
                containsBuilding = true;
                break;
            }
        }
        //TODO: doesn't account for soldier damage if we're spawning soldiers
        for (RobotInfo enemy : enemyRobots) {
            if (!offensive && enemy.getType()==RobotType.SAGE || enemy.getType()==RobotType.SOLDIER || enemy.getType()==RobotType.WATCHTOWER){
                offensive = true;
            }
            if (!enemy.getLocation().isWithinDistanceSquared(rc.getLocation(), RobotType.SAGE.actionRadiusSquared)){
                continue;
            }
            RobotType type = enemy.getType();
            int charge = 22;
            int fury = 10;
            switch (type){
                //TODO: calculate fury damage for buildings
                case ARCHON:
                    if (archon == null || (enemy.getHealth() > archon.getHealth() && enemy.getHealth()<=RobotType.SAGE.damage) || enemy.getHealth()<archon.getHealth() && archon.getHealth()>RobotType.SAGE.damage) {
                        archon = enemy;
                    }
                    if (!containsBuilding && enemy.getMode()==RobotMode.TURRET){
                        furyPotentialDamage+=Math.min(enemy.getHealth(),enemy.getType().health/10);
                        if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+fury>=100) furyPotentialKills++;
                    }
                    break;
                case SAGE:
                    chargePotentialDamage+=Math.min(enemy.getHealth(),22*enemy.getType().health/100);
                    if (sage == null || (enemy.getHealth() > sage.getHealth() && enemy.getHealth()<=RobotType.SAGE.damage) || enemy.getHealth()<sage.getHealth() && sage.getHealth()>RobotType.SAGE.damage) {
                        sage = enemy;
                    }
                    /*
                    for (RobotInfo ally: myRobots){
                        if (ally.getType()!=RobotType.SAGE) continue;
                        if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) charge+=22;
                    }
                    */
                    if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+charge>=100) chargePotentialKills++;
                    break;
                case LABORATORY:
                    if (!containsBuilding && enemy.getMode()==RobotMode.TURRET){
                        furyPotentialDamage+=Math.min(enemy.getHealth(),enemy.getType().health/10);
                        /*
                        for (RobotInfo ally: myRobots){
                            if (ally.getType()!=RobotType.SAGE) continue;
                            if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) fury+=10;
                        }
                        */
                        if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+fury>=100) furyPotentialKills++;
                    }
                    if (lab == null || (enemy.getHealth() > lab.getHealth() && enemy.getHealth()<=RobotType.SAGE.damage) || enemy.getHealth()<lab.getHealth() && lab.getHealth()>RobotType.SAGE.damage) {
                        lab = enemy;
                    }
                    break;
                case WATCHTOWER:
                    if (!containsBuilding && enemy.getMode()==RobotMode.TURRET){
                        furyPotentialDamage+=Math.min(enemy.getHealth(),enemy.getType().health/10);
                        /*
                        for (RobotInfo ally: myRobots){
                            if (ally.getType()!=RobotType.SAGE) continue;
                            if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) fury+=10;
                        }
                        */
                        if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+fury>=100) furyPotentialKills++;
                    }
                    if (watchtower == null || (enemy.getHealth() > watchtower.getHealth() && enemy.getHealth()<=RobotType.SAGE.damage) || enemy.getHealth()<watchtower.getHealth() && watchtower.getHealth()>RobotType.SAGE.damage) {
                        watchtower = enemy;
                    }
                    break;
                case SOLDIER:
                    //TODO: target soldier based on distance away?
                    chargePotentialDamage+=Math.min(enemy.getHealth(),22*enemy.getType().health/100);
                    if (soldier == null || (enemy.getHealth() > soldier.getHealth() && enemy.getHealth()<=RobotType.SAGE.damage) || enemy.getHealth()<soldier.getHealth() && soldier.getHealth()>RobotType.SAGE.damage) {
                        soldier = enemy;
                    }
                    /*
                    for (RobotInfo ally: myRobots){
                        if (ally.getType()!=RobotType.SAGE) continue;
                        if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) charge+=22;
                    }
                    */
                    if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+charge>=100) chargePotentialKills++;
                    break;
                case MINER:
                    chargePotentialDamage+=Math.min(enemy.getHealth(),22*enemy.getType().health/100);
                    /*
                    for (RobotInfo ally: myRobots){
                        if (ally.getType()!=RobotType.SAGE) continue;
                        if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) charge+=22;
                    }
                    */
                    if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+charge>=100) chargePotentialKills++;
                    if (miner == null || enemy.getHealth()>miner.getHealth()) miner = enemy;
                    break;
                case BUILDER:
                    chargePotentialDamage+=Math.min(enemy.getHealth(),22*enemy.getType().health/100);
                    /*
                    for (RobotInfo ally: myRobots){
                        if (ally.getType()!=RobotType.SAGE) continue;
                        if (ally.getLocation().distanceSquaredTo(enemy.getLocation())<=ally.getType().actionRadiusSquared) charge+=22;
                    }
                    */
                    if ((enemy.getType().health-enemy.getHealth())*100/enemy.getType().health+charge>=100) chargePotentialKills++;
                    if (builder == null || enemy.getHealth()>builder.getHealth()) builder = enemy;
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
        else if (offensive){ //if an offensive troop is within vision, save action to kill that
            target = null;
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

        if (chargePotentialKills>0 && rc.canEnvision(AnomalyType.CHARGE)){
            rc.envision(AnomalyType.CHARGE);
        }
        else if (chargePotentialDamage>45 && rc.canEnvision(AnomalyType.CHARGE)){
            rc.envision(AnomalyType.CHARGE);
        }
        else if (!containsBuilding && furyPotentialKills>0 && rc.canEnvision(AnomalyType.FURY)){
            rc.envision(AnomalyType.FURY);
        }
        else if (!containsBuilding && furyPotentialDamage>45 && rc.canEnvision(AnomalyType.FURY)){
            rc.envision(AnomalyType.FURY);
        }
        else if(!containsBuilding && archon!=null && archon.getMode()==RobotMode.TURRET && target.equals(archon.getLocation()) && rc.canEnvision(AnomalyType.FURY)){
            rc.envision(AnomalyType.FURY);
        }
        else{
            tryAttack(target);
        }
        /*if (Clock.getBytecodeNum()-bytecode>4000){
            System.out.println("Sage targetting takes "+(Clock.getBytecodeNum()-bytecode)+" bytecode, check for overflow");
        }*/
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

    public void kite() throws GameActionException{
        if (!rc.isMovementReady()){
            return;
        }
        int average_x=0,average_y=0;
        myLocation = rc.getLocation();
        int enemyCount = 0;
        RobotInfo[] enemyBotsInVision = rc.senseNearbyRobots(RobotType.SAGE.visionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo robot:enemyBotsInVision){
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
        Direction lowest = Direction.CENTER;
        int lowest_rubble = rc.senseRubble(rc.getLocation()); 
        //int lowest_rubble = 99; 
        for (Direction d: Direction.allDirections()){
            MapLocation adjacent=rc.adjacentLocation(d);
            if(adjacent.distanceSquaredTo(enemy) < myLocation.distanceSquaredTo(enemy))continue;
            if(rc.canMove(d)){
                int rubbleAtLoc = rc.senseRubble(adjacent);
                if(rubbleAtLoc < lowest_rubble || rubbleAtLoc == lowest_rubble && adjacent.distanceSquaredTo(enemy) > myLocation.distanceSquaredTo(enemy)){
                    lowest = d;
                    lowest_rubble = rubbleAtLoc;
                }
            }
        }
        if(rc.canMove(lowest)){
            rc.move(lowest);
            myLocation = rc.getLocation();
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