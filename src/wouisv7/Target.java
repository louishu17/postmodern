package wouisv7;

import battlecode.common.MapLocation;

public class Target {

    static final int ADAMANTIUM_TYPE = 0;
    static final int MANA_TYPE = 1;

    final int type;
    final int id; //id of unit for arhcon/soldiers, and amt. for lead/gold
    final MapLocation location;

    Target(int type, int id, MapLocation loc){
        this.type = type;
        this.id = id;
        this.location = loc;
    }

    Target(int code1, int code2){
        this.type = code1 >>> 12;
        this.id = code2;
        this.location = new MapLocation((code1 >>> 6)&63, code1&63);
    }

    static int getCode(MapLocation loc, int type){
        return (type << 12) | (loc.x << 6) | loc.y;
    }



}
