package louis;

import battlecode.common.*;

public class Carrier extends Robot{

    int actionRadius;
    boolean shouldMove = true;

    ResourceType[] resourceTypes = {ResourceType.ADAMANTIUM, ResourceType.MANA};

    Carrier(RobotController rc){
        super(rc);
        actionRadius = rc.getType().actionRadiusSquared;
    }
    void play(){
        tryCollectAnchor();
        tryDepositAnchor();
        tryCollectResource();
        tryDepositResource();
        moveToTarget();
        tryCollectAnchor();
        tryDepositAnchor();
        tryCollectResource();
        tryDepositResource();
    }

    void moveToTarget(){
        if(!rc.isMovementReady()) return;
        rc.setIndicatorString("Trying to move");
        MapLocation loc = getTarget();
        if (loc != null) rc.setIndicatorString("Target not null!: " + loc.toString());
        bfs.move(loc);
    }

    MapLocation getTarget(){
        if(!shouldMove) return rc.getLocation();
        int totalResources = getTotalResources();
        MapLocation loc = null;
        try{
            if(rc.getAnchor() != null){
                loc = explore.getClosestFreeIsland();
                if(loc == null) return explore.getExploreTarget();
                if (loc != null){
                    rc.setIndicatorString("Going to " + loc.toString());
                    rc.setIndicatorDot(loc,100,100,100);
                }
            }else{
                if(totalResources == GameConstants.CARRIER_CAPACITY){
                    loc = explore.getClosestMyHeadquarters();
                }
                if(totalResources == 0)
                {
                    loc = explore.getClosestAdamantium();
                    if (loc == null) loc = explore.getClosestMana();
                    if (loc == null) loc = explore.getClosestEnemyOccupiedIsland();
                    if (loc == null) return explore.getExploreTarget();
                    if (loc != null){
                        rc.setIndicatorString("Going to " + loc.toString());
                        rc.setIndicatorDot(loc,100,100,100);
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return loc;
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
            for(Direction d: directions){
                MapLocation newLoc = rc.getLocation().add(d);
                if(!rc.onTheMap(newLoc)) continue;
                if(rc.canCollectResource(newLoc,-1)){
                    rc.collectResource(newLoc,-1);
                    return;
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
                for(Direction d: directions){
                    int amount = rc.getResourceAmount(r);
                    if(amount == 0) continue;
                    MapLocation newLoc = rc.getLocation().add(d);
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
}
