<<<<<<< HEAD
<<<<<<< HEAD:src/bot2/robots/buildings/Lab.java
package bot2.robots.buildings;
=======
package bot1.robots.buildings;
>>>>>>> b76914a947c4b5aa0f839bdeaa0adeb29ca5ff89:src/bot1/robots/buildings/Lab.java
=======
package bot2.robots.buildings;
>>>>>>> b76914a947c4b5aa0f839bdeaa0adeb29ca5ff89

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
<<<<<<< HEAD
		readConstraints();
		if (canMakeGold()){
=======
        readConstraints();
        if (canMakeGold()){
>>>>>>> b76914a947c4b5aa0f839bdeaa0adeb29ca5ff89
            rc.transmute();
        }
    }

    @Override
    public void run() throws GameActionException {
        avoidFury();
        retransform();
<<<<<<< HEAD
		readConstraints();
		if (canMakeGold()){
=======
        readConstraints();
        if (canMakeGold()){
>>>>>>> b76914a947c4b5aa0f839bdeaa0adeb29ca5ff89
            rc.transmute();
        }
    }

<<<<<<< HEAD
   	private void readConstraints() throws GameActionException {
        //see if constraints have any updates, may take 1 turn before lab updates to match due to random ordering
		int bin = rc.readSharedArray(9);
		minLead = bin % 4096; //need to have a factor multiplied to cover larger lead mins?
		maxRate = bin / 4096 + 5;
		rc.setIndicatorString(Integer.toString(maxRate));
=======
    private void readConstraints() throws GameActionException {
        //see if constraints have any updates, may take 1 turn before lab updates to match due to random ordering
        int bin = rc.readSharedArray(9);
        minLead = bin % 4096; //need to have a factor multiplied to cover larger lead mins?
        maxRate = bin / 4096 + 5;
        rc.setIndicatorString(Integer.toString(maxRate));
>>>>>>> b76914a947c4b5aa0f839bdeaa0adeb29ca5ff89
    }

    private boolean canMakeGold() throws GameActionException {
        /*constraints:
        	current rate < maximum allwoed rate
        	have enough lead leaving minimum reserve
        	can transmute*/
<<<<<<< HEAD
    	int currLead = rc.getTeamLeadAmount(rc.getTeam());
		return rc.getTransmutationRate() < maxRate && currLead - rc.getTransmutationRate() > minLead && rc.canTransmute();
	}
=======
        int currLead = rc.getTeamLeadAmount(rc.getTeam());
        return rc.getTransmutationRate() < maxRate && currLead - rc.getTransmutationRate() > minLead && rc.canTransmute();
    }
>>>>>>> b76914a947c4b5aa0f839bdeaa0adeb29ca5ff89
}
