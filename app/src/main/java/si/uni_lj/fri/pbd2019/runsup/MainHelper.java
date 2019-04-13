package si.uni_lj.fri.pbd2019.runsup;

import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;

import java.util.Date;

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
     * round number to 2 decimal places and return as string
     */
    public static String formatPace(double n) {
        return String.format("%.2f", n*1.0d-3);
    }

    /**
     * round number to integer
     */
    public static String formatCalories(double n) {
        return String.format("%d", (int)Math.round(n));

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
    public static double mpsToMiph(double n) {
        return n*MpS_TO_MIpH;

    }

    /**
     *  convert min/km to min/mi (multiply with a corresponding constant)
     * */
    public static double minpkmToMinpmi(double n) {
        return n*MINpKM_TO_MINpMI;
    }
}
