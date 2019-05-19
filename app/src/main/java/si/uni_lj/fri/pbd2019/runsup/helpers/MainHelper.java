package si.uni_lj.fri.pbd2019.runsup.helpers;

import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;

import java.util.Date;
import java.util.Locale;

import si.uni_lj.fri.pbd2019.runsup.Constant;
import si.uni_lj.fri.pbd2019.runsup.R;
import si.uni_lj.fri.pbd2019.runsup.WorkoutDetailActivity;

public final class MainHelper {

    /* constants */
    private static final float MpS_TO_MIpH = 2.23694f;
    private static final float KM_TO_MI = 0.62137119223734f;
    private static final float MINpKM_TO_MINpMI = 1.609344f;


    // formatDuration: format duration of workout to HH:mm:ss form.
    public static String formatDuration(long time) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date(time * 1000L));
    }

    // formatDistance: format distance (return distance in kilometers with 2 decimals)
    public static String formatDistance(double n) {
        return String.format(Locale.getDefault(), "%.2f", n*1.0e-3);
    }


    // formatPace: format pace (return min/km with 2 decimals)
    public static String formatPace(double n) {
        return String.format(Locale.getDefault(), "%.2f", n);
    }


    // formatCalories: format calories (return rounded integer)
    public static String formatCalories(double n) {
        return String.format(Locale.getDefault(), "%d", (int)Math.round(n));
    }

    // formatCaloriesWithUnits: format calories (return rounded integer with unit abbreviation appended
    public static String formatCaloriesWithUnits(double n) {
        return formatCalories(n) + " " + WorkoutDetailActivity.resources.getString(R.string.all_labelcaloriesunit);
    }


    // kmToMi: convert kilometers to miles.
    public static double kmToMi(double n) {
        return n*KM_TO_MI;

    }

    // mpsToMiph: convert meters/second to miles/hour
    public static float mpsToMiph(float n) {
        return n*MpS_TO_MIpH;

    }

    // kmphToMiph: conver kilometers/hour to miles/hour
    public static double kmphToMiph(Double n) {
        return n*KM_TO_MI;
    }


    // minpkmToMinpmi: convert minutes/kilometer to minutes/mile
    public static double minpkmToMinpmi(double n) {
        return n*MINpKM_TO_MINpMI;
    }


    // getSportActivityName: Get name of sport activity from its integer code.
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
