package firstBot.robots.droids;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import firstBot.robots.Robot;

public abstract class Droid extends Robot {
    protected Direction initDirection;

    public Droid(RobotController rc) {
        super(rc);
    }

    public void explore(){

    }
}
