package louisv5;

import battlecode.common.ResourceType;
import battlecode.common.RobotController;

/**
 * Written by Keith
 *
 * Objects of this class are used by Headquarters objects to track their stockpiled resources
 */
public class HQStockPile {
    int savedAd, savedMana, savedElixir;
    int oldAd, oldMana, oldElixir;
    int typeToSaveFor;

    /**
     * constructor, initialize everything to 0
     */
    public HQStockPile(){
        savedAd = 0;
        savedMana = 0;
        savedElixir = 0;

        oldAd = 0;
        oldMana = 0;
        oldElixir = 0;

        typeToSaveFor = UnitTracker.getUnitIndex(null, true, false); //start saving for anchor
    }

    /**
     * call this method when you are going to save this turn
     * @param hq
     * @param rc
     */
    public void beginSave(Headquarters hq, RobotController rc){
        oldAd = rc.getResourceAmount(ResourceType.ADAMANTIUM);
        oldMana = rc.getResourceAmount(ResourceType.MANA);
        oldElixir = rc.getResourceAmount(ResourceType.ELIXIR);
    }

    /**
     * call this method when you saved on the last turn, it will update the stockpiled resources
     * @param hq
     * @param rc
     */
    public void endSave(Headquarters hq, RobotController rc){
        savedAd += rc.getResourceAmount(ResourceType.ADAMANTIUM) - oldAd;
        savedMana += rc.getResourceAmount(ResourceType.MANA)  - oldMana;
        savedElixir += rc.getResourceAmount(ResourceType.ELIXIR) - oldElixir;
    }

    /**
     * call this method after you used the stockpile to make an item using the stockpile's resources
     * It will subtract the correct amount from the stockpile.
     * @param unitIndex
     */
    public void useStockpileToConstruct(int unitIndex){
        if (unitIndex != typeToSaveFor){
            return;
        }
        int[] unitCosts = UnitTracker.getUnitCost(unitIndex);
        savedAd -= unitCosts[0];
        savedMana -= unitCosts[1];
        savedElixir -= unitCosts[2];
    }

    /**
     * This method has logic that will determine what we should save for
     * for now, just save for standard anchors every time
     * @param hq
     * @param rc
     */
    public void setNextTypeToSaveFor(Headquarters hq, RobotController rc){
        typeToSaveFor = UnitTracker.getUnitIndex(null, true, false);
    }

    public int getSavedAd(){
        return savedAd;
    }

    public int getSavedMana(){
        return savedMana;
    }

    public int getSavedElixir(){
        return savedElixir;
    }

    public int getTypeToSaveFor(){
        return typeToSaveFor;
    }


}
