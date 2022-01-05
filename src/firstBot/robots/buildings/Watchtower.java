package firstBot.robots.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;

public class Watchtower extends Building{
    public Watchtower(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        
    }

//TODO: fix targetting
    @Override
    public void run() throws GameActionException {
        RobotInfo [] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        
        for(RobotInfo r : enemies){
            if (r.getType()==RobotType.SAGE){
                if (rc.canAttack(r.getLocation())){
                    rc.attack(r.getLocation());
                }
            }
        }
        if (enemies.length>0 && rc.canAttack(enemies[0].getLocation())){
            rc.attack(enemies[0].getLocation());
        }

        // Lattice Formation
        if (rc.getMode()!=RobotMode.TURRET){
            if(rc.canTransform()){
                rc.transform();
            }
        }
        RobotInfo [] range = rc.senseNearbyRobots(1, rc.getTeam());
        for (RobotInfo r: range){
            if(r.getType()==RobotType.WATCHTOWER){
                if (rc.canMove(rc.getLocation().directionTo(r.getLocation()).opposite())){
                    rc.move(rc.getLocation().directionTo(r.getLocation()).opposite());
                }
            }
        }
    }
}