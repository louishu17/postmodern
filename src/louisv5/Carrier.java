package louisv5;

import battlecode.common.*;

public class Carrier extends Robot {

    int actionRadius;

    ResourceType[] resourceTypes = {ResourceType.ADAMANTIUM, ResourceType.MANA};

    int myID;

    Carrier(RobotController rc){
        super(rc);
        actionRadius = rc.getType().actionRadiusSquared;
        myID = rc.getID();
    }
    void play(){
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

    void moveToTarget(){
        MapLocation loc = getTarget();
        int i=2;
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
                if(loc == null) return explore.getExploreTarget();
            }else{
                if(totalResources == GameConstants.CARRIER_CAPACITY){
                    loc = explore.getClosestMyHeadquarters();
                }
                else
                {
                    loc = getClosestMana();
                    if (loc == null) loc = getClosestAdamantium();
//                    if (loc == null) loc = explore.getClosestEnemyOccupiedIsland();
                    if (loc == null) return explore.getExploreTarget();
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return loc;
    }

    MapLocation getClosestAdamantium(){
        MapLocation ans = explore.getClosestAdamantium();
        if(ans == null) ans = comm.getClosestAdamantium();
        return ans;
    }
    MapLocation getClosestMana(){
        MapLocation ans = explore.getClosestMana();
        if(ans == null) ans = comm.getClosestMana();
        return ans;
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
                int totalResouces = getTotalResources();
                MapLocation wellLoc = well.getMapLocation();
                if(totalResouces == GameConstants.CARRIER_CAPACITY){
                    break;
                }
                while(rc.canCollectResource(wellLoc,-1)){
                    rc.collectResource(wellLoc, -1);
                    if(totalResouces == GameConstants.CARRIER_CAPACITY){
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

    boolean checkIfNextToWell(MapLocation target){
        MapLocation myLoc = rc.getLocation();
        for(Direction dir: Robot.directions){
            MapLocation newLoc = myLoc.add(dir);
            if(newLoc.equals(target)){
                return true;
            }
        }
        return false;
    }
}
