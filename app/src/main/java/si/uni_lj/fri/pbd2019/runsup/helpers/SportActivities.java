package si.uni_lj.fri.pbd2019.runsup.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import si.uni_lj.fri.pbd2019.runsup.Constant;

public final class SportActivities {

    // ### Define tables for MET values. ###

    // speed to MET conversion for running
    public static Map<Integer, Double> speedToMET_running = new HashMap<Integer, Double>() {{
        put(4, 6.0);
        put(5, 8.3);
        put(6, 9.8);
        put(7, 11.0);
        put(8, 11.8);
        put(9, 12.8);
        put(10, 14.5);
        put(11, 16.0);
        put(12, 19.0);
        put(13, 19.8);
        put(14, 23.0);
    }};

    // speed to MET conversion for walking
    public static Map<Integer, Double> speedToMET_walking = new HashMap<Integer, Double>() {{
        put(1, 2.0);
        put(2, 2.8);
        put(3, 3.1);
        put(4, 3.5);
    }};

    // speed to MET conversion for cycling.
    public static Map<Integer, Double> speedToMET_cycling = new HashMap<Integer, Double>() {{
        put(10, 6.8);
        put(12, 8.0);
        put(14, 10.0);
        put(16, 12.8);
        put(18, 13.6);
        put(20, 15.8);
    }};

    // Map sports activities to multipliers to use when actual speed not in tables.
    public static Map<Integer, Double> MET_aux = new HashMap<Integer, Double>() {{
        put(Constant.RUNNING, 1.535353535);
        put(Constant.WALKING, 1.14);
        put(Constant.CYCLING, 0.744444444);
    }};



    /**
    * Returns MET value for an activity.
    * @param activityType - sport activity type (0 - running, 1 - walking, 2 - cycling)
    * @param speed - speed in m/s
    * @return
    */
    public static double getMET(int activityType, float speed) {
        switch (activityType) {
            case Constant.RUNNING:
                if (speedToMET_running.containsKey((int)Math.ceil(speed))) {
                    return speedToMET_running.get((int)Math.ceil(speed));
                } else {
                    return speed*MET_aux.get(activityType);
                }
            case Constant.WALKING:
                if (speedToMET_walking.containsKey((int)Math.ceil(speed))) {
                    return speedToMET_walking.get((int)Math.ceil(speed));
                } else {
                    return speed*MET_aux.get(activityType);
                }
            case Constant.CYCLING:
                if (speedToMET_cycling.containsKey((int)Math.ceil(speed))) {
                    return speedToMET_cycling.get((int)Math.ceil(speed));
                } else {
                    return speed*MET_aux.get(activityType);
                }
            default:
                throw new IllegalArgumentException("Unknown activity type.");
        }
    }


    /**
     * Returns final calories computed from the data provided (returns value in kcal)
     * @param sportActivity - sport activity type (0 - running, 1 - walking, 2 - cycling)
     * @param weight - weight in kg
     * @param speedList - list of all speed values recorded (unit = m/s)
     * @param timeFillingSpeedListInHours - time of collecting speed list (duration of sport activity from first to last speedPoint in speedList)
     * @return
     */
    public static double countCalories(int sportActivity, float weight, List<Float> speedList, double timeFillingSpeedListInHours) {

        // Get average speed in speed list.
        float avgSpeed = 0.0f;
        for (float f : speedList) {
            avgSpeed += f;
        }
        avgSpeed /= (float)speedList.size();

        // Compute calories used.
        return getMET(sportActivity, MainHelper.mpsToMiph(avgSpeed)) * weight * timeFillingSpeedListInHours;
    }

}
