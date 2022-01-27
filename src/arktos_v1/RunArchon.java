package arktos_v1;

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
    static int leadcount;
    static int counttotal;


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
        MapLocation me = rc.getLocation();
        /*int minercounter = 0;
        for(RobotInfo robot : troops){
            if(robot.team == rc.getTeam() && robot.type == RobotType.MINER){
                minercounter++;
            }
        }*/
        if (rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length > 0) {
            rand = rng.nextInt(8);
            if (rand == 0) {
                // Let's try to build a miner.
                build(rc, RobotType.MINER);
            } else if (rand == 1) {
                // Let's try to build a builder.
                build(rc, RobotType.BUILDER);
            } else {
                build(rc, RobotType.SOLDIER);
            }
        } else if (rc.getTeamLeadAmount(rc.getTeam()) > 1000) {
            rand = rng.nextInt(8);
            if (rand == 0) {
                // Let's try to build a miner.
                build(rc, RobotType.MINER);
            } else if (rand < 4) {
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
            } else if (rand < 4) {
                // Let's try to build a builder.
                build(rc, RobotType.BUILDER);
            } else {
                build(rc, RobotType.SOLDIER);
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

        //add reflection locations
        if(rc.getRoundNum() == 1){
            addloc(rc, new MapLocation(me.x, rc.getMapHeight()-me.y-1));
            addloc(rc, new MapLocation(rc.getMapWidth()-me.x-1, me.y));
            addloc(rc, new MapLocation(rc.getMapWidth()-me.x-1, rc.getMapHeight()-me.y-1));
        } else if (rc.getRoundNum()%100==0){
            int x = rc.readSharedArray(0);
            int y = rc.readSharedArray(1);
            MapLocation loc = new MapLocation(x - 1, y - 1);
            System.out.println("current target: " + loc);
        }

        //keep track of lead, and by consequence, whether to rush
        if(rc.getRoundNum()%100==0){
            leadcount = 0;
            counttotal = 0;
        }
        if(rc.getTeamLeadAmount(rc.getTeam()) > rc.getTeamLeadAmount(rc.getTeam().opponent())){
            leadcount++;
        }
        counttotal++;
        if(leadcount > counttotal*3/4){
            rc.writeSharedArray(49, 1);
        } else {
            rc.writeSharedArray(49, 0);
        }


        //add home base to array
        if(rc.readSharedArray(36) == 0){
            rc.writeSharedArray(36, me.x+1);
            rc.writeSharedArray(37, me.y+1);
        } else {
            //move toward home base if secondary and if not close
            MapLocation home = new MapLocation(rc.readSharedArray(36)-1, rc.readSharedArray(37)-1);
            if(home.distanceSquaredTo(me) > 40){
                //turn into mobile
                if(rc.getMode() == RobotMode.TURRET && rc.canTransform()){
                    rc.transform();
                }

                //move towards home
                RobotPlayer.pathfind(rc, home);

            } else {

                //transform back
                if(rc.getMode() == RobotMode.PORTABLE && rc.canTransform()){
                    rc.transform();
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

    static void addloc(RobotController rc, MapLocation loc) throws GameActionException{
        System.out.println("Adding reflection loc: " + loc.toString());
        int index = 0;
        while(index < 32){
            int x = rc.readSharedArray(index);
            int y = rc.readSharedArray(index+1);
            if(x==0 && y==0){
                rc.writeSharedArray(index, loc.x+1);
                rc.writeSharedArray(index+1, loc.y+1);
                break;
            } else if (loc.equals(new MapLocation(x-1, y-1))){
                break;
            } else {
                index = index+2;
            }
        }
    }

}
