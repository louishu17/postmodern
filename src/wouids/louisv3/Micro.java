package wouids.louisv3;

import battlecode.common.RobotController;

public abstract class Micro {
    RobotController rc;
    static final int MAX_MICRO_BYTECODE = 4000;

    Micro(RobotController rc){
        this.rc = rc;
    }

    abstract boolean doMicro();

}
