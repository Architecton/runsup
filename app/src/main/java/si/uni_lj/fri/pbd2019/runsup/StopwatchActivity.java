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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StopwatchActivity extends AppCompatActivity {



    // ### PROPERTIES ###

    public static final String TAG = StopwatchActivity.class.getName();  // class tag

    private boolean bound;  // indicator that indicates whether the service is bound
    private ServiceConnection sConn;  // connection to service
    private TrackerService service;  // Service proxy instance

    private int sportActivity;  // current sport activity
    private long duration;  // current workout duration
    private double distance;  // current distance
    private double pace;  // current pace
    private double calories;  // current calories used
    private ArrayList<List<Location>> positions;

    Button stopwatchStartButton;
    Button endWorkoutButton;

    // ## BROADCAST RECEIVER ##
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        /* Anonymous BroadcastReceiver instance that receives commands */
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update TextView fields in UI using received values.
            updateDuration(intent.getLongExtra("duration", 0));
            updateDistance(intent.getDoubleExtra("distance", 0.0));
            updatePace(intent.getDoubleExtra("pace", 0.0));
            updateCalories(intent.getDoubleExtra("calories", 0.0));
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

        this.positions = new ArrayList<>();  // initialize list of positions lists.

        // ## INTENT FILTER INITIALIZATION ##
        IntentFilter filter = new IntentFilter();  // Instantiate IntentFilter.
        filter.addAction(Constant.TICK);  // Register action.
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
            }
        };

        // Bind service to activity.
        bindService(new Intent(this, TrackerService.class), sConn, BIND_AUTO_CREATE);
    }

    // onStart: method that is called when the layout becomes visible to the user.
    @Override
    protected void onStart() {
        super.onStart();
        // Initialize button pointers.
        this.stopwatchStartButton = findViewById(R.id.button_stopwatch_start);
        this.endWorkoutButton = findViewById(R.id.button_stopwatch_endworkout);

        // set listener on button to listen for workout start.
        this.stopwatchStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  // callback method for when the button is pressed
                startStopwatch(v);
            }
        });

        // set listener on button to listen for workout end.
        this.endWorkoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                endWorkout();
            }
        });
    }

    // onPause: method run when the activity is paused.
    @Override
    public void onPause() {
        super.onPause();
        this.sendBroadcast(new Intent(Constant.COMMAND_PAUSE));
    }

    // onResume: method run when the activity is resumed.
    @Override
    protected void onResume() {
        super.onResume();
        this.sendBroadcast(new Intent(Constant.COMMAND_CONTINUE));
        // Start service TrackerService.
    }

    // onDestroy: method called when the activity is destoryed.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.bound) {  // If service still bounded, unbind.
            unbindService(sConn);
            this.bound = false;
        }

    }


    // ### METHOD FOR CONTROLLING THE WORKOUT STATE ###

    // startStopwatch: method used to start the workout
    public void startStopwatch(final View view) {

        // Check for location access permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constant.LOCATION_PERMISSION_REQUEST_CODE);
        }

        // start TrackerService with action si.uni_lj.fri.pbd2019.runsup.COMMAND_START
        this.sendBroadcast(new Intent(Constant.COMMAND_START));
        this.updateStartButtonText(Constant.STATE_RUNNING);

        // set listener to button with id button_stopwatch_start - listen for pause.
        this.stopwatchStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  // callback method for when the button is pressed
                pauseStopwatch(v);
            }
        });
    }

    // endWorkout: method used to end current workout.
    public void endWorkout() {

        // Prompt user to confirm decision to end workout.
        new AlertDialog.Builder(this)
                .setTitle("Stop Workout")
                .setMessage("Are you sure you want to end this workout?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendBroadcast(new Intent(Constant.COMMAND_STOP));  // Send command to stop workout.
                        if (bound) {  // If service still bounded, unbind.
                            unbindService(sConn);
                            bound = false;
                        }

                        // Initialize intent to start new activity and put info to display in extras.
                        Intent workoutDetailsIntent = new Intent(StopwatchActivity.this, WorkoutDetailActivity.class);
                        workoutDetailsIntent.putExtra("sportActivity", sportActivity); //Optional parameters
                        workoutDetailsIntent.putExtra("duration", duration);
                        workoutDetailsIntent.putExtra("distance", distance);
                        workoutDetailsIntent.putExtra("pace", pace);
                        workoutDetailsIntent.putExtra("calories", calories);
                        workoutDetailsIntent.putExtra("finalPositionList", positions);
                        StopwatchActivity.this.startActivity(workoutDetailsIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, null)  // Do nothing if user selects cancel.
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // pauseStopWatch: method used to pause the stopwatch.
    public void pauseStopwatch(View view) {

        // Send broadcast to service.
        this.sendBroadcast(new Intent(Constant.COMMAND_PAUSE));
        // Toggle text on start button.
        this.updateStartButtonText(Constant.STATE_PAUSED);


        // set listener to button to listen for commands to continue workout.
        this.stopwatchStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  // callback method for when the button is pressed
                continueStopwatch(v);
            }
        });

        // Make button for ending workout visible.
        this.endWorkoutButton.setVisibility(View.VISIBLE);

    }

    // continueStopwatch: method used to resume the workout paused.
    public void continueStopwatch(View view) {

        // Send broadcast to service.
        this.sendBroadcast(new Intent(Constant.COMMAND_CONTINUE));
        // TOggle text on start button.
        this.updateStartButtonText(Constant.STATE_RUNNING);

        // set listener to button to listen for commands to pause the workout.
        this.stopwatchStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  // callback method for when the button is pressed
                pauseStopwatch(v);
            }
        });

        // Make button for ending workout invisible.
        this.endWorkoutButton.setVisibility(View.INVISIBLE);
    }

    // ### METHOD FOR CONTROLLING THE WORKOUT STATE ###




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
        this.pace = pace;
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
                this.stopwatchStartButton.setText(R.string.workout_running_start_button_text);
                break;
            case Constant.STATE_PAUSED:
                this.stopwatchStartButton.setText(R.string.workout_paused_start_button_text);
                break;
            case Constant.STATE_STOPPED:
                this.stopwatchStartButton.setText(R.string.workout_paused_start_button_text);
                break;
            case Constant.STATE_CONTINUE:
                this.stopwatchStartButton.setText(R.string.workout_running_start_button_text);
                break;
        }

    }

    // ### /METHODS FOR UPDATING THE UI ###

}
