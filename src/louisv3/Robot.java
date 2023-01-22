package louisv3;

import battlecode.common.*;

public abstract class Robot {
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

    static RobotController rc;
    static BFS bfs;
    static Explore explore;
    static Communication comm;

    static boolean isCarrier = false;


    public Robot(RobotController rc){
        this.rc = rc;
        bfs = new BFS(rc);
        explore = new Explore(rc);
        comm = new Communication(rc);
        if(rc.getType() == RobotType.CARRIER) isCarrier = true;
    }

    abstract void play();
    void initTurn(){
        comm.reportSelf();
        if(isCarrier) explore.reportResources();
    }
    void endTurn(){
        if(!isCarrier) explore.reportResources();
        explore.reportIslands();
        explore.reportUnits();
    }

    void moveRandom(){
        try {
            int d = (int) (Math.random() * 8.0);
            Direction dir = directions[d];
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(dir)) rc.move(dir);
                dir = dir.rotateLeft();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    void tryAttack(boolean onlyAttackers){
        if(!rc.isActionReady()) return;
        try {
            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
            AttackTarget bestTarget = null;
            for (RobotInfo enemy : enemies) {
                if(enemy.getType() == RobotType.HEADQUARTERS) continue;
                if (onlyAttackers && !Util.isAttacker(enemy.getType())) continue;
                if (rc.canAttack(enemy.location)) {
                    AttackTarget at = new AttackTarget(enemy);
                    if (at.isBetterThan(bestTarget)) bestTarget = at;
                }
            }
            if (bestTarget != null && rc.canAttack(bestTarget.mloc)) {
                rc.attack(bestTarget.mloc);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    boolean constructRobotGreedy(RobotType t){
        return constructRobotGreedy(t, null);
    }

    boolean constructRobotGreedy(RobotType t, MapLocation target){
        try {
            MapLocation myLoc = rc.getLocation();
            Direction bestDir = null;
            int leastEstimation = 0;
            for (Direction d : directions) {
                if (!rc.canBuildRobot(t,myLoc.add(d))) continue;
                int e = 1000000;
                if (target != null) e = myLoc.add(d).distanceSquaredTo(target);
                if (bestDir == null || e < leastEstimation){
                    leastEstimation = e;
                    bestDir = d;
                }
            }
            if (bestDir != null){
                if (rc.canBuildRobot(t, myLoc.add(bestDir))) rc.buildRobot(t, myLoc.add(bestDir));
                return true;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
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
