package bot6_JJ;

import battlecode.common.*;
import bot6_JJ.robots.Robot;
import bot6_JJ.robots.buildings.Archon;
import bot6_JJ.robots.buildings.Lab;
import bot6_JJ.robots.buildings.Watchtower;
import bot6_JJ.robots.droids.Builder;
import bot6_JJ.robots.droids.Miner;
import bot6_JJ.robots.droids.Sage;
import bot6_JJ.robots.droids.Soldier;

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
