package louis;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Carrier extends Robot{
    Carrier(RobotController rc){
        super(rc);
    }
    void play(){
        moveToTarget();
    }

    void moveToTarget(){
        if(!rc.isMovementReady()) return;
        MapLocation loc = new MapLocation(27,5);
        bfs.move(loc);
    }
}
