package arcblight_v2;

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
    static RobotController rc;
    static boolean probmode = false;
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

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case ARCHON:     RunArchon.runArchon(rc);  break;
                    case MINER:      RunMiner.runMiner(rc);   break;
                    case SOLDIER:    RunSoldier.runSoldier(rc); break;
                    case LABORATORY: RunLab.runLaboratory(rc); break;// Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: RunWatch.runWatchtower(rc); break;// You might want to give them a try!
                    case BUILDER:    RunBuilder.runBuilder(rc); break;
                    case SAGE:       RunSage.runSage(rc); break;
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


    static void moverandom(RobotController rc) throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        candomove(rc, dir);
    }

    static boolean pathfind(RobotController rc, MapLocation target) throws GameActionException{
        return pathfind(rc, rc.getLocation().directionTo(target));
    }

    static boolean pathfind(RobotController rc, Direction dir) throws GameActionException{
        if (candomove(rc, dir)) {
            return true;
        } else {
            int one = (int) (rng.nextInt(2));
            if(one == 1) {
                if (candomove(rc, dir.rotateLeft()))  return true;
                else if (candomove(rc, dir.rotateRight())) return true;
                else if (candomove(rc, directions[rng.nextInt(directions.length)])) return true;
            }
            if(one == 0) {
                if (candomove(rc, dir.rotateRight()))  return true;
                else if (candomove(rc, dir.rotateLeft())) return true;
                else if (candomove(rc, directions[rng.nextInt(directions.length)])) return true;
            }
        }
        return false;
    }

    static boolean candomove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }


    public static void marklocs(RobotController rc) throws GameActionException{
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo enemy : enemies){
            if(enemy.type.isBuilding()){
                int index = 0;
                while(index < 16){
                    int x = rc.readSharedArray(index);
                    int y = rc.readSharedArray(index+1);
                    if(x==0 && y==0){
                        rc.writeSharedArray(index, enemy.location.x+1);
                        rc.writeSharedArray(index+1, enemy.location.y+1);
                        break;
                    } else if (enemy.location.equals(new MapLocation(x-1, y-1))){
                        break;
                    } else {
                        index = index+2;
                    }
                }
            }
        }
    }

    public static void removelocs(RobotController rc) throws GameActionException{
        int index = 0;
        MapLocation loc;
        int counter;
        while(index < 16){
            int x = rc.readSharedArray(index);
            int y = rc.readSharedArray(index+1);
            if(!(x==0 && y==0)){
                loc = new MapLocation(x-1, y-1);

                //remove from array if not building anymore
                if(rc.canSenseLocation(loc)){
                    if (!rc.canSenseRobotAtLocation(loc) || !rc.senseRobotAtLocation(loc).type.isBuilding()){
                        counter = index;
                        while (counter < 16){
                            if(rc.readSharedArray(counter) == 0){
                                break;
                            }
                            rc.writeSharedArray(counter, rc.readSharedArray(counter+2));
                            counter++;
                        }
                    }
                }
            } else {
                break;
            }
            index = index+2;
        }
    }

    public static Direction get_enemy_dir(RobotController rc) throws GameActionException{
        if(probmode) {
            int[] probs = new int[8];
            int total = 0;
            for (int i = 0; i < 8; i++) {
                total += rc.readSharedArray(i + 56);
                probs[i] = rc.readSharedArray(i + 56);
            }

            if (total > 0) {
                int rand = rng.nextInt(total);
                int prob_tally = 0;
                for (int i = 0; i < 8; i++) {
                    prob_tally += probs[i];
                    if (prob_tally > rand) {
                        return directions[i];
                    }
                }
                return Direction.CENTER;
            } else {
                return directions[rng.nextInt(directions.length)];
            }
        } else {
            int counter = 0;
            int dir = 8;
            for (int i = 0; i < 8; i++) {
                if(rc.readSharedArray(i + 56) > counter){
                    counter = rc.readSharedArray(i+56);
                    dir = i;
                }
            }
            if (counter > 0) {
                return directions[dir];
            } else {
                return directions[rng.nextInt(directions.length)];
            }
        }
    }

    public static int dir_to_num(Direction dir) throws GameActionException{
        if(dir == Direction.NORTH){
            return 0;
        } else if (dir == Direction.NORTHEAST){
            return 1;
        } else if (dir == Direction.EAST){
            return 2;
        } else if (dir == Direction.SOUTHEAST){
            return 3;
        } else if (dir == Direction.SOUTH){
            return 4;
        } else if (dir == Direction.SOUTHWEST){
            return 5;
        } else if (dir == Direction.WEST){
            return 6;
        } else if (dir == Direction.NORTHWEST){
            return 7;
        } else {
            return -1;
        }
    }

}
