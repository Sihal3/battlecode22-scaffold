package arktos_v1;

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

/*
import battlecode.common.*;
import scala.collection.mutable.Map;

import java.util.Random;

strictfp class RunMiner {
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     *//*
    static boolean mineToZero = false;
    static MapLocation home;
    static boolean safe = true;
    static MapLocation searchVector;
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */

    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    /*static void runMiner(RobotController rc) throws GameActionException {

        home = new MapLocation(rc.readSharedArray(36)-1, rc.readSharedArray(37)-1);
        if(searchVector == null || rc.canSenseLocation(searchVector)){
            searchVector = findSearchVector(rc);
        }

        MapLocation me = rc.getLocation();
        MapLocation target = findtarget(rc, me);
        if(me.distanceSquaredTo(home) > 600 && !safe){
            mineToZero = true;
        } else {
            mineToZero = false;
        }

        RobotPlayer.marklocs(rc);

        if (target != null) {
            //move towards target, if exists
            RobotPlayer.pathfind(rc, target);
        }


        // Try to mine on squares around us.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation) && (rc.senseLead(mineLocation) > 1 || mineToZero)) {
                    rc.mineLead(mineLocation);
                }
            }
        }

        //go
    }

    public static MapLocation findtarget(RobotController rc, MapLocation me) throws GameActionException{
        RobotInfo[] robots = rc.senseNearbyRobots();
        MapLocation target = new MapLocation(1000, 1000);

        //join lattice if in safe zone
        if(safeZone(rc, robots)){
            //if already in lattice
            if(me.x % 3 == 0 && me.y % 3 == 0 && rc.senseNearbyLocationsWithLead(2).length > 0){
                return null;
            } else {
                //find nearest lattice point
                for(MapLocation loc : rc.getAllLocationsWithinRadiusSquared(me,100)){
                    //is on lattice?
                    if(loc.x % 3 == 0 && loc.y % 3 == 0){
                        //is not taken by miner?
                        if(!rc.isLocationOccupied(loc) || (rc.senseRobotAtLocation(loc).type != RobotType.MINER)) {
                            //has lead?
                            if (rc.senseNearbyLocationsWithLead(loc, 2, 0).length > 0) {
                                //closer than current target?
                                if(loc.distanceSquaredTo(me) < target.distanceSquaredTo(me)){
                                    target = loc;
                                }
                            }
                        }
                    }
                }
            }
            if (target != null){
                return target;
            } else {
                return searchVector;
            }
        }

        target = searchVector;

        //find gold
        MapLocation[] golds = rc.senseNearbyLocationsWithGold(100);
        if (golds.length > 0) {
            return golds[0];
        }

        //find largest lead nearby
        MapLocation[] leads = rc.senseNearbyLocationsWithLead(100);

        for (MapLocation search : leads) {
            if (rc.senseLead(search) > 1) {
                if (target.x != 1000 && target.y != 1000) {
                    if (rc.senseLead(search) > rc.senseLead(target)) {
                        target = search;
                    }
                } else {
                    target = search;
                }
            }
        }

        return target.x < 1000 ? target : null;
    }

    public static boolean safeZone(RobotController rc, RobotInfo[] robots){
        for(RobotInfo robot : robots){
            if(robot.team == rc.getTeam() && !(robot.type == RobotType.MINER) && !(robot.type == RobotType.SOLDIER)){
                safe = true;
                return true;
            }
        }
        safe = false;
        return false;
    }

    public static MapLocation findSearchVector(RobotController rc){
        int rand = rng.nextInt(rc.getMapWidth()+rc.getMapHeight());
        if(rand < rc.getMapWidth()){
            if(home.x < rc.getMapWidth()/2){
                return new MapLocation(rc.getMapWidth()-1, rng.nextInt(rc.getMapHeight()));
            } else {
                return new MapLocation(0, rng.nextInt(rc.getMapHeight()));
            }
        } else {
            if(home.y < rc.getMapHeight()/2){
                return new MapLocation(rng.nextInt(rc.getMapWidth()),rc.getMapHeight()-1);
            } else {
                return new MapLocation(rng.nextInt(rc.getMapWidth()),0);
            }
        }
    }

}
*/