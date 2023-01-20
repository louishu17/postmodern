package louisv2;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.RobotController;

public strictfp class RobotPlayer {
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

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc){
        Robot robot;
        switch(rc.getType()){
            case HEADQUARTERS:
                robot = new Headquarters2(rc);
                break;
            case CARRIER:
                robot = new Carrier(rc);
                break;
            case LAUNCHER:
                robot = new Launcher(rc);
                break;
            case BOOSTER:
                robot = new Booster(rc);
                break;
            case DESTABILIZER:
                robot = new Destabilizer(rc);
                break;
            default:
                robot = new Amplifier(rc);
                break;
        }

        while(true){
            robot.initTurn();
            robot.play();
            robot.endTurn();
            Clock.yield();
        }

    }
}
