package louisv5;

import battlecode.common.*;

public class BFS {
    Pathfinding path;
    Micro micro;
    static RobotController rc;

    BFS(RobotController rc){
        this.rc = rc;
        this.path = new Pathfinding(rc);
        if(Util.isAttacker(rc.getType())){
            this.micro = new MicroAttackers(rc);
        } else{
            this.micro = new MicroCarriers(rc);
        }
    }

    void move(MapLocation target){
        if (!rc.isMovementReady()) return;
        if(micro.doMicro()) return;
        if (target == null) return;
        rc.setIndicatorString("BYTECODES USED: " + Clock.getBytecodeNum());
        if(rc.getLocation().distanceSquaredTo(target) == 0 || (rc.getType() == RobotType.CARRIER && checkIfNextToWell(target) && getTotalResources() < GameConstants.CARRIER_CAPACITY)) return;
        path.move(target);
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
