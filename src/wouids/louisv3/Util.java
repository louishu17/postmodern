package wouids.louisv3;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public class Util {
    static int encodeLoc(MapLocation loc){
        return (loc.x << 6) | loc.y;
    }

    static MapLocation getLocation(int code){
        return new MapLocation(code >>> 6, code & 63);
    }

    static boolean isAttacker(RobotType r){
        switch (r){
            case LAUNCHER:
            case DESTABILIZER: return true;
            default: return false;
        }
    }

    static boolean hurt(int h){
        return h < Constants.CRITICAL_HEALTH;
    }

    static Integer minMiners = null;

    static int getMinMiners(){
        if (minMiners != null) return minMiners;
        minMiners = (Robot.rc.getMapHeight()* Robot.rc.getMapWidth())/200;
        if (minMiners > Constants.INITIAL_MINERS) minMiners = Constants.INITIAL_MINERS;
        return minMiners;
    }
}
