package firstBot.util;

import battlecode.common.Direction;

public class Constants {
    public static int[][] VIEWABLE_TILES_20 = {{-4, -2}, {-4, -1}, {-4, 0}, {-4, 1}, {-4, 2}, {-3, -3}, {-3, -2},
            {-3, -1}, {-3, 0}, {-3, 1}, {-3, 2}, {-3, 3}, {-2, -4}, {-2, -3}, {-2, -2}, {-2, -1}, {-2, 0}, {-2, 1},
            {-2, 2}, {-2, 3}, {-2, 4}, {-1, -4}, {-1, -3}, {-1, -2}, {-1, -1}, {-1, 0}, {-1, 1}, {-1, 2}, {-1, 3},
            {-1, 4}, {0, -4}, {0, -3}, {0, -2}, {0, -1}, {0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}, {1, -4}, {1, -3},
            {1, -2}, {1, -1}, {1, 0}, {1, 1}, {1, 2}, {1, 3}, {1, 4}, {2, -4}, {2, -3}, {2, -2}, {2, -1}, {2, 0},
            {2, 1}, {2, 2}, {2, 3}, {2, 4}, {3, -3}, {3, -2}, {3, -1}, {3, 0}, {3, 1}, {3, 2}, {3, 3}, {4, -2},
            {4, -1}, {4, 0}, {4, 1}, {4, 2}};
    public static Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST,};
    public static int[][] DIRECTION_OFFSETS = {{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1}};
}
