package louis;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Amplifier extends Robot{
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
        MapLocation loc = null;
        try{
            loc = comm.getClosestEnemyHeadquarters();
            if(loc == null) loc = explore.getExploreTarget();
        }catch(Exception e){
            e.printStackTrace();
        }
        return loc;
    }
}
