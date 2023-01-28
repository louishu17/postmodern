package microComparison;

import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * written by Keith
 * Objects of this class are used in the HeadQuarters class to track the priority level for making
 * a type of unit. That priority level is held in the score field in this class.
 *
 * Each time HQ makes a certain type of unit, it will update the UnitScore object corresponding
 * to that type.
 */

public class UnitTracker {
    int score;
    int unitsMade;
    int unitIndex;
    RobotType type;
    boolean standardAnchor, accAnchor;


    public static final int INIT_ANCHOR_SCORE = 50;


    /**
     * constructor
     * @param type is like RobotType.LAUNCHER, RobotType.CARRIER, etc..
     *             Should be null for an anchor
     * @param standardAnchor should be true iff this is for a stnadard anchor
     * @param accAnchor should be true iff this is for an accelerating anchor
     */
    public UnitTracker(RobotType type, boolean standardAnchor, boolean accAnchor){
        this.type = type;
        this.standardAnchor = standardAnchor;
        this.accAnchor = this.accAnchor;
        score = 0;
        if (standardAnchor || accAnchor){
            score = INIT_ANCHOR_SCORE;
        }
        unitsMade = 0;
        unitIndex = getUnitIndex(type, standardAnchor, accAnchor);
    }

    /**
     * This will update the priority level for making more of this unit by incrementing the score field
     * @param rc
     */
    public void updateScore(RobotController rc){
        unitsMade += 1;
        if (type == RobotType.AMPLIFIER){
            updateAmplifierScore(rc);
        } else if (type == RobotType.CARRIER){
            updateCarrierScore(rc);
        } else if (type == RobotType.LAUNCHER){
            updateLauncherScore(rc);
        } else if (standardAnchor || accAnchor){
            updateAnchorScore(rc);
        }
    }

    private void updateCarrierScore(RobotController rc){
        if(rc.getRoundNum() < 100) score += 1;
        else if(rc.getRoundNum() < 300) score += 2;
        else {
            //if this hq has a ton of resources, then we can place lower priority on spawning carriers
            boolean tooMuchAd = rc.getResourceAmount(ResourceType.ADAMANTIUM) > 400;
            boolean tooMuchMana = rc.getResourceAmount(ResourceType.MANA) > 400;
            if (tooMuchAd && tooMuchMana){
                score += 30;
            } else if (tooMuchAd || tooMuchMana){
                score += 20;
            } else {
                score += 5;
            }
        }
    }

    private void updateLauncherScore(RobotController rc){
        score += 2;
    }
    private void updateAmplifierScore(RobotController rc){
        if(rc.getRoundNum() < 100) score += 2;
        else if(rc.getRoundNum() < 300) score += 4;
        else score += 10;
    }

    private void updateAnchorScore(RobotController rc){
        score += 5;
    }

    /**
     * get the score
     * @return score
     */
    public int getScore(){
        return score;
    }


    /**
     * returns an index identifying the type of unit
     * @param t should be null for an anchor
     * @param standardAnchor
     * @param accAnchor
     * @return
     */
    public static int getUnitIndex(RobotType t, boolean standardAnchor, boolean accAnchor){
        if (t != null){
            if (t.equals(RobotType.AMPLIFIER)){
                return 0;
            } else if (t.equals(RobotType.BOOSTER)){
                return 1;
            } else if (t.equals(RobotType.CARRIER)){
                return 2;
            } else if (t.equals(RobotType.DESTABILIZER)){
                return 3;
            } else if (t.equals(RobotType.HEADQUARTERS)){
                return 4;
            } else if (t.equals(RobotType.LAUNCHER)){
                return 5;
            } else{
                return 9;
            }
        } else {
            if (standardAnchor){
                return 6;
            } else if (accAnchor){
                return 7;
            } else{
                return 8;
            }
        }
    }

    /**
     * get the cost of a type of unit
     * @param index, found via the method Headquarters.getUnitIndex
     * @return an array of [Adamantium cost, Mana cost, Elixir cost]
     */
    public static int[] getUnitCost(int index){
        if (index == 0){
            return new int[]{40, 40, 0};
        } else if (index == 1){
            return new int[]{0, 0, 150};
        } else if (index == 2){
            return new int[]{50, 0, 0};
        } else if (index == 3){
            return new int[]{0, 0, 200};
        } else if (index == 4){
            return new int[]{999999, 9999999, 999999}; //can't make an HQ
        } else if (index == 5){
            return new int[]{0, 60, 0};
        } else if (index == 6){
            return new int[]{100, 100, 0};
        } else if (index == 7){
            return new int[]{0, 0, 300};
        } else {
            return new int[]{999999, 999999, 999999};
        }
    }




}

