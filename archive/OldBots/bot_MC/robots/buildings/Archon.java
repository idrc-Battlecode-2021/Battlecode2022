package bot_MC.robots.buildings;

import bot_MC.util.Constants;
import battlecode.common.*;

public class Archon extends Building{
    private static int minerCount, builderCount, sageCount, soldierCount, labCount, watchtowerCount;
    private static int globalMinerCount, globalBuilderCount, globalSageCount, globalSoldierCount, globalWatchtowerCount, globalLabCount;
    private static int targetMinerCount; //target # of miners to build across all archons

    private static int minerBuild = 10; //miners to build
    private static int soldierBuild = 10; //soldiers to build
    private static int builderBuild = 0; //builders to build
    private static int watchtowerBuild = 5; //watchtowers to build; *currently not in use*
    private static int labBuild = 0; //labs to build

    private static int minerIndex = 0; //spawning miners
    private static int soldierIndex = 0;
    private static int archonOrder = 0; //reverse position of archonID in shared array
    private static int power = 0; // power of 16 that corresponds with archonOrder

    private static String indicatorString = "";

    private static int spawnPhase = 0; 
    /*
    1 - finish building miners
    2 - finish building builders
    3 - finish building lab & 2 defensive watchtowers
    */

    private static int msBuildType = 0; // controls ratio between miners and soldiers (phase 1)
    private static int lwBuildType = 0; // controls ratio between labs and watchtowers (phase 3)
    private static int wsBuildType = 0; // controls ratio between watchtowers and soldiers (late game)

    //private static int buildType = 0;
    private static boolean minerDone, builderDone, labDone;

    private static int tempMinerCount = 0;

    public Archon(RobotController rc) {
        super(rc);
    }

    public Direction[] closestDirections(Direction d){
		return new Direction[]{
							d,
							d.rotateLeft(),
							d.rotateRight(),
							d.rotateLeft().rotateLeft(),
							d.rotateRight().rotateRight(),
							d.rotateLeft().rotateLeft().rotateLeft(),
							d.rotateRight().rotateRight().rotateRight(),
							d.opposite().rotateRight(),
							d.opposite().rotateLeft()
						};
	}

    @Override
    public void init() throws GameActionException {
        initialArchons = rc.getArchonCount();
        parseAnomalies();
        // write Archon ID to shared array
        if (rc.getArchonCount()==1){
            archonOrder = 0;
        }
        else {
            while(rc.readSharedArray(63-archonOrder)>0){
                archonOrder++;
            }
        }
        rc.writeSharedArray(63-archonOrder,rc.getID()+1);
        power = (int)Math.pow(16,archonOrder);
        // Choose # of miners to build based on lead in surroundings
        int leadTiles = rc.senseNearbyLocationsWithLead(34).length;
        minerBuild = Math.max(10, (int)(0.75*(double)leadTiles));
        soldierBuild = minerBuild;
        myArchonID = rc.getID();
        myArchonOrder = archonOrder;
    }

    @Override
    public void run() throws GameActionException {
        indicatorString = "";
        checkArchonsAlive();
        // update spawn phases if necessary
        if (minerCount>=minerBuild && spawnPhase==2){ 
            spawnPhase++;
            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
        }
        if (builderCount>=builderBuild && spawnPhase==3){ 
            spawnPhase++;
            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
        }
        minerBuild = Math.max(minerBuild, (int)(60*((double)rc.senseNearbyLocationsWithLead(34).length/rc.getAllLocationsWithinRadiusSquared(myLocation,34).length)));
        
        if (defense()){indicatorString+="defense"; rc.setIndicatorString(indicatorString);return;}
        avoidFury();
        retransform();
        updateLabConstraints();
        updateTroopCount();

        // reset total # of troops in shared array
        if (rc.getRoundNum()%3==1){
            rc.writeSharedArray(10, 0);
            for (int i=0;i<4;i++){
                rc.writeSharedArray(i, 0);
            }
        }

        watchtowerCount = rc.readSharedArray(archonOrder+5);
        labCount = (rc.readSharedArray(4) % (power*16))/power;
        indicatorString+=" buildCount: "+builderCount;
        if (!canProceed(spawnPhase)){
            // shouldn't continue if other archons haven't caught up with spawning
            rc.setIndicatorString(indicatorString);
            return;
        }
        //surplus
        if (builderCount<13 && rc.getTeamLeadAmount(rc.getTeam())>Constants.SURPLUS_THRESHOLD+180*5+RobotType.BUILDER.buildCostLead+RobotType.WATCHTOWER.buildCostLead){
            if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.BUILDER.buildCostLead){
                Direction directions[] = Direction.allDirections();
                int i = 0;
                while (!rc.canBuildRobot(RobotType.BUILDER,directions[i]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.BUILDER,directions[i])){
                    builderCount++;
                    rc.buildRobot(RobotType.BUILDER,directions[i]);
                    indicatorString+=" builders "+builderCount;
                }
            }
        }
        else if (!minerDone && minerCount<minerBuild){
            int mod;
            if (minerCount<minerBuild/3){
                mod=1;
            }
            else if (minerCount<minerBuild*2/3){
                mod=3;
            }
            else{
                mod=2;
            }
            indicatorString += " "+minerBuild;
            //TODO: make archons spawn in direction of deposits
            if (!minerDone && msBuildType % mod == 1){
                if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.SOLDIER.buildCostLead){
                    Direction directions[] = Direction.allDirections();
                    int i=0;
                    while (!rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8]) && i<8){
                        i++;
                    }
                    if (rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8])){
                        soldierIndex = (soldierIndex+i)%8;
                        msBuildType++;
                        rc.buildRobot(RobotType.SOLDIER,directions[soldierIndex]);
                        soldierIndex++;
                        soldierCount++;
                    }
                }
            }
            else{
                if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.MINER.buildCostLead){
                    Direction directions[] = Direction.allDirections();
                    int i=0;
                    while (!rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8]) && i<8){
                        i++;
                    }
                    if (rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8])){
                        minerIndex = (minerIndex+i)%8;
                        rc.buildRobot(RobotType.MINER,directions[minerIndex]);
                        msBuildType++;
                        minerIndex++;
                        minerCount++; // OK to manually increment minerCount because it will take <=3 rounds to reset minerCount again
                        /*
                        if (!minerDone && minerCount == minerBuild){
                            minerDone = true;
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            spawnPhase++;
                            msBuildType = 0;
                        }
                        */
                        if (minerCount == minerBuild/3){
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            spawnPhase++;
                        }
                        else if (minerCount == minerBuild*2/3){
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            spawnPhase++;
                        }
                        else if (minerCount == minerBuild){
                            minerDone = true;
                            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                            spawnPhase++;
                        }
                    }
                }
            }
        }
        else {
            int mod, cost = 0;
            if (minerCount>=minerBuild*2/3){ 
                mod=4;
            }
            else{
                mod=2;
            }
            switch(wsBuildType%mod){
                case 0:
                    cost=RobotType.MINER.buildCostLead; break;
                default:
                    cost=RobotType.SOLDIER.buildCostLead; break;
            }
            int archonBuildStatus = rc.readSharedArray(11);
            int diff = archonBuildStatus - archonOrder;
            if (diff<0){
                if (rc.getTeamLeadAmount(rc.getTeam())-75*-1*diff<cost){
                    rc.setIndicatorString(indicatorString);
                    return;
                }
            }
            if (diff>0){
                int space = rc.getArchonCount()-diff;
                if (rc.getTeamLeadAmount(rc.getTeam())-75*space<cost){
                    rc.setIndicatorString(indicatorString);
                    return;
                }
            }
            if (wsBuildType%mod==0 && rc.getTeamLeadAmount(rc.getTeam())>=RobotType.MINER.buildCostLead){
                Direction directions[] = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.MINER,directions[(minerIndex+i)%8])){
                    if (diff==0){
                        if (archonBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,archonBuildStatus+1);
                        }
                    }
                    minerIndex = (minerIndex+i)%8;
                    wsBuildType++;
                    rc.buildRobot(RobotType.MINER,directions[minerIndex]);
                    minerCount++;
                }
            }
            else if(rc.getTeamLeadAmount(rc.getTeam())>=RobotType.SOLDIER.buildCostLead){
                Direction directions[] = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8])){
                    if (diff==0){
                        if (archonBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,archonBuildStatus+1);
                        }
                    }
                    soldierIndex = (soldierIndex+i)%8;
                    wsBuildType++;
                    rc.buildRobot(RobotType.SOLDIER,directions[soldierIndex]);
                    soldierCount++;
                }
            }
            else { //repair robots if can't build anything
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.ARCHON.actionRadiusSquared,rc.getTeam()); 
                int lowestHealth = 99999;
                MapLocation location = null;
                for (RobotInfo robot : robots){
                    if (robot.getMode() == RobotMode.DROID && robot.getHealth()<lowestHealth){
                        lowestHealth = robot.getHealth();
                        location = robot.getLocation();
                    }
                }
                if (location!=null && rc.canRepair(location)){
                    rc.repair(location);
                }
            }
        }
        rc.setIndicatorString(indicatorString);
    }

    public boolean canProceed(int n) throws GameActionException {
        // First check if other archons are trying to defend
        int defenseStatus = rc.readSharedArray(56);
        for (int i=0;i<16;i+=4){
            if (rc.readSharedArray(63-i/4)==0){break;} // don't check archons that don't exist
            int temp = (int)Math.pow(2,i);
            int thisDefense = (defenseStatus % (temp*16))/temp;
            if (thisDefense > 0){
                indicatorString += " other archon defending";
                return false;
            }
        }
        // Check if all archons have passed phase n, phases on top of file
        int archonStatus = rc.readSharedArray(57);
        for (int i=0;i<16;i+=4){
            if (rc.readSharedArray(63-i/4)==0){break;}
            int temp = (int)Math.pow(2,i);
            int thisPhase = (archonStatus % (temp*16))/temp;
            if (thisPhase < n){
                indicatorString += " phase too low ";
                return false;
            }
        }
        return true;
    }
    
     
    public void updateLabConstraints() throws GameActionException {
        //TODO: update constraints for lab based on current lead amounts and needs
        int minLead = 300; //placeholder
        int maxRate = 20; //placeholder, 5 to 20
        rc.writeSharedArray(9, (maxRate-5)*4096+minLead); //minLead may need to be divided by a factor to fit in 12 bits if minLead is large
        
    }

    // if enemies are near archon, spawn soldiers and inform other archons
    public boolean defense() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int current = rc.readSharedArray(56);
        int myValue = (current % (power*16))/power;
        if (enemies.length>0){
            rc.writeSharedArray(56, current - myValue*power + power);
        }
        else{
            rc.writeSharedArray(56, current - myValue*power);
            return false;
        }
        if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.SOLDIER.buildCostLead){
            Direction directions[] = closestDirections(rc.getLocation().directionTo(enemies[0].getLocation()));
            int i=0;
            while (!rc.canBuildRobot(RobotType.SOLDIER,directions[i]) && i<8){
                i++;
            }
            if (rc.canBuildRobot(RobotType.SOLDIER,directions[i])){
                rc.buildRobot(RobotType.SOLDIER,directions[i]);
            }
        }
        return true;
    }

    // NOT substitute for build soldier code in run(), only used when surplus of lead is achieved
    public void buildSoldier() throws GameActionException {
        Direction directions[] = Direction.allDirections();
        int i=0;
        while (!rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8]) && i<8){
            i++;
        }
        if (rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8])){
            soldierIndex = (soldierIndex+i)%8;
            rc.buildRobot(RobotType.SOLDIER,directions[soldierIndex]);
            soldierIndex++;
            soldierCount++;
        }
    }

    // NOT substitute for build watchtower code in run(), only used when surplus of lead is achieved
    public void buildWatchtower() throws GameActionException {
        int temp = (int)Math.pow(4,archonOrder);
        int currentValue = rc.readSharedArray(58);
        int previousBuildCommand = (currentValue % (temp * 4))/temp;
        int buildCommand = currentValue - previousBuildCommand * temp + temp * 2;
        watchtowerCount++;
        rc.writeSharedArray(58, buildCommand);
    }

    // Checks is all archons are alive; if not, refactor information in shared arrays
    public void checkArchonsAlive() throws GameActionException{
        if (rc.getArchonCount()<initialArchons){
            // check if the archon IDs in the shared array have been deleted (i.e. if another archon has already updated the IDs)
            int i=0;
            while (rc.readSharedArray(63-i)>0 && i<=initialArchons-1){
                i++;
            }
            if (i==initialArchons){ //this is the first archon to update the array
                //resets the archon IDs
                for(int j=0;j<4;j++){
                    rc.writeSharedArray(63-j, 0);
                }
                archonOrder = 0;
                //reset relevant arrays
                rc.writeSharedArray(63,rc.getID()+1);
                rc.writeSharedArray(56,0);
                rc.writeSharedArray(11,0);
            }
            else{
                //set appropriate archonOrder
                archonOrder = i;
                rc.writeSharedArray(63-archonOrder,rc.getID()+1);
            }
            power = (int)Math.pow(16,archonOrder);
            System.out.println("Reassigning archonOrder "+archonOrder);
            initialArchons = rc.getArchonCount();
        }
    }

    // update troop count variables from shared array
    public void updateTroopCount() throws GameActionException{
        if (rc.getRoundNum()%3==0){
            if (archonOrder<=1){
                minerCount = (rc.readSharedArray(0)%((int)Math.pow(256,archonOrder+1)))/(int)Math.pow(256,archonOrder);
            }
            else{
                minerCount = (rc.readSharedArray(10)%((int)Math.pow(256,archonOrder-1)))/(int)Math.pow(256,archonOrder-2);
            }
            builderCount = (rc.readSharedArray(1)%(power*16))/(power);
            // update troop count variables from shared array
            globalSageCount = rc.readSharedArray(2);
            globalSoldierCount = rc.readSharedArray(3);
            globalWatchtowerCount = rc.readSharedArray(5) + rc.readSharedArray(6) + rc.readSharedArray(7) + rc.readSharedArray(8);
            int lc = rc.readSharedArray(4);
            globalLabCount = lc % 16 + (lc % 256)/16 + (lc % 4096)/256 + lc / 4096;
        }
    }
}
