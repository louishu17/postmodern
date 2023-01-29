package wouis.louisv5.louisv4;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Amplifier extends Robot {
    Amplifier(RobotController rc){
        super(rc);
    }
    void play(){
        moveToTarget();
    }

    void moveToTarget(){
        if(!rc.isMovementReady()) return;
        rc.setIndicatorString("Trying to move");
        MapLocation loc = getTarget();
        if (loc != null) rc.setIndicatorString("Target not null!: " + loc.toString());
        bfs.move(loc);
    }

    MapLocation getTarget(){
        MapLocation loc = comm.getClosestEnemyHeadquarters();
        if(loc == null) loc = explore.getExploreTarget();
        return loc;
    }
}
