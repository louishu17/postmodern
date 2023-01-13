package louis;

import battlecode.common.MapLocation;

public class Util {
    static int encodeLoc(MapLocation loc){
        return (loc.x << 6) | loc.y;
    }

    static MapLocation getLocation(int code){
        return new MapLocation(code >>> 6, code & 63);
    }
}
