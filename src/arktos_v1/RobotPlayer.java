package arktos_v1;

import battlecode.common.*;

import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */

/**
List of changes planned for Arktos:

 1. Improve miner ability to scout out and spread in search of lead.
 (Recognize when enough miners to deal with patch of lead and leave?) (Destructively mine?)

 2. Improve initial defensive behavior to ward off rushes
 Monolithic walls are the best defense, so perhaps build a solid radius of towers?
 5. Perhaps add soldiers to initial defense? Right now I don't use them at first.
 Again, solid radius perhaps?

 9. Improve attacking troop micro-code

 6. Add use of labs and sages once dominant. (or maybe for initial D)

 3. Move 2ndary archons closer to primary archons, they do be useless otherwise. CHECK

 4. Add mutation code, since mutations are really effective now. CHECK

 7. Add pathfinding, at least something basic so attacking troops are more effective. Probs not.

 8. Improve troop rush once dominance is established. Probs not.


 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    static RobotController rc;
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    static int maxFollow = 0;

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

        if(maxFollow == 0){
            maxFollow = (int)((Math.max(rc.getMapWidth(), rc.getMapHeight())/2.5)*(Math.max(rc.getMapWidth(), rc.getMapHeight())/2.5));
        }

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
        MapLocation home = new MapLocation(rc.readSharedArray(36)-1, rc.readSharedArray(37)-1);
        for(RobotInfo enemy : enemies){
            if(enemy.type.isBuilding()){
                int index;
                if(enemy.type == RobotType.ARCHON){
                    index = 0;
                } else {
                    index = 16;
                }
                while(index < 32){
                    int x = rc.readSharedArray(index);
                    int y = rc.readSharedArray(index+1);
                    if(x==0 && y==0){
                        rc.writeSharedArray(index, enemy.location.x+1);
                        rc.writeSharedArray(index+1, enemy.location.y+1);
                        System.out.println("Wrote loc: " + enemy.location.toString() + " at " + index);
                        break;
                    } else if (enemy.location.equals(new MapLocation(x-1, y-1))){
                        break;
                    } else {
                        index = index+2;
                    }
                }
            }

            if(rc.readSharedArray(34) == 0 && rc.readSharedArray(35) == 0){
                rc.writeSharedArray(34, enemy.location.x+1);
                rc.writeSharedArray(35, enemy.location.y+1);
            } else {
                MapLocation soldierTarget = new MapLocation(rc.readSharedArray(34)-1, rc.readSharedArray(35)-1);
                if(enemy.location.distanceSquaredTo(home) < soldierTarget.distanceSquaredTo(home)){
                    rc.writeSharedArray(34, enemy.location.x+1);
                    rc.writeSharedArray(35, enemy.location.y+1);
                }
            }
        }
    }

    public static void removelocs(RobotController rc) throws GameActionException{
        //these are surprise variables to help us later
        int index = 0;
        MapLocation loc;
        int counter;

        //target locations are stored from index 0 to 32
        while(index < 32){
            int x = rc.readSharedArray(index);
            int y = rc.readSharedArray(index+1);
            if(!(x==0 && y==0)){
                loc = new MapLocation(x-1, y-1);

                //remove from array if not building anymore
                if(rc.canSenseLocation(loc)){
                    if (!rc.canSenseRobotAtLocation(loc) || rc.senseRobotAtLocation(loc).team == rc.getTeam() || !rc.senseRobotAtLocation(loc).type.isBuilding()){

                        System.out.println("Removing loc: " + loc.toString());

                        counter = index;
                        while (counter != 15 || counter != 31){
                            if(rc.readSharedArray(counter) == 0){
                                break;
                            }
                            if(counter == 14 || counter == 15) {
                                rc.writeSharedArray(counter, 0);
                            } else {
                                rc.writeSharedArray(counter, rc.readSharedArray(counter + 2));
                            }
                            counter++;
                        }
                    }
                }
            } else {
                break;
            }
            index = index+2;
        }

        //for soldier target
        if(rc.readSharedArray(34) != 0 && rc.readSharedArray(35) != 0) {
            MapLocation soldierTarget = new MapLocation(rc.readSharedArray(34) - 1, rc.readSharedArray(35) - 1);
            if (rc.canSenseLocation(soldierTarget)) {
                if (!rc.canSenseRobotAtLocation(soldierTarget) || rc.senseRobotAtLocation(soldierTarget).team == rc.getTeam()) {
                    rc.writeSharedArray(34, 0);
                    rc.writeSharedArray(35,0);
                }
            }
        }
    }

}
