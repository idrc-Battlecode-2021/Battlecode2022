package bot2.util;

import battlecode.common.Direction;

public class Constants {
    public static final class Droids{
        public static final int[][] VIEWABLE_TILES_20 = {{0, 0}, {0, 1}, {0, -1}, {0, 2}, {0, -2}, {0, 3}, {0, -3}, {0, 4},
                {0, -4}, {1, 0}, {-1, 0}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 2}, {-1, -2}, {1, -2}, {-1, 2}, {1, 3},
                {-1, -3}, {1, -3}, {-1, 3}, {1, 4}, {-1, -4}, {1, -4}, {-1, 4}, {2, 0}, {-2, 0}, {2, 1}, {-2, -1}, {2, -1},
                {-2, 1}, {2, 2}, {-2, -2}, {2, -2}, {-2, 2}, {2, 3}, {-2, -3}, {2, -3}, {-2, 3}, {2, 4}, {-2, -4}, {2, -4},
                {-2, 4}, {3, 0}, {-3, 0}, {3, 1}, {-3, -1}, {3, -1}, {-3, 1}, {3, 2}, {-3, -2}, {3, -2}, {-3, 2}, {3, 3},
                {-3, -3}, {3, -3}, {-3, 3}, {4, 0}, {-4, 0}, {4, 1}, {-4, -1}, {4, -1}, {-4, 1}, {4, 2}, {-4, -2}, {4, -2},
                {-4, 2}};
        public static final int[][] OUTER_TILES_20 = {{-4, -2}, {-4, -1}, {-4, 0}, {-4, 1}, {-4, 2}, {-3, -3}, {-3, 3},
                {-2, -4}, {-2, 4}, {-1, -4}, {-1, 4}, {0, -4}, {0, 4}, {1, -4}, {1, 4}, {2, -4}, {2, 4}, {3, -3}, {3, 3},
                {4, -2}, {4, -1}, {4, 0}, {4, 1}, {4, 2}};
    }
    public static final int[][] VIEWABLE_TILES_34 = {
        {0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {1, 0}, {1, 1}, {1, 2}, {1, 3},
        {1, 4}, {1, 5}, {2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4}, {2, 5}, {3, 0}, {3, 1},
        {3, 2}, {3, 3}, {3, 4}, {3, 5}, {4, 0}, {4, 1}, {4, 2}, {4, 3}, {4, 4}, {5, 0},
        {5, 1}, {5, 2}, {5, 3}, {-1, 0}, {-1, 1}, {-1, 2}, {-1, 3}, {-1, 4}, {-1, 5},
        {-2, 0}, {-2, 1}, {-2, 2}, {-2, 3}, {-2, 4}, {-2, 5}, {-3, 0}, {-3, 1}, {-3, 2},
        {-3, 3}, {-3, 4}, {-3, 5}, {-4, 0}, {-4, 1}, {-4, 2}, {-4, 3}, {-4, 4}, {-5, 0},
        {-5, 1}, {-5, 2}, {-5, 3}, {0, -1}, {0, -2}, {0, -3}, {0, -4}, {0, -5}, {1, -1},
        {1, -2}, {1, -3}, {1, -4}, {1, -5}, {2, -1}, {2, -2}, {2, -3}, {2, -4}, {2, -5},
        {3, -1}, {3, -2}, {3, -3}, {3, -4}, {3, -5}, {4, -1}, {4, -2}, {4, -3}, {4, -4},
        {5, -1}, {5, -2}, {5, -3}, {-1, -1}, {-1, -2}, {-1, -3}, {-1, -4}, {-1, -5},
        {-2, -1}, {-2, -2}, {-2, -3}, {-2, -4}, {-2, -5}, {-3, -1}, {-3, -2}, {-3, -3},
        {-3, -4}, {-3, -5}, {-4, -1}, {-4, -2}, {-4, -3}, {-4, -4}, {-5, -1}, {-5, -2}, {-5, -3}};
    public static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST,};
    public static final Direction[] COMPOSITE_DIRECTIONS = {Direction.NORTHEAST,Direction.NORTHWEST, Direction.SOUTHWEST,
            Direction.SOUTHEAST};
    public static final Direction[] BASIC_DIRECTIONS = {Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
}
