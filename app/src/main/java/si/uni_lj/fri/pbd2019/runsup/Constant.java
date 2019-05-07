package si.uni_lj.fri.pbd2019.runsup;

public class Constant {
    public static final String COMMAND_START = "si.uni_lj.fri.pbd2019.runsup.COMMAND_START";
    public static final String COMMAND_CONTINUE = "si.uni_lj.fri.pbd2019.runsup.COMMAND_CONTINUE";
    public static final String COMMAND_PAUSE = "si.uni_lj.fri.pbd2019.runsup.COMMAND_PAUSE";
    public static final String COMMAND_STOP = "si.uni_lj.fri.pbd2019.runsup.COMMAND_STOP";

    public static final String TICK = "si.uni_lj.fri.pbd2019.runsup.TICK";

    public static final int RUNNING = 0;
    public static final int WALKING = 1;
    public static final int CYCLING = 2;

    public static final int STATE_STOPPED = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_CONTINUE = 3;

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 3273;

    public static final int PAUSE_DIST_CHANGE_THRESH = 100;
}
