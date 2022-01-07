package ryansbot;

import battlecode.common.*;

public strictfp class RunSoldier {
    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runSoldier(RobotController rc) throws GameActionException {
        // Move to attack, and prioritize the Archon
        MapLocation me = rc.getLocation();
        MapLocation closestEnemy = new MapLocation(0,0);
        MapLocation archonLoc = null;
        for(RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.team == rc.getTeam().opponent()) {
                if (robot.type == RobotType.ARCHON) {
                    closestEnemy = robot.location;
                    archonLoc = robot.location;
                    break;
                } else if (robot.type == RobotType.SOLDIER || robot.type == RobotType.MINER) {
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
        if (archonLoc != null) {
            rc.writeSharedArray(0, archonLoc.x);
            rc.writeSharedArray(1, archonLoc.y);
        }
        MapLocation sharedLoc = new MapLocation(rc.readSharedArray(0), rc.readSharedArray(1));
        if (sharedLoc.x != 0 && sharedLoc.y != 0 && rc.getID() %2== 0) {
            if (rc.canMove(me.directionTo(sharedLoc))) {
                rc.move(me.directionTo(sharedLoc));
                return;
            }
        }

        // Try to attack closest enemy
        if (rc.canAttack(closestEnemy)) {
            rc.attack(closestEnemy);
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
        RobotPlayer.moveRandom(rc);
    }
}
