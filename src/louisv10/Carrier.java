package louisv10;

import battlecode.common.*;

import java.util.HashSet;

public class Carrier extends Robot {

    int actionRadius;

    HashSet<Integer> oldAdWells = new HashSet<>();
    HashSet<Integer> newAdWells = new HashSet<>();
    HashSet<Integer> oldManaWells = new HashSet<>();
    HashSet<Integer> newManaWells = new HashSet<>();

    ResourceType[] resourceTypes = {ResourceType.ADAMANTIUM, ResourceType.MANA};

    int myID;

    boolean goingForMana;

    Carrier(RobotController rc) throws GameActionException{
        super(rc);
        actionRadius = rc.getType().actionRadiusSquared;
        myID = rc.getID();
        checkResourceBehavior();
    }

    void checkResourceBehavior(){
        try{
            int carrierIndex = rc.readSharedArray(comm.CARRIER_COUNT);
            if(carrierIndex % 2 == 1) {
                goingForMana = true;
            }
            else{
                goingForMana = false;
                explore.setChecker(carrierIndex/2);
            }
            comm.increaseIndex(comm.CARRIER_COUNT, 1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    void play(){
//        if(goingForMana)
//        {
//            rc.setIndicatorString("MANA COLLECTOR");
//        }else{
//            rc.setIndicatorString("ADAMANTIUM COLLECTOR");
//        }

        if(rc.getRoundNum() <= 2000) { //well queue only actively expanding in first 60 rounds
            memoryWells();
        }
        if(getTotalResources() > 0){
            tryAttack(true);
        }
        tryCollectAnchor();
        tryDepositAnchor();
        tryCollectResource();
        tryDepositResource();
        moveToTarget();
        if(getTotalResources() > 0){
            tryAttack(false);
        }
        tryCollectAnchor();
        tryDepositAnchor();
        tryCollectResource();
        tryDepositResource();
    }

    void memoryWells(){
        try {
            rememberWells();
            if(rc.canWriteSharedArray(20,20) && newAdWells.size() != 0 && newManaWells.size() != 0) { //everytime it gets within writing distance of the headquarters
                Integer[] adWells = newAdWells.toArray(new Integer[newAdWells.size()]);
                comm.reportAdamantium(adWells);
                Integer[] manaWells = newManaWells.toArray(new Integer[newManaWells.size()]);
                comm.reportMana(manaWells);
                for(Integer adWell: newAdWells) {
                    oldAdWells.add(adWell);
                }
                for(Integer manaWell: newManaWells) {
                    oldManaWells.add(manaWell);
                }
                newAdWells.clear();
                oldAdWells.clear();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void moveToTarget(){
        MapLocation loc = getTarget();
        if(checkIfNextToWell(loc)){
            return;
        }
        rc.setIndicatorLine(rc.getLocation(),loc,0,0,255);
        int i = 2;
        while(i-- >= 0){
            bfs.move(loc);
        }
    }

    MapLocation getTarget(){
        int totalResources = getTotalResources();
        MapLocation loc = null;
        try{
            if(rc.getAnchor() != null){
                loc = explore.getClosestFreeIsland();
                if(loc == null) return explore.getExploreTarget(false);
            }else{
                if(totalResources == GameConstants.CARRIER_CAPACITY){
                    loc = comm.getClosestAllyHeadquarter();
                    //rc.setIndicatorString("HAVE A HQ IN MIND: " + loc);
                }
                else
                {
                    if(goingForMana) {
                        loc = getClosestMana();
                    } else {
                        loc = getClosestAdamantium();
                    }
                    /*loc = getClosestMana();
                    if (loc == null) loc = getClosestAdamantium();*/
//                    if (loc == null) loc = explore.getClosestEnemyOccupiedIsland();

                    if (loc == null){
                        return explore.getExploreTarget(false);
                    }else{
                        //rc.setIndicatorString("HAVE A WELL IN MIND: " + loc);
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return loc;
    }

    boolean checkIfNextToWell(MapLocation target){
        MapLocation myLoc = rc.getLocation();
        for(Direction dir: directions){
            MapLocation newLoc = myLoc.add(dir);
            if(newLoc.equals(target)){
                return true;
            }
        }
        return false;
    }

    void tryCollectAnchor(){
        if(!rc.isActionReady()) return;
        try{
            if(rc.getAnchor() != null) return;
            for(Direction d: directions){
                MapLocation newLoc = rc.getLocation().add(d);
                if(!rc.onTheMap(newLoc)) continue;
                if(rc.canTakeAnchor(newLoc,Anchor.STANDARD)){
                    rc.takeAnchor(newLoc,Anchor.STANDARD);
                    return;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void tryDepositAnchor(){
        if(!rc.isActionReady()) return;
        try{
            if(rc.getAnchor() == null) return;
            if(rc.canPlaceAnchor()) rc.placeAnchor();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    void tryCollectResource(){
        if(!rc.isActionReady()) return;
        try{
            WellInfo[] wells = rc.senseNearbyWells(actionRadius);
            for (WellInfo well: wells){
                int totalResources = getTotalResources();
                MapLocation wellLoc = well.getMapLocation();
                if(totalResources == GameConstants.CARRIER_CAPACITY){
                    break;
                }
                while(rc.canCollectResource(wellLoc,-1)){
                    rc.collectResource(wellLoc, -1);
                    if(totalResources == GameConstants.CARRIER_CAPACITY){
                        break;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void tryDepositResource(){
        if(!rc.isActionReady()) return;
        try{
            for(ResourceType r: resourceTypes){
                int amount = rc.getResourceAmount(r);
                if(amount == 0) continue;
                RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam());
                for (RobotInfo ally : allies) {
                    if (! (ally.getType() == RobotType.HEADQUARTERS )) continue;
                    MapLocation newLoc = ally.getLocation();
                    if(!rc.onTheMap(newLoc)) continue;
                    if(rc.canTransferResource(newLoc,r,amount)){
                        rc.transferResource(newLoc,r, amount);
                        break;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    int getTotalResources(){
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA);
    }

    /**
     * Stores an active set of new wells seen.
     * Already seen wells are not added.
     * @throws GameActionException
     */
    void rememberWells() throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        for(WellInfo well : wells) {
            Integer codeLoc = Util.encodeLoc(well.getMapLocation());
            if(well.getResourceType() == ResourceType.ADAMANTIUM && !oldAdWells.contains(codeLoc)) {
                newAdWells.add(codeLoc);
            }
            if(well.getResourceType() == ResourceType.MANA && !oldManaWells.contains(codeLoc)) {
                newManaWells.add(codeLoc);
            }
        }
    }
}
