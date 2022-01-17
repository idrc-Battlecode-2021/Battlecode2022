package bot3_MC;

import battlecode.common.*;
import bot3_MC.robots.Robot;
import bot3_MC.robots.buildings.Archon;
import bot3_MC.robots.buildings.Lab;
import bot3_MC.robots.buildings.Watchtower;
import bot3_MC.robots.droids.Builder;
import bot3_MC.robots.droids.Miner;
import bot3_MC.robots.droids.Sage;
import bot3_MC.robots.droids.Soldier;
import bot3_MC.robots.droids.Droid;

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
