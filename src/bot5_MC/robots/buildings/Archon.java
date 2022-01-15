package bot5_MC.robots.buildings;

import battlecode.common.*;

public class Archon extends Building{
    private static Integer minerCount = 0, minerFoundCount = 0, builderCount = 0, sageCount = 0, soldierCount = 0, labCount = 0, watchtowerCount = 0, minerCountMax = 0;
    private static int count = 0;
    private static int globalMinerCount = 0, globalBuilderCount, globalSageCount, globalSoldierCount, globalWatchtowerCount, globalLabCount;
    private static int targetMinerCount; //target # of miners to build across all archons
    private static int minersForNearbyLead;

    private static Integer minerIndex = 0; //spawning miners
    private static Integer soldierIndex = 0;
    private static int archonOrder = 0; //reverse position of archonID in shared array
    private static int power = 0; // power of 16 that corresponds with archonOrder

    private static final int SURPLUS_THRESHOLD = 500;

    private static String indicatorString = "";

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
        //write archon location to array
        MapLocation myLocation = rc.getLocation();
        int x = myLocation.x/4;
        int y = myLocation.y/4;
        if (archonOrder<=1){
            int temp = (int)Math.pow(256,archonOrder);
            rc.writeSharedArray(49, rc.readSharedArray(49)+x*temp+y*(temp*16));
        }
        else{
            int temp = (int)Math.pow(256,archonOrder-2);
            rc.writeSharedArray(50, rc.readSharedArray(50)+x*temp+y*(temp*16));
        }
        if (archonOrder==rc.getArchonCount()-1){
            //if this is the last archon, find the average distance between all of the archons
            if(archonOrder==0){
                rc.writeSharedArray(14, (int)Math.pow(2,14));
            }
            else if (archonOrder==1){
                int temp = rc.readSharedArray(49);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation one = new MapLocation(x,y);
                x = temp%4096/256*4;
                y = temp/4096*4;
                MapLocation two = new MapLocation(x,y);
                System.out.println(one);
                System.out.println(two);
                rc.writeSharedArray(14, movementTileDistance(one, two));
                System.out.println("average: "+movementTileDistance(one, two));
            }
            else if (archonOrder==2){
                int temp = rc.readSharedArray(49);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation one = new MapLocation(x,y);
                x = temp%4096/256*4;
                y = temp/4096*4;
                MapLocation two = new MapLocation(x,y);
                temp = rc.readSharedArray(50);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation three = new MapLocation(x,y);
                int average = (movementTileDistance(one, two)+movementTileDistance(one, three)+movementTileDistance(three, two))/3;
                rc.writeSharedArray(14, average);
                System.out.println("average: "+average);
            }
            else{
                int temp = rc.readSharedArray(49);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation one = new MapLocation(x,y);
                x = temp%4096/256*4;
                y = temp/4096*4;
                MapLocation two = new MapLocation(x,y);
                temp = rc.readSharedArray(50);
                x = temp%16*4;
                y = temp%256/16*4;
                MapLocation three = new MapLocation(x,y);
                x = temp%4096/256*4;
                y = temp/4096*4;
                MapLocation four = new MapLocation(x,y);
                int average = (movementTileDistance(one, two)+movementTileDistance(one, three)+movementTileDistance(one, four)+movementTileDistance(three, two)+movementTileDistance(four, two)+movementTileDistance(three, four))/6;
                rc.writeSharedArray(14, average);
                System.out.println("average: "+average);
            }
        }
        rc.writeSharedArray(63-archonOrder,rc.getID()+1);
        power = (int)Math.pow(16,archonOrder);
        // Choose # of miners to build based on lead in surroundings
        minersForNearbyLead = (int) Math.ceil(rc.senseNearbyLocationsWithLead(34).length/9.0);
        myArchonID = rc.getID();
        myArchonOrder = archonOrder;
        //labBuild = rc.getMapHeight()/40+1;
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

    public boolean canProceed() throws GameActionException {
        // Check if other archons are trying to defend
        int defenseStatus = rc.readSharedArray(56);
        for (int i=0;i<16;i+=4){
            if (rc.readSharedArray(63-i/4)==0){break;} // don't check archons that don't exist
            int temp = (int)Math.pow(2,i);
            int thisDefense = (defenseStatus % (temp*16))/temp;
            if (thisDefense > 0){
                indicatorString += "defense";
                return false;
            }
        }
        return true;
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
         if (roundNum%3==1){ //Reset Troop Counts
            rc.writeSharedArray(10, 0);
            for (int i=0;i<4;i++){
                rc.writeSharedArray(i, 0);
            }
            for (int i = 31; i < 35; i++){
                rc.writeSharedArray(i,0);
            }
            rc.writeSharedArray(44,0);
         }else if (rc.getRoundNum()%3==0){ //Create New Values
            if (archonOrder<=1){
                minerCount = (rc.readSharedArray(0)%((int)Math.pow(256,archonOrder+1)))/(int)Math.pow(256,archonOrder);
            }
            else{
                minerCount = (rc.readSharedArray(10)%((int)Math.pow(256,archonOrder-1)))/(int)Math.pow(256,archonOrder-2);
            }
            if(minerCount > minerCountMax)minerCountMax = minerCount;
            minerFoundCount = rc.readSharedArray(31+archonOrder);
            builderCount = (rc.readSharedArray(1)%(power*16))/(power);
            globalSageCount = rc.readSharedArray(2);
            globalMinerCount = rc.readSharedArray(44);
            globalSoldierCount = rc.readSharedArray(3);
            globalWatchtowerCount = rc.readSharedArray(5) + rc.readSharedArray(6) + rc.readSharedArray(7) + rc.readSharedArray(8);
            int lc = rc.readSharedArray(4);
            globalLabCount = lc % 16 + (lc % 256)/16 + (lc % 4096)/256 + lc / 4096;
            watchtowerCount = rc.readSharedArray(archonOrder+5);
            labCount = (rc.readSharedArray(4) % (power*16))/power;
        }
    }

    public boolean checkBuildStatus(int diff, int cost) throws GameActionException{ //controls the order of archons building troops so troops are spawned evenly
        // diff is the difference between the next archon to spawn and this archon's ID
        // if diff!=0, the archon is later in the order. Check if there are enough resources for this archon to build troops before the
        // next archon to spawn, otherwise don't spawn anything (return false).
        if (diff<0){
            if (rc.getTeamLeadAmount(rc.getTeam())-cost*-1*diff<cost){
                rc.setIndicatorString(indicatorString);
                return false;
            }
        }
        if (diff>0){
            int space = rc.getArchonCount()-diff;
            if (rc.getTeamLeadAmount(rc.getTeam())-cost*space<cost){
                rc.setIndicatorString(indicatorString);
                return false;
            }
        }
        return true;
    }
    
    private int roundNum = 0;
    @Override
    public void run() throws GameActionException {
        indicatorString = "";
        checkArchonsAlive();
        avoidFury();
        retransform();
        updateTroopCount();
        roundNum = rc.getRoundNum();
        if (defense()){
            indicatorString += " defense";
            rc.setIndicatorString(indicatorString);
            return;
        }
        if (!canProceed()){
            indicatorString += " can't proceed";
            rc.setIndicatorString(indicatorString);
            return;
        }
        // START SPAWNING
        int archonBuildStatus = rc.readSharedArray(11);
        int diff = archonBuildStatus - archonOrder;
        int cost = RobotType.MINER.buildCostLead;
        RobotType type = RobotType.MINER;
        indicatorString+=" miners";
        if (!checkBuildStatus(diff, cost)) return;
        int mod = 4;
        if (rc.getTeamLeadAmount(rc.getTeam())>1000 && builderCount<7){
            mod = 5;
        }
        if(rc.readSharedArray(40) !=0){
            mod = 1;
        }else if (rc.readSharedArray(42)!= 0){ // if a miner has been sighted
            mod = 2;
        }
        if (globalMinerCount < 6 || count%mod == 1){
            if (rc.getTeamLeadAmount(rc.getTeam())>=cost){
                Direction directions[] = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(type,directions[(minerIndex+i)%8]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(type,directions[(minerIndex+i)%8])){
                    if (diff==0){
                        if (archonBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,archonBuildStatus+1);
                        }
                    }
                    minerIndex = (minerIndex+i)%8;
                    rc.buildRobot(type,directions[minerIndex]);
                    minerIndex++;
                    minerCount++;
                    globalMinerCount++;
                    count++;
                }
            }
        }
        else /*if (count %mod==1)*/{
            cost = RobotType.SOLDIER.buildCostLead;
            type = RobotType.SOLDIER;
            indicatorString += " soldiers";
            if (!checkBuildStatus(diff, cost)) return;
            if (rc.getTeamLeadAmount(rc.getTeam())>=cost){
                Direction directions[] = Direction.allDirections();
                int i=0;
                while (!rc.canBuildRobot(type,directions[(soldierIndex+i)%8]) && i<8){
                    i++;
                }
                if (rc.canBuildRobot(type,directions[(soldierIndex+i)%8])){
                    if (diff==0){
                        if (archonBuildStatus == rc.getArchonCount()-1){
                            rc.writeSharedArray(11,0);
                        }
                        else{
                            rc.writeSharedArray(11,archonBuildStatus+1);
                        }
                    }
                    soldierIndex = (soldierIndex+i)%8;
                    rc.buildRobot(type,directions[soldierIndex]);
                    soldierIndex++;
                    soldierCount++;
                    count++;
                }
            }
        }

        rc.setIndicatorString(indicatorString);
    }
}
