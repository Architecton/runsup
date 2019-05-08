package si.uni_lj.fri.pbd2019.runsup.helpers;

import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;

import java.util.Date;

import si.uni_lj.fri.pbd2019.runsup.Constant;
import si.uni_lj.fri.pbd2019.runsup.R;
import si.uni_lj.fri.pbd2019.runsup.WorkoutDetailActivity;

public final class MainHelper {

    /* constants */
    private static final float MpS_TO_MIpH = 2.23694f;
    private static final float KM_TO_MI = 0.62137119223734f;
    private static final float MINpKM_TO_MINpMI = 1.609344f;




    /**
     * return string of time in format HH:MM:SS
     * @param time - in seconds
     */
    public static String formatDuration(long time) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date(time * 1000L));
    }

    /**
     * convert m to km and round to 2 decimal places and return as string
     */
    public static String formatDistance(double n) {
        return String.format("%.2f", n*1.0e-3);
    }


    /**
     * conver m to km and round to 2 decimal places and return as string with units
     */
    public static String formatDistanceWithUnits(double n) {
        return formatDistance(n) + " " + WorkoutDetailActivity.resources.getString(R.string.distance_unit);
    }

    /**
     * round number to 2 decimal places and return as string
     */
    public static String formatPace(double n) {
        return String.format("%.2f", n);
    }

    /**
     * round number to 2 decimal places and return as string
     */
    public static String formatPaceWithUnits(double n) {
        return formatPace(n) + " " + WorkoutDetailActivity.resources.getString(R.string.pace_unit);
    }


    /**
     * round number to integer
     */
    public static String formatCalories(double n) {
        return String.format("%d", (int)Math.round(n));
    }

    /**
     * round number to integer
     */
    public static String formatCaloriesWithUnits(double n) {
        return formatCalories(n) + " " + WorkoutDetailActivity.resources.getString(R.string.calories_unit);
    }

    /**
     * convert km to mi (multiply with a corresponding constant)
     * */
    public static double kmToMi(double n) {
        return n*KM_TO_MI;

    }

    /**
     * convert m/s to mi/h (multiply with a corresponding constant)
     * */
    public static float mpsToMiph(float n) {
        return n*MpS_TO_MIpH;

    }

    /**
     * convert m/s to mi/h (multiply with a corresponding constant)
     * */
    public static double kmphToMiph(Double n) {
        return n*KM_TO_MI;
    }

    /**
     *  convert min/km to min/mi (multiply with a corresponding constant)
     * */
    public static double minpkmToMinpmi(double n) {
        return n*MINpKM_TO_MINpMI;
    }

    /**
     * Convert constant representing a sport activity to name of sport activity.
     * @param sportActivity
     * @return
     */
    public static String getSportActivityName(int sportActivity) {
        switch (sportActivity) {
            case Constant.RUNNING:
                return "Running";
            case Constant.CYCLING:
                return  "Cycling";
            case Constant.WALKING:
                return "Walking";
            default:
                return "Unknown";
        }
    }
}
