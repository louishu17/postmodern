package louis;

import battlecode.common.*;
public class Micro {
    final int INF = 1000000;
    RobotController rc;
    Direction[] dirs = Direction.values();

    boolean attacker = false;
    boolean shouldPlaySafe = false;
    boolean alwaysInRange = false;
    boolean hurt = false;
    static int myRange;
    static int myVisionRange;
    static double myDPS;
    static final int MAX_MICRO_BYTECODE = 6000;

    double[] DPS = new double[]{0,0,0,0,0,0};
    int[] rangeExtended = new int[]{0, 0, 0, 0,0,0, 0};

    Micro(RobotController rc){
        this.rc = rc;
        if(rc.getType() == RobotType.LAUNCHER) attacker = true;
        myRange = rc.getType().actionRadiusSquared;
        myVisionRange = rc.getType().visionRadiusSquared;

        DPS[RobotType.LAUNCHER.ordinal()] = 6;
        DPS[RobotType.DESTABILIZER.ordinal()] = 5;
        rangeExtended[RobotType.LAUNCHER.ordinal()] = 16;
        rangeExtended[RobotType.DESTABILIZER.ordinal()] = 13;
        myDPS = DPS[rc.getType().ordinal()];
    }

    static double currentDPS = 0;
    static double currentRangeExtended;
    static double currentActionRadius;
    static boolean canAttack;

    final static double MAX_COOLDOWN_DIFF = 0.2;

    boolean doMicro(){
        try{
            if (!rc.isMovementReady()) return false;
            shouldPlaySafe = false;
            RobotInfo[] units = rc.senseNearbyRobots(myVisionRange, rc.getTeam().opponent());
            canAttack = rc.isActionReady();

            int uIndex = units.length;
            while (uIndex-- > 0){
                RobotInfo r = units[uIndex];
                switch(r.getType()){
                    case LAUNCHER:
                    case DESTABILIZER:
                        shouldPlaySafe = true;
                        break;
                }
            }
            if (!shouldPlaySafe) return false;

            alwaysInRange = false;
            if (!attacker || !rc.isActionReady()) alwaysInRange = true;

            MicroInfo[] microInfo = new MicroInfo[9];
            for (int i = 0; i < 9; ++i) microInfo[i] = new MicroInfo(dirs[i]);


            double minCooldown = microInfo[8].cooldown;
            if (microInfo[7].canMove && minCooldown > microInfo[7].cooldown) minCooldown = microInfo[7].cooldown;
            if (microInfo[6].canMove && minCooldown > microInfo[6].cooldown) minCooldown = microInfo[6].cooldown;
            if (microInfo[5].canMove && minCooldown > microInfo[5].cooldown) minCooldown = microInfo[5].cooldown;
            if (microInfo[4].canMove && minCooldown > microInfo[4].cooldown) minCooldown = microInfo[4].cooldown;
            if (microInfo[3].canMove && minCooldown > microInfo[3].cooldown) minCooldown = microInfo[3].cooldown;
            if (microInfo[2].canMove && minCooldown > microInfo[2].cooldown) minCooldown = microInfo[2].cooldown;
            if (microInfo[1].canMove && minCooldown > microInfo[1].cooldown) minCooldown = microInfo[1].cooldown;
            if (microInfo[0].canMove && minCooldown > microInfo[0].cooldown) minCooldown = microInfo[0].cooldown;

            minCooldown += MAX_COOLDOWN_DIFF;

            if (microInfo[8].cooldown > minCooldown) microInfo[8].canMove = false;
            if (microInfo[7].cooldown > minCooldown) microInfo[7].canMove = false;
            if (microInfo[6].cooldown > minCooldown) microInfo[6].canMove = false;
            if (microInfo[5].cooldown > minCooldown) microInfo[5].canMove = false;
            if (microInfo[4].cooldown > minCooldown) microInfo[4].canMove = false;
            if (microInfo[3].cooldown > minCooldown) microInfo[3].canMove = false;
            if (microInfo[2].cooldown > minCooldown) microInfo[2].canMove = false;
            if (microInfo[1].cooldown > minCooldown) microInfo[1].canMove = false;
            if (microInfo[0].cooldown > minCooldown) microInfo[0].canMove = false;

            for (RobotInfo unit : units) {
                if (Clock.getBytecodeNum() > MAX_MICRO_BYTECODE) break;
                int t = unit.getType().ordinal();
                currentDPS = DPS[t] * (rc.senseMapInfo(unit.getLocation()).getCooldownMultiplier(rc.getTeam()));
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
                    currentDPS = DPS[unit.getType().ordinal()] * (rc.senseMapInfo(unit.getLocation()).getCooldownMultiplier(rc.getTeam()));
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
        double cooldown = 0.0;

        public MicroInfo(Direction dir){
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if(!rc.canMove(dir)) canMove = false;
            else{
                try{
                    cooldown = rc.senseMapInfo(rc.getLocation()).getCooldownMultiplier(rc.getTeam());
                }catch(Exception e){
                    e.printStackTrace();
                }

                if(!hurt){
                    try{
                        MapInfo locationInfo = rc.senseMapInfo(this.location);
                        if(canAttack){
                            this.DPSreceived -= myDPS*(locationInfo.getCooldownMultiplier(rc.getTeam()));
                            this.alliesTargeting += myDPS*(locationInfo.getCooldownMultiplier(rc.getTeam()));
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
            alliesTargeting += currentDPS;
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

            if (hurt) return true;

            if (inRange() && !M.inRange()) return true;
            if (!inRange() && M.inRange()) return false;

            if (cooldown < M.cooldown) return true;
            if (M.cooldown < cooldown) return false;

            if (alliesTargeting > M.alliesTargeting) return true;
            if (alliesTargeting < M.alliesTargeting) return false;

            if (inRange()) return minDistanceToEnemy >= M.minDistanceToEnemy;
            else return minDistanceToEnemy <= M.minDistanceToEnemy;
        }
    }


}
