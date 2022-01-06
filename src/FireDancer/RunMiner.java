package FireDancer;

import battlecode.common.*;

import java.util.Random;

strictfp class RunMiner {
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

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

        //find gold/lead around miner
        MapLocation target = new MapLocation(0,0);
        boolean found_gold = false;
        for (MapLocation search : rc.getAllLocationsWithinRadiusSquared(me, 20)){
            if (rc.senseGold(search) > 0){
                if(search.distanceSquaredTo(me) < target.distanceSquaredTo(me)) {
                    target = search;
                    found_gold = true;
                }
            }
            if(!found_gold && rc.senseLead(search) > 0) {
                if (search.distanceSquaredTo(me) < target.distanceSquaredTo(me)) {
                    target = search;
                }
            }
        }

        //quit if sitting on lead
        if (rc.senseLead(me) > 0 && !found_gold) {
            return;
        }

        // move to loc if found
        if (target.distanceSquaredTo(me) <= 20) {
            if (rc.canMove(me.directionTo(target))) {
                rc.move(me.directionTo(target));
            }
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

        // If nothing found, move randomly.
        RobotPlayer.moverandom(rc);
    }
}
