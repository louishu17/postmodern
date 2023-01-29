package wouisv6;


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
        final int MAX_MAP_SIZE = GameConstants.MAP_MAX_HEIGHT;

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
                    MapLocation newLoc = myLoc.add(dir);
                    Direction currentDir = Direction.CENTER;
                    if(rc.onTheMap(newLoc) && rc.canSenseLocation(newLoc)){
                        currentDir = rc.senseMapInfo(newLoc).getCurrentDirection();
                    }
                    if (canMove(dir) && rc.onTheMap(newLoc) && checkCurrent(dir,currentDir)) {
                        //rc.setIndicatorString("MOVING TOWARD " + dir);
                        rc.move(dir);
                        return true;
                    }
                    if (!rc.onTheMap(newLoc)) rotateRight = !rotateRight;
                    else lastObstacleFound = myLoc.add(dir);
                    if (rotateRight) dir = dir.rotateRight();
                    else dir = dir.rotateLeft();
                }

                if (canMove(dir)){
//                    rc.setIndicatorString("MOVING TOWARD " + dir);
                    rc.move(dir);
                }
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

    public boolean checkCurrent(Direction dir, Direction currentDir){
        if(currentDir == Direction.CENTER){
            return true;
        }
        int dx = 0;
        int dy = 0;
        int cdx = 0;
        int cdy = 0;

        switch(dir){
            case EAST:
                dx = 1;
                dy = 0;
                break;
            case NORTHEAST:
                dx = 1;
                dy = 1;
                break;
            case NORTH:
                dx = 0;
                dy = 1;
                break;
            case NORTHWEST:
                dx = -1;
                dy = 1;
                break;
            case WEST:
                dx = -1;
                dy = 0;
                break;
            case SOUTHWEST:
                dx = -1;
                dy = -1;
                break;
            case SOUTH:
                dx = 0;
                dy = -1;
                break;
            case SOUTHEAST:
                dx = 1;
                dy = -1;
                break;
        }

        switch(currentDir){
            case EAST:
                cdx = 1;
                cdy = 0;
                break;
            case NORTHEAST:
                cdx = 1;
                cdy = 1;
                break;
            case NORTH:
                cdx = 0;
                cdy = 1;
                break;
            case NORTHWEST:
                cdx = -1;
                cdy = 1;
                break;
            case WEST:
                cdx = -1;
                cdy = 0;
                break;
            case SOUTHWEST:
                cdx = -1;
                cdy = -1;
                break;
            case SOUTH:
                cdx = 0;
                cdy = -1;
                break;
            case SOUTHEAST:
                cdx = 1;
                cdy = -1;
                break;
        }
        return (dx * cdx + dy * cdy) > 0;
    }


}
