package louis;

import battlecode.common.*;
public abstract class Robot {
    static RobotController rc;
    public Robot(RobotController rc){
        this.rc = rc;
    }

    abstract void play();
    void initTurn(){
        return;
    }
    void endTurn(){
        return;
    }
}
