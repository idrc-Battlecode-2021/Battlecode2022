package bot9_JJ.robots.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Lab extends Building{
    public int minLead;
    public int maxRate;

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
        avoidFury();
        retransform();
		readConstraints();
		if (canMakeGold()){
            rc.transmute();
        }
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
