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
    static int[] dir_counts;

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

        RobotInfo[] troops = rc.senseNearbyRobots();
        /*int minercounter = 0;
        for(RobotInfo robot : troops){
            if(robot.team == rc.getTeam() && robot.type == RobotType.MINER){
                minercounter++;
            }
        }*/

        if (rc.getTeamLeadAmount(rc.getTeam()) > 1000 || rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length > 0) {
            rand = rng.nextInt(8);
            if (rand == 0) {
                // Let's try to build a miner.
                build(rc, RobotType.MINER);
            } else if(rand < 5) {
                // Let's try to build a builder.
                build(rc, RobotType.BUILDER);
            } else {
                build(rc, RobotType.SOLDIER);
            }
        } else {
            //build miner or builder
            rand = rng.nextInt(6);
            if (RobotPlayer.turnCount < 30 || rand == 0) {
                // Let's try to build a miner.
                build(rc, RobotType.MINER);
            } else {
                // Let's try to build a builder.
                build(rc, RobotType.BUILDER);
            }
        }


        //heal troops
        for(RobotInfo robot : troops){
            if (robot.team == rc.getTeam() && !robot.type.isBuilding()){
                if (robot.health < robot.type.health && rc.canRepair(robot.location)){
                    rc.repair(robot.location);
                }
            }
        }

        //clean enemy troop direction logs
        if(RobotPlayer.turnCount%50 == 0){
            if(rc.readSharedArray(56) != 0){

            }
        }

        //tally up enemy directions
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length > 0) {
            dir_counts = new int[8];
            for (RobotInfo enemy : enemies){
                //add to dircounts
                dir_counts[RobotPlayer.dir_to_num(rc.getLocation().directionTo(enemy.location))]++;
            }
            for(int i = 0; i < 8; i++){
                rc.writeSharedArray(i+56, rc.readSharedArray(i+56)+dir_counts[i]);
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
