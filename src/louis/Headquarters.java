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
    Headquarters(RobotController rc){
        super(rc);
    }
    void play(){
        constructRobot(RobotType.CARRIER);
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
