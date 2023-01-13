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
        tryCollectResource();
        tryDepositResource();
        moveToTarget();
        tryCollectResource();
        tryDepositResource();
    }

    void moveToTarget(){
        if(!rc.isMovementReady()) return;
        rc.setIndicatorString("Trying to move");
        MapLocation loc = getTarget();
//        if (loc != null) rc.setIndicatorString("Target not null!: " + loc.toString());
        bfs.move(loc);
    }

    MapLocation getTarget(){
        if(!shouldMove) return rc.getLocation();
        int totalResources = getTotalResources();
        MapLocation loc = null;

        if(totalResources == GameConstants.CARRIER_CAPACITY){
            loc = explore.getClosestMyHeadquarters();
        }
        if(totalResources == 0)
        {
            loc = getClosestAdamantium();
            if (loc == null) return explore.getExploreTarget();
            if (loc != null){
                rc.setIndicatorString("Going to " + loc.toString());
                rc.setIndicatorDot(loc,100,100,100);
            }
        }

        return loc;
    }

    MapLocation getClosestAdamantium(){
        MapLocation ans = explore.getClosestAdamantium();
        return ans;
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
            for (ResourceType r: resourceTypes){
                int amount = rc.getResourceAmount(r);
                if(amount > 0 && rc.canTransferResource(explore.getClosestMyHeadquarters(),r,amount)){
                    rc.transferResource(explore.getClosestMyHeadquarters(),r,amount);
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
