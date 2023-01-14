package louis;

import battlecode.common.*;

import java.util.Random;

public class Headquarters extends Robot{
    static final Random rng = new Random(6147);

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

    int carrierScore = 0;
    int launcherScore = 0;
    int amplifierScore = 0;

    Headquarters(RobotController rc){
        super(rc);
    }
    void play(){
        if(carrierScore <= launcherScore){
            if(constructRobot(RobotType.CARRIER)) updateCarrierScore();
        } else if(amplifierScore <= launcherScore){
            if(constructRobot(RobotType.AMPLIFIER)) updateAmplifierScore();
        }
        else if(constructRobot(RobotType.LAUNCHER)) updateLauncherScore();

        constructRobot(RobotType.CARRIER);
    }

    void updateCarrierScore(){
        if(rc.getRoundNum() < 100) carrierScore += 1;
        else if(rc.getRoundNum() < 300) carrierScore += 2;
        else carrierScore += 5;
    }

    void updateLauncherScore(){
        launcherScore += 2;
    }
    void updateAmplifierScore(){
        amplifierScore += 10;
    }

    boolean constructRobot(RobotType t){
        System.out.println("Building " + t.name());
        try{
            MapLocation myLoc = rc.getLocation();
            MapLocation buildLoc = null;
            for (Direction d : directions) {
                MapLocation newLoc = myLoc.add(d);
                if (rc.canBuildRobot(t, newLoc)) {
                    buildLoc = newLoc;
                }
            }
            if (buildLoc != null){
                rc.buildRobot(t, buildLoc);
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
