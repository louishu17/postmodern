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
        MapLocation target = getTarget();
        bfs.move(target);
    }

    MapLocation getTarget(){
        if(rc.getRoundNum() < Constants.ATTACK_TURN && comm.isEnemyTerritoryRadial(rc.getLocation())) return comm.getClosestAllyHeadquarter();
        MapLocation ans = getBestTarget();
        if(ans != null) return ans;
        ans = comm.getClosestEnemyHeadquarters();
        if(ans != null) return ans;
        return explore.getExploreTarget();
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
