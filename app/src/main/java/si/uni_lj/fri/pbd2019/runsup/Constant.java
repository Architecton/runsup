package si.uni_lj.fri.pbd2019.runsup;

import android.app.Application;

public class Constant {

    // Commands to Service
    public static final String COMMAND_START = "si.uni_lj.fri.pbd2019.runsup.COMMAND_START";
    public static final String COMMAND_CONTINUE = "si.uni_lj.fri.pbd2019.runsup.COMMAND_CONTINUE";
    public static final String COMMAND_PAUSE = "si.uni_lj.fri.pbd2019.runsup.COMMAND_PAUSE";
    public static final String COMMAND_STOP = "si.uni_lj.fri.pbd2019.runsup.COMMAND_STOP";
    public static final String UPDATE_SPORT_ACTIVITY = "si.uni_lj.fri.pbd2019.runsup.UPDATE_SPORT_ACTIVITY";

    // list of activities
    public static final String[] activities = {"Running", "Walking", "Cycling"};

    // Tick intent
    public static final String TICK = "si.uni_lj.fri.pbd2019.runsup.TICK";

    // Workout activity codes
    public static final int RUNNING = 0;
    public static final int WALKING = 1;
    public static final int CYCLING = 2;

    // Workout state codes
    public static final int STATE_STOPPED = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_CONTINUE = 3;

    // Location Permission request Code
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 3273;

    // Threshold to consider workout after pause a new session
    public static final int PAUSE_DIST_CHANGE_THRESH = 100;


    // distance unit codes.
    public static final int UNITS_KM = 0;
    public static final int UNITS_MI = 1;

    // units abbreviations.
    public static final String UNITS_KM_ABBR = App.getAppResources().getString(R.string.all_labeldistanceunitkilometers);
    public static final String UNITS_MI_ABBR = App.getAppResources().getString(R.string.all_labeldistanceunitmiles);
    public static final String UNITS_MINPKM_ABBR = App.getAppResources().getString(R.string.all_labelpaceunitkilometers);
    public static final String UNITS_MINPMI_ABBR = App.getAppResources().getString(R.string.all_labelpaceunitmiles);


    // Default values of user.
    public static final int DEFAULT_WEIGHT = 65;
    public static final int DEFAULT_AGE = 30;
    public static final String DEFAULT_WORKOUT_TITLE_FORMAT_STRING = "Workout %d";
    public static final long LOCATION_UPDATES_TIMEOUT_TO_SAVE = 10000;

    // Base url for the back-end on the cloud.
    public static final String BASE_CLOUD_URL = "https://runsup.herokuapp.com";
}
