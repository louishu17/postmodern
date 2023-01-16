package louis;

import battlecode.common.Direction;
import battlecode.common.RobotController;

public abstract class Micro {
    Pathfinding path;

    RobotController rc;
    static final Direction[] dirs = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };

    static final int MAX_MICRO_BYTECODE_REMAINING = 2000;

    Micro(RobotController rc){
        this.rc = rc;
    }

    abstract boolean doMicro();

    void setPathfinding(Pathfinding path){
        this.path = this.path;
    }

}
