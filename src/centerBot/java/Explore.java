package centerBot.java;

import battlecode.common.*;

public class Explore {
    RobotController rc;

    boolean compareWells;
    int myVisionRange;
    boolean visibleAdamantium;
    MapLocation closestAdamantium = null;
    int distAdamantium = 0;

    boolean visibleMana;
    MapLocation closestMana = null;
    int distMana = 0;

    boolean compareFreeIslands = false;
    boolean visibleFreeIsland;
    MapLocation closestFreeIsland = null;
    int distFreeIsland = 0;

    boolean compareEnemyOccupiedIslands = false;
    boolean visibleEnemyOccupiedIsland;
    MapLocation closestEnemyOccupiedIsland = null;
    int distEnemyOccupiedIsland = 0;


    MapLocation exploreLoc = null;
    MapLocation closestEnemyHeadquarters = null;
    int distEnemyHeadquarters = 0;

    MapLocation closestMyHeadquarters = null;
    int distMyHeadquarters = 0;

    static int BYTECODE_EXPLORE_RESOURCE_LIMIT;
    MapLocation[] checkLocs = new MapLocation[5];
    boolean checker = false;

    Explore(RobotController rc){
        this.rc = rc;
        myVisionRange = rc.getType().visionRadiusSquared;
        if (rc.getType() == RobotType.CARRIER){
            compareWells = true;
            compareFreeIslands = true;
            compareEnemyOccupiedIslands = true;
        }

        switch(rc.getType()){
            case CARRIER:
                BYTECODE_EXPLORE_RESOURCE_LIMIT = 4000;
                break;
            default:
                BYTECODE_EXPLORE_RESOURCE_LIMIT = 3000;
                break;
        }
        generateLocs();
    }

    void generateLocs(){
        int w = rc.getMapWidth();
        int h = rc.getMapHeight();
        checkLocs[0] = new MapLocation(w/2,h/2);
        checkLocs[1] = new MapLocation(0,0);
        checkLocs[2] = new MapLocation(w-1,0);
        checkLocs[3] = new MapLocation(0,h-1);
        checkLocs[4] = new MapLocation(w-1,h-1);
    }

    void setChecker(int init){
        exploreLoc = checkLocs[init%checkLocs.length];
        checker = true;
    }


    void reportResources(){
        try{
            visibleAdamantium = false;
            visibleMana = false;
            closestAdamantium = null;
            closestMana = null;
            distAdamantium = 0;
            distMana = 0;

            MapLocation myLoc = rc.getLocation();
            MapLocation[] mapLocs = rc.getAllLocationsWithinRadiusSquared(myLoc, myVisionRange);
            for(int i = 0; i < mapLocs.length; i++){
                if(Clock.getBytecodeNum() > BYTECODE_EXPLORE_RESOURCE_LIMIT) break;
                WellInfo well = rc.senseWell(mapLocs[i]);
                if (well != null){
                    if(well.getResourceType() == ResourceType.ADAMANTIUM){
                        visibleAdamantium = true;
                        if(compareWells){
                            int d = mapLocs[i].distanceSquaredTo(myLoc);
                            if(closestAdamantium == null || d< distAdamantium){
                                closestAdamantium = mapLocs[i];
                                distAdamantium = d;
                            }
                        }
                    }else if(well.getResourceType() == ResourceType.MANA){
                        visibleMana = true;
                        if(compareWells){
                            int d = mapLocs[i].distanceSquaredTo(myLoc);
                            if(closestMana == null || d< distMana){
                                closestMana = mapLocs[i];
                                distMana = d;
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void reportIslands(){
        try{
            visibleFreeIsland = false;
            visibleEnemyOccupiedIsland = false;
            closestFreeIsland = null;
            closestEnemyOccupiedIsland = null;
            distFreeIsland = 0;
            distEnemyOccupiedIsland = 0;

            int[] ids = rc.senseNearbyIslands();
            for(int id: ids){
                if(Clock.getBytecodeNum() > BYTECODE_EXPLORE_RESOURCE_LIMIT) break;
                if(rc.senseTeamOccupyingIsland(id) == Team.NEUTRAL){
                    visibleFreeIsland = true;
                    if(compareFreeIslands){
                        MapLocation[] islandLocs = rc.senseNearbyIslandLocations(id);
                        if(islandLocs.length > 0){
                            closestFreeIsland = islandLocs[0];
                        }
                    }
                }else if(rc.senseTeamOccupyingIsland(id) == rc.getTeam().opponent()){
                    visibleEnemyOccupiedIsland = true;
                    if(compareEnemyOccupiedIslands){
                        MapLocation[] islandLocs = rc.senseNearbyIslandLocations(id);
                        if(islandLocs.length > 0){
                            closestEnemyOccupiedIsland = islandLocs[0];
                        }
                    }
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    MapLocation getClosestAdamantium(){
        return closestAdamantium;
    }

    MapLocation getClosestMana(){
        return closestMana;
    }

    MapLocation getClosestFreeIsland(){
        return closestFreeIsland;
    }

    MapLocation getClosestEnemyOccupiedIsland(){
        return closestEnemyOccupiedIsland;
    }

    void getRandomTarget(int tries, boolean checkDanger) {
        MapLocation myLoc = rc.getLocation();
        int maxX = rc.getMapWidth();
        int maxY = rc.getMapHeight();
        while (tries-- > 0){
            if (exploreLoc != null) return;
            MapLocation newLoc = new MapLocation((int)(Math.random()*maxX), (int)(Math.random()*maxY));
            if (checkDanger && Robot.comm.isEnemyTerritoryRadial(newLoc)) continue;
            if (myLoc.distanceSquaredTo(newLoc) > myVisionRange){
                exploreLoc = newLoc;
            }
        }
    }
    void getCheckerTarget(int tries){
        MapLocation myLoc = rc.getLocation();
        while (tries-- > 0){
            int checkerIndex = (int)(Math.random()* checkLocs.length);
            MapLocation newLoc = checkLocs[checkerIndex];
            if (myLoc.distanceSquaredTo(newLoc) > myVisionRange){
                exploreLoc = newLoc;
            }
        }
    }

    MapLocation getExploreTarget(boolean checkDanger){
        if (exploreLoc != null && rc.getLocation().distanceSquaredTo(exploreLoc) <= myVisionRange) exploreLoc = null;
        if(exploreLoc == null){
            if(checker) getCheckerTarget(15);
            else{
                getRandomTarget(15, checkDanger);
            }
        }
        return exploreLoc;
    }

    MapLocation getClosestMyHeadquarters(){
        return closestMyHeadquarters;
    }

    void reportUnits(){
        closestEnemyHeadquarters = null;
        try{
            RobotInfo[] enemies = rc.senseNearbyRobots(myVisionRange, rc.getTeam().opponent());
            for(RobotInfo enemy: enemies){
                if(enemy.getType() != RobotType.HEADQUARTERS) continue;
                int d = enemy.getLocation().distanceSquaredTo(rc.getLocation());
                if(closestEnemyHeadquarters == null || d < distEnemyHeadquarters){
                    distEnemyHeadquarters = d;
                    closestEnemyHeadquarters = enemy.getLocation();
                }
            }

            RobotInfo[] allies = rc.senseNearbyRobots(myVisionRange, rc.getTeam());
            for(RobotInfo ally: allies){
                if(ally.getType() != RobotType.HEADQUARTERS) continue;
                int d = ally.getLocation().distanceSquaredTo(rc.getLocation());
                if(closestMyHeadquarters == null || d < distMyHeadquarters){
                    distMyHeadquarters = d;
                    closestMyHeadquarters = ally.getLocation();
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
