package louisv10;

import battlecode.common.*;

public class Headquarters2 extends Robot {
    int carrierScore;
    int launcherScore;
    int carrierRound = 0;
    final int CARRIER_WAITING = 20;

    MapLocation closestEnemy = null;
    Headquarters2(RobotController rc){
        super(rc);
    }

    void play() {
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
        if(rc.getRoundNum() < 5){
            if(rc.getResourceAmount(ResourceType.MANA) >= 60 && constructRobotGreedy(RobotType.LAUNCHER, comm.getClosestEnemyHeadquarters())){
                comm.reportBuilt(RobotType.LAUNCHER, updateLauncherScore(launcherScore));
                return;
            }else{
                MapLocation closestAdamantium = getClosestAdamantium();
                if(rc.getResourceAmount(ResourceType.ADAMANTIUM) >= 50 && constructRobotGreedy(RobotType.CARRIER, closestAdamantium)){
                    comm.reportBuilt(RobotType.CARRIER, updateCarrierScore(carrierScore));
                    return;
                }
            }
        }
        computeClosestEnemy();
        if(closestEnemy != null){
            if(rc.getRoundNum() > 5) comm.activateDanger();
            if(rc.getResourceAmount(ResourceType.MANA) >= 45 * 5){
                int j = 5;
                while (j-- >= 0) {
                    if(!rc.isActionReady()) return;
                    constructRobotGreedy(RobotType.LAUNCHER,closestEnemy);
                }
            }
            return;
        }
        try{
            if(rc.getRoundNum() > 1500 && rc.getNumAnchors(Anchor.STANDARD) < rc.getIslandCount()){
                if (rc.getResourceAmount(ResourceType.ADAMANTIUM) >= 100 && rc.getResourceAmount(ResourceType.MANA) >= 100) {
                    rc.buildAnchor(Anchor.STANDARD);
                    //System.out.println("MADE ANCHOR");
                }
                return;
            }
        } catch(Exception e){
            e.printStackTrace();
        }

        if(closestEnemy == null || carrierScore < 0){
            if(rc.getResourceAmount(ResourceType.ADAMANTIUM) >= Constants.MIN_ADAMANTIUM_STOP_MINERS && rc.getResourceAmount(ResourceType.MANA) >= Constants.MIN_MANA_STOP_MINERS){
                if(carrierScore <= launcherScore){
                    comm.reportBuilt(RobotType.CARRIER, updateCarrierScore(carrierScore) + Util.getMinMiners());
                    return;
                }
            } else {
                if (((explore.visibleMana || explore.visibleAdamantium) && rc.getRoundNum() - carrierRound > CARRIER_WAITING) || carrierScore <= launcherScore) {
                    if (constructRobotGreedy(RobotType.CARRIER, explore.closestAdamantium)) {
                        comm.reportBuilt(RobotType.CARRIER, updateCarrierScore(carrierScore) + Util.getMinMiners());
                        carrierRound = rc.getRoundNum();
                        return;
                    }
                }
            }
        }
        if (constructRobotGreedy(RobotType.LAUNCHER, comm.getClosestEnemyHeadquarters())){
            comm.reportBuilt(RobotType.LAUNCHER, updateLauncherScore(launcherScore));
        }


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
