package louis;

import battlecode.common.*;
public abstract class Robot {
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

    static RobotController rc;
    static BFS bfs;
    static Explore explore;
    static Communication comm;


    public Robot(RobotController rc){
        this.rc = rc;
        bfs = new BFS(rc);
        explore = new Explore(rc);
        comm = new Communication(rc);
    }

    abstract void play();
    void initTurn(){
        comm.reportSelf();
    }
    void endTurn(){
        explore.reportResourcesAndIslands();
        explore.reportUnits();
    }

    void moveRandom(){
        try {
            int d = (int) (Math.random() * 8.0);
            Direction dir = directions[d];
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(dir)) rc.move(dir);
                dir = dir.rotateLeft();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
