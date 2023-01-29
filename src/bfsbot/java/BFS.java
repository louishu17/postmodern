package bfsbot.java;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public abstract class BFS {
    Pathfinding path;
    Micro micro;
    static RobotController rc;
    final int BYTECODE_REMAINING = 1000;
    final int GREEDY_TURNS = 4;

    MapTracker mapTracker = new MapTracker();

    int turnsGreedy = 0;
    MapLocation currentTarget = null;

    BFS(RobotController rc){
        this.rc = rc;
        this.path = new Pathfinding(rc);
        if(Util.isAttacker(rc.getType())){
            this.micro = new MicroAttackers(rc);
        } else{
            this.micro = new MicroCarriers(rc);
        }
    }
    void reset(){
        turnsGreedy = 0;
        mapTracker.reset();
    }
    void update(MapLocation target){
        if (currentTarget == null || target.distanceSquaredTo(currentTarget) > 0){
            reset();
        } else --turnsGreedy;
        currentTarget = target;
        mapTracker.add(rc.getLocation());
    }
    void activateGreedy(){
        turnsGreedy = GREEDY_TURNS;
    }

    void move(MapLocation target){
        move(target, false);
    }

    void move(MapLocation target,boolean greedy){
        if (!rc.isMovementReady()) return;
        if(micro.doMicro()){
            reset();
            return;
        }
        if (target == null) return;
        if(rc.getLocation().distanceSquaredTo(target) == 0) return;
        update(target);

        if(!greedy && turnsGreedy <= 0){

            int t = Clock.getBytecodesLeft();

            Direction dir = null;
            try{
                if(!rc.senseMapInfo(rc.getLocation()).hasCloud()){
                    dir = getBestDir(target);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            t = Clock.getBytecodesLeft() - t;
            rc.setIndicatorString("Using bfs!!! " + t);
            if (dir != null && !mapTracker.check(rc.getLocation().add(dir))){
                move(dir);
                return;
            } else activateGreedy();
        }

        if (Clock.getBytecodesLeft() >= BYTECODE_REMAINING){
            path.move(target);
            --turnsGreedy;
        }
    }

    void move(Direction dir){
        try{
            if (!rc.canMove(dir)) return;
            rc.move(dir);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    abstract Direction getBestDir(MapLocation target);


}
