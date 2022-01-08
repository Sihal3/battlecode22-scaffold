package ryansbot;

import FireDancer.RobotPlayer;
import battlecode.common.*;

import java.util.Random;

public strictfp class RunMiner {
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
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc) throws GameActionException {

        MapLocation me = rc.getLocation();
        MapLocation target = findTarget(rc, me);

        if (target != null) {
            //move towards target, if exists
            ryansbot.RobotPlayer.pathFind(rc, target);
        } else {
            // If nothing found, move randomly.
            ryansbot.RobotPlayer.moveRandom(rc);
        }


        // Try to mine on squares around us.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
                    rc.mineLead(mineLocation);
                }
            }
        }

    }

    public static MapLocation findTarget(RobotController rc, MapLocation me) throws GameActionException{

        // disintegrate if lots of miners
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        int minercounter = 0;
        for(RobotInfo robot : robots){
            if(robot.type == RobotType.MINER){
                minercounter++;
            }
            if (minercounter > 7 && rc.senseLead(me) == 0){
                rc.disintegrate();
            }
        }


        // move away from Archon
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ARCHON && robot.location.distanceSquaredTo(me) < 5) {
                return me.subtract(me.directionTo(robot.location));
            }
        }

        // move away from soldiers
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.MINER && robot.location.distanceSquaredTo(me) < 5) {
                return me.subtract(me.directionTo(robot.location));
            }
        }

        //find gold
        MapLocation[] golds = rc.senseNearbyLocationsWithGold(100);
        if (golds.length > 0){
            return golds[1];
        }

        //find largest lead nearby
        MapLocation[] leads = rc.senseNearbyLocationsWithLead(100);
        MapLocation target = new MapLocation(0,0);

        for (MapLocation search : leads){
            if(rc.senseLead(search) > 1) {
                if (target.x != 0) {
                    if(rc.senseLead(search) > rc.senseLead(target)){
                        target = search;
                    }
                } else {
                    target = search;
                }
            }
        }

        return target.x > 0? target : null;
    }
}
