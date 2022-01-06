package firstBot.robots.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;
import battlecode.common.MapLocation;

import java.util.Map;

public class Watchtower extends Building {
    private boolean isDefensive = false;
    private MapLocation archon = null;
    private MapLocation latticeCenter = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
    private boolean inPosition = false;
    private MapLocation[] latticePositions = new MapLocation[14];

    public Watchtower(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        int[] dx = {0, 6, -6, 12, -12, 18, -18};
        int[] dy = {3, -3};
        int counter = -1;
        for (int i : dx) {
            counter = counter + 1;
            for (int j : dy) {
                latticePositions[counter] = latticeCenter.translate(i, j);
            }
        }
    }

    //TODO: fix targeting
    @Override
    public void run() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : enemies) {
            if (r.getType() == RobotType.SAGE) {
                if (rc.canAttack(r.getLocation())) {
                    rc.attack(r.getLocation());
                }
            }
        }
        if (enemies.length > 0 && rc.canAttack(enemies[0].getLocation())) {
            rc.attack(enemies[0].getLocation());
        }
        int counter = -1;
        for (MapLocation j : latticePositions) {
            counter = counter + 1;
            if (rc.getLocation() == j) {
                inPosition = true;
                rc.writeSharedArray(56, rc.readSharedArray(56) + (int) Math.pow(2, counter));
            }
            // Lattice Formation
            // 7 x 2 formation
            // 0 - (0,3)
            // 1 - (6,3)
            // 2 - (-6,3)
            // 3 - (12,3)
            // 4 - (-12,3)
            // 5 - (-18,3)
            // 6 - (18,3)
            // 7 - row 2 (x, -3)

            if (isDefensive) {
                if (archon == null) {
                    for (RobotInfo r : rc.senseNearbyRobots()) {
                        if (r.getType() == RobotType.ARCHON && r.getTeam() == rc.getTeam()) {
                            archon = r.getLocation();
                        }
                    }
                } else {
                    if (rc.getLocation().isWithinDistanceSquared(archon, 20)) {
                        if (rc.getMode() == RobotMode.PORTABLE && rc.canTransform()) {
                            rc.transform();
                        }
                    } else intermediateMove(archon);
                }
            } else {
                if (rc.getMode() == RobotMode.TURRET) {
                    if (rc.canTransform()) {
                        rc.transform();
                    }
                }
                if (inPosition) {
                    if (rc.getMode() != RobotMode.TURRET) {
                        if (rc.canTransform()) {
                            rc.transform();
                        }
                    }
                }
                if (!inPosition) {
                    int locations = rc.readSharedArray(56);
                    String bstring = Integer.toBinaryString(locations);
                    bstring = "00000000000000".substring(bstring.length()) + bstring;
                    int[] enc = new int[bstring.length()];
                    for (int i = 0; i < bstring.length(); i++) {
                        enc[i] = Character.getNumericValue(bstring.charAt(i));
                    }
                    MapLocation target = null;
                    for (int i = 0; i < enc.length; i++) {
                        if (enc[i] == 0) {
                            target = latticePositions[i];
                        }
                    }
                    if (target != null) intermediateMove(target);
                    int counter1 = -1;
                    for (MapLocation ii : latticePositions) {
                        counter1 = counter1 + 1;
                        if (rc.getLocation() == ii) {
                            inPosition = true;
                            rc.writeSharedArray(56, rc.readSharedArray(56) + (int) Math.pow(2, counter1));
                        }
                    }
                }
            }
        }
    }
}