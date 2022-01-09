package arcblight_v1;

import battlecode.common.*;

import java.util.Random;

strictfp class RunBuilder {
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static boolean build;
    static RobotType buildtype;
    static RobotInfo[] robots;
    static MapLocation home;
    static MapLocation target;
    static MapLocation me;
    static int surrounded_count = 0;


    /**
     * Run a single turn for a Builder.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runBuilder(RobotController rc) throws GameActionException {

        me = rc.getLocation();
        build = false;
        buildtype = null;
        robots = rc.senseNearbyRobots();

        RobotPlayer.marklocs(rc);

        //try to heal
        int health_percent = 100;
        for(RobotInfo robot : robots){
            if(robot.team == rc.getTeam() && robot.type.isBuilding() && ((robot.health*100)/robot.type.health) < health_percent){
                target = robot.location;
                //health_percent = ((robot.health*100)/robot.type.health);
            }
        }
        if(target != null){
            if (rc.canRepair(target)) {
                rc.repair(target);
            }
        }

        //move away from home
        for (RobotInfo robot : robots){
            if (robot.type == RobotType.ARCHON){
                home = robot.location;
            }
            if(rc.canMove(me.directionTo(home).opposite())){
                rc.move(me.directionTo(home).opposite());
            }
        }

        //try to build
        boolean tower_in_range = false;
        for (RobotInfo robot : robots){
            if(robot.team == rc.getTeam() && robot.type == RobotType.WATCHTOWER && robot.location.distanceSquaredTo(me) < 9){
                tower_in_range = true;
                break;
            }
        }
        if(!tower_in_range){
            buildwatch(rc);
        }

        //self-destruct if on empty ground
        if(rc.senseLead(me) == 0){
            rc.disintegrate();
        } else if (is_surrounded(rc)) {
            rc.disintegrate();
        }


    }

    public static void buildwatch(RobotController rc) throws GameActionException{
        Direction dir = me.directionTo(home).opposite();
        for(int i = 0; i < 8; i++){
            if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                rc.buildRobot(RobotType.WATCHTOWER, dir);
                break;
            } else {
                dir = dir.rotateRight();
            }
        }
    }

    public static boolean is_surrounded(RobotController rc) throws GameActionException{
        if(rc.senseNearbyRobots(2).length >= 8){
            surrounded_count++;
        } else {
            surrounded_count = 0;
        }
        if (surrounded_count > 50){
            return true;
        } else {
            return false;
        }
    }

}
