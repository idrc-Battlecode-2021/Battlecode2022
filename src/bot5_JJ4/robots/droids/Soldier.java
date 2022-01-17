package bot5_JJ4.robots.droids;
import battlecode.common.*;
import bot5_JJ4.util.PathFindingSoldier;

public class Soldier extends Droid{
    private MapLocation target;
    private MapLocation archonLoc;
    private MapLocation [] corners = new MapLocation[4];
    private MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    private int globalSoldierCount = 0;
    private boolean defensive = false;
    private boolean reachedLocation = false,shouldHeal = false;
    private PathFindingSoldier pfs;
    public Soldier(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        readArchonLocs();
        possibleArchonLocs();
        parseAnomalies();
        RobotInfo [] r = rc.senseNearbyRobots(2,myTeam);
        for (RobotInfo ro : r){
            if(ro.getTeam()==myTeam && ro.getType()==RobotType.ARCHON){
                archonLoc = ro.getLocation();
            }
        }
        corners[0]=new MapLocation (0,0);
        corners[1]=new MapLocation(0,rc.getMapHeight());
        corners[2]=new MapLocation(rc.getMapWidth(),0);
        corners[3]=new MapLocation(rc.getMapWidth(),rc.getMapHeight());
        defensive = isDefensive();
        pfs = new PathFindingSoldier(rc);
    }

    @Override
    public void run() throws GameActionException {
        Direction dir;
        if(hasMapLocation(43)){
            MapLocation target = decode(43);
            if(rc.canSenseLocation(target)){
                RobotInfo[] a = rc.senseNearbyRobots(20,myTeam.opponent());
                if(a.length == 0)rc.writeSharedArray(43,0);
            }
            dir = pfs.getBestDir(target);
            selectPriorityTarget();
        }else{
            dir = pfs.getBestDir(new MapLocation(40,20));
        }
        tryMoveMultiple(dir);
        rc.setIndicatorString(dir+"");
        if(rc.getRoundNum() == 700)rc.resign();
    }
    public boolean isDefensive() throws GameActionException{
        RobotInfo [] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam.opponent());
        RobotInfo [] friends = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam);
        boolean justSpawned = false;
        for (RobotInfo r: friends){
            if(r.getType()==RobotType.ARCHON){
                justSpawned = true;
            }
        }
        if (enemies.length>0 && justSpawned){
            return true;
        }

        return false;
    }
    public void retreat() throws GameActionException{
        if(rc.getHealth()>42){
            shouldHeal=false;
            rc.writeSharedArray(31+myArchonOrder,0);
            return;
        }
        if(rc.getHealth()>15)return;
        rc.writeSharedArray(31+myArchonOrder,rc.getID());
        shouldHeal=true;
        intermediateMove(archonLoc);
    }

}