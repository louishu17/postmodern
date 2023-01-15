package louis;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Robot{
    Launcher(RobotController rc){
        super(rc);
    }
    void play(){
        tryAttack(true);
        tryMove();
        tryAttack(false);
    }

    void tryMove(){
        if(!rc.isMovementReady()) return;
        MapLocation target = getBestTarget();
        if (target == null) target = comm.getClosestEnemyHeadquarters();
        if (target == null) target = explore.getExploreTarget();
        bfs.move(target);
    }

    MapLocation getBestTarget(){
        try{
            MoveTarget bestTarget = null;
            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), explore.myVisionRange, rc.getTeam().opponent());
            for(RobotInfo enemy: enemies){
                MoveTarget mt = new MoveTarget(enemy);
                if (mt.isBetterThan(bestTarget)) bestTarget = mt;
            }
            if (bestTarget != null) return bestTarget.mloc;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
