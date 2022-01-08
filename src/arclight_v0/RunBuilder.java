package arclight_v0;

import battlecode.common.*;

import java.util.Random;

strictfp class RunBuilder {
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    static boolean build;
    static RobotType buildtype;
    static RobotInfo[] robots;

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
     * Run a single turn for a Builder.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runBuilder(RobotController rc) throws GameActionException {

        MapLocation me = rc.getLocation();
        MapLocation target = findtarget(rc, me);
        build = false;
        buildtype = null;

        RobotPlayer.marklocs(rc);

        //try to heal
        int health_percent = 100;
        MapLocation healtarget = null;
        for(RobotInfo robot : robots){
            if(robot.type.isBuilding() && ((robot.health*100)/robot.type.health) < health_percent){
                healtarget = robot.location;
                health_percent = ((robot.health*100)/robot.type.health);
            }
        }
        if(healtarget != null){
            if (rc.canRepair(healtarget)) {
                rc.repair(healtarget);
            }
        }


        if (target != null) {
            //move towards target, if exists
            RobotPlayer.pathfind(rc, target);

            // Try to build
            if(build){
                if (target.isWithinDistanceSquared(me, 2) && rc.canBuildRobot(buildtype, me.directionTo(target))) {
                    rc.buildRobot(buildtype, me.directionTo(target));
                }
            }
        } else {
            // If nothing found, move randomly.
            RobotPlayer.moverandom(rc);
        }


    }

    public static MapLocation findtarget(RobotController rc, MapLocation me) throws GameActionException{

        //disintegrate if lots of miners
        robots = rc.senseNearbyRobots();
        /*minercounter = 0;
        for(RobotInfo robot : robots){
            if(robot.team == rc.getTeam() && robot.type == RobotType.MINER){
                minercounter++;
            }
            if ((minercounter > 7 || rng.nextInt(1000)==0)&& rc.senseLead(me) == 0){
                rc.disintegrate();
            }
        }


        //move away from Archon or enemy
        enemycount = 0;
        for (RobotInfo robot : robots){
            if ( robot.type == RobotType.ARCHON && robot.location.distanceSquaredTo(me) < 3){
                return me.subtract(me.directionTo(robot.location));
            }
            if (robot.team==rc.getTeam().opponent()){
                if(robot.type.canAttack()) {
                    return me.subtract(me.directionTo(robot.location));
                }
                enemycount++;
            }
        }

        //find gold
        MapLocation[] golds = rc.senseNearbyLocationsWithGold(100);
        if (golds.length > 0){
            return golds[0];
        }

        //find largest lead nearby
        MapLocation[] leads = rc.senseNearbyLocationsWithLead(100);
        MapLocation target = new MapLocation(0,0);

        for (MapLocation search : leads){
            if(rc.senseLead(search) > 1) {
                if (target.x != 0 && target.y != 0) {
                    if(rc.senseLead(search) > rc.senseLead(target)){
                        target = search;
                    }
                } else {
                    target = search;
                }
            }
        }

        return target.x > 0? target : null;*/
        return null;
    }

}
