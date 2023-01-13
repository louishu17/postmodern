package louis;

import battlecode.common.*;

public class Explore {
    RobotController rc;

    boolean compareWells;
    boolean visibleIsland;
    int myVisionRange;
    boolean visibleAdamantium;
    MapLocation closestAdamantium;
    int distAdamantium;

    boolean visibleMana;
    MapLocation closestMana;
    int distMana;

    boolean compareIslands;
    MapLocation closestIsland;
    int distIsland;

    static final int BYTECODE_EXPLORE_RESOURCE_LIMIT = 4000;

    Explore(RobotController rc){
        this.rc = rc;
        myVisionRange = rc.getType().visionRadiusSquared;
        if (rc.getType() == RobotType.CARRIER){
            compareWells = true;
        }
    }

    void reportResourcesAndIslands(){
        try{
            visibleAdamantium = false;
            visibleMana = false;
            visibleIsland = false;
            closestAdamantium = null;
            closestMana = null;
            closestIsland = null;
            distAdamantium = 0;
            distMana = 0;
            distIsland = 0;

            MapLocation myLoc = rc.getLocation();
            MapLocation[] mapLocs = rc.getAllLocationsWithinRadiusSquared(myLoc, myVisionRange);
            for(int i = 0; i < mapLocs.length; i++){
                if(Clock.getBytecodeNum() > BYTECODE_EXPLORE_RESOURCE_LIMIT) break;
                WellInfo well = rc.senseWell(mapLocs[i]);
                int island = rc.senseIsland(mapLocs[i]);
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
                if(island != -1){
                    visibleIsland = true;
                    if(compareIslands){
                        int d = mapLocs[i].distanceSquaredTo(myLoc);
                        if(closestIsland == null || d < distIsland){
                            closestIsland = mapLocs[i];
                            distIsland = d;
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

    MapLocation getClosestIsland(){
        return closestIsland;
    }

    MapLocation getExploreTarget(){
        return null;
    }

    void reportUnits(){

    }
}
