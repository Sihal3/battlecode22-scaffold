package arcblight_v3;

import battlecode.common.*;

import java.util.Random;

strictfp class RunMiner {
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static int enemycount;

    /** Array containing all the possible movement directions. */

    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc) throws GameActionException {

        MapLocation me = rc.getLocation();
        MapLocation target = findtarget(rc, me);

        RobotPlayer.marklocs(rc);

        if (target != null) {
            //move towards target, if exists
            RobotPlayer.pathfind(rc, target);
        } else {
            // If nothing found, move randomly.
            RobotPlayer.moverandom(rc);
        }


        // Try to mine on squares around us.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation) && (rc.senseLead(mineLocation) > 1 || enemycount>5)) {
                    rc.mineLead(mineLocation);
                }
            }
        }

    }

    public static MapLocation findtarget(RobotController rc, MapLocation me) throws GameActionException{
        RobotInfo[] robots = rc.senseNearbyRobots();


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

        return target.x > 0? target : null;
    }


}
