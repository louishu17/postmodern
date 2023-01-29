package wouisv7;

import battlecode.common.*;

public class Headquarters2 extends Robot {
    int launcherScore;
    int carrierScore;

    MapLocation closestEnemy = null;
    static int minSoldierScore = 100;

    boolean builtAmplifier = false;

    Headquarters2(RobotController rc){
        super(rc);
        if (minSoldierScore > Util.getMinCarriers()) minSoldierScore = Util.getMinCarriers();
    }

    void play() {
        computeClosestEnemy();
        builtAmplifier = false;
        int i = 5;
        try {
            while(i-- >= 0){
                buildUnit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void buildUnit() throws GameActionException{
        if(!rc.isActionReady()) return;

        if(closestEnemy != null){
//            if(rc.getRoundNum() > 5) comm.activateDanger();
            if(rc.getResourceAmount(ResourceType.MANA) >= 45 * 5){
                int j = 5;
                while (j-- >= 0) {
                    if(!rc.isActionReady()) return;
                    constructRobotGreedy(RobotType.LAUNCHER,closestEnemy);
                }
            }
            return;
        }

//        launcherScore = Math.max(comm.getBuildingScore(RobotType.LAUNCHER), minSoldierScore);
//        carrierScore = comm.getBuildingScore(RobotType.CARRIER);

        if(tryBuildAnchor()) return;
        if(tryBuildAmplifier()) return;
        if(tryBuildLauncher()) return;
        if(tryBuildCarrier()) return;
    }

    boolean tryBuildAnchor(){
        try {
            if (rc.getNumAnchors(Anchor.STANDARD) < rc.getIslandCount()) {
                if (rc.getRoundNum() > 1500 && rc.getResourceAmount(ResourceType.ADAMANTIUM) >= 80 && rc.getResourceAmount(ResourceType.MANA) >= 80) {
                    rc.buildAnchor(Anchor.STANDARD);
                    //System.out.println("MADE ANCHOR");
                    return true;
                }
            }
            return false;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    boolean tryBuildLauncher(){
        if(rc.getResourceAmount(ResourceType.MANA) < 45){
            return false;
        }
        if(comm.curEnemyHQTarget == null){
            comm.getClosestEnemyHeadquarters();
        }
        if (constructRobotGreedy(RobotType.LAUNCHER, comm.curEnemyHQTarget)){
//            comm.reportBuilt(RobotType.LAUNCHER, updateLauncherScore(launcherScore));
            return true;
        }
        return false;
    }

    boolean tryBuildCarrier(){
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) < 50){
            return false;
        }
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) >= Constants.MIN_ADAMANTIUM_STOP_MINERS && rc.getResourceAmount(ResourceType.MANA) >= Constants.MIN_MANA_STOP_MINERS){
//            if(carrierScore <= launcherScore){
//                comm.reportBuilt(RobotType.CARRIER, updateCarrierScore(carrierScore));
//            }
            return false;
        }

//        if (carrierScore > launcherScore) return false;

        MapLocation closestWellTarget = explore.getClosestMana();
        if(closestWellTarget == null){
            closestWellTarget = explore.getClosestAdamantium();
        }

        if (constructRobotGreedy(RobotType.CARRIER, closestWellTarget)) {
//            comm.reportBuilt(RobotType.CARRIER, updateCarrierScore(carrierScore));
            return true;
        }

        return false;
    }

    boolean tryBuildAmplifier(){
        if(builtAmplifier) return false;
        if (rc.getRoundNum() < 5 || rc.getResourceAmount(ResourceType.MANA) < 30 || rc.getResourceAmount(ResourceType.ADAMANTIUM) < 80) return false;

        if (constructRobotGreedy(RobotType.AMPLIFIER, null)) {
            builtAmplifier = true;
//            comm.reportBuilt(RobotType.BUILDER, updateBuilderScore(builderScore));
            return true;
        }
        return false;
    }


    boolean constructRobotGreedy(RobotType t){
        return constructRobotGreedy(t, null);
    }

    boolean constructRobotGreedy(RobotType t, MapLocation target){
        try {
            MapLocation myLoc = rc.getLocation();
            MapLocation bestLoc = null;
            int leastEstimation = 0;
            MapLocation[] mapLocations = rc.getAllLocationsWithinRadiusSquared(myLoc,rc.getType().actionRadiusSquared);
            for(MapLocation m: mapLocations){
                if (!rc.canBuildRobot(t,m)) continue;
                int e = 1000000;
                if (target != null) e = m.distanceSquaredTo(target);
                if (bestLoc == null || e < leastEstimation){
                    leastEstimation = e;
                    bestLoc = m;
                }
            }
            if (bestLoc != null){
                if (rc.canBuildRobot(t, bestLoc)) rc.buildRobot(t, bestLoc);
                return true;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    void computeClosestEnemy(){
        MapLocation myLoc = rc.getLocation();
        closestEnemy = null;
        int closestDist = 0;
        try{
            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), rc.getType().visionRadiusSquared, rc.getTeam().opponent());
            for (RobotInfo enemy : enemies){
                if (!Util.isAttacker(enemy.getType())) continue;
                int d = enemy.getLocation().distanceSquaredTo(myLoc);
                if (closestEnemy == null || d < closestDist){
                    closestEnemy = enemy.location;
                    closestDist = d;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    int updateLauncherScore(int oldScore){
        if (oldScore < Util.getMinCarriers()){
            if (oldScore + 3 > Util.getMinCarriers()) return Util.getMinCarriers();
            return oldScore + 3;
        }
        return oldScore + 1;
    }

    int updateCarrierScore(int oldScore){
        if (oldScore <= 0) return oldScore + 1;
        return oldScore +3;
    }

    int updateAmplifierScore(int oldScore){
        return oldScore+1;
    }

}
