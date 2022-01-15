package bot3_JJ4.robots.buildings;

import battlecode.common.*;

public class Archon extends Building{
    private static int minerCount, minerFoundCount, builderCount, sageCount, soldierCount, labCount, watchtowerCount;
    private static int globalMinerCount, globalBuilderCount, globalSageCount, globalSoldierCount = 0, globalWatchtowerCount, globalLabCount;
    private int prevSoldierCount = 0;
    private static int targetMinerCount; //target # of miners to build across all archons

    private static int minerBuild = 7; //miners to build
    private static int soldierBuild = 10; //soldiers to build
    private static int soldierBuild2 = 25;
    private static int builderBuild = 4; //builders to build
    private static int watchtowerBuild = 5; //watchtowers to build; *currently not in use*
    private static int labBuild = 1; //labs to build

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
        //int leadTiles = rc.senseNearbyLocationsWithLead(34).length;
        
        //minerBuild = Math.min(30,Math.max(minerBuild, (int)(60*((double)leadTiles/rc.getAllLocationsWithinRadiusSquared(myLocation,34).length))));
        soldierBuild = minerBuild;
        myArchonID = rc.getID();
        myArchonOrder = archonOrder;
        labBuild = rc.getMapHeight()/40+1;
    }

    public boolean canProceed(int n) throws GameActionException {
        // First check if other archons are trying to defend
        int defenseStatus = rc.readSharedArray(56);
        for (int i=0;i<16;i+=4){
            if (rc.readSharedArray(63-i/4)==0){ // don't check archons that don't exist
                break;
            }
            int temp = (int)Math.pow(2,i);
            int thisDefense = (defenseStatus % (temp*16))/temp;
            if (thisDefense > 0){
                return false;
            }
        }
        // Check if other archons have built enough miners
        /* Doesn't work since archons may spawn different # of miners 
        int minerStatus;
        for (int i=0;i<4;i++){
            if (rc.readSharedArray(63-i)==0){ // don't check archons that don't exist
                break;
            }
            if(i<=1){
                minerStatus = rc.readSharedArray(0);
            }
            else{
                minerStatus = rc.readSharedArray(10);
            }
            if (i%2==0){
                if (minerStatus%256<minerCount){
                    return false;
                }
            }
            else{
                if (minerStatus/256<minerCount){
                    return false;
                }
            }
        }
        */
        // Check if all archons have passed phase n
        // 0 - spawn 1/3 of minerBuild, 1 - spawn 2/3 of minerBuild, etc.
        int archonStatus = rc.readSharedArray(57);
        for (int i=0;i<16;i+=4){
            if (rc.readSharedArray(63-i/4)==0){ // don't check archons that don't exist
                continue;
            }
            int temp = (int)Math.pow(2,i);
            int thisPhase = (archonStatus % (temp*16))/temp;
            if (thisPhase < n){
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
    
    public boolean defense() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        int current = rc.readSharedArray(56);
        int myValue = (current % (power*16))/power;
        try{
            if (enemies.length>0){
                rc.writeSharedArray(56, current - myValue*power + power);
            }
            else{
                rc.writeSharedArray(56, current - myValue*power);
                return false;
            }
        }
        catch(Exception e){
            //System.out.println(Integer.toBinaryString(rc.readSharedArray(56)));
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
    
    @Override
    public void run() throws GameActionException {
        int soldierBuildStart = rc.readSharedArray(42);
        if (rc.getArchonCount()<initialArchons){ // change all relevant shared array items when an archon dies
            // update spawnphase array (57)
            int i=0;
            // check if the archon IDs have been deleted (i.e. if another archon has already updated the IDs)
            while (rc.readSharedArray(63-i)>0 && i<=initialArchons-1){
                i++;
            }
            if (i==initialArchons){ //this is the first archon to update after one died
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
            //System.out.println("Reassigning archonOrder "+archonOrder);
            initialArchons = rc.getArchonCount();
        }
        if (globalLabCount==labBuild && spawnPhase==2){
            spawnPhase++;
            rc.writeSharedArray(57, rc.readSharedArray(57)+power);
        }
        //minerBuild = Math.max(minerBuild, (int)(60*((double)rc.senseNearbyLocationsWithLead(34).length/rc.getAllLocationsWithinRadiusSquared(myLocation,34).length)));
        if (defense()){
            return;
        }
        avoidFury();
        retransform();
        updateLabConstraints();
        indicatorString = indicatorString + "minerCount: "+minerCount;
        // read total/global # of robots
        if (rc.getRoundNum()%3==0){
            if (archonOrder<=1){
                minerCount = (rc.readSharedArray(0)%((int)Math.pow(256,archonOrder+1)))/(int)Math.pow(256,archonOrder);
            }
            else{
                minerCount = (rc.readSharedArray(10)%((int)Math.pow(256,archonOrder-1)))/(int)Math.pow(256,archonOrder-2);
            }
            minerFoundCount = rc.readSharedArray(31+archonOrder);
            builderCount = (rc.readSharedArray(1)%(power*16))/(power);
            globalSageCount = rc.readSharedArray(2);
            prevSoldierCount = globalSoldierCount;
            globalSoldierCount = rc.readSharedArray(3);
            globalWatchtowerCount = rc.readSharedArray(5) + rc.readSharedArray(6) + rc.readSharedArray(7) + rc.readSharedArray(8);
            int lc = rc.readSharedArray(4);
            globalLabCount = lc % 16 + (lc % 256)/16 + (lc % 4096)/256 + lc / 4096;
        }

        // reset total # of troops in shared array
        if (rc.getRoundNum()%3==1){
            rc.writeSharedArray(10, 0);
            for (int i=0;i<4;i++){
                rc.writeSharedArray(i, 0);
            }
            for (int i = 31; i < 35; i++){
                rc.writeSharedArray(i,0);
            }
        }
        // find individual watchtower count
        if (archonOrder==0){
            watchtowerCount = rc.readSharedArray(5);
        }
        if (archonOrder==1){
            watchtowerCount = rc.readSharedArray(6);
        }
        if (archonOrder==2){
            watchtowerCount = rc.readSharedArray(7);
        }
        if (archonOrder==3){
            watchtowerCount = rc.readSharedArray(8);
        }
        // find individual lab count
        labCount = (rc.readSharedArray(4) % (power*16))/power;
        int proposedExpenses = Math.min(rc.readSharedArray(11),180);
        /*;
        if (rc.getTeamLeadAmount(rc.getTeam())>=1000+proposedExpenses){
            //if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.LABORATORY.buildCostLead){
            int temp = (int)Math.pow(4,archonOrder);
            int currentValue = rc.readSharedArray(58);
            int previousBuildCommand = (currentValue % (temp*4))/temp;
            int buildCommand = currentValue - previousBuildCommand * temp + temp;
            rc.writeSharedArray(58, buildCommand);
            rc.writeSharedArray(11, proposedExpenses+RobotType.LABORATORY.buildCostLead);
            //lwBuildType++
        ;
        else */
        if (/*prevSoldierCount <= globalSoldierCount && (minerCount-minerFoundCount<minerBuild && (globalSoldierCount >= soldierBuild*rc.getArchonCount() || soldierBuildStart == 0) || minerCount < minerBuild) &&
                rc.getTeamLeadAmount(myTeam) < 1000*rc.getArchonCount()*/(minerCount+globalSoldierCount)%3 != 2 && soldierBuildStart == 0){
            //rc.setIndicatorString("phase 1");
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
                    if (!minerDone && minerCount == minerBuild){
                        minerDone = true;
                        rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                        spawnPhase++;
                        msBuildType = 0;
                    }
                }
            }

        } //Learn how to use Builders later
        /*else if (globalSoldierCount >= soldierBuild*rc.getArchonCount() && (builderCount<builderBuild || (rc.getTeamLeadAmount(myTeam) > 2000 && builderCount < builderBuild*2))){
            if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.BUILDER.buildCostLead){
                Direction directions[] = Direction.allDirections();
                int i = 0;
                while (!rc.canBuildRobot(RobotType.BUILDER,directions[i]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.BUILDER,directions[i])){
                    rc.buildRobot(RobotType.BUILDER,directions[i]);
                    builderCount++;
                    if (!builderDone && builderCount == builderBuild){
                        builderDone = true;
                        rc.writeSharedArray(57, rc.readSharedArray(57)+power);
                        spawnPhase++;
                    }
                }
            }
        }*/
        else {
            /*
            if (globalSoldierCount+globalWatchtowerCount>40 && rc.getTeamGoldAmount(rc.getTeam())>=RobotType.SAGE.buildCostGold){
                Direction[] directions = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(RobotType.SAGE,directions[i]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.SAGE,directions[i])){
                    rc.buildRobot(RobotType.SAGE,directions[i]);
                    sageCount++;
                }
            }
            else */
            if (rc.getTeamLeadAmount(myTeam)>4000*rc.getArchonCount() && globalLabCount < 4){
                if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.LABORATORY.buildCostLead+proposedExpenses){
                    //if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.LABORATORY.buildCostLead){
                    int temp = (int)Math.pow(4,archonOrder);
                    int currentValue = rc.readSharedArray(58);
                    int previousBuildCommand = (currentValue % (temp*4))/temp;
                    int buildCommand = currentValue - previousBuildCommand * temp + temp;
                    rc.writeSharedArray(58, buildCommand);
                    rc.writeSharedArray(11, proposedExpenses+RobotType.LABORATORY.buildCostLead);
                    lwBuildType++;
                }
            }
            int mod =2;
            //System.out.println("bt: "+wsBuildType+" mod: "+mod+" pe: "+proposedExpenses);
            if (wsBuildType%mod==1 && rc.getTeamLeadAmount(rc.getTeam())>=RobotType.WATCHTOWER.buildCostLead && (globalSoldierCount >= soldierBuild2*rc.getArchonCount())){
                int temp = (int)Math.pow(4,archonOrder); // power corresponding to this Archon's bits
                int currentValue = rc.readSharedArray(58);
                int previousBuildCommand = (currentValue % (temp * 4))/temp; // previous two-bit build command
                int buildCommand = currentValue - previousBuildCommand * temp + temp * 2; // subtract previous command and add new command
                watchtowerCount++;
                wsBuildType++;
                rc.writeSharedArray(11, proposedExpenses+RobotType.WATCHTOWER.buildCostLead);
                rc.writeSharedArray(58, buildCommand);
            }
            if (rc.getTeamLeadAmount(rc.getTeam())>=RobotType.SOLDIER.buildCostLead){
                Direction directions[] = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(RobotType.SOLDIER,directions[(soldierIndex+i)%8])){
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
    }

}
