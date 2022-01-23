package bot10;

import battlecode.common.*;
import bot10.robots.Robot;
import bot10.robots.buildings.Archon;
import bot10.robots.buildings.Lab;
import bot10.robots.buildings.Watchtower;
import bot10.robots.droids.Builder;
import bot10.robots.droids.Miner;
import bot10.robots.droids.Sage;
import bot10.robots.droids.Soldier;

public strictfp class RobotPlayer {
    static RobotController rc;
    static Robot robot;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        switch (rc.getType()) {
            case ARCHON: robot = new Archon(rc); break;
            case MINER: robot = new Miner(rc); break;
            case SOLDIER: robot = new Soldier(rc); break;
            case LABORATORY: robot = new Lab(rc); break;
            case WATCHTOWER: robot = new Watchtower(rc); break;
            case BUILDER: robot = new Builder(rc); break;
            case SAGE: robot = new Sage(rc); break;
        }
        robot.init();
        while (true) {
            try{
                robot.run();
            }catch (Exception e){
                e.printStackTrace();
            }
            Clock.yield();
        }
    }
}
