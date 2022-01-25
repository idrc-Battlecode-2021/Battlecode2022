package bot10.robots.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Lab extends Building{
    private int minLead;
    private int maxRate;
    private int globalMinerCount;

    public Lab(RobotController rc) {
        super(rc);
    }

    @Override
    public void init() throws GameActionException {
        parseAnomalies();
		readConstraints();
		if (canMakeGold()){
            rc.transmute();
        }
    }

    @Override
    public void run() throws GameActionException {
        if (rc.getRoundNum()%3==2){
            rc.writeSharedArray(4, rc.readSharedArray(4)+1);
        }
        if (rc.getRoundNum()%3==0){
            globalMinerCount = rc.readSharedArray(44);
        }
        if (rc.getTeamLeadAmount(rc.getTeam())>rc.getTransmutationRate() && rc.canTransmute() && globalMinerCount>=3){
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

    //TODO: See if can better Optimize gold production
    private boolean canMakeGold() throws GameActionException {
        /*constraints:
        	current rate < maximum allwoed rate
        	have enough lead leaving minimum reserve
        	can transmute*/
    	int currLead = rc.getTeamLeadAmount(rc.getTeam());
		return rc.getTransmutationRate() < maxRate && currLead - rc.getTransmutationRate() > minLead && rc.canTransmute();
	}
}
