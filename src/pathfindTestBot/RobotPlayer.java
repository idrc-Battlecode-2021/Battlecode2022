package pathfindTestBot;

import battlecode.common.*;
import pathfindTestBot.robots.Robot;
import pathfindTestBot.robots.buildings.Archon;
import pathfindTestBot.robots.buildings.Lab;
import pathfindTestBot.robots.buildings.Watchtower;
import pathfindTestBot.robots.droids.Builder;
import pathfindTestBot.robots.droids.Miner;
import pathfindTestBot.robots.droids.Sage;
import pathfindTestBot.robots.droids.Soldier;

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
