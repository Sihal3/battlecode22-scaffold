package arcblight_v1;

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
    static int[] dir_counts;


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
        //tally up enemy directions
        for (RobotInfo robot : robots){
            dir_counts = new int[8];
            if ( robot.type == RobotType.ARCHON && robot.location.distanceSquaredTo(me) < 3){
                return me.subtract(me.directionTo(robot.location));
            }
            if (robot.team==rc.getTeam().opponent()){
                dir_counts[RobotPlayer.dir_to_num(me.directionTo(robot.location))]++;
                if(robot.type.canAttack()) {
                    return me.subtract(me.directionTo(robot.location));
                }
                enemycount++;
            }
        }
        for(int i = 0; i < 8; i++){
            rc.writeSharedArray(i+56, rc.readSharedArray(i+56)+dir_counts[i]);
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
