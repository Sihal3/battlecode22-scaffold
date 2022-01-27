package arktos_v1;

import battlecode.common.*;

import java.util.Random;

strictfp class RunWatch {
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static int since_enemy = 100;
    static int enemycount;
    static boolean mobile = false;
    static RobotInfo[] robots;

    /**
     * Run a single turn for a Watchtower.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runWatchtower(RobotController rc) throws GameActionException {
        since_enemy++;

        RobotPlayer.marklocs(rc);

        MapLocation me = rc.getLocation();
        robots = rc.senseNearbyRobots(-1, rc.getTeam());


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


        //go to mobile mode
        if (since_enemy > 100) {
            if (rc.getMode() == RobotMode.TURRET && rc.canTransform()) {
                rc.transform();
            }
        }

        //go to turret mode
        if (enemycount > 1) {
            if (rc.getMode() == RobotMode.PORTABLE && rc.canTransform()) {
                rc.transform();
            }
        } //else if (enemycount<100 && watchWall())

        //move if in portable
        if (rc.getMode() == RobotMode.PORTABLE) {
            target = findtarget(rc, me);
            if (target != null) {
                //move towards target, if exists
                RobotPlayer.pathfind(rc, target);
            } else {
                // If nothing found, move randomly.
                RobotPlayer.moverandom(rc);
            }
        }


    }

    public static MapLocation findenemy(RobotController rc, MapLocation me) throws GameActionException{
        MapLocation enemy = null;
        int enemhealth = 10000;
        int attackpriority = 0;
        enemycount = 0;
        for(RobotInfo robot : rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent())) {
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
            } else if (attackpriority < 3 && (robot.type == RobotType.WATCHTOWER && robot.type == RobotType.SAGE)) {
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
        return enemy;
    }

    public static MapLocation findtarget(RobotController rc, MapLocation me) throws GameActionException{

        RobotPlayer.removelocs(rc);

        //find enemies around
        MapLocation target = new MapLocation(1000,1000);
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            for (RobotInfo enemy : enemies){
                if(enemy.location.distanceSquaredTo(me) < target.distanceSquaredTo(me)){
                    target = enemy.location;
                }
            }
            return target;
        }

        //check target array
        if(rc.readSharedArray(49) == 1){
            int x = rc.readSharedArray(0);
            int y = rc.readSharedArray(1);
            if(!(x==0 && y==0)) {
                return new MapLocation(x - 1, y - 1);
            } else if (rc.readSharedArray(16) != 0){
                return new MapLocation(rc.readSharedArray(16) - 1, rc.readSharedArray(17) - 1);
            }
        }

        //move towards planted watches
        //finds closest builder, multiple closest if same distance, and moves away.
        int diff;
        int multiple = 1;
        boolean wall = false;

        //find closest loc, mark if multiple closest
        for (RobotInfo robot : robots) {
            if (robot.team == rc.getTeam() && robot.type == RobotType.WATCHTOWER) {
                if(!wall && robot.mode == RobotMode.PORTABLE) {
                    diff = robot.location.distanceSquaredTo(me) - target.distanceSquaredTo(me);
                    if (diff < 0) {
                        target = robot.location;
                        multiple = 1;
                    } else if (diff == 0) {
                        multiple++;
                    }
                } else if (!wall && robot.mode == RobotMode.TURRET){
                    wall = true;
                    target = robot.location;
                    multiple = 1;
                } else if (wall && robot.mode == RobotMode.TURRET){
                    if(target.distanceSquaredTo(me) > robot.location.distanceSquaredTo(me)){
                        target = robot.location;
                    }
                }

            }
        }

        // if multiple, average
        if (multiple > 1) {
            int dx = 0;
            int dy = 0;
            for (RobotInfo robot : rc.senseNearbyRobots(target.distanceSquaredTo(me), rc.getTeam())) {
                if (robot.type == RobotType.BUILDER) {
                    dx += robot.location.x - me.x;
                    dy += robot.location.y - me.y;
                }
            }
            target = new MapLocation(me.x + dx, me.y + dy);
        }

        //inverts direction if moving away instead of walling
        if(!wall){
            target = me.subtract(me.directionTo(target));
        }

        return (target.x < 1000)? target : null;
    }


    public static MapLocation watchWall(RobotController rc, MapLocation me) throws GameActionException{
        MapLocation target = new MapLocation(1000,1000);
        for(RobotInfo robot : robots){
            if(robot.type == RobotType.WATCHTOWER && robot.mode == RobotMode.TURRET){
                if(target.distanceSquaredTo(me) > robot.location.distanceSquaredTo(me)){
                    target = robot.location;
                }
            }
        }
        return target;

    }
}
