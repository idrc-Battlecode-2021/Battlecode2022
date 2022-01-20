package bot5_JJ8.robots.buildings;

import battlecode.common.*;
import bot5_JJ8.robots.Robot;

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
