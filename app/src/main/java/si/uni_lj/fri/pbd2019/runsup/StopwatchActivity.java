package si.uni_lj.fri.pbd2019.runsup;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.helpers.MainHelper;
import si.uni_lj.fri.pbd2019.runsup.services.TrackerService;

public class StopwatchActivity extends AppCompatActivity {



    // ### PROPERTIES ###

    public static final String TAG = StopwatchActivity.class.getName();  // class tag

    private boolean bound;  // indicator that indicates whether the service is bound
    private ServiceConnection sConn;  // connection to service
    private TrackerService service;  // Service proxy instance

    private int sportActivity;  // current sport activity
    private long duration;  // current workout duration
    private double distance;  // current distance
    private double paceAccumulator; // average pace
    private long updateCounter;  // counter for number of data updates.
    private double calories;  // current calories used
    private ArrayList<ArrayList<Location>> positions;
    private IntentFilter filter;

    // State of the stopwatch (see Constant class for values)
    private int state;
    Context context = this;  // Context of instance

    // buttons for starting/pausing the workout and for ending the workout
    Button stopwatchStartButton;
    Button endWorkoutButton;
    Button toggleSportActivityButton;


    // ## class level listeners ##

    View.OnClickListener pauseListener = new View.OnClickListener() {
        public void onClick(View v) {  // callback method for when the button is pressed
            pauseStopwatch(v);
        }
    };

    View.OnClickListener continueListener = new View.OnClickListener() {
        public void onClick(View v) {
            continueStopwatch(v);
        }
    };

    View.OnClickListener endListener = new View.OnClickListener() {
        public void onClick(View v) {
            endWorkout();
        }
    };

    // ## /class level listeners ##


    // ## BROADCAST RECEIVER ##
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        /* Anonymous BroadcastReceiver instance that receives commands */
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCounter++;  // Increment update counter.

            // Update TextView fields in UI using received values.
            updateDuration(intent.getLongExtra("duration", 0));
            updateDistance(intent.getDoubleExtra("distance", 0.0));
            updatePace(intent.getDoubleExtra("pace", 0.0));
            updateCalories(intent.getDoubleExtra("calories", 0.0));

            // Add locations list to list of location lists.
            positions.add(intent.<Location>getParcelableArrayListExtra("positionList"));

            // Set property indicating current sport activity.
            sportActivity = intent.getIntExtra("sportActivity", -1);
        }
    };
    // ## /BROADCAST RECEIVER ##


    // ### /PROPERTIES ###





    // oncCreate: method called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Call onCreate of the superclass.

        setContentView(R.layout.activity_stopwatch);  // Set layout for UI.

        // Initialize buttons
        this.stopwatchStartButton = findViewById(R.id.button_stopwatch_start);
        this.endWorkoutButton = findViewById(R.id.button_stopwatch_endworkout);
        this.toggleSportActivityButton = findViewById(R.id.button_stopwatch_selectsport);

        // set listener on button to listen for workout start.
        this.stopwatchStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  // callback method for when the button is pressed
                startStopwatch(v);
            }
        });

        this.toggleSportActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == Constant.STATE_STOPPED) {
                    toggleSportActivity();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Change Sport Activity")
                            .setMessage("You cannot change the sport activity while a workout is in progress!")

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setPositiveButton(android.R.string.yes, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });

        // set listener on button to listen for workout end.
        this.endWorkoutButton.setOnClickListener(endListener);


        // Check for location access permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constant.LOCATION_PERMISSION_REQUEST_CODE);
        }

        // property initializations
        this.positions = new ArrayList<>();  // initialize list of positions lists.
        this.paceAccumulator = 0;  // Initialize pace accumulator;
        this.updateCounter = 0;  // Initialize counter of data updates.
        this.state = Constant.STATE_STOPPED;
        this.sportActivity = Constant.RUNNING;  // Initialize sportActivity indicator.

        // ## INTENT FILTER INITIALIZATION ##
        this.filter = new IntentFilter();
        this.filter.addAction(Constant.TICK);  // Register action.
        registerReceiver(receiver, filter);  // Register receiver.
        // ## INTENT FILTER INITIALIZATION ##


        // Create a new service connection.
        sConn = new ServiceConnection() {

            // callback that is called when the service is connected.
            public void onServiceConnected(ComponentName name, IBinder binder) {
                service = ((TrackerService.LocalBinder)binder).getService();  // Call getService of passed binder.
                bound = true;  // Set bound indicator to true.
            }

            // callback that is called when the service is disconnected.
            public void onServiceDisconnected(ComponentName name) {
                bound = false; // Set bound indicator to false.
                unregisterReceiver(receiver);
            }
        };

        // Bind service to activity.
        bindService(new Intent(this, TrackerService.class), sConn, BIND_AUTO_CREATE);
        this.bound = true;  // Set bound indicator.
    }


    // onPause: method run when the activity is paused.
    @Override
    public void onPause() {
        super.onPause();
        // Check if workout running.
        if (this.state == Constant.STATE_STOPPED || this.state == Constant.STATE_PAUSED) {
            if (this.bound) {
                unbindService(sConn);
                this.bound = false;
                stopService(new Intent(StopwatchActivity.this, TrackerService.class));
                // unregisterReceiver(receiver); // TODO
            }
        }
    }

    // onResume: method run when the activity is resumed.
    @Override
    protected void onResume() {
        super.onResume();  // Call onResume method of superclass.

        // rebind click listeners according to state.
        switch (this.state) {
            case Constant.STATE_RUNNING:
                this.stopwatchStartButton.setOnClickListener(pauseListener);
                break;
            case Constant.STATE_PAUSED:
                this.stopwatchStartButton.setOnClickListener(continueListener);
                break;
            case Constant.STATE_STOPPED:
                break;
            case Constant.STATE_CONTINUE:
                this.stopwatchStartButton.setOnClickListener(pauseListener);
                break;
        }
        // If service not bound, bind it.
        if (!this.bound) {
            // Bind service to activity.
            bindService(new Intent(this, TrackerService.class), sConn, BIND_AUTO_CREATE);
            registerReceiver(receiver, filter);
        }
    }

    // onDestroy: method called when the activity is destoryed.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.bound) {  // If service still bounded, unbind.
            unbindService(sConn);
            this.bound = false;
            stopService(new Intent(StopwatchActivity.this, TrackerService.class));
            unregisterReceiver(receiver);
        }

    }

    // ### METHODS FOR CONTROLLING THE WORKOUT STATE ###

    // startStopwatch: method used to start the workout
    public void startStopwatch(final View view) {

        // start TrackerService with action si.uni_lj.fri.pbd2019.runsup.COMMAND_START
        // this.sendBroadcast(new Intent(Constant.COMMAND_START));
        Intent startIntent = new Intent(this, TrackerService.class);
        startIntent.setAction(Constant.COMMAND_START);
        startIntent.putExtra("sportActivity", this.sportActivity);
        this.startService(startIntent);
        this.updateStartButtonText(Constant.STATE_RUNNING);

        // set listener to button with id button_stopwatch_start - listen for pause.
        this.stopwatchStartButton.setOnClickListener(pauseListener);

        this.state = Constant.STATE_RUNNING;  // Update state.


        // TODO

        TextView cl = findViewById(R.id.textview_stopwatch_calories);
        cl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click");
                // Initialize intent to start new activity and put info to display in extras.
                Intent activeWorkoutMapIntent = new Intent(StopwatchActivity.this, ActiveWorkoutMapActivity.class);
                StopwatchActivity.this.startActivity(activeWorkoutMapIntent);
            }
        });


    }

    // endWorkout: method used to end current workout.
    public void endWorkout() {

        // Prompt user to confirm decision to end workout.
        new AlertDialog.Builder(this)
                .setTitle("Stop Workout")
                .setMessage("Are you sure you want to end this workout?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // Initialize intent for starting the service.
                        Intent startIntent = new Intent(context, TrackerService.class);
                        startIntent.setAction(Constant.COMMAND_STOP);  // Set action.
                        startService(startIntent);
                        if (bound) {  // If service still bounded, unbind.
                            unbindService(sConn);
                            bound = false;  // Update bound indicator.
                            stopService(new Intent(StopwatchActivity.this, TrackerService.class));
                        }

                        state = Constant.STATE_STOPPED;  // Update state.

                        // Initialize intent to start new activity and put info to display in extras.
                        Intent workoutDetailsIntent = new Intent(StopwatchActivity.this, WorkoutDetailActivity.class);
                        workoutDetailsIntent.putExtra("sportActivity", sportActivity); //Optional parameters
                        workoutDetailsIntent.putExtra("duration", duration);
                        workoutDetailsIntent.putExtra("distance", distance);
                        workoutDetailsIntent.putExtra("pace", paceAccumulator/updateCounter);
                        workoutDetailsIntent.putExtra("calories", calories);
                        workoutDetailsIntent.putExtra("finalPositionsList", positions);
                        StopwatchActivity.this.startActivity(workoutDetailsIntent);
                    }
                })
                .setNegativeButton(R.string.no, null)  // Do nothing if user selects cancel.
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // pauseStopWatch: method used to pause the stopwatch.
    public void pauseStopwatch(View view) {

        // Initialize the intent for starting the service.
        Intent startIntent = new Intent(this, TrackerService.class);
        startIntent.setAction(Constant.COMMAND_PAUSE);
        startService(startIntent);

        // Set text on start button.
        this.updateStartButtonText(Constant.STATE_PAUSED);

        // set listener to button to listen for commands to continue workout.
        this.stopwatchStartButton.setOnClickListener(continueListener);

        // Make button for ending workout visible.
        this.endWorkoutButton.setVisibility(View.VISIBLE);

        this.state = Constant.STATE_PAUSED;  // Update state.

    }

    // continueStopwatch: method used to resume the workout paused.
    public void continueStopwatch(View view) {

        // Initialize the intent for starting the service.
        Intent startIntent = new Intent(this, TrackerService.class);
        startIntent.setAction(Constant.COMMAND_CONTINUE);
        if (this.positions.size() >= 1) {
            startIntent.putParcelableArrayListExtra("positions", this.positions.get(this.positions.size()-1));
        } else {
            startIntent.putParcelableArrayListExtra("positions", new ArrayList<Location>());
        }
        startService(startIntent);  // Start service with intent.

        // Set text on start button.
        this.updateStartButtonText(Constant.STATE_RUNNING);

        // set listener to button to listen for commands to pause the workout.
        this.stopwatchStartButton.setOnClickListener(pauseListener);

        // Make button for ending workout invisible.
        this.endWorkoutButton.setVisibility(View.INVISIBLE);
        this.state = Constant.STATE_CONTINUE;  // Update state.
    }

    // ### /METHOD FOR CONTROLLING THE WORKOUT STATE ###




    // ### METHODS FOR UPDATING THE UI ###

    // updateDuration: update the workout duration display.
    private void updateDuration(long duration) {
        this.duration = duration;
        TextView durationText = findViewById(R.id.textview_stopwatch_duration);
        durationText.setText(MainHelper.formatDuration(duration));
    }

    // updateDistance: update the workout distance display.
    private void updateDistance(double dist) {
       this.distance = dist;
       TextView distanceText = findViewById(R.id.textview_stopwatch_distance);
       distanceText.setText(MainHelper.formatDistance(dist));
    }

    // updatePace: update the workout pace display.
    private void updatePace(double pace) {
        this.paceAccumulator += pace;
        TextView paceText = findViewById(R.id.textview_stopwatch_pace);
        paceText.setText(MainHelper.formatPace(pace));
    }

    // updateCalories: update the workout calories display.
    private void updateCalories(double calories) {
        this.calories = calories;
        TextView caloriesText = findViewById(R.id.textview_stopwatch_calories);
        caloriesText.setText(MainHelper.formatCalories(calories));
    }

    // updateStartButton: update text on Start button.
    private void updateStartButtonText(int state) {

        // Switch on state.
        switch(state) {
            case Constant.STATE_RUNNING:
                this.stopwatchStartButton.setText(R.string.stopwatch_stop);
                break;
            case Constant.STATE_PAUSED:
                this.stopwatchStartButton.setText(R.string.stopwatch_continue);
                break;
            case Constant.STATE_STOPPED:
                this.stopwatchStartButton.setText(R.string.stopwatch_continue);
                break;
            case Constant.STATE_CONTINUE:
                this.stopwatchStartButton.setText(R.string.stopwatch_stop);
                break;
        }

    }

    // updateSportActivity: update text on sport activity button.
    private void updateSportActivityText(int sportActivity) {
        this.toggleSportActivityButton.setText(MainHelper.getSportActivityName(sportActivity));
    }

    // toggleSportActivity: toggle the sport activity
    private void toggleSportActivity() {
        this.sportActivity += 1;
        this.sportActivity %= 3;
        updateSportActivityText(this.sportActivity);
    }

    // ### /METHODS FOR UPDATING THE UI ###
}
