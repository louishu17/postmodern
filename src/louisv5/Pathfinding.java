package louisv5;


import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;

public class Pathfinding {

    RobotController rc;
    MapLocation target = null;

    BugNav bugNav = new BugNav();

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };

    boolean[] impassable = null;

    void setImpassable(boolean[] impassable){
        this.impassable = impassable;
    }

    void initTurn(){
        impassable = new boolean[directions.length];
    }

    boolean canMove(Direction dir){
        if (!rc.canMove(dir)) return false;
        try{
            MapLocation newLoc = rc.getLocation().add(dir);
            if (!rc.onTheMap(newLoc) || (rc.senseMapInfo(newLoc).getCurrentDirection() != Direction.CENTER && rc.senseMapInfo(newLoc).getCurrentDirection() == getOppositeDirection(dir))) return false;
        }catch(Exception e){
            e.printStackTrace();
        }
//        if (impassable[dir.ordinal()]) return false;
        return true;
    }


    Pathfinding(RobotController rc){
        this.rc = rc;
    }

    public void move(MapLocation loc){
        if (!rc.isMovementReady()) return;
        target = loc;
        bugNav.move();
    }

    class BugNav{

        BugNav(){}

        final int INF = 1000000;
        boolean rotateRight = true; //if I should rotate right or left
        MapLocation lastObstacleFound = null; //latest obstacle I've found in my way
        int minDistToEnemy = INF; //minimum distance I've been to the enemy while going around an obstacle
        MapLocation prevTarget = null; //previous target
        HashSet<Integer> visited = new HashSet<>();

        boolean move() {
            try{
                if (prevTarget == null || target.distanceSquaredTo(prevTarget) > 0) resetPathfinding();

                MapLocation myLoc = rc.getLocation();
                int d = myLoc.distanceSquaredTo(target);
                if (d <= minDistToEnemy) resetPathfinding();

                int code = getCode();

                if (visited.contains(code)) resetPathfinding();
                visited.add(code);

                prevTarget = target;
                minDistToEnemy = Math.min(d, minDistToEnemy);

                Direction dir = myLoc.directionTo(target);
                if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);
                if (canMove(dir)){
                    resetPathfinding();
                }

                for (int i = 0; i < 8; i++) {
                    if (canMove(dir)) {
                        rc.setIndicatorString("MOVING TOWARD " + dir);
                        rc.move(dir);
                        return true;
                    }
                    MapLocation newLoc = myLoc.add(dir);
                    if (!rc.onTheMap(newLoc)) rotateRight = !rotateRight;
                    else lastObstacleFound = myLoc.add(dir);
                    if (rotateRight) dir = dir.rotateRight();
                    else dir = dir.rotateLeft();
                }

                if (canMove(dir)) rc.move(dir);
            } catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }

        void resetPathfinding(){
            lastObstacleFound = null;
            minDistToEnemy = INF;
            visited.clear();
        }

        int getCode(){
            int x = rc.getLocation().x;
            int y = rc.getLocation().y;
            Direction obstacleDir = rc.getLocation().directionTo(target);
            if (lastObstacleFound != null) obstacleDir = rc.getLocation().directionTo(lastObstacleFound);
            int bit = rotateRight ? 1 : 0;
            return (((((x << 6) | y) << 4) | obstacleDir.ordinal()) << 1) | bit;
        }
    }

    Direction getOppositeDirection(Direction dir){
        switch(dir){
            case NORTH:
                return Direction.SOUTH;
            case NORTHEAST:
                return Direction.SOUTHWEST;
            case EAST:
                return Direction.WEST;
            case SOUTHEAST:
                return Direction.NORTHWEST;
            case SOUTH:
                return Direction.NORTH;
            case SOUTHWEST:
                return Direction.NORTHEAST;
            case WEST:
                return Direction.EAST;
            case NORTHWEST:
                return Direction.SOUTHEAST;
        }
        return null;
    }


}
