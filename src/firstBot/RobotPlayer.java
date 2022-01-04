package firstBot;

import battlecode.common.*;
import firstBot.robots.Robot;
import firstBot.robots.buildings.Archon;
import firstBot.robots.buildings.Lab;
import firstBot.robots.buildings.Watchtower;
import firstBot.robots.droids.Builder;
import firstBot.robots.droids.Miner;
import firstBot.robots.droids.Sage;
import firstBot.robots.droids.Soldier;

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
