package louis;

import battlecode.common.*;

public class BFS {
    Pathfinding path;
    Micro micro;
    static RobotController rc;

    static final Direction[] directions = {
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
        if(rc.getType() == RobotType.CARRIER && checkIfNextToWell(target)) return;
        if(rc.getLocation().distanceSquaredTo(target) == 0) return;
        path.move(target);
    }

    boolean checkIfNextToWell(MapLocation target){
        MapLocation myLoc = rc.getLocation();
        for(Direction dir: directions){
            MapLocation newLoc = myLoc.add(dir);
            if(newLoc.equals(target)){
                return true;
            }
        }
        return false;
    }
}
