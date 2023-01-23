package wouis.louisv2;

import battlecode.common.*;

public class MicroAttackers extends Micro {

    final int INF = 1000000;
    Direction[] dirs = Direction.values();

    boolean attacker = false;
    boolean shouldPlaySafe = false;
    boolean alwaysInRange = false;
    boolean hurt = false;
    static int myRange;
    static int myVisionRange;
    static double myDPS;

    boolean severelyHurt = false;

    double[] DPS = new double[]{0,0,0,0,0,0};
    int[] rangeExtended = new int[]{0, 0, 0, 0,0,0, 0};

    MicroAttackers(RobotController rc){
        super(rc);
        if(rc.getType() == RobotType.LAUNCHER) attacker = true;
        myRange = rc.getType().actionRadiusSquared;
        myVisionRange = rc.getType().visionRadiusSquared;

        DPS[RobotType.CARRIER.ordinal()] = 10;
        DPS[RobotType.LAUNCHER.ordinal()] = 30;
        DPS[RobotType.DESTABILIZER.ordinal()] = 50;
        setRange();
        myDPS = DPS[rc.getType().ordinal()];
    }

    void setRange(){
        try{
            if(rc.senseMapInfo(rc.getLocation()).hasCloud()){
                rangeExtended[RobotType.CARRIER.ordinal()] = 4;
                rangeExtended[RobotType.LAUNCHER.ordinal()] = 4;
                rangeExtended[RobotType.DESTABILIZER.ordinal()] = 4;
            }else{
                rangeExtended[RobotType.CARRIER.ordinal()] = 20;
                rangeExtended[RobotType.LAUNCHER.ordinal()] = 20;
                rangeExtended[RobotType.DESTABILIZER.ordinal()] = 20;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    static double currentDPS = 0;
    static double currentRangeExtended;
    static double currentActionRadius;
    static boolean canAttack;

    boolean doMicro(){try{
            if (!rc.isMovementReady()) return false;
            setRange();
            shouldPlaySafe = false;
            severelyHurt = Util.hurt(rc.getHealth());
            RobotInfo[] units = rc.senseNearbyRobots(myVisionRange, rc.getTeam().opponent());
            canAttack = rc.isActionReady();

            int uIndex = units.length;
            while (uIndex-- > 0){
                RobotInfo r = units[uIndex];
                switch(r.getType()){
                    case CARRIER:
                    case LAUNCHER:
                    case DESTABILIZER:
                        shouldPlaySafe = true;
                        break;
                }
            }
            if (!shouldPlaySafe) return false;

            alwaysInRange = false;
            if(!canAttack) alwaysInRange = true;
            if(severelyHurt) alwaysInRange = true;

            MicroInfo[] microInfo = new MicroInfo[9];
            for (int i = 0; i < 9; ++i) microInfo[i] = new MicroInfo(dirs[i]);

            for (RobotInfo unit : units) {
                if (Clock.getBytecodeNum() > MAX_MICRO_BYTECODE) break;
                int t = unit.getType().ordinal();
                currentDPS = DPS[t] / (rc.senseMapInfo(unit.getLocation()).getCooldownMultiplier(rc.getTeam()));
                if (currentDPS <= 0) continue;
                currentRangeExtended = rangeExtended[t];
                currentActionRadius = unit.getType().actionRadiusSquared;
                microInfo[0].updateEnemy(unit);
                microInfo[1].updateEnemy(unit);
                microInfo[2].updateEnemy(unit);
                microInfo[3].updateEnemy(unit);
                microInfo[4].updateEnemy(unit);
                microInfo[5].updateEnemy(unit);
                microInfo[6].updateEnemy(unit);
                microInfo[7].updateEnemy(unit);
                microInfo[8].updateEnemy(unit);
            }

            if (myDPS > 0) {
                units = rc.senseNearbyRobots(myVisionRange, rc.getTeam());
                for (RobotInfo unit : units) {
                    if (Clock.getBytecodeNum() > MAX_MICRO_BYTECODE) break;
                    currentDPS = DPS[unit.getType().ordinal()] / (rc.senseMapInfo(unit.getLocation()).getCooldownMultiplier(rc.getTeam()));
                    microInfo[0].updateAlly(unit);
                    microInfo[1].updateAlly(unit);
                    microInfo[2].updateAlly(unit);
                    microInfo[3].updateAlly(unit);
                    microInfo[4].updateAlly(unit);
                    microInfo[5].updateAlly(unit);
                    microInfo[6].updateAlly(unit);
                    microInfo[7].updateAlly(unit);
                    microInfo[8].updateAlly(unit);
                }
            }

            MicroInfo bestMicro = microInfo[8];
            for (int i = 0; i < 8; ++i) {
                if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
            }

            if (bestMicro.dir == Direction.CENTER) return true;

            if (rc.canMove(bestMicro.dir)) {
                rc.setIndicatorString("Moving back: " + bestMicro.dir);
                rc.move(bestMicro.dir);
                return true;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }


    class MicroInfo{
        Direction dir;
        MapLocation location;
        int minDistanceToEnemy = INF;
        double DPSreceived = 0;
        double enemiesTargeting = 0;
        double alliesTargeting = 0;
        boolean canMove = true;

        public MicroInfo(Direction dir){
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if(!rc.canMove(dir)) canMove = false;
            else{
                if(!hurt){
                    try{
                        MapInfo locationInfo = rc.senseMapInfo(this.location);
                        if(canAttack){
                            this.DPSreceived -= myDPS/(locationInfo.getCooldownMultiplier(rc.getTeam()));
                            this.alliesTargeting += myDPS/(locationInfo.getCooldownMultiplier(rc.getTeam()));
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    minDistanceToEnemy = rangeExtended[RobotType.LAUNCHER.ordinal()];
                } else minDistanceToEnemy = INF;
            }
        }

        void updateEnemy(RobotInfo unit){
            if(!canMove) return;
            int dist = unit.getLocation().distanceSquaredTo(location);
            if (dist < minDistanceToEnemy)  minDistanceToEnemy = dist;
            if (dist <= currentActionRadius) DPSreceived += currentDPS;
            if (dist <= currentRangeExtended) enemiesTargeting += currentDPS;
        }

        void updateAlly(RobotInfo unit){
            if (!canMove) return;
            int dist = unit.getLocation().distanceSquaredTo(location);
            if (dist <= currentRangeExtended) alliesTargeting += currentDPS;
        }

        int safe(){
            if (!canMove) return -1;
            if (DPSreceived > 0) return 0;
            if (enemiesTargeting > alliesTargeting) return 1;
            return 2;
        }

        boolean inRange(){
            if (alwaysInRange) return true;
            return minDistanceToEnemy <= myRange;
        }

        //equal => true
        boolean isBetter(MicroInfo M){

            if (safe() > M.safe()) return true;
            if (safe() < M.safe()) return false;

            if (inRange() && !M.inRange()) return true;
            if (!inRange() && M.inRange()) return false;

            if(!severelyHurt){
                if (alliesTargeting > M.alliesTargeting) return true;
                if (alliesTargeting < M.alliesTargeting) return false;
            }

            if (inRange()) return minDistanceToEnemy >= M.minDistanceToEnemy;
            else return minDistanceToEnemy <= M.minDistanceToEnemy;
        }
    }


}
