package louis;

import battlecode.common.*;

public class Communication {
    RobotController rc;

    static final int MAX_HEADQUARTERS = 4;
    static final int HEADQUARTERS_INDEX = 0;
    static final int HEADQUARTERS_NB_INDEX = 14;

    static final int CARRIER_COUNT = 60;
    static final int LAUNCHER_COUNT = 59;


    static final int H_SYM = 63;
    static final int V_SYM = 62;
    static final int R_SYM = 61;

    int mapWidth, mapHeight;

    static int myID;
    static int myHeadquartersIndex = -1;
    static boolean headquarter = false;

    Communication(RobotController rc){
        this.rc = rc;
        myID = rc.getID();
        if(rc.getType() == RobotType.HEADQUARTERS) headquarter = true;
        if(headquarter) setHeadquartersIndex();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
    }

    void setHeadquartersIndex(){
        try{
            int i = MAX_HEADQUARTERS;
            while(i-- > 0){
                ++myHeadquartersIndex;
                int id = rc.readSharedArray(3 * myHeadquartersIndex);
                if(id == 0){
                    rc.writeSharedArray(3 * myHeadquartersIndex, myID+1);
                }
                break;
            }
            rc.writeSharedArray(HEADQUARTERS_NB_INDEX, myHeadquartersIndex+1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //only headquarters
    void reportSelf(){
        if(!headquarter) return;
        try{
         int locCode = Util.encodeLoc(rc.getLocation());
         rc.writeSharedArray(3*myHeadquartersIndex+1, locCode);
         rc.writeSharedArray(3*myHeadquartersIndex+2, rc.getRoundNum());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    MapLocation getHSym(MapLocation loc){
        return new MapLocation(mapWidth - loc.x - 1, loc.y);
    }

    MapLocation getVSym(MapLocation loc){
        return new MapLocation(loc.x, mapHeight - loc.y - 1);
    }

    MapLocation getRSym(MapLocation loc){
        return new MapLocation(mapWidth - loc.x - 1, mapHeight - loc.y - 1);
    }

    MapLocation getClosestEnemyHeadquarters(){
        try{
            MapLocation myLoc = rc.getLocation();
            MapLocation ans = null;
            int bestDist = 0;
            int i = rc.readSharedArray(HEADQUARTERS_NB_INDEX);
            int hSym = rc.readSharedArray(H_SYM);
            boolean updateh = false;
            int vSym = rc.readSharedArray(V_SYM);
            boolean updatev = false;
            int rSym = rc.readSharedArray(R_SYM);
            boolean updater = false;
            while (i-- > 0){
                MapLocation newLoc = Util.getLocation(rc.readSharedArray(3*i+1));
                if ((hSym&1) == 0 && (hSym & (1 << (i+1))) == 0){
                    MapLocation symLoc = getHSym(newLoc);
                    if (rc.canSenseLocation(symLoc)){
                        RobotInfo r = rc.senseRobotAtLocation(symLoc);
                        if (r == null || r.getType() != RobotType.HEADQUARTERS || r.getTeam() != rc.getTeam().opponent()){
                            hSym += (1 << (i+1));
                            updateh = true;
                        }
                    }
                    int d = myLoc.distanceSquaredTo(symLoc);
                    if (ans == null || d < bestDist){
                        bestDist = d;
                        ans = symLoc;
                    }
                }
                if ((vSym&1) == 0 && (vSym & (1 << (i+1))) == 0){
                    MapLocation symLoc = getVSym(newLoc);
                    if (rc.canSenseLocation(symLoc)){
                        RobotInfo r = rc.senseRobotAtLocation(symLoc);
                        if (r == null || r.getType() != RobotType.HEADQUARTERS || r.getTeam() != rc.getTeam().opponent()){
                            vSym += (1 << (i+1));
                            updatev = true;
                        }
                    }
                    int d = myLoc.distanceSquaredTo(symLoc);
                    if (ans == null || d < bestDist){
                        bestDist = d;
                        ans = symLoc;
                    }
                }if ((rSym&1) == 0 && (rSym & (1 << (i+1))) == 0){
                    MapLocation symLoc = getRSym(newLoc);
                    if (rc.canSenseLocation(symLoc)){
                        RobotInfo r = rc.senseRobotAtLocation(symLoc);
                        if (r == null || r.getType() != RobotType.HEADQUARTERS || r.getTeam() != rc.getTeam().opponent()){
                            rSym += (1 << (i+1));
                            updater = true;
                        }
                    }
                    int d = myLoc.distanceSquaredTo(symLoc);
                    if (ans == null || d < bestDist){
                        bestDist = d;
                        ans = symLoc;
                    }
                }
            }
            if (rc.canWriteSharedArray(H_SYM,hSym) && updateh){
                rc.writeSharedArray(H_SYM, hSym);
            }
            if (rc.canWriteSharedArray(V_SYM,vSym) && updatev) {
                rc.writeSharedArray(V_SYM, vSym);
            }
            if (rc.canWriteSharedArray(R_SYM, rSym) && updater){
                rc.writeSharedArray(R_SYM, rSym);
            }
            return ans;

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    void increaseIndex(int index, int amount){
        try {
            rc.writeSharedArray(index, rc.readSharedArray(index) + amount);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
