package si.uni_lj.fri.pbd2019.runsup.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import si.uni_lj.fri.pbd2019.runsup.ActiveWorkoutMapActivity;
import si.uni_lj.fri.pbd2019.runsup.Constant;
import si.uni_lj.fri.pbd2019.runsup.R;
import si.uni_lj.fri.pbd2019.runsup.WorkoutDetailActivity;
import si.uni_lj.fri.pbd2019.runsup.helpers.MainHelper;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;
import si.uni_lj.fri.pbd2019.runsup.services.TrackerService;
import si.uni_lj.fri.pbd2019.runsup.settings.SettingsActivity;

import static android.content.Context.MODE_PRIVATE;

public class StopwatchFragment extends Fragment {



    // ### PROPERTIES ###

    public static final String TAG = StopwatchFragment.class.getName();  // class tag

    private boolean bound;  // indicator that indicates whether the service is bound
    private ServiceConnection sConn;  // connection to service
    private TrackerService service;  // Service proxy instance

    private int sportActivity;  // current sport activity
    private long duration;  // current workout duration
    private double distance;  // current distance
    private double paceAccumulator; // average pace
    private long updateCounter;  // counter for number of data updates.
    private double calories;  // current calories used
    private ArrayList<Location> positions;  // list of positions
    private IntentFilter filter;  // intent filter

    // State of the stopwatch (see Constant class for values)
    private int state;

    // buttons for starting/pausing the workout and for ending the workout
    private Button stopwatchStartButton;
    private Button endWorkoutButton;
    private Button showMapButton;
    private Button sportActivityButton;

    // shared preferences
    private SharedPreferences preferences;
    // OnSharedPreferenceChangeListener instance
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    // distance units to use (see Constant class for values)
    private int distUnits;

    // firstRun: indicator that indicates fragment being run for first time
    private boolean firstRun = true;

    // lastPausedWorkout - if not null, it holds the last paused workout found in the database.
    Workout lastUnfinishedWorkout;

    // ## class level listeners ##

    View.OnClickListener pauseListener = new View.OnClickListener() {
        public void onClick(View v) {  // callback method for when the button is pressed
            pauseStopwatch();
        }
    };

    View.OnClickListener continueListener = new View.OnClickListener() {
        public void onClick(View v) {
            continueStopwatch();
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
            updateDistance(intent.getDoubleExtra("distance", 0.0), distUnits);
            updatePace(intent.getDoubleExtra("pace", 0.0), distUnits);
            updateCalories(intent.getDoubleExtra("calories", 0.0));

            // Add locations list to list of location lists.
            Location receivedLocation = intent.<Location>getParcelableExtra("position");
            if (receivedLocation != null) {
                positions.add(receivedLocation);
            }
            // Set property indicating current sport activity.
            sportActivity = intent.getIntExtra("sportActivity", -1);
        }
    };

    // Flag that indicates whether receiver is registered.
    private boolean receiverRegistered;

    // ## /BROADCAST RECEIVER ##


    // ### /PROPERTIES ###


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize shared preferences instance.
        this.preferences = getActivity().getSharedPreferences(Constant.STATE_PREF_NAME, MODE_PRIVATE);

        // Initialize OnSharedPreferenceChangeListener instance.
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                // Handle preference changes.
                if (key.equals("unit")) {  // If value with key "unit" changed, change value of distUnits variable.
                    distUnits = preferences.getInt(key, Constant.UNITS_KM) == Constant.UNITS_KM
                            ? Constant.UNITS_KM : Constant.UNITS_MI;
                    preferences.edit().putBoolean("unitsChanged", true).apply();  // Set indicator that units changed.
                }
                if (key.equals("pref_location_access_value")) {
                    // pass
                }
            }
        };

        // Register OnSharedPreferenceChangeListener instance.
        preferences.registerOnSharedPreferenceChangeListener(prefListener);

        // Set value of variable storing distance units.
        this.distUnits = this.preferences.getInt("unit", Constant.UNITS_KM) == Constant.UNITS_KM
                ? Constant.UNITS_KM : Constant.UNITS_MI;

        // Check for location access permissions.
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Prompt user to confirm decision to end workout.
            new AlertDialog.Builder(getContext())
                    .setTitle("Access To Location")
                    .setMessage("Please turn on location access in the application's settings for the best experience.")
                    .setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent settingsActivityIntent = new Intent(getContext(), SettingsActivity.class);
                            getContext().startActivity(settingsActivityIntent);
                        }
                    })
                    .setNegativeButton(R.string.dismiss, null)  // Do nothing if user selects cancel.
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

        // property initializations
        this.positions = new ArrayList<>();     // initialize list of positions lists.
        this.paceAccumulator = 0;               // Initialize pace accumulator;
        this.updateCounter = 0;                 // Initialize counter of data updates.
        this.state = Constant.STATE_STOPPED;    // Initial state is STATE_STOPPED.
        this.sportActivity = Constant.RUNNING;  // Initialize sportActivity indicator.

        // ## INTENT FILTER INITIALIZATION AND RECEIVER REGISTRATION ##
        this.filter = new IntentFilter();                  // Initialize intent filter.
        this.filter.addAction(Constant.TICK);              // Register action.
        getActivity().registerReceiver(receiver, filter);  // Register receiver.
        this.receiverRegistered = true;                    // Set flag that indicates receiver is registered.
        // ## /INTENT FILTER INITIALIZATION AND RECEIVER REGISTRATION ##

        // Create a new service connection.
        sConn = new ServiceConnection() {

            // callback that is called when the service is connected
            public void onServiceConnected(ComponentName name, IBinder binder) {
                service = ((TrackerService.LocalBinder) binder).getService();  // Call getService of passed binder.
                bound = true;  // Set bound indicator to true.
            }

            // callback that is called when the service is disconnected
            public void onServiceDisconnected(ComponentName name) {
                bound = false; // Set bound indicator to false.
                getActivity().unregisterReceiver(receiver);  // Unregister receiver.
            }
        };

        // Bind service to activity.
        getActivity().bindService(new Intent(getContext(), TrackerService.class), sConn, Context.BIND_AUTO_CREATE);
        this.bound = true;  // Set bound indicator to true.
    }

    // onCreateView: method called when view is to be created.
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        setHasOptionsMenu(true);  // Fragment has an options menu.
        return inflater.inflate(R.layout.fragment_stopwatch, parent, false);
    }


    // updateUnitsUI: update units displayed in user interface.
    private void updateUnitsUI(int distUnits) {

        // Get TextView instances containing the unit abbreviations.
        TextView distUnitsTextView = getActivity().findViewById(R.id.textview_stopwatch_distanceunit);
        TextView paceUnitsTextView = getActivity().findViewById(R.id.textview_stopwatch_unitpace);

        // Set text on TextView instances
        if (distUnits == Constant.UNITS_KM) {
            distUnitsTextView.setText(Constant.UNITS_KM_ABBR);
            paceUnitsTextView.setText(Constant.UNITS_MINPKM_ABBR);
        } else {
            distUnitsTextView.setText(Constant.UNITS_MI_ABBR);
            paceUnitsTextView.setText(Constant.UNITS_MINPMI_ABBR);
        }
    }


    // confirmSportActivitySelection: prompt user to confirm sport activity selection and apply selection if user confirmed.
    private void confirmSportActivitySelection(final int sportActivityCode) {
        new AlertDialog.Builder(getContext())
                .setTitle("Change Sport Activity")
                .setMessage(String.format("Are you sure you want to change the sport activity to %s?", MainHelper.getSportActivityName(sportActivityCode)))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setSportActivity(sportActivityCode);  // Apply selection.
                    }
                })
                .setNegativeButton(R.string.cancel, null)  // Do nothing if user selects cancel.
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // oncCreate: method called when view is created.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Initialize Button instances.
        this.stopwatchStartButton = view.findViewById(R.id.button_stopwatch_start);
        this.endWorkoutButton = view.findViewById(R.id.button_stopwatch_endworkout);
        this.showMapButton = view.findViewById(R.id.button_stopwatch_activeworkout);
        this.sportActivityButton = view.findViewById(R.id.button_stopwatch_selectsport);

        // Set last paused workout to null.
        lastUnfinishedWorkout = null;

        switch (this.state) {
            case Constant.STATE_STOPPED:
                // Set listener on button to listen for workout start.
                this.stopwatchStartButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startStopwatch();
                    }
                });

                // Set UI state.
                this.updateStartButtonText(Constant.STATE_STOPPED);
                this.updateDuration(this.duration);
                this.updateCalories(this.calories);
                this.updateDistance(this.distance, this.distUnits);

                try {

                    // Get dao for workouts.
                    Dao<Workout, Long> workoutDao = new DatabaseHelper(getContext()).workoutDao();
                    Log.d("HEREIAM", Long.toString(workoutDao.countOf()));
                    //DeleteBuilder<Workout, Long> deleteBuilder = workoutDao.deleteBuilder();
                    //deleteBuilder.where().eq("status", 2);
                    //deleteBuilder.delete();

                    // Make a prepared query to get last unfinished workout from database.
                    PreparedQuery<Workout> query = workoutDao.queryBuilder()
                            .limit(1l)
                            .orderBy("lastUpdate", false)
                            .where()
                            .eq("status", Constant.STATE_RUNNING)
                            .or()
                            .eq("status", Constant.STATE_CONTINUE)
                            .or()
                            .eq("status", Constant.STATE_PAUSED)
                            .prepare();
                    List<Workout> lastUnfinishedWorkoutList = workoutDao.query(query);  // Get last paused workout from database.
                    if (lastUnfinishedWorkoutList.size() > 0) {  // if found
                        Log.d(TAG, "Unfinished workout retreived from database.");

                        // Get last workout.
                        lastUnfinishedWorkout = lastUnfinishedWorkoutList.get(0);

                        // Mock paused state.

                        // Update TextView fields in UI depending on found values of last paused workout.
                        updateDuration(Math.round(1.0e-3 * (double) lastUnfinishedWorkout.getDuration()));
                        updateDistance(lastUnfinishedWorkout.getDistance(), distUnits);
                        updatePace(lastUnfinishedWorkout.getPaceAvg(), distUnits);
                        updateCalories(lastUnfinishedWorkout.getTotalCalories());

                        // Set text on start button.
                        this.updateStartButtonText(Constant.STATE_PAUSED);

                        // Make button for showing map invisible and button for ending workout visible.
                        this.showMapButton.setVisibility(View.INVISIBLE);
                        this.endWorkoutButton.setVisibility(View.VISIBLE);

                    } else {
                        Log.d(TAG, "No paused workouts found");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case Constant.STATE_RUNNING:
                this.stopwatchStartButton.setOnClickListener(pauseListener);

                // Set UI state.
                this.updateStartButtonText(Constant.STATE_RUNNING);
                this.updateDuration(this.duration);
                this.updateCalories(this.calories);
                this.updateDistance(this.distance, this.distUnits);
                break;
            case Constant.STATE_CONTINUE:

                // Set UI state.
                this.updateStartButtonText(Constant.STATE_RUNNING);
                this.updateDuration(this.duration);
                this.updateCalories(this.calories);
                this.updateDistance(this.distance, this.distUnits);
                break;
            case Constant.STATE_PAUSED:
                this.stopwatchStartButton.setOnClickListener(continueListener);

                // Set UI state.
                this.updateStartButtonText(Constant.STATE_PAUSED);
                this.updateDuration(this.duration);
                this.updateCalories(this.calories);
                this.updateDistance(this.distance, this.distUnits);
                break;
        }


        // If fragment loaded for first time or if units changed, set unit abbreviations on UI.
        if (firstRun || preferences.getBoolean("unitsChanged", true)) {
            updateUnitsUI(distUnits);
            preferences.edit().putBoolean("unitsChanged", false).apply();
            this.firstRun = false;
        }

        // Set listener on button that is used to change the current sport activity.
        this.sportActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Select Activity")
                        .setItems(Constant.activities, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        confirmSportActivitySelection(0);
                                        break;
                                    case 1:
                                        confirmSportActivitySelection(1);
                                        break;
                                    case 2:
                                        confirmSportActivitySelection(2);
                                        break;
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Set listener on button to listen for requests to go to start ActiveWorkoutMapActivity.
        this.showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Start ActiveWorkoutMapActivity.
                Intent activeMapIntent = new Intent(getContext(), ActiveWorkoutMapActivity.class);
                StopwatchFragment.this.startActivity(activeMapIntent);
            }
        });

        // set listener on button to listen for workout end.
        this.endWorkoutButton.setOnClickListener(endListener);


        // Load last non-ended workout from database if it exists.
        // initially null
    }

    // onPause: method called when the activity is paused.
    @Override
    public void onPause() {
        super.onPause();

        // If workout is not running, unbind and stop service.
        if (this.state == Constant.STATE_STOPPED || this.state == Constant.STATE_PAUSED) {
            if (this.bound) {
                getActivity().unbindService(sConn);
                this.bound = false;
                getActivity().stopService(new Intent(getContext(), TrackerService.class));
            }

            // If receiver registered, unregister.
            if (this.receiverRegistered) {
                getActivity().unregisterReceiver(this.receiver);
                this.receiverRegistered = false;
            }
        }
    }

    // onResume: method called when the activity is resumed.
    @Override
    public void onResume() {
        super.onResume();

        // If service not bound, bind it.
        if (!this.bound) {
            getActivity().bindService(new Intent(getContext(), TrackerService.class), sConn, Context.BIND_AUTO_CREATE);
            this.bound = true;
        }

        // If receiver not registered, register.
        if (!this.receiverRegistered) {
            getActivity().registerReceiver(this.receiver, this.filter);
            this.receiverRegistered = true;
        }
    }

    // onDestroy: method called when the activity is destroyed.
    @Override
    public void onDestroy() {
        super.onDestroy();

        // If service bound, unbind and stop it.
        if (this.bound) {
            getActivity().unbindService(sConn);
            this.bound = false;
            getActivity().stopService(new Intent(getContext(), TrackerService.class));
            getActivity().unregisterReceiver(receiver);
        }

        // If receiver registered, unregister it.
        if (this.receiverRegistered) {
            getActivity().unregisterReceiver(this.receiver);
            this.receiverRegistered = false;
        }

    }

    // ### METHODS FOR CONTROLLING THE WORKOUT STATE ###

    // startStopwatch: method used to start the workout
    public void startStopwatch() {

        // start TrackerService with action si.uni_lj.fri.pbd2019.runsup.COMMAND_START
        Intent startIntent = new Intent(getContext(), TrackerService.class);
        startIntent.setAction(Constant.COMMAND_START);
        startIntent.putExtra("sportActivity", this.sportActivity);
        if (this.lastUnfinishedWorkout != null) {
            startIntent.putExtra("unfinishedWorkout", this.lastUnfinishedWorkout);
        }
        getActivity().startService(startIntent);
        this.updateStartButtonText(Constant.STATE_RUNNING);

        // set listener to button with id button_stopwatch_start - listen for pause.
        this.stopwatchStartButton.setOnClickListener(pauseListener);

        this.state = Constant.STATE_RUNNING;  // Update state.
    }

    // endWorkout: method used to end current workout.
    public void endWorkout() {

        // Prompt user to confirm decision to end workout.
        new AlertDialog.Builder(getContext())
                .setTitle("Stop Workout")
                .setMessage("Are you sure you want to end this workout?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // Initialize intent for startin the dharmag the service.
                        Intent startIntent = new Intent(getContext(), TrackerService.class);
                        startIntent.setAction(Constant.COMMAND_STOP);  // Set action.
                        getContext().startService(startIntent);
                        if (bound) {  // If service still bounded, unbind.
                            getContext().unbindService(sConn);
                            bound = false;  // Update bound indicator.
                            getContext().stopService(new Intent(getContext(), TrackerService.class));
                        }

                        state = Constant.STATE_STOPPED;  // Update state.

                        // Initialize intent to start new activity and put additional data in extras.
                        Intent workoutDetailsIntent = new Intent(getContext(), WorkoutDetailActivity.class);
                        workoutDetailsIntent.putExtra("sportActivity", sportActivity); //Optional parameters
                        workoutDetailsIntent.putExtra("duration", duration);
                        workoutDetailsIntent.putExtra("distance", distance);
                        workoutDetailsIntent.putExtra("pace", paceAccumulator/updateCounter);
                        workoutDetailsIntent.putExtra("calories", calories);
                        workoutDetailsIntent.putExtra("positions", positions);
                        workoutDetailsIntent.putExtra("workoutId", 129123);  // TODO
                        StopwatchFragment.this.startActivity(workoutDetailsIntent);
                    }
                })
                .setNegativeButton(R.string.no, null)  // Do nothing if user selects cancel.
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // pauseStopWatch: method used to pause the stopwatch.
    public void pauseStopwatch() {

        // Initialize the intent for starting the service.
        Intent startIntent = new Intent(getContext(), TrackerService.class);
        startIntent.setAction(Constant.COMMAND_PAUSE);
        getActivity().startService(startIntent);

        // Set text on start button.
        this.updateStartButtonText(Constant.STATE_PAUSED);

        // set listener to button to listen for commands to continue workout.
        this.stopwatchStartButton.setOnClickListener(continueListener);

        // Make button for showing map invisible and button for ending workout visible.
        this.showMapButton.setVisibility(View.INVISIBLE);
        this.endWorkoutButton.setVisibility(View.VISIBLE);

        this.state = Constant.STATE_PAUSED;  // Update state.

    }

    // continueStopwatch: method used to resume the workout paused.
    public void continueStopwatch() {

        // Initialize the intent for starting the service.
        Intent startIntent = new Intent(getContext(), TrackerService.class);
        startIntent.setAction(Constant.COMMAND_CONTINUE);

        // Send list of positions to reconstruct service's list of speeds.
        if (this.positions.size() >= 1) {
            startIntent.putParcelableArrayListExtra("positions", this.positions);
        } else {
            startIntent.putParcelableArrayListExtra("positions", new ArrayList<Location>());
        }
        getActivity().startService(startIntent);  // Start service with intent.

        // Set text on start button.
        this.updateStartButtonText(Constant.STATE_RUNNING);

        // set listener to button to listen for commands to pause the workout.
        this.stopwatchStartButton.setOnClickListener(pauseListener);

        // Make button for ending workout invisible and button for showing map visible.
        this.endWorkoutButton.setVisibility(View.INVISIBLE);
        this.showMapButton.setVisibility(View.VISIBLE);
        this.state = Constant.STATE_CONTINUE;  // Update state.
    }

    // ### /METHOD FOR CONTROLLING THE WORKOUT STATE ###




    // ### METHODS FOR UPDATING THE UI ###

    // updateDuration: update the workout duration display.
    private void updateDuration(long duration) {
        this.duration = duration;
        TextView durationText = getActivity().findViewById(R.id.textview_stopwatch_duration);
        if (durationText != null) {
            durationText.setText(MainHelper.formatDuration(duration));
        }
    }

    // updateDistance: update the workout distance display.
    private void updateDistance(double dist, int distUnits) {
       this.distance = dist;
       TextView distanceText = getActivity().findViewById(R.id.textview_stopwatch_distance);
       if (distanceText!= null) {
           if (distUnits == Constant.UNITS_MI) {
               distanceText.setText(MainHelper.formatDistance(MainHelper.kmToMi(dist)));
           } else {
               distanceText.setText(MainHelper.formatDistance(dist));
           }
       }
    }

    // updatePace: update the workout pace display.
    private void updatePace(double pace, int distUnits) {
        this.paceAccumulator += pace;
        TextView paceText = getActivity().findViewById(R.id.textview_stopwatch_pace);
        if (paceText != null) {
            if (distUnits == Constant.UNITS_MI) {
                paceText.setText(MainHelper.formatPace(MainHelper.minpkmToMinpmi(pace)));
            } else {
                paceText.setText(MainHelper.formatPace(pace));
            }
        }
    }

    // updateCalories: update the workout calories display.
    private void updateCalories(double calories) {
        this.calories = calories;
        TextView caloriesText = getActivity().findViewById(R.id.textview_stopwatch_calories);
        if (caloriesText != null) {
            caloriesText.setText(MainHelper.formatCalories(calories));
        }
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
                this.stopwatchStartButton.setText(R.string.stopwatch_start);
                break;
            case Constant.STATE_CONTINUE:
                this.stopwatchStartButton.setText(R.string.stopwatch_stop);
                break;
        }

    }

    // updateSportActivityText: update text on sport activity button.
    private void updateSportActivityText(int sportActivityCode) {
        this.sportActivityButton.setText(MainHelper.getSportActivityName(sportActivityCode));
    }

    // ### /METHODS FOR UPDATING THE UI ###


    // setSportActivity: handle requests to change the sports activity
    private void setSportActivity(int activityCode) {

        // start TrackerService with action si.uni_lj.fri.pbd2019.runsup.UPDATE_SPORT_ACTIVITY
        Intent startIntent = new Intent(getContext(), TrackerService.class);
        startIntent.setAction(Constant.UPDATE_SPORT_ACTIVITY);
        startIntent.putExtra("sportActivity", activityCode);
        getActivity().startService(startIntent);
        this.sportActivity = activityCode;

        // Update text on button.
        this.updateSportActivityText(activityCode);
    }

    // onCreateOptionsMenu: called when options menu created.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.stopwatch_shared, menu);

        // If user not signed in, hide synchronization option in menu.
        if (!preferences.getBoolean("userSignedIn", false)) {
            MenuItem menuItem = menu.findItem(R.id.stopwatchfragment_menuitem_sync);
            menuItem.setVisible(false);
        }
    }

}
