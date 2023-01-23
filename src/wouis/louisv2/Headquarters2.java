package wouis.louisv2;

import battlecode.common.*;

public class Headquarters2 extends Robot {
    int carrierScore;
    int launcherScore;
    int amplifierScore;
    int carrierRound = 0;
    final int CARRIER_WAITING = 20;

    MapLocation closestEnemy = null;
    Headquarters2(RobotController rc){
        super(rc);
    }

    void play() {
        if(rc.getRoundNum() < 5) {
            try{
                reportResources();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            if(rc.getResourceAmount(ResourceType.MANA) >= 30 && constructRobotGreedy(RobotType.LAUNCHER, comm.getClosestEnemyHeadquarters())){
                comm.reportBuilt(RobotType.LAUNCHER, updateLauncherScore(launcherScore));
            }else{
                if(constructRobotGreedy(RobotType.CARRIER,explore.closestAdamantium)){
                    comm.reportBuilt(RobotType.CARRIER, updateCarrierScore(carrierScore));
                }
            }
        }
        computeClosestEnemy();
        if(closestEnemy != null){
            if(rc.getRoundNum() > 5) comm.activateDanger();
            constructRobotGreedy(RobotType.LAUNCHER,closestEnemy);
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
//                    rc.writeSharedArray(40,rc.readSharedArray(40) + 1); //index in the array that stores carriers made
                    return;
                }
            } else {
                if (((explore.visibleMana || explore.visibleAdamantium) && rc.getRoundNum() - carrierRound > CARRIER_WAITING) || carrierScore <= launcherScore) {
                    if (constructRobotGreedy(RobotType.CARRIER, explore.closestAdamantium)) {
                        comm.reportBuilt(RobotType.CARRIER, updateCarrierScore(carrierScore) + Util.getMinMiners());
                        carrierRound = rc.getRoundNum();
//                        rc.writeSharedArray(40,rc.readSharedArray(40) + 1);
                        return;
                    }
                }
            }
        }
        if (constructRobotGreedy(RobotType.LAUNCHER, comm.getClosestEnemyHeadquarters())){
            comm.reportBuilt(RobotType.LAUNCHER, updateLauncherScore(launcherScore));
        }


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

    int updateAmplifierScore(int oldScore){
        return oldScore+1;
    }

}
