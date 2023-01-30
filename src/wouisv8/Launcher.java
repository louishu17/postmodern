package wouisv8;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Robot {

    boolean explorer;

    boolean chickenBehavior = false;

    boolean touchedCenter = false;
    MapLocation center;

    boolean found = false;

    Launcher(RobotController rc){
        super(rc);
        checkExploreBehavior();
        center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    }
    void play(){
        found = false;
        rc.setIndicatorString("ENEMY HQ TARGET: "+ comm.curEnemyHQTarget);
//        if(explorer) rc.setIndicatorString("I'm an Explorer!");
        checkChickenBehavior();
        tryAttack(true);
//        checkIfTouchedCenter();
        tryMove();
        tryAttack(false);
    }

    void checkIfTouchedCenter(){
        if(rc.getLocation().isWithinDistanceSquared(center,5)) touchedCenter = true;
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
        if (target != null){
            rc.setIndicatorLine(rc.getLocation(),target, 255, 0, 0);
        }
        bfs.move(target);
    }

    MapLocation getTarget(){
//        if(rc.getRoundNum() < Constants.ATTACK_TURN && comm.isEnemyTerritoryRadial(rc.getLocation())){
//            rc.setIndicatorString("IM IN ENEMY TERRITORY RADIAL");
//            return comm.getClosestAllyHeadquarter();
//        }
        checkIfNeedNewEnemyHQTarget();
        MapLocation target = getBestTarget();
        if(target != null) return target;
        if(target == null){
            target = explore.getClosestEnemyOccupiedIsland();
        }
        if(target == null){
//            if(touchedCenter){
            if(comm.curEnemyHQTarget == null){
                comm.getClosestEnemyHeadquarters();
            }
            target = comm.curEnemyHQTarget;
//            }else{
//                target = center;
//            }
        }
        if(target != null) return target;
        return explore.getExploreTarget(true);
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
            MapLocation closestEnemyHQ = null;
            int dist = 10000;
            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), explore.myVisionRange, rc.getTeam().opponent());
            for(RobotInfo enemy: enemies){
                if(enemy.getType() != RobotType.HEADQUARTERS) found = true;
                if(enemy.getType() == RobotType.HEADQUARTERS){
                    if(rc.getLocation().distanceSquaredTo(enemy.getLocation()) < dist){
                        closestEnemyHQ = enemy.getLocation();
                        dist = rc.getLocation().distanceSquaredTo(enemy.getLocation());
                    }
                }
                MoveTarget mt = new MoveTarget(enemy);
                if (mt.isBetterThan(bestTarget)) bestTarget = mt;
            }
            if(closestEnemyHQ != null){
                if(closestEnemyHQ.isWithinDistanceSquared(rc.getLocation(),RobotType.HEADQUARTERS.actionRadiusSquared)){
                    return comm.getClosestAllyHeadquarter();
                }
                if(closestEnemyHQ.isWithinDistanceSquared(rc.getLocation(),RobotType.HEADQUARTERS.actionRadiusSquared+25)){
                    return rc.getLocation();
                }
            }
            if (bestTarget != null)
            {
                if(bestTarget.type == RobotType.HEADQUARTERS){
                    if (bestTarget.mloc.isWithinDistanceSquared(rc.getLocation(),RobotType.HEADQUARTERS.actionRadiusSquared)) {
                        return comm.getClosestAllyHeadquarter();
                    }
                    if(bestTarget.mloc.isWithinDistanceSquared(rc.getLocation(),RobotType.HEADQUARTERS.actionRadiusSquared+5)){
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

    void checkIfNeedNewEnemyHQTarget(){
        if(comm.curEnemyHQTarget != null && comm.curEnemyHQTarget.isWithinDistanceSquared(rc.getLocation(),RobotType.LAUNCHER.visionRadiusSquared+5) && !found){
            comm.getClosestEnemyHeadquarters();
        }
    }

    void checkChickenBehavior(){
        if (!chickenBehavior && hurt()) chickenBehavior = true;
        if (chickenBehavior && rc.getHealth() >= rc.getType().getMaxHealth()) chickenBehavior = false;
    }
    boolean hurt(){
        return rc.getHealth()*3 <= rc.getType().getMaxHealth();
    }

}
