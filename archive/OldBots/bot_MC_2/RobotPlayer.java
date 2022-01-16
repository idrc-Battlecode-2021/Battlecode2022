package bot_MC_2;

import battlecode.common.*;
import bot_MC_2.robots.Robot;
import bot_MC_2.robots.buildings.Archon;
import bot_MC_2.robots.buildings.Lab;
import bot_MC_2.robots.buildings.Watchtower;
import bot_MC_2.robots.droids.Builder;
import bot_MC_2.robots.droids.Miner;
import bot_MC_2.robots.droids.Sage;
import bot_MC_2.robots.droids.Soldier;

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
            robot.run();
            Clock.yield();
        }
    }
}
