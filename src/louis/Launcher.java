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
        tryAttack();
        tryMove();
        tryAttack();
    }

    void tryAttack(){
        if(!rc.isActionReady()) return;
        try{
            RobotInfo[] enemies = rc.senseNearbyRobots(explore.myVisionRange, rc.getTeam().opponent());
            AttackTarget bestTarget = null;
            for(RobotInfo enemy: enemies){
                if(rc.canAttack(enemy.location)){
                    AttackTarget at = new AttackTarget(enemy);
                    if(at.isBetterThan(bestTarget)) bestTarget = at;
                }
            }
            if(bestTarget != null && rc.canAttack(bestTarget.mloc)) rc.attack(bestTarget.mloc);
        }catch(Exception e){
            e.printStackTrace();
        }
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


    class AttackTarget{
        RobotType type;
        int health;
        boolean attacker = false;
        MapLocation mloc;

        boolean isBetterThan(AttackTarget t){
            if (t == null) return true;
            if (attacker & !t.attacker) return true;
            if (!attacker & t.attacker) return false;
            return health <= t.health;
        }

        AttackTarget(RobotInfo r){
            type = r.getType();
            health = r.getHealth();
            mloc = r.getLocation();
            switch(type){
                case HEADQUARTERS:
                    attacker = true;
                default:
                    break;
            }
        }
    }

    class MoveTarget{
        RobotType type;
        int health;
        int priority;
        MapLocation mloc;

        boolean isBetterThan(MoveTarget t){
            if(priority <= 1) return false;
            if (t == null) return true;
            if (t.priority <= 1) return true;
            if(priority > t.priority) return true;
            if(priority < t.priority) return true;
            return health <= t.health;
        }

        MoveTarget(RobotInfo r){
            this.type = r.getType();
            this.health = r.getHealth();
            this.mloc = r.getLocation();
            switch(r.getType()){
                case HEADQUARTERS:
                    priority = 6;
                    break;
                case LAUNCHER:
                    priority = 5;
                    break;
                case CARRIER:
                    priority = 4;
                    break;
                case DESTABILIZER:
                    priority = 3;
                    break;
                case AMPLIFIER:
                    priority = 2;
                    break;
                case BOOSTER:
                    priority = 1;
                    break;
            }
        }
    }
}
