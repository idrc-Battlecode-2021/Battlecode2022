package bot10_MC4.robots.buildings;

import battlecode.common.*;

public class Lab extends Building{
    private int minLead;
    private int maxRate;
    private int globalMinerCount;
    private int globalLabCount;
    private int targetMinerCount;

    public Lab(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        targetMinerCount = 3+(Math.min(mapHeight, mapWidth)-20)/3;
    }

    @Override
    public void run() throws GameActionException {
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(4, rc.readSharedArray(4)+1);
        }
        if (rc.getRoundNum()%3==0){
            globalLabCount = rc.readSharedArray(4);
            globalMinerCount = rc.readSharedArray(44);
        }
        rc.setIndicatorString(globalMinerCount+"");
        
            /*
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.LABORATORY.visionRadiusSquared, rc.getTeam().opponent());
        
        int mod = Math.max(1, 2+enemies.length/3);
        if (rc.getTeamLeadAmount(rc.getTeam())>rc.getTransmutationRate() && rc.canTransmute() && globalMinerCount>=3 && enemies.length==0){
            */
        int minerMod = globalMinerCount==0?1:(targetMinerCount-globalMinerCount)*targetMinerCount/globalMinerCount+1;
        int mod = Math.max(1,minerMod); //globalLabCount
        if (rc.getTeamLeadAmount(rc.getTeam())>rc.getTransmutationRate() && rc.canTransmute() && globalMinerCount>=3 /*&& rc.getRoundNum()%mod==0*/){
            rc.transmute();
        }
        /*
		readConstraints();
		if (canMakeGold()){
            rc.transmute();
        }
        */
    }

   	private void readConstraints() throws GameActionException {
        //see if constraints have any updates, may take 1 turn before lab updates to match due to random ordering
		int bin = rc.readSharedArray(9);
		minLead = bin % 4096; //need to have a factor multiplied to cover larger lead mins?
		maxRate = bin / 4096 + 5;
		rc.setIndicatorString(Integer.toString(maxRate));
    }

    private boolean canMakeGold() throws GameActionException {
        /*constraints:
        	current rate < maximum allwoed rate
        	have enough lead leaving minimum reserve
        	can transmute*/
    	int currLead = rc.getTeamLeadAmount(rc.getTeam());
		return rc.getTransmutationRate() < maxRate && currLead - rc.getTransmutationRate() > minLead && rc.canTransmute();
	}
}
