package arcblight_v1;

import battlecode.common.*;

import java.util.Random;

strictfp class RunArchon {
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    static int rand;


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
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        if (rc.getTeamLeadAmount(rc.getTeam()) > 1000 || rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length > 0) {
            rand = rng.nextInt(3);
            if (rand == 0) {
                // Let's try to build a miner.
                build(rc, RobotType.MINER);
            } else if(rand == 1) {
                // Let's try to build a builder.
                build(rc, RobotType.BUILDER);
            } else {
                build(rc, RobotType.SOLDIER);
            }
        } else {
            //build miner or builder
            rand = rng.nextInt(2);
            if (rand == 0) {
                // Let's try to build a miner.
                build(rc, RobotType.MINER);
            } else {
                // Let's try to build a builder.
                build(rc, RobotType.BUILDER);
            }
        }


        //heal troops
        RobotInfo[] troops = rc.senseNearbyRobots();
        for(RobotInfo robot : troops){
            if (robot.team == rc.getTeam() && !robot.type.isBuilding()){
                if (robot.health < robot.type.health && rc.canRepair(robot.location)){
                    rc.repair(robot.location);
                }
            }
        }
    }

    static void build(RobotController rc, RobotType type) throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        for(int i = 0; i < 8; i++){
            if (rc.canBuildRobot(type, dir)) {
                rc.buildRobot(type, dir);
                break;
            } else {
                dir = dir.rotateRight();
            }
        }
    }

}
