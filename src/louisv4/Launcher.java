package louisv4;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Robot {

    boolean explorer;

    boolean chickenBehavior = false;
    Launcher(RobotController rc){
        super(rc);
        if(comm.getBuildingScore(RobotType.LAUNCHER) % 3 == 1) explorer = true;
    }
    void play(){
        checkChickenBehavior();
        tryAttack(true);
        tryMove();
        tryAttack(false);
    }

    void tryMove(){
        if(!rc.isMovementReady()) return;
        MapLocation target = getTarget();
        if (target != null){
            rc.setIndicatorLine(rc.getLocation(),target, 255, 0, 0);
        }
        bfs.move(target);
    }

    MapLocation getTarget(){
//        if(comm.isEnemyTerritoryRadial(rc.getLocation())){
//            rc.setIndicatorString("IM IN ENEMY TERRITORY RADIAL");
//            return comm.getClosestAllyHeadquarter();
//        }
        MapLocation target = getBestTarget();
        if(target != null) return target;
        if(!explorer && target == null) target = comm.getClosestEnemyHeadquarters();
        if(target != null) return target;
        return explore.getExploreTarget();
    }

    MapLocation getBestTarget(){
        try{
            if(chickenBehavior){
                MapLocation ans = comm.getClosestAllyHeadquarter();
                if(ans != null){
                    int d = ans.distanceSquaredTo(rc.getLocation());
                    if (d <= RobotType.LAUNCHER.actionRadiusSquared) return rc.getLocation();
                    return ans;
                }
            }
            MoveTarget bestTarget = null;
            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), explore.myVisionRange, rc.getTeam().opponent());
            for(RobotInfo enemy: enemies){
                MoveTarget mt = new MoveTarget(enemy);
                if (mt.isBetterThan(bestTarget)) bestTarget = mt;
            }
            if (bestTarget != null)
            {
                if(bestTarget.type == RobotType.HEADQUARTERS && bestTarget.mloc.isWithinDistanceSquared(rc.getLocation(),RobotType.HEADQUARTERS.actionRadiusSquared+25)){
                    return rc.getLocation();
                }else{
                    return bestTarget.mloc;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    void checkChickenBehavior(){
        if (!chickenBehavior && hurt()) chickenBehavior = true;
        if (chickenBehavior && rc.getHealth() >= rc.getType().getMaxHealth()) chickenBehavior = false;
    }
    boolean hurt(){
        return rc.getHealth()*3 <= rc.getType().getMaxHealth();
    }

}
