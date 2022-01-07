package agrobot;

import battlecode.common.*;
import agrobot.robots.Robot;
import agrobot.robots.buildings.Archon;
import agrobot.robots.buildings.Lab;
import agrobot.robots.buildings.Watchtower;
import agrobot.robots.droids.Builder;
import agrobot.robots.droids.Miner;
import agrobot.robots.droids.Sage;
import agrobot.robots.droids.Soldier;

import java.util.Random;

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
