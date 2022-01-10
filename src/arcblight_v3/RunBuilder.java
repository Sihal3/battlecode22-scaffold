package arcblight_v3;

import battlecode.common.*;

import java.util.Map;
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
        //mark home
        if(home == null){
            for(RobotInfo robot : robots){
                if(robot.team == rc.getTeam() && robot.type == RobotType.ARCHON){
                    home = robot.location;
                }
            }
        }

        RobotPlayer.marklocs(rc);

        //try to heal and upgrade
        int health = 0;
        target = null;
        for(RobotInfo robot : robots){
            if(robot.team == rc.getTeam() && robot.type.isBuilding() && robot.health < robot.type.health){
                if(robot.health > health) {
                    target = robot.location;
                    health = robot.health * 100;
                }
            }
        }
        if(target != null){
            rc.setIndicatorString("healing"+health);
            if (rc.canRepair(target)) {
                rc.repair(target);
            }
            RobotPlayer.pathfind(rc, target);
        }



        //try to build if no other towers
        Direction builder = builddir(rc);
        if (builder != null) {
            boolean tower_in_range = false;
            boolean proto = false;
            for (RobotInfo robot : robots) {
                if (robot.team == rc.getTeam() && robot.type == RobotType.WATCHTOWER) {
                    if(robot.location.distanceSquaredTo(me.add(builder)) < 11){
                        tower_in_range = true;
                    }
                    if(robot.mode == RobotMode.PROTOTYPE){
                        proto = true;
                    }
                    if(tower_in_range && proto){
                        break;
                    }
                }
            }
            if (!tower_in_range) {
                rc.setIndicatorString("building far");
                buildwatch(rc, builder);
                return;
            } else if (!proto && rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length > 0){
                rc.setIndicatorString("enemy! building");
                buildwatch(rc, builder);
                return;
            } else if (!proto && rc.getTeamLeadAmount(rc.getTeam())>2000) {
                buildwatch(rc, builder);
                return;
            }
        }



        //move away from other miners and toward empty space
        if (rc.senseLead(me) != 0) {
            target = findtarget(rc);
            if (target != null) {
                //move towards target, if exists
                RobotPlayer.pathfind(rc, target);
            } else {
                // If nothing found, move randomly.
                RobotPlayer.moverandom(rc);
            }
        }

        //self-destruct if on empty ground or clogged for too long
        //be a block if see enemy
        if(rc.getTeamLeadAmount(rc.getTeam()) > 1000 || rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length == 0){
            if(rc.senseLead(rc.getLocation()) == 0){
                rc.disintegrate();
            } else if (is_surrounded(rc)) {
                rc.disintegrate();
            }
        }


    }

    public static void buildwatch(RobotController rc, Direction dir) throws GameActionException{
        rc.buildRobot(RobotType.WATCHTOWER, dir);
    }

    public static Direction builddir(RobotController rc ) throws GameActionException{
        Direction dir = me.directionTo(home).opposite();
        for(int i = 0; i < 8; i++){
            if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                return dir;
            } else {
                dir = dir.rotateRight();
            }
        }
        return null;
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

    public static MapLocation findtarget(RobotController rc) throws GameActionException{
        if(rc.isMovementReady()) {

            //find empty space
            target = null;
            for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(me, 100)){
                if(rc.senseLead(loc) == 0 && !rc.canSenseRobotAtLocation(loc)) {
                    if(target == null) {
                        target = loc;
                    } else if (loc.distanceSquaredTo(me) < target.distanceSquaredTo(me)){
                        target = loc;
                    }
                }
            }
            if(target != null){
                rc.setIndicatorString("empty space found"+target.toString());
                return target;
            }


            //finds closest builder, multiple closest if same distance, and moves away.
            int diff;
            int multiple = 1;

            //find closest loc, mark if multiple closest
            for (RobotInfo robot : robots) {
                if (robot.team == rc.getTeam() && robot.type == RobotType.BUILDER) {
                    if (target != null) {
                        diff = robot.location.distanceSquaredTo(me) - target.distanceSquaredTo(me);
                        if (diff < 0) {
                            target = robot.location;
                            multiple = 1;
                        } else if (diff == 0) {
                            multiple++;
                        }
                    } else {
                        target = robot.location;
                    }
                } else if (robot.type == RobotType.ARCHON && robot.location.distanceSquaredTo(me) < 3) {
                    return me.subtract(me.directionTo(robot.location));
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

            if (target != null) {
                return me.subtract(me.directionTo(target));
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

}

/*public static void avoid(RobotController rc) throws GameActionException{
        //collects all the builders in sight, averages their locations, and moves away from it
        int buildercounter = 0;
        avoids = new MapLocation[robots.length];
        int xtot = 0;
        int ytot = 0;

        //find all locs
        for(RobotInfo robot : robots){
            if(robot.team == rc.getTeam()){
                if (robot.type == RobotType.BUILDER){
                    avoids[buildercounter] = robot.location;
                    buildercounter++;
                }
            }
        }

        //averaging
        for(int i = 0; i < buildercounter; i++){
            xtot += avoids[i].x;
            ytot += avoids[i].y;
        }
        target = new MapLocation((int)Math.round((double)xtot/buildercounter), (int)Math.round((double)ytot/buildercounter));

        if (rc.canMove(me.directionTo(target).opposite())){
            rc.move(me.directionTo(target).opposite());
            System.out.println("dodjing");
        }

    }*/