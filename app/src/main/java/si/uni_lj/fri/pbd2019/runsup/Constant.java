package si.uni_lj.fri.pbd2019.runsup;

public class Constant {

    // Commands to Service
    public static final String COMMAND_START = "si.uni_lj.fri.pbd2019.runsup.COMMAND_START";
    public static final String COMMAND_CONTINUE = "si.uni_lj.fri.pbd2019.runsup.COMMAND_CONTINUE";
    public static final String COMMAND_PAUSE = "si.uni_lj.fri.pbd2019.runsup.COMMAND_PAUSE";
    public static final String COMMAND_STOP = "si.uni_lj.fri.pbd2019.runsup.COMMAND_STOP";

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

    // Location Permission Eequest Code
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 3273;

    // Threshold to consider workout after pause a new session
    public static final int PAUSE_DIST_CHANGE_THRESH = 100;

}
