package ryansbot;

import battlecode.common.*;

import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

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
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        // System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!
            System.out.println("Age: " + turnCount + "; Location: " + rc.getLocation());

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case ARCHON:     runArchon(rc);  break;
                    case MINER:      runMiner(rc);   break;
                    case SOLDIER:    runSoldier(rc); break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER:
                    case BUILDER:
                    case SAGE:       break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runArchon(RobotController rc) throws GameActionException {
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.getRobotCount() <= 25) {
            // Let's try to build a miner.
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        } else {
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        }
    }

    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        for (MapLocation mineLocation : rc.getAllLocationsWithinRadiusSquared(me, 2)){
            while (rc.canMineGold(mineLocation)) {
                rc.mineGold(mineLocation);
            }
            while (rc.canMineLead(mineLocation)) {
                rc.mineLead(mineLocation);
            }
        }

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

        // move to loc if found
        if (target.distanceSquaredTo(me) <= 20) {
            if (rc.canMove(me.directionTo(target))){
                rc.move(me.directionTo(target));
                return;
            }
        }

        // move away from miners if no target
        MapLocation closest = new MapLocation(0,0);
        for(RobotInfo robot : rc.senseNearbyRobots()){
            if(robot.team == rc.getTeam()){
                if (robot.type == RobotType.MINER || robot.type == RobotType.SOLDIER){
                    if(robot.location.distanceSquaredTo(me) < closest.distanceSquaredTo(me)){
                        closest = robot.location;
                    }
                }
            }
        }
        if(closest.isWithinDistanceSquared(me,20)){
            if (rc.canMove(me.directionTo(closest).opposite())){
                rc.move(me.directionTo(closest).opposite());
                return;
            }
        }

        // If nothing found, move randomly.
        moveRandom(rc);
    }

    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runSoldier(RobotController rc) throws GameActionException {
        // Move to attack, and prioritize the Archon
        MapLocation me = rc.getLocation();
        MapLocation closestEnemy = new MapLocation(0,0);
        MapLocation archonLoc = new MapLocation(0, 0);
        for(RobotInfo robot : rc.senseNearbyRobots()){
            if(robot.team == rc.getTeam().opponent()) {
                if (robot.type == RobotType.ARCHON) {
                    closestEnemy = robot.location;
                    archonLoc = robot.location;
                }

                else if (robot.type == RobotType.SOLDIER || robot.type == RobotType.MINER) {
                    if (robot.location.distanceSquaredTo(me) < closestEnemy.distanceSquaredTo(me)) {
                        closestEnemy = robot.location;
                    }
                }
            }
        }
        if (closestEnemy.isWithinDistanceSquared(me,20)){
            if (rc.canMove(me.directionTo(closestEnemy))){
                rc.move(me.directionTo(closestEnemy));
                return;
            }
        }

        // Move robots to Archon
        rc.writeSharedArray(archonLoc.x, archonLoc.y);
        MapLocation sharedLoc = new MapLocation(rc.readSharedArray(0), rc.readSharedArray(1));
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.team == rc.getTeam().opponent() && robot.type == RobotType.ARCHON) {
                if (rc.canMove(me.directionTo(sharedLoc))) {
                    rc.move(me.directionTo(sharedLoc));
                }
            }
            // Try to attack closest enemy
            else if (robot.team == rc.getTeam().opponent()) {
                if (robot.type == RobotType.SOLDIER || robot.type == RobotType.MINER) {
                    if (rc.canAttack(closestEnemy)) {
                        rc.attack(closestEnemy);
                    }
                }
            }
        }

        // Moves away from other soldiers
        MapLocation closest = new MapLocation(0,0);
        for(RobotInfo robot : rc.senseNearbyRobots()){
            if(robot.team == rc.getTeam()){
                if (robot.type == RobotType.SOLDIER){
                    if(robot.location.distanceSquaredTo(me) < closest.distanceSquaredTo(me)){
                        closest = robot.location;
                    }
                }
            }
        }
        if (closest.isWithinDistanceSquared(me,20)){
            if (rc.canMove(me.directionTo(closest).opposite())){
                rc.move(me.directionTo(closest).opposite());
                return;
            }
        }

        // try to move randomly.
        moveRandom(rc);
    }

    static void moveRandom(RobotController rc) throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}
