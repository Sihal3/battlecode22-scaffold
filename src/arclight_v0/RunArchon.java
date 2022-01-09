package arclight_v0;

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
    static int index;


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

        if (RobotPlayer.turnCount == 0){
            index = 0;
            while(index < 10) {
                if (rc.readSharedArray(index) == 0) {
                    rc.writeSharedArray(index, me.x);
                    rc.writeSharedArray(index+1, me.y);
                    break;
                } else {
                    index++;
                }
            }
        }

        if(RobotPlayer.turnCount > 300 && rc.getTeamLeadAmount(rc.getTeam()) < 200) {
            //heal troops
            RobotInfo[] troops = rc.senseNearbyRobots();
            for(RobotInfo robot : troops){
                if (robot.team == rc.getTeam() && !robot.type.isBuilding()){
                    if (robot.health < robot.type.health && rc.canRepair(robot.location)){
                        rc.repair(robot.location);
                    }
                }
            }
            return;
        }


        int rand = rng.nextInt(100);

        //build factor
        int bf = (Math.max(0, (500 - rc.getTeamLeadAmount(rc.getTeam()))) / 100) + 1;
        if (RobotPlayer.turnCount % bf == (rc.getID() % bf)) {
            if (RobotPlayer.turnCount < 30) {
                build(rc, RobotType.MINER);
            } else {
                if (rand > 50) {
                    build(rc, RobotType.SOLDIER);
                } else if (rand <= 45) {
                    build(rc, RobotType.MINER);
                } else {
                    build(rc, RobotType.BUILDER);
                }
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
