<<<<<<< HEAD
<<<<<<< HEAD:src/bot2/robots/buildings/Building.java
=======
>>>>>>> b76914a947c4b5aa0f839bdeaa0adeb29ca5ff89
package bot2.robots.buildings;

import battlecode.common.*;
import bot2.robots.Robot;
<<<<<<< HEAD
=======
package bot1.robots.buildings;

import battlecode.common.*;
import bot1.robots.Robot;
>>>>>>> b76914a947c4b5aa0f839bdeaa0adeb29ca5ff89:src/bot1/robots/buildings/Building.java
=======
>>>>>>> b76914a947c4b5aa0f839bdeaa0adeb29ca5ff89

import java.util.ArrayList;

public abstract class Building extends Robot {
    private AnomalyScheduleEntry[] anomaly = rc.getAnomalySchedule();
    private boolean avoidingFury=false;
    private ArrayList<AnomalyScheduleEntry> relevantAnomalies = new ArrayList<AnomalyScheduleEntry>();
    public Building(RobotController rc) {
        super(rc);
    }

    public void parseAnomalies() {
        for (AnomalyScheduleEntry a : anomaly) {
            if (a.anomalyType == AnomalyType.FURY) {
                relevantAnomalies.add(a);
            }
        }
    }
    public void avoidFury() throws GameActionException {
        for (AnomalyScheduleEntry a: relevantAnomalies){
            if(rc.getRoundNum()+15>a.roundNumber){
                if(rc.canTransform() && rc.getMode()== RobotMode.TURRET){
                    rc.transform();
                    avoidingFury=true;
                }
            }
        }
    }
    public void retransform() throws GameActionException{
        for (int i=0; i<relevantAnomalies.size(); i++){
            AnomalyScheduleEntry a = relevantAnomalies.get(i);
            if(avoidingFury && a.roundNumber<rc.getRoundNum()){
                if (rc.canTransform() && rc.getMode()==RobotMode.PORTABLE){
                    rc.transform();
                    avoidingFury=false;
                    relevantAnomalies.remove(i);
                    break;
                }
            }
        }

    }

}
