package wouis.louisv2;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Headquarters extends Robot {

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final double SAVE_THRESHOLD = 20; //will save resources once in every SAVE_THRESHOLD number of turns

    UnitTracker carrierTracker, launcherTracker, amplifierTracker, anchorTracker;
    ArrayList<UnitTracker> unitTrackers;
    boolean saved_last_turn;
    HQStockPile stockpile;



    Headquarters(RobotController rc){
        super(rc);
        carrierTracker = new UnitTracker(RobotType.CARRIER, false, false);
        launcherTracker = new UnitTracker(RobotType.LAUNCHER, false, false);
        amplifierTracker = new UnitTracker(RobotType.AMPLIFIER, false, false);
        anchorTracker = new UnitTracker(null, true, false);

        unitTrackers = new ArrayList<UnitTracker>();
        unitTrackers.add(carrierTracker);
        unitTrackers.add(launcherTracker);
        unitTrackers.add(amplifierTracker);
        unitTrackers.add(anchorTracker);

        saved_last_turn = false;
        stockpile = new HQStockPile();
    }

    /**
     * Most of the time, the HQ
     * will produce the unit of the highest priority that it is capable of producing
     * but some turns it will choose to save up instead
     */
    void play(){
        while(true){
            //System.out.println("I have " + rc.getResourceAmount(ResourceType.ADAMANTIUM) + " Ad  and " + rc.getResourceAmount(ResourceType.MANA) + " Mana");
            if (saved_last_turn){
                saved_last_turn = false;
                stockpile.endSave(this, rc);
            }
            if (shouldSaveThisTurn()){
                saved_last_turn = true;
                stockpile.beginSave(this, rc);

            } else {
                produceUnit();
            }
            break;
            /*
            if(carrierScore <= launcherScore){
                if(constructRobot(RobotType.CARRIER)){
                    updateCarrierScore();
                    continue;
                }
            } else if(amplifierScore <= launcherScore){
                if(constructRobot(RobotType.AMPLIFIER)){
                    updateAmplifierScore();
                    continue;
                }
            } else if (anchorScore <= launcherScore){
                if (constructAnchor(Anchor.STANDARD)){
                    updateAnchorScore();
                    continue;
                }
            }
            else if(constructRobot(RobotType.LAUNCHER)){
                updateLauncherScore();
                continue;
            }
            break;
             */
        }
    }

    /**
     *
     * @return true iff we should save resource this turn (to save up for anchors, boosters, etc..)
     */
    private boolean shouldSaveThisTurn(){
        if (rc.getRoundNum() < 200) {
            return false;
        }else if ((rc.getRoundNum() % SAVE_THRESHOLD ) == 0){
            return true;
        }
        return false;
    }

    /**
     * produce a unit with the highest priority that it is capable of producing
     */
    void produceUnit(){
        Collections.sort(unitTrackers, Comparator.comparing(UnitTracker::getScore));
        for (UnitTracker tracker : unitTrackers){
            if (constructUnit(tracker.type, tracker.standardAnchor, tracker.accAnchor)){
                //now, a unit was actually constructed
                tracker.updateScore(rc);
                if (tracker.unitIndex == stockpile.typeToSaveFor){
                    //we used the stockpile to construct the unit, so update the stockpile
                    stockpile.useStockpileToConstruct(tracker.unitIndex);
                    stockpile.setNextTypeToSaveFor(this, rc);
                }
            }
        }
    }

    /**
     *
     * @param t
     * @param standardAnchor
     * @param accAnchor
     * @return true iff the unit was actually constructd, false if it failed to construct
     */
    boolean constructUnit(RobotType t, boolean standardAnchor, boolean accAnchor){
        if (!haveEnoughResources(t, standardAnchor, accAnchor)){
            return false;
        }

        if (standardAnchor){
            return constructAnchor(Anchor.STANDARD);
        } else if (accAnchor){
            return constructAnchor(Anchor.ACCELERATING);
        } else {
            return constructRobot(t);
        }
    }

    boolean constructRobot(RobotType t){
        //System.out.println("Building " + t.name());

        try{
            MapLocation myLoc = rc.getLocation();
            MapLocation buildLoc = null;
            for (Direction d : directions) {
                MapLocation newLoc = myLoc.add(d);
                if (rc.canBuildRobot(t, newLoc)) {
                    buildLoc = newLoc;
                }
            }
            if (buildLoc != null && rc.canBuildRobot(t,buildLoc)){
                rc.buildRobot(t, buildLoc);
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }


    boolean constructAnchor(Anchor anchor){
        //System.out.println("Building an anchor");
        try{
            if (rc.canBuildAnchor(anchor)){
                rc.buildAnchor(anchor);
                return true;
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;

    }

    /**
     * check if we have enough non-stockpiled resources to create the desired unit
     * We should not take out of the stockpile.
     *
     * Will have to update this to track what we are stockpiling for.
     * @param t
     * @param standardAnchor
     * @param accAnchor
     * @return
     */
    private boolean haveEnoughResources(RobotType t, boolean standardAnchor, boolean accAnchor){
        int availAd, availMan, availElixir;

        int unitIndex = UnitTracker.getUnitIndex(t, standardAnchor, accAnchor);
        int[] unitCost = UnitTracker.getUnitCost(unitIndex);

        if (unitIndex == stockpile.getTypeToSaveFor()){
            //can use the stockpiled resources
            availAd = rc.getResourceAmount(ResourceType.ADAMANTIUM);
            availMan = rc.getResourceAmount(ResourceType.MANA);
            availElixir = rc.getResourceAmount(ResourceType.ELIXIR);
        } else {
            //can't use the stockpile
            availAd = rc.getResourceAmount(ResourceType.ADAMANTIUM) - stockpile.getSavedAd();
            availMan = rc.getResourceAmount(ResourceType.MANA) - stockpile.getSavedMana();
            availElixir = rc.getResourceAmount(ResourceType.ELIXIR) - stockpile.getSavedElixir();
        }

        if ((availAd >= unitCost[0]) && (availMan >= unitCost[1]) && (availElixir >= unitCost[2])){
            return true;
        } else {
            return false;
        }
    }


}