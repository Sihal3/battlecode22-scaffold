package FireDancer;

import battlecode.common.*;

import java.util.Random;

strictfp class RunSoldier {
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    static MapLocation[] archons;

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runSoldier(RobotController rc) throws GameActionException {

        if (RobotPlayer.turnCount == 0){
            archons = RobotPlayer.markarchons(rc);
        }

        MapLocation me = rc.getLocation();
        MapLocation target = findtarget(rc, me);

        if (target.distanceSquaredTo(me) <= 1000) {
            //move towards target, if exists
            RobotPlayer.pathfind(rc, target);
        } else {
            // If nothing found, move randomly.
            RobotPlayer.moverandom(rc);
        }

        // Try to attack someone
        if (rc.canAttack(target)) {
            rc.attack(target);
        }


    }
    public static MapLocation findtarget(RobotController rc, MapLocation me) throws GameActionException {

        //run home if greviously injured
        if(rc.getHealth() < 15){
            archons = RobotPlayer.markarchons(rc);
            MapLocation closest = new MapLocation(0,0);
            for(MapLocation loc : archons){
                if(loc.distanceSquaredTo(me) < closest.distanceSquaredTo(me)){
                    closest = loc;
                }
            }
            return closest;
        }

        //find enemies around
        MapLocation target = new MapLocation(0,0);
        /*int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            for (RobotInfo enemy : enemies){
                if(enemy.location.distanceSquaredTo(me) < target.distanceSquaredTo(me)){
                    target = enemy.location;
                }
            }
            return target;
        }*/

        /*/away from other soldiers
        RobotInfo[] troops = rc.senseNearbyRobots(radius, rc.getTeam());
        for (RobotInfo robot : troops){
            if(robot.type == RobotType.SOLDIER && robot.location.distanceSquaredTo(me) < target.distanceSquaredTo(me)){
                target = robot.location;
            }
        }*/
        return target;

    }
}
