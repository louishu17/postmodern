package wouis.louisv2;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class BFS {
    Pathfinding path;
    Micro micro;
    static RobotController rc;

    BFS(RobotController rc){
        this.rc = rc;
        this.path = new Pathfinding(rc);
        if(Util.isAttacker(rc.getType())){
            this.micro = new MicroAttackers(rc);
        } else{
            this.micro = new MicroCarriers(rc);
        }
    }

    void move(MapLocation target){
        if (target == null) return;
        if (!rc.isMovementReady()) return;
        if(micro.doMicro()) return;
        if(rc.getType() == RobotType.CARRIER )
        if(rc.getLocation().distanceSquaredTo(target) == 0) return;
        path.move(target);
    }


}
