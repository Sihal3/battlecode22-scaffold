package FireDancer;

import battlecode.common.*;
import scala.collection.Map;

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

        RobotPlayer.marklocs(rc);

        MapLocation me = rc.getLocation();
        MapLocation target = findtarget(rc, me);

        if (target != null) {
            //move towards target, if exists
            RobotPlayer.pathfind(rc, target);
        } else {
            // If nothing found, move randomly.
            RobotPlayer.moverandom(rc);
        }

        // Try to attack someone
        MapLocation enemy = findenemy(rc, me);
        if (enemy != null && rc.canAttack(enemy)) {
            rc.attack(enemy);
        }


    }
    public static MapLocation findtarget(RobotController rc, MapLocation me) throws GameActionException{

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
        int x = rc.readSharedArray(0);
        int y = rc.readSharedArray(1);
        if(!(x==0 && y==0)){
            target = new MapLocation(x-1, y-1);

            //remove from array if not building anymore
            if(rc.canSenseLocation(target)){
                if (!rc.canSenseRobotAtLocation(target) || !rc.senseRobotAtLocation(target).type.isBuilding()){
                    int index = 0;
                    while (index < 16){
                        if(rc.readSharedArray(index) == 0){
                            break;
                        }
                        rc.writeSharedArray(index, index +2);
                        rc.writeSharedArray(index+1, index +2);
                    }
                }
            }
        }

        //away from other soldiers
        int soldiercount = 0;
        RobotInfo[] troops = rc.senseNearbyRobots(radius, rc.getTeam());
        for (RobotInfo robot : troops){
            if(robot.type == RobotType.SOLDIER){
                soldiercount++;
                if (soldiercount > 3 && robot.location.distanceSquaredTo(me) < target.distanceSquaredTo(me)){
                    target = me.subtract(me.directionTo(robot.location));
                }
            }
        }
        return (target.x > 0)? target : null;
    }

    public static MapLocation findenemy(RobotController rc, MapLocation me) throws GameActionException{
        MapLocation enemy = new MapLocation(0,0);
        int enemhealth = 10000;
        int attackpriority = 0;
        for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam().opponent())) {
            if (robot.type == RobotType.ARCHON) {
                if(attackpriority < 3){
                    attackpriority = 3;
                    enemy = new MapLocation(0,0);
                    enemhealth = 10000;
                }
                if (robot.health < enemhealth) {
                    enemy = robot.location;
                    enemhealth = robot.health;
                }
            } else if (attackpriority < 3 && robot.type == RobotType.WATCHTOWER) {
                if(attackpriority < 2){
                    attackpriority = 2;
                    enemy = new MapLocation(0,0);
                    enemhealth = 10000;
                }
                if (robot.health < enemhealth) {
                    enemy = robot.location;
                    enemhealth = robot.health;
                }
            } else if (attackpriority < 2 && robot.type == RobotType.SOLDIER) {
                if(attackpriority < 1){
                    attackpriority = 1;
                    enemy = new MapLocation(0,0);
                }
                if (robot.health < enemhealth) {
                    enemy = robot.location;
                    enemhealth = robot.health;
                }
            } else {
                if (robot.location.distanceSquaredTo(me) < enemy.distanceSquaredTo(me)) {
                    enemy = robot.location;
                }
            }
        }
        return (enemy.x > 0)? enemy : null;
    }
}
