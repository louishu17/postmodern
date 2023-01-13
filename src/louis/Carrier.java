package louis;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Carrier extends Robot{
    Carrier(RobotController rc){
        super(rc);
    }
    void play(){
        tryCollectResource();
        moveToTarget();
        tryCollectResource();
    }

    void moveToTarget(){
        if(!rc.isMovementReady()) return;
        rc.setIndicatorString("Trying to move");
        MapLocation loc = getTarget();
        if (loc != null) rc.setIndicatorString("Target not null!: " + loc.toString());
        bfs.move(loc);
    }

    MapLocation getTarget(){
        MapLocation loc = getClosestAdamantium();
        if (loc == null) return explore.getExploreTarget();
        if (loc != null){
            rc.setIndicatorString("Going to " + loc.toString());
            rc.setIndicatorDot(loc,100,100,100);
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
}
