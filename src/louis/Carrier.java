package louis;

import battlecode.common.*;
import battlecode.world.Well;

public class Carrier extends Robot{

    int actionRadius;
    boolean shouldMove = true;

    ResourceType[] resourceTypes = {ResourceType.ADAMANTIUM, ResourceType.MANA};

    int myID;

    Carrier(RobotController rc){
        super(rc);
        actionRadius = rc.getType().actionRadiusSquared;
        myID = rc.getID();
    }
    void play(){
        tryAttack(true);
        tryCollectResource();
        tryDepositResource();
        tryCollectAnchor();
        tryDepositAnchor();
        moveToTarget();
        tryAttack(false);
        tryCollectResource();
        tryDepositResource();
        tryCollectAnchor();
        tryDepositAnchor();
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
                    rc.setIndicatorDot(loc,0,0,255);
                }
            }else{
                if(totalResources == GameConstants.CARRIER_CAPACITY){
                    loc = explore.getClosestMyHeadquarters();
                }
                if(totalResources == 0)
                {
                    if(myID % 2 == 0){
                        loc = explore.getClosestAdamantium();
                        if (loc == null) loc = explore.getClosestMana();
                    }
                    else{
                        loc = explore.getClosestMana();
                        if (loc == null) loc = explore.getClosestAdamantium();
                    }
//                    if (loc == null) loc = explore.getClosestEnemyOccupiedIsland();
                    if (loc == null) return explore.getExploreTarget();
                    if (loc != null){
                        rc.setIndicatorString("Going to " + loc.toString());
                        rc.setIndicatorDot(loc,0,0,255);
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
}
