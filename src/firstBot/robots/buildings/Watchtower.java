package firstBot.robots.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;

public class Watchtower extends Building{
    private MapLocation archon;
    public Watchtower(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        
    }

    @Override
    public void run() throws GameActionException {
        RobotInfo [] enemies = rc.senseNearbyRobots();
        
        if(enemies.length !=0){
            if (rc.canAttack(enemies[0].getLocation())){
                rc.attack(enemies[0].getLocation());
            }
        }
    }
}
