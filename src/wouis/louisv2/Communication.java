package wouis.louisv2;

import battlecode.common.*;

public class Communication {
    RobotController rc;

    static final int MAX_HEADQUARTERS = 4;

    static final int ADAMANTIUM_INDEX = 15;
    static final int ADAMANTIUM_QUEUE_SIZE = 9;

    static final int MANA_INDEX = 25;
    static final int MANA_QUEUE_SIZE = 9;


    static final int HEADQUARTERS_NB_INDEX = 45;
    static final int HEADQUARTERS_CARRIERS_MADE = 40;
    static final int BUILDING_QUEUE_INDEX = 54;
    static final int HEADQUARTERS_LOC_INDEX = 41;
    static final int CARRIER_COUNT = 60;
    static final int LAUNCHER_COUNT = 59;



    static final int H_SYM = 63;
    static final int V_SYM = 62;
    static final int R_SYM = 61;

    int mapWidth, mapHeight;

    static int myID;
    static int myHeadquartersIndex = -1;
    static boolean headquarter = false;
    static boolean soldier = false;

    static final int INF_COMM = (1 << 16) - 1;


    Communication(RobotController rc) {
        this.rc = rc;
        myID = rc.getID();
        if(rc.getType() == RobotType.HEADQUARTERS) headquarter = true;
        if(rc.getType() == RobotType.LAUNCHER) soldier = true;
        if(headquarter) setHeadquartersLoc();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
    }

    void setHeadquartersLoc(){
        try{
            int i = MAX_HEADQUARTERS;
            while(i-- > 0){
                ++myHeadquartersIndex;
                if(rc.readSharedArray(HEADQUARTERS_LOC_INDEX + myHeadquartersIndex) == 0) {
                    rc.writeSharedArray(HEADQUARTERS_LOC_INDEX + myHeadquartersIndex, Util.encodeLoc(rc.getLocation()));
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
        try{
            if(headquarter){
                int locCode = Util.encodeLoc(rc.getLocation());
                rc.writeSharedArray(3*myHeadquartersIndex+1, locCode);
                rc.writeSharedArray(3*myHeadquartersIndex+2, rc.getRoundNum());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * The parameter is a locally stored array of Adamantium wells.
     * If the wells are closer than existing wells in the queue, then overwrite old wells
     * Uses array spots 0-9
     * @param AdWells
     */
    void reportAdamantium(Integer[] AdWells) throws GameActionException {
        for(Integer well: AdWells) {
            MapLocation wellLoc = Util.getLocation(well);
            int i = -1;
            int shortestDist = 10000;
            while(i++ < 3) {
                MapLocation HQLoc =  Util.getLocation(rc.readSharedArray(HEADQUARTERS_LOC_INDEX + i));
                int dist = HQLoc.distanceSquaredTo(wellLoc);
                if(shortestDist > dist) {
                    shortestDist = dist;
                }
            }
            int j = -1;
            while(j++ < 4) {
                if(rc.readSharedArray(j * 2) == 0) { //if well queue is empty
                    rc.writeSharedArray(j * 2, Util.encodeLoc(wellLoc));
                    rc.writeSharedArray(j * 2 + 1, shortestDist);
                    break;
                }
            }
            int k = -1;
            int indexToBeReplaced = -1;
            int longestDist = 0;
            while(k++ < 4) {
                int currentDist = rc.readSharedArray(k * 2 + 1);
                if(currentDist > longestDist) {
                    longestDist = currentDist; //greatest distance out of known wells
                    indexToBeReplaced = k * 2;
                }
            }
            if(shortestDist < longestDist) { //if well is closer than the farthest known well, replaces it in the queue
                rc.writeSharedArray(indexToBeReplaced, Util.encodeLoc(wellLoc));
                rc.writeSharedArray(indexToBeReplaced + 1, shortestDist);
            }
        }
    }
    /**
     * The parameter is a locally stored array of Mana wells.
     * If the wells are closer than existing wells in the queue, then overwrite old wells
     * Even array spots are well locations, odd array spots are distances
     * Uses array spots 10-19
     * @param ManaWells
     */
    void reportMana(Integer[] ManaWells) throws GameActionException {
        for(int well: ManaWells) {
            MapLocation wellLoc = Util.getLocation(well);
            int i = -1;
            int shortestDist = 10000;
            while(i++ < 3) {
                MapLocation HQLoc =  Util.getLocation(rc.readSharedArray(HEADQUARTERS_LOC_INDEX + i));
                int dist = HQLoc.distanceSquaredTo(wellLoc);
                if(shortestDist > dist) {
                    shortestDist = dist;
                }
            }
            int j = -1;
            while(j++ < 4) {
                if(rc.readSharedArray(j * 2 + 10) == 0) { //if well queue is empty
                    rc.writeSharedArray(j * 2 + 10, Util.encodeLoc(wellLoc));
                    rc.writeSharedArray(j * 2 + 11, shortestDist);
                    break;
                }
            }
            int k = -1;
            int indexToBeReplaced = -1;
            int longestDist = 0;
            while(k++ < 4) {
                int currentDist = rc.readSharedArray(k * 2 + 11);
                if(currentDist > longestDist) {
                    longestDist = currentDist; //greatest distance out of known wells
                    indexToBeReplaced = k * 2 + 10;
                }
            }
            if(shortestDist < longestDist) { //if well is closer than the farthest known well, replaces it in the queue
                rc.writeSharedArray(indexToBeReplaced, Util.encodeLoc(wellLoc));
                rc.writeSharedArray(indexToBeReplaced + 1, shortestDist);
            }
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
                MapLocation newLoc = Util.getLocation(rc.readSharedArray(HEADQUARTERS_LOC_INDEX + i));
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
    MapLocation getClosestAllyHeadquarter(){
        MapLocation ans = null;
        int bestDist = 0;
        MapLocation myLoc = rc.getLocation();
        try {
            RobotInfo[] allies = rc.senseNearbyRobots(myLoc, rc.getType().visionRadiusSquared, rc.getTeam());
            for (RobotInfo r : allies){
                if (r.getType() != RobotType.HEADQUARTERS) continue;
                int d = r.getLocation().distanceSquaredTo(myLoc);
                if (ans == null || bestDist > d) {
                    bestDist = d;
                    ans = r.getLocation();
                }
            }
            if (ans != null) return ans;

            int i = rc.readSharedArray(HEADQUARTERS_NB_INDEX);
            while (i-- > 0) {
                MapLocation newLoc = Util.getLocation(rc.readSharedArray(3 * i + 1));
                int d = myLoc.distanceSquaredTo(newLoc);
                if (ans == null || bestDist > d) {
                    bestDist = d;
                    ans = newLoc;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return ans;
    }

    boolean isEnemyTerritoryRadial(MapLocation loc){
        try {
            double minDistAlly = -1, minDistEnemy = -1;
            int i = rc.readSharedArray(HEADQUARTERS_NB_INDEX);
            int hSym = rc.readSharedArray(H_SYM);
            int vSym = rc.readSharedArray(V_SYM);
            int rSym = rc.readSharedArray(R_SYM);
            while (i-- > 0) {
                MapLocation newLoc = Util.getLocation(rc.readSharedArray( HEADQUARTERS_LOC_INDEX + i));
                int d = loc.distanceSquaredTo(newLoc);
                if (minDistAlly < 0 || d < minDistAlly) minDistAlly = d;
                if ((hSym & 1) == 0 && (hSym & (1 << (i + 1))) == 0) {
                    MapLocation symLoc = getHSym(newLoc);
                    d = loc.distanceSquaredTo(symLoc);
                    if (minDistEnemy < 0 || d < minDistEnemy) minDistEnemy = d;
                }
                if ((vSym & 1) == 0 && (vSym & (1 << (i + 1))) == 0) {
                    MapLocation symLoc = getVSym(newLoc);
                    d = loc.distanceSquaredTo(symLoc);
                    if (minDistEnemy < 0 || d < minDistEnemy) minDistEnemy = d;
                }
                if ((rSym & 1) == 0 && (rSym & (1 << (i + 1))) == 0) {
                    MapLocation symLoc = getRSym(newLoc);
                    d = loc.distanceSquaredTo(symLoc);
                    if (minDistEnemy < 0 || d < minDistEnemy) minDistEnemy = d;
                }
            }
            if (minDistEnemy < 0) return false;
            if (minDistAlly <= minDistEnemy) return false;
            if (minDistEnemy <= Constants.DANGER_RADIUS) return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    void increaseIndex(int index, int amount){
        try {
            rc.writeSharedArray(index, rc.readSharedArray(index) + amount);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    void reportBuilt(RobotType t, int amount){
        try {
            rc.writeSharedArray(BUILDING_QUEUE_INDEX + t.ordinal(), amount);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    int getBuildingScore(RobotType r){
        try {
            return rc.readSharedArray(BUILDING_QUEUE_INDEX + r.ordinal());
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    void activateDanger(){
        try{
            int carrierScore = getBuildingScore(RobotType.CARRIER);
            if (carrierScore <= Util.getMinMiners()) {
                rc.writeSharedArray(BUILDING_QUEUE_INDEX + RobotType.CARRIER.ordinal(), Util.getMinMiners() + 1);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    MapLocation getClosestAdamantium() throws GameActionException{
        int i = -1;
        int closestDist = 10000;
        MapLocation loc = null;
        while(i++ < 4) {
            int codeLoc = rc.readSharedArray(i * 2);
            if(codeLoc != 0 && rc.getLocation().distanceSquaredTo(Util.getLocation(codeLoc)) < closestDist) {
                loc = Util.getLocation(codeLoc);
                closestDist = rc.getLocation().distanceSquaredTo(Util.getLocation(codeLoc));
            }
        }
        return loc;
    }
    MapLocation getClosestMana() throws GameActionException{
        int i = -1;
        int closestDist = 10000;
        MapLocation loc = null;
        while(i++ < 4) {
            int codeLoc = rc.readSharedArray(i * 2 + 10);
            if(codeLoc != 0 && rc.getLocation().distanceSquaredTo(Util.getLocation(codeLoc)) < closestDist) {
                loc = Util.getLocation(codeLoc);
                closestDist = rc.getLocation().distanceSquaredTo(Util.getLocation(codeLoc));
            }
        }
        return loc;
    }

}
