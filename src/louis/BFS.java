package louis;

import battlecode.common.*;

public class BFS {
    Pathfinding path;
    Micro micro;
    static RobotController rc;

    BFS(RobotController rc){
        this.rc = rc;
        this.path = new Pathfinding(rc);
        this.micro = new Micro(rc);
    }
    void initTurn(){
        path.initTurn();
    }

    void move(MapLocation target){
        if (target == null) return;
        if (!rc.isMovementReady()) return;
        if(micro.doMicro()) return;
        if(rc.getLocation().distanceSquaredTo(target) == 0) return;

        path.move(target);
    }

    void move(Direction dir){
        try{
            if(!rc.canMove(dir)){
                return;
            }
            rc.move(dir);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
