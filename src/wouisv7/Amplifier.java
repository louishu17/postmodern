package wouisv7;

import battlecode.common.*;

import java.util.HashSet;

public class Amplifier extends Robot {
    HashSet<Integer> oldAdWells = new HashSet<>();
    HashSet<Integer> newAdWells = new HashSet<>();
    HashSet<Integer> oldManaWells = new HashSet<>();
    HashSet<Integer> newManaWells = new HashSet<>();
    Amplifier(RobotController rc){
        super(rc);
    }
    void play(){
        memoryWells();
        moveToTarget();
    }

    void moveToTarget(){
        if(!rc.isMovementReady()) return;
        rc.setIndicatorString("Trying to move");
        MapLocation loc = getTarget();
        if (loc != null) rc.setIndicatorString("Target not null!: " + loc.toString());
        bfs.move(loc);
    }

    MapLocation getTarget(){
        MapLocation loc = explore.getExploreTarget(false);
        return loc;
    }

    void memoryWells(){
        try {
            rememberWells();
            if(rc.canWriteSharedArray(20,20) && newAdWells.size() != 0 && newManaWells.size() != 0) { //everytime it gets within writing distance of the headquarters
                Integer[] adWells = newAdWells.toArray(new Integer[newAdWells.size()]);
                comm.reportAdamantium(adWells);
                Integer[] manaWells = newManaWells.toArray(new Integer[newManaWells.size()]);
                comm.reportMana(manaWells);
                for(Integer adWell: newAdWells) {
                    oldAdWells.add(adWell);
                }
                for(Integer manaWell: newManaWells) {
                    oldManaWells.add(manaWell);
                }
                newAdWells.clear();
                oldAdWells.clear();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Stores an active set of new wells seen.
     * Already seen wells are not added.
     * @throws GameActionException
     */
    void rememberWells() throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        for(WellInfo well : wells) {
            Integer codeLoc = Util.encodeLoc(well.getMapLocation());
            if(well.getResourceType() == ResourceType.ADAMANTIUM && !oldAdWells.contains(codeLoc)) {
                newAdWells.add(codeLoc);
            }
            if(well.getResourceType() == ResourceType.MANA && !oldManaWells.contains(codeLoc)) {
                newManaWells.add(codeLoc);
            }
        }
    }
}
