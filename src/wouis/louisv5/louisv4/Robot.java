package wouis.louisv5.louisv4;

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
        //comm.reportSelf();
        if(rc.getType() == RobotType.HEADQUARTERS && rc.getRoundNum() < 10) {
            try{
                reportResources();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                return;
            }
            if(rc.getType() == RobotType.LAUNCHER && bestTarget == null){
                MapLocation[] cloudLocs = rc.senseNearbyCloudLocations(rc.getType().actionRadiusSquared);
                if(cloudLocs.length > 0){
                    rc.attack(cloudLocs[0]);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    MapLocation getClosestAdamantium() throws GameActionException{
        MapLocation ans = explore.getClosestAdamantium();
        if(ans == null) ans = comm.getClosestAdamantium();
//        System.out.println("ADAMANTIUM: " + ans);
        return ans;
    }
    MapLocation getClosestMana() throws GameActionException{
        MapLocation ans = explore.getClosestMana();
        if(ans == null) ans = comm.getClosestMana();
//        System.out.println("MANA: " + ans);
        return ans;
    }

    void reportResources() throws GameActionException{
        WellInfo[] adWells = rc.senseNearbyWells(ResourceType.ADAMANTIUM);
        WellInfo[] manaWells = rc.senseNearbyWells(ResourceType.MANA);
        Integer[] adWellLocs = new Integer[adWells.length];
        Integer[] manaWellLocs = new Integer[manaWells.length];

        int i = 0;
        for(WellInfo adWell: adWells) {
            adWellLocs[i] = Util.encodeLoc(adWell.getMapLocation());
            i++;
        }
        int j = 0;
        for(WellInfo manaWell: manaWells) {
            manaWellLocs[j] = Util.encodeLoc(manaWell.getMapLocation());
            j++;
        }
        comm.reportAdamantium(adWellLocs);
        comm.reportMana(manaWellLocs);
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
