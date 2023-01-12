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


    public Robot(RobotController rc){
        this.rc = rc;
        bfs = new BFS(rc);

    }

    abstract void play();
    void initTurn(){
        return;
    }
    void endTurn(){
        return;
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
