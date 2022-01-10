package arcblight_v1;

import battlecode.common.*;

import java.util.Random;

strictfp class RunWatch {
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static int since_enemy = 0;
    static int enemycount;
    static boolean mobile = false;
    static RobotInfo[] robots;
    static boolean avoidwatch = false;
    static int[] dir_counts;


    /**
     * Run a single turn for a Watchtower.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runWatchtower(RobotController rc) throws GameActionException {
        since_enemy++;

        MapLocation me = rc.getLocation();
        robots = rc.senseNearbyRobots(-1, rc.getTeam());

        //decide mode
        if(rc.getMode() == RobotMode.PROTOTYPE) {
            mobile = near_watch(rc, me);
        }

        //attack enemy if present
        MapLocation enemy = findenemy(rc, me);
        if(enemy != null){
            if(rc.canAttack(enemy)){
                rc.attack(enemy);
            }
            since_enemy = 0;
        }

        //find heal targets
        MapLocation target = null;
        int health_percent = 100;
        for(RobotInfo robot : robots){
            if(robot.type.isBuilding() && ((robot.health*100)/robot.type.health) < health_percent){
                target = robot.location;
                health_percent = ((robot.health*100)/robot.type.health);
            }
        }
        //try to heal
        if(target != null){
            if (rc.canRepair(target)) {
                rc.repair(target);
            }
        }

        if(mobile) {
            //go to mobile mode
            if (since_enemy > 20 && near_watch(rc, me)) {
                if (rc.getMode() == RobotMode.TURRET && rc.canTransform()) {
                    rc.transform();
                }
            }
            //go to turret mode
            if (enemycount > 1) {
                if (rc.getMode() == RobotMode.PORTABLE && rc.canTransform()) {
                    rc.transform();
                }
            }

            //move if in portable
            if (rc.getMode() == RobotMode.PORTABLE) {
                target = findtarget(rc, me);
                if (target != null) {
                    //move towards target, if exists
                    RobotPlayer.pathfind(rc, target);
                } else {
                    // If nothing found, move randomly.
                    RobotPlayer.pathfind(rc, RobotPlayer.get_enemy_dir(rc));
                }
            }
        }

    }

    public static MapLocation findenemy(RobotController rc, MapLocation me) throws GameActionException{
        MapLocation enemy = null;
        int enemhealth = 10000;
        int attackpriority = 0;
        enemycount = 0;
        dir_counts = new int[8];
        for(RobotInfo robot : rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent())) {
            dir_counts[RobotPlayer.dir_to_num(rc.getLocation().directionTo(robot.location))]++;

            if (robot.type == RobotType.ARCHON) {
                if(attackpriority < 3){
                    attackpriority = 3;
                    enemy = null;
                    enemhealth = 10000;
                }
                if (robot.health < enemhealth) {
                    enemy = robot.location;
                    enemhealth = robot.health;
                }
            } else if (attackpriority < 3 && robot.type == RobotType.WATCHTOWER) {
                if(attackpriority < 2){
                    attackpriority = 2;
                    enemy = null;
                    enemhealth = 10000;
                }
                if (robot.health < enemhealth) {
                    enemy = robot.location;
                    enemhealth = robot.health;
                }
            } else if (attackpriority < 2 && robot.type == RobotType.SOLDIER) {
                if(attackpriority < 1){
                    attackpriority = 1;
                    enemy = null;
                }
                if (robot.health < enemhealth) {
                    enemy = robot.location;
                    enemhealth = robot.health;
                }
            } else {
                if (robot.health < enemhealth) {
                    enemy = robot.location;
                    enemhealth = robot.health;
                }
            }
            enemycount++;
        }
        for(int i = 0; i < 8; i++){
            rc.writeSharedArray(i+56, rc.readSharedArray(i+56)+dir_counts[i]);
        }
        return enemy;
    }

    public static MapLocation findtarget(RobotController rc, MapLocation me) throws GameActionException{

        //find enemies around
        MapLocation target = new MapLocation(0,0);
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            for (RobotInfo enemy : enemies){
                if(enemy.location.distanceSquaredTo(me) < target.distanceSquaredTo(me)){
                    target = enemy.location;
                }
            }
            return (target.distanceSquaredTo(me)>20)? target : null;
        }

        //away from other watchtowers
        if (avoidwatch) {
            RobotInfo[] troops = rc.senseNearbyRobots(radius, rc.getTeam());
            for (RobotInfo robot : troops) {
                if (robot.type == RobotType.WATCHTOWER) {
                    if (robot.location.distanceSquaredTo(me) < target.distanceSquaredTo(me)) {
                        target = me.subtract(me.directionTo(robot.location));
                    }
                }
            }
        }

        //check target array
        int x = rc.readSharedArray(0);
        int y = rc.readSharedArray(1);
        if(!(x==0 && y==0)) {
            target = new MapLocation(x - 1, y - 1);
        }
        RobotPlayer.removelocs(rc);

        return (target.x > 0)? target : null;
    }

    public static boolean near_watch(RobotController rc, MapLocation me) throws GameActionException{
        for(RobotInfo robot : robots){
            if(robot.type == RobotType.WATCHTOWER && robot.location.distanceSquaredTo(me) < 11){
                return true;
            }
        }

        return false;
    }
}
