package wouisv7;

import battlecode.common.*;

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
        try{
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
                    dir = bestDir(target);
                }catch(Exception e){
                    e.printStackTrace();
                }
                t = Clock.getBytecodesLeft() - t;
                rc.setIndicatorString("Using bfs!!! " + t);
                if (dir != null && !mapTracker.check(rc.getLocation().add(dir)) && !rc.senseCloud(rc.getLocation())){
                    move(dir);
                    return;
                } else activateGreedy();
            }

            if (Clock.getBytecodesLeft() >= BYTECODE_REMAINING){
                path.move(target);
                --turnsGreedy;
            }
        }catch (Exception e){
            e.printStackTrace();
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

    abstract Direction bestDir(MapLocation target);
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
