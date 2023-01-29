package wouisv6;

import battlecode.common.*;

public class Communication {
    RobotController rc;

    static final int MAX_HEADQUARTERS = 4;

    static final int ADAMANTIUM_INDEX = 15;
    static final int ADAMANTIUM_QUEUE_SIZE = 9;

    static final int MANA_INDEX = 25;
    static final int MANA_QUEUE_SIZE = 9;


    static final int HEADQUARTERS_LOC_INDEX = 54;

    static final int HEADQUARTERS_NB_INDEX = 58;
//    static final int HEADQUARTERS_CARRIERS_MADE = 40;
//    static final int BUILDING_QUEUE_INDEX = 54;

    static final int LAUNCHER_COUNT = 59;
    static final int CARRIER_COUNT = 60;



    static final int H_SYM = 63;
    static final int V_SYM = 62;
    static final int R_SYM = 61;

    int mapWidth, mapHeight;

    static int myID;
    static boolean headquarter = false;
    static boolean soldier = false;

    int horizontalSymmetry;
    int verticalSymmetry;
    int rotationSymmetry;

    MapLocation curEnemyHQTarget;


    Communication(RobotController rc) {
        this.rc = rc;
        myID = rc.getID();
        if(rc.getType() == RobotType.HEADQUARTERS) headquarter = true;
        if(rc.getType() == RobotType.LAUNCHER) soldier = true;
        if(headquarter){
//            System.out.println("HI I'm " + myID + " and I'm setting my location.");
            setHeadquartersLoc();
        }
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        horizontalSymmetry = 0;
        verticalSymmetry = 0;
        rotationSymmetry = 0;

    }

    void setHeadquartersLoc(){
        try{
            int i = -1;
            while(i++ < 3){
                if(rc.readSharedArray(HEADQUARTERS_LOC_INDEX + i) == 0) {
//                    System.out.println("WROTE TO: " + (HEADQUARTERS_LOC_INDEX + i));
                    rc.writeSharedArray(HEADQUARTERS_LOC_INDEX + i, Util.encodeLoc(rc.getLocation()));
                    break;
                }
            }
            rc.writeSharedArray(HEADQUARTERS_NB_INDEX, rc.readSharedArray(HEADQUARTERS_NB_INDEX) + 1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //only headquarters
    /*void reportSelf(){
        try{
            if(headquarter){
                int locCode = Util.encodeLoc(rc.getLocation());
                rc.writeSharedArray(3*myHeadquartersIndex+1, locCode);
                rc.writeSharedArray(3*myHeadquartersIndex+2, rc.getRoundNum());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }*/

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
                    return;
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

    void getClosestEnemyHeadquarters(){
        try {
//                System.out.println("FINDING NEW ENEMY HQ...");
//                System.out.println("CURRENT ENEMY HQ TARGET: " + curEnemyHQTarget);
            MapLocation tempEnemyHQTarget = curEnemyHQTarget;
            MapLocation myLoc = rc.getLocation();
            int bestDist = 10000;
            int hSym = 0;
            if(horizontalSymmetry != 0){
                hSym = horizontalSymmetry;
            }else{
                hSym = rc.readSharedArray(H_SYM);
            }
            boolean updateh = false;
            int vSym = 0;
            if(verticalSymmetry != 0){
                vSym = verticalSymmetry;
            }else{
                vSym = rc.readSharedArray(V_SYM);
            }
            boolean updatev = false;
            int rSym = 0;
            if(rotationSymmetry != 0){
                rSym = rotationSymmetry;
            }else{
                rSym = rc.readSharedArray(R_SYM);
            }
            boolean updater = false;
            boolean updateSymmetries = rc.getRoundNum() <= 5;
//            System.out.println("Horizontal Sym: " + hSym);
//            System.out.println("Vertical Sym: " +vSym);
//            System.out.println("Rotation Sym: " + rSym);

            int i = rc.readSharedArray(HEADQUARTERS_NB_INDEX);
            while (i-- > 0){
                MapLocation newLoc = Util.getLocation(rc.readSharedArray(HEADQUARTERS_LOC_INDEX + i));
//                    System.out.println("CONSIDERING SYMMETRIES FROM: " + newLoc);
                if ((hSym&1) == 0 && (hSym & (1 << (i+1))) == 0){
                    MapLocation symLoc = getHSym(newLoc);
                    if (rc.canSenseLocation(symLoc)){
                        RobotInfo r = rc.senseRobotAtLocation(symLoc);
                        if (r == null || r.getType() != RobotType.HEADQUARTERS || r.getTeam() != rc.getTeam().opponent()){
                            hSym += (1 << (i+1));
                            updateh = true;
                            if (updateSymmetries){
                                hSym +=1;
                                System.out.println("Not Horizontal!");
                            }
                        }
                    }
                    if(!symLoc.equals(curEnemyHQTarget)){
                        int d = myLoc.distanceSquaredTo(symLoc);
                        if (bestDist > d){
                            bestDist = d;
                            tempEnemyHQTarget = symLoc;
                        }
//                            System.out.println("CONSIDERING: "+ symLoc);
                    }
                }
                if ((vSym&1) == 0 && (vSym & (1 << (i+1))) == 0){
                    MapLocation symLoc = getVSym(newLoc);
                    if (rc.canSenseLocation(symLoc)){
                        RobotInfo r = rc.senseRobotAtLocation(symLoc);
                        if (r == null || r.getType() != RobotType.HEADQUARTERS || r.getTeam() != rc.getTeam().opponent()){
                            vSym += (1 << (i+1));
                            updatev = true;
                            if (updateSymmetries){
                                vSym += 1;
//                                    System.out.println("Not Vertical!");
                            }
                        }
                    }
                    if(!symLoc.equals(curEnemyHQTarget)){
                        int d = myLoc.distanceSquaredTo(symLoc);
                        if (bestDist > d){
                            bestDist = d;
                            tempEnemyHQTarget = symLoc;
                        }
//                            System.out.println("CONSIDERING: "+ symLoc);
                    }
                }if ((rSym&1) == 0 && (rSym & (1 << (i+1))) == 0){
                    MapLocation symLoc = getRSym(newLoc);
                    if (rc.canSenseLocation(symLoc)){
                        RobotInfo r = rc.senseRobotAtLocation(symLoc);
                        if (r == null || r.getType() != RobotType.HEADQUARTERS || r.getTeam() != rc.getTeam().opponent()){
                            rSym += (1 << (i+1));
                            updater = true;
                            if (updateSymmetries){
                                rSym += 1;
                                System.out.println("Not Rotational!");
                            }
                        }
                    }
                    if(!symLoc.equals(curEnemyHQTarget)){
                        int d = myLoc.distanceSquaredTo(symLoc);
                        if (bestDist > d){
                            bestDist = d;
                            tempEnemyHQTarget = symLoc;
                        }
//                            System.out.println("CONSIDERING: "+ symLoc);
                    }
                }
            }
            if (updateh){
                if(rc.canWriteSharedArray(H_SYM, hSym)) rc.writeSharedArray(H_SYM, hSym);
                horizontalSymmetry = hSym;
            }
            if (updatev){
                if(rc.canWriteSharedArray(V_SYM, vSym)) rc.writeSharedArray(V_SYM, vSym);
                verticalSymmetry = vSym;
            }
            if (updater){
                if(rc.canWriteSharedArray(R_SYM, rSym)) rc.writeSharedArray(R_SYM, rSym);
                rotationSymmetry = rSym;
            }
            curEnemyHQTarget = tempEnemyHQTarget;
//                System.out.println("NEW ENEMY HQ TARGET: " + curEnemyHQTarget);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    MapLocation getClosestAllyHeadquarter(){
        MapLocation ans = null;
        int bestDist = 10000;
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


            int i = rc.readSharedArray(HEADQUARTERS_NB_INDEX)  ;
            while (i-- > 0) {
                MapLocation newLoc = Util.getLocation(rc.readSharedArray(HEADQUARTERS_LOC_INDEX + i));
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
            if(rc.canWriteSharedArray(index,rc.readSharedArray(index) + amount )) rc.writeSharedArray(index, rc.readSharedArray(index) + amount);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

//    void reportBuilt(RobotType t, int amount){
//        try {
//            rc.writeSharedArray(BUILDING_QUEUE_INDEX + t.ordinal(), amount);
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    int getBuildingScore(RobotType r){
//        try {
//            return rc.readSharedArray(BUILDING_QUEUE_INDEX + r.ordinal());
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        return 0;
//    }
//
//    void activateDanger(){
//        try{
//            int carrierScore = getBuildingScore(RobotType.CARRIER);
//            if (carrierScore <= Util.getMinCarriers()) {
//                rc.writeSharedArray(BUILDING_QUEUE_INDEX + RobotType.CARRIER.ordinal(), Util.getMinCarriers() + 1);
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }

    MapLocation getClosestAdamantium() throws GameActionException{
        int i = -1;
        int closestDist = 100000;
        MapLocation loc = null;
        while(i++ < 4) {
            int codeLoc = rc.readSharedArray(i * 2);
            if(codeLoc != 0) {
                MapLocation currentLoc = Util.getLocation(codeLoc);
                int currentDist = rc.getLocation().distanceSquaredTo(currentLoc);
                if(currentDist + 3 * rc.readSharedArray(i * 2 + 1) < closestDist) {
                    loc = currentLoc;
                    closestDist = currentDist + 2 * rc.readSharedArray(i * 2 + 1);
                }
            }
        }
        return loc;
    }
    MapLocation getClosestMana() throws GameActionException{
        int i = -1;
        int closestDist = 100000;
        MapLocation loc = null;
        while(i++ < 4) {
            int codeLoc = rc.readSharedArray(i * 2 + 10);
            if(codeLoc != 0) {
                MapLocation currentLoc = Util.getLocation(codeLoc);
                int currentDist = rc.getLocation().distanceSquaredTo(currentLoc);
                if(currentDist + 3 * rc.readSharedArray(i * 2 + 11) < closestDist) {
                    loc = currentLoc;
                    closestDist = currentDist + 2 * rc.readSharedArray(i * 2 + 11);
                }
            }
        }
        return loc;
    }

}
