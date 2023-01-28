package wouids.louisv3;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Robot {

    boolean explorer;

    boolean chickenBehavior = false;
    Launcher(RobotController rc){
        super(rc);
        checkExploreBehavior();
    }
    void play(){
//        if(explorer) rc.setIndicatorString("I'm an Explorer!");
        checkChickenBehavior();
        tryAttack(true);
        tryMove();
        tryAttack(false);
    }

    void checkExploreBehavior(){
        try{
            int launcherIndex = rc.readSharedArray(comm.LAUNCHER_COUNT);
            if(launcherIndex % 3 == 2) explorer = true;
            comm.increaseIndex(comm.LAUNCHER_COUNT, 1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void tryMove(){
        if(!rc.isMovementReady()) return;
        MapLocation target = getTarget();
//        if (target != null){
//            rc.setIndicatorLine(rc.getLocation(),target, 255, 0, 0);
//        }
        bfs.move(target);
    }

    MapLocation getTarget(){
//        if(rc.getRoundNum() < Constants.ATTACK_TURN && comm.isEnemyTerritoryRadial(rc.getLocation())){
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
//            if(chickenBehavior){
//                MapLocation ans = comm.getClosestAllyHeadquarter();
//                if(ans != null){
//                    int d = ans.distanceSquaredTo(rc.getLocation());
//                    if (d <= RobotType.LAUNCHER.actionRadiusSquared) return rc.getLocation();
//                    return ans;
//                }
//            }
            MoveTarget bestTarget = null;
            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), explore.myVisionRange, rc.getTeam().opponent());
            for(RobotInfo enemy: enemies){
                MoveTarget mt = new MoveTarget(enemy);
                if (mt.isBetterThan(bestTarget)) bestTarget = mt;
            }
            if (bestTarget != null)
            {
                if(bestTarget.type == RobotType.HEADQUARTERS){
                    if (bestTarget.mloc.isWithinDistanceSquared(rc.getLocation(),RobotType.HEADQUARTERS.actionRadiusSquared)) {
                        return comm.getClosestAllyHeadquarter();
                    }
                    if(bestTarget.mloc.isWithinDistanceSquared(rc.getLocation(),RobotType.HEADQUARTERS.actionRadiusSquared+25)){
                        return rc.getLocation();
                    }
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
