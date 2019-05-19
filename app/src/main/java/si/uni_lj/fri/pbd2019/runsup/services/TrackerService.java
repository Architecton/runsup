package si.uni_lj.fri.pbd2019.runsup.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import si.uni_lj.fri.pbd2019.runsup.Constant;
import si.uni_lj.fri.pbd2019.runsup.helpers.SportActivities;
import si.uni_lj.fri.pbd2019.runsup.model.GpsPoint;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;

public class TrackerService extends Service {

    // ### PROPERTIES ###

    public static final String TAG = TrackerService.class.getName();  // Class' tag

    private final long BROADCAST_PERIOD = 1000;  // The broadcast period in milliseconds.
    private final long MIN_TIME_BETWEEN_UPDATES = 3000;  // Minimum delta time between updates.
    private final long MIN_DISTANCE_CHANGE_BETWEEN_UPDATES = 10;  // Minimum delta distance between updates.
    private final long MIN_DIST_CHANGE = 2;  // Minimum distance between current and next location to store next location.

    public static final String STATE_PREF_NAME = "state";

    // Instance that allows interaction with the location API.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // request for location and callback called when location received.
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    Location mCurrentLocation;
    String mLastUpdateTime;
    volatile boolean mRequestingLocationUpdates;


    private Handler h;  // handler
    private Runnable r;  // runnable
    Handler locationsTimeoutHandler;
    Runnable locationTimeoutRunnable;
    private volatile boolean broadcasting; // volatile boolean specifying whether the service is broadcasting.

    private ArrayList<Location> positionList;  // List for storing locations.
    private ArrayList<Float> speedList;  // List for storing speeds.
    private double distanceAccumulator;  // distanceAccumulator that accumulates the distance
    private long durationAccumulator;  // durationAccumulator that accumulates the duration
    long prevTimeMeas;

    private int sportActivity;  // sport activity indicator
    private int trackingState;  // state of the service

    private volatile boolean firstMeasAfterPause;  // if true indicated that the next measurement will be the first after a pause.

    private double pace;  // Current pace
    private double caloriesAcc; // Current calories

    private final IBinder mBinder = new LocalBinder();  // binder that provides an interface to this service.

    SharedPreferences preferences;

    private DatabaseHelper databaseHelper;

    private Workout currentWorkout;

    private int sessionNumber;

    private User currentUser;
    // ### /PROPERTIES ###



    // onCreate: method called when this service is created.
    @Override
    public void onCreate() {
        super.onCreate();  // Call onCreate method of superclass.

        // Instantiate mFusedLocationProviderClient.
        this.mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        this.durationAccumulator = 0;  // Initialize duration accumulator.
        this.positionList = new ArrayList<>();  // Initialize list of positions.
        this.speedList = new ArrayList<>();  // Initialize list of speeds.

        // Initialize location request and location callback function.
        createLocationRequest();
        createLocationCallback();

        // Initialize shared preferences pointer.
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.databaseHelper = new DatabaseHelper(this);

        this.locationTimeoutRunnable = new Runnable() {
            public void run() {

                // Update workout in database.
                updateWorkout(true);
            }
        };

        this.locationsTimeoutHandler = new android.os.Handler();


        // Initialize handler.
        this.h = new Handler();

        // Instantiate a class extending Runnable.
        this.r = new Runnable() {
            @Override
            public void run() {

                // Initialize contents to send.
                Intent toSend = new Intent();
                toSend.setAction(Constant.TICK);

                // Update duration accumulator and set previous time measurement value.
                durationAccumulator += SystemClock.elapsedRealtime() - prevTimeMeas;
                prevTimeMeas = SystemClock.elapsedRealtime();

                // Add duration and distance to intent broadcast.
                toSend.putExtra("duration", Math.round(1.0e-3 * (double)durationAccumulator));
                toSend.putExtra("distance", distanceAccumulator);

                // Compute pace.
                pace = 0.0;
                // If list of positions is not empty and if last position update less than threshold ago, compute pace from speed.
                if (positionList != null && !positionList.isEmpty() &&
                        SystemClock.elapsedRealtime() - positionList.get(positionList.size()-1)
                                .getElapsedRealtimeNanos()*1.0e-6 < MIN_TIME_BETWEEN_UPDATES*2) {
                    if (!speedList.isEmpty()) {
                       pace = 1.0/speedList.get(speedList.size() - 1)*(1000.0/60.0);
                    }
                }

                // Add pace to intent broadcast.
                toSend.putExtra("pace", pace);
                // preferences.edit().putLong("pace", Double.doubleToRawLongBits(pace));

                // Compute cumulative number of calories used until now. NOTE: weight is hardcoded for now.
                double caloriesNxt = 0.0;
                if (speedList.size() >= 2) {
                    caloriesNxt = SportActivities.countCalories(sportActivity, preferences.getInt("weight", Constant.DEFAULT_WEIGHT), speedList, durationAccumulator*1.0e-3*Math.pow(60.0, -2.0));
                }

                caloriesAcc = caloriesNxt;

                // Add data about calories, service state, current sport activity and positions
                // to intent broadcast.
                toSend.putExtra("calories", caloriesNxt);
                toSend.putExtra("state", trackingState);
                toSend.putExtra("sportActivity", sportActivity);
                toSend.putExtra("position", mCurrentLocation);
                toSend.putExtra("workoutId", currentWorkout.getId());
                sendBroadcast(toSend);

                // If last location same than in previous broadcast, update noLocationCounter.

                // If broadcasting, broadcast.
                if (broadcasting) {
                    h.postDelayed(this, BROADCAST_PERIOD);
                }
            }
        };
    }

    // onDestroy: method called when the service is destroyed.
    @Override
    public void onDestroy() {
        super.onDestroy();  // Call onDestroy method of superclass.
        stopLocationUpdates();  // Disable location updates.
    }



    // ### COMMAND HANDLING ###

    // onStartCommand: callback called every time the startService is called in the StopwatchFragment.
    // This callback is used to get commands from the bound activity.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();  // Get intent action and switch on it.
        assert action != null;
        switch (action) {
            case Constant.COMMAND_START:

                Workout unfinishedWorkout = null;
                if (intent.hasExtra("unfinishedWorkout")) {
                    unfinishedWorkout = (Workout) intent.getSerializableExtra("unfinishedWorkout");
                }

                this.durationAccumulator = (unfinishedWorkout == null) ? 0 : unfinishedWorkout.getDuration();  // Initialize duration accumulator.

                this.trackingState = Constant.STATE_RUNNING;  // Set service state.
                this.firstMeasAfterPause = unfinishedWorkout != null;  // initialize indicator.

                // Initialize sport activity from start command.
                this.sportActivity = intent.getIntExtra("sportActivity", Constant.RUNNING);
                this.prevTimeMeas = SystemClock.elapsedRealtime();  // Set previous measurement time to now.
                this.startLocationUpdates();  // Start location updates.
                this.broadcasting = true;  // Start broadcasting TICK actions.
                this.h.postDelayed(this.r, this.BROADCAST_PERIOD);
                this.sessionNumber = (unfinishedWorkout == null) ? 0 : Integer.parseInt(unfinishedWorkout.getTitle().substring(unfinishedWorkout.getTitle().indexOf(' ')+1));

                // Get current user
                long userId = intent.getLongExtra("userId", -1L);
                if (userId != -1L) {
                    try {
                        List<User> res = databaseHelper
                                .userDao()
                                .queryBuilder()
                                .where()
                                .eq("accId", userId)
                                .query();
                        if (res.size() > 0) {
                            this.currentUser = res.get(0);
                        } else {
                            this.currentUser = new User("anonymous");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.currentUser = new User("anonymous");
                }

                // Initialize current workout
                if (unfinishedWorkout == null) {
                    this.currentWorkout = new Workout(String.format(Locale.getDefault(), Constant.DEFAULT_WORKOUT_TITLE_FORMAT_STRING, sessionNumber), sportActivity);
                    this.currentWorkout.setId(new Date().hashCode());
                    this.currentWorkout.setCreated(new Date());
                    this.currentWorkout.setUser(this.currentUser);
                    updateWorkout(false);
                    try {
                        this.databaseHelper.workoutDao().create(this.currentWorkout);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                   this.currentWorkout = unfinishedWorkout;
                }


                break;
            case Constant.COMMAND_CONTINUE:
                this.trackingState = Constant.STATE_CONTINUE;  // Set service state.
                this.prevTimeMeas = SystemClock.elapsedRealtime();  // Set time measurement to now.
                this.startLocationUpdates();  // Start location updates.
                this.firstMeasAfterPause = true;  // Next measurement will be the first after a pause.

                // RESTORE STATE from preferences.
                this.durationAccumulator = this.preferences.getLong("duration", 0);  // Restore duration from shared preferences.
                this.distanceAccumulator = Double.longBitsToDouble(this.preferences.getLong("distance", 0));  // Restore distance from preferences.
                this.sportActivity = this.preferences.getInt("sportActivity", Constant.RUNNING);  // Restore sport activity from preferences.
                this.positionList = intent.getParcelableArrayListExtra("positions");  // Restore positions list from StopwatchFragment.
                if (this.positionList != null) {  // If position list is not null, reconstruct speedList.
                    this.speedList = this.positionsToSpeedList();  // Reconstruct speedList from positionList.
                } else {
                    this.positionList = new ArrayList<>();
                }


                this.broadcasting = true;  // Start broadcasting.
                this.h.postDelayed(r, BROADCAST_PERIOD);
                this.sessionNumber += 1;

                // Update workout in database.
                updateWorkout(true);
                break;
            case Constant.COMMAND_PAUSE:
                this.trackingState = Constant.STATE_PAUSED;  // Set service state.
                this.stopLocationUpdates();  // Stop location updates and broadcasting.
                this.broadcasting = false;

                // Store state in preferences to restore in case StopwatchFragment paused.
                this.preferences.edit().putLong("duration", this.durationAccumulator).apply();
                this.preferences.edit().putLong("distance", Double.doubleToRawLongBits(this.distanceAccumulator)).apply();
                this.preferences.edit().putInt("sportActivity", this.sportActivity).apply();

                // Update workout in database.
                updateWorkout(true);
                break;
            case Constant.COMMAND_STOP:
                this.trackingState = Constant.STATE_STOPPED;  // Set service state.
                this.stopLocationUpdates();  // Stop location updates and broadcasting.
                this.broadcasting = false;
                this.preferences.edit().clear().apply();  // Clear all stored data about state in preferences.

                // Update workout in database.
                updateWorkout(true);
                break;
            case Constant.UPDATE_SPORT_ACTIVITY:

                // Set sent sport activity.
                this.sportActivity = intent.getIntExtra("sportActivity", Constant.RUNNING);
                this.preferences.edit().putInt("sportActivity", this.sportActivity).apply();

                // Update workout in database.
                updateWorkout(true);
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // updateWorkoutInDb: update current workout in database
    private void updateWorkout(boolean save) {
        if (this.currentWorkout != null) {
            this.currentWorkout.setDistance(this.distanceAccumulator);
            this.currentWorkout.setDuration(this.durationAccumulator);
            this.currentWorkout.setTotalCalories(this.caloriesAcc);
            this.currentWorkout.setPaceAvg(this.pace);
            this.currentWorkout.setLastUpdate(new Date());
            this.currentWorkout.setSportActivity(this.sportActivity);
            this.currentWorkout.setTitle(String.format(Locale.getDefault(), Constant.DEFAULT_WORKOUT_TITLE_FORMAT_STRING, this.sessionNumber));
            this.currentWorkout.setStatus(this.trackingState);
            this.currentWorkout.setUser(this.currentUser);

            // If flag to update in database set...
            if (save) {
                try {
                    this.databaseHelper.workoutDao().update(this.currentWorkout);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // ### /COMMAND HANDLING ###



    // Create nested class that extends Binder and provides method to return service proxy.
    public class LocalBinder extends Binder {
        public TrackerService getService() {
            return TrackerService.this;
        }
    }

    // onBind: method called when this service is bound.
    // @androidx.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    // createLocationRequest: initialize location request with specified constant values.
    protected void createLocationRequest() {
        this.mLocationRequest = new LocationRequest();
        this.mLocationRequest.setInterval(MIN_TIME_BETWEEN_UPDATES);
        this.mLocationRequest.setFastestInterval(MIN_TIME_BETWEEN_UPDATES);
        this.mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_BETWEEN_UPDATES);
        this.mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // createLocationCallback: initialize callback function called when location received.
    private void createLocationCallback() {
        this.mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                locationsTimeoutHandler.removeCallbacks(locationTimeoutRunnable);
                locationsTimeoutHandler.postDelayed(locationTimeoutRunnable, Constant.LOCATION_UPDATES_TIMEOUT_TO_SAVE);

                super.onLocationResult(locationResult);  // Call method of superclass.
                Location nxtLocation = locationResult.getLastLocation();  // Get received location.

                // If distance from previous location point greater than 2 meters, add new
                // position to locations list.
                if (mCurrentLocation != null && mCurrentLocation.distanceTo(nxtLocation) < MIN_DIST_CHANGE) {
                    // ignore
                } else {
                    // If previous location exists, compute speed in m/s.
                    if (mCurrentLocation != null) {
                        // If first measurement after pause and location changed by more than 100 meters, discard.
                        if (firstMeasAfterPause && mCurrentLocation.distanceTo(nxtLocation) > Constant.PAUSE_DIST_CHANGE_THRESH) {
                            firstMeasAfterPause = false;
                        } else {

                            // If first measurement after pause and distance changed by less than
                            // PAUSE_DIST_CHANGE_THRESH, add pause flag to location.
                            if (firstMeasAfterPause) {
                                firstMeasAfterPause = false;
                                Bundle flags = new Bundle();
                                flags.putByte("pauseFlag", (byte)1);
                                nxtLocation.setExtras(flags);
                            }
                            double delta_time = 1.0e-9 * (nxtLocation.getElapsedRealtimeNanos() - mCurrentLocation.getElapsedRealtimeNanos());
                            speedList.add((float) (mCurrentLocation.distanceTo(nxtLocation) / delta_time));  // Compute speed in m/s.
                            distanceAccumulator += mCurrentLocation.distanceTo(nxtLocation);  // Add distance to distance accumulator.
                        }
                    }
                    // Set current location to last retrieved location and set update time.
                    mCurrentLocation = nxtLocation;
                    mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                    if (mCurrentLocation != null) {
                        positionList.add(mCurrentLocation);  // Add location to list of locations.
                    }
                    try {
                        updateWorkout(true);
                        GpsPoint gpsPointNxt = new GpsPoint(currentWorkout, sessionNumber, nxtLocation, durationAccumulator, speedList.size() > 0 ? speedList.get(speedList.size()-1) : 0, pace, caloriesAcc);
                        gpsPointNxt.setCreated(new Date());
                        gpsPointNxt.setLastUpdate(new Date());
                        if (mCurrentLocation.getExtras() != null && mCurrentLocation.getExtras().getByte("pauseFlag", (byte)0) == (byte)1) {
                            gpsPointNxt.setPauseFlag((byte)1);
                        }
                        databaseHelper.gpsPointDao().create(gpsPointNxt);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    // startLocationUpdates: method used to start requesting periodical location updates.
    protected void startLocationUpdates() {
        // Check permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Check if looper initialized and start requesting location updates.
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            this.mFusedLocationProviderClient.requestLocationUpdates(this.mLocationRequest, this.mLocationCallback, Looper.myLooper());
            this.mRequestingLocationUpdates = true;  // update receiving locations indicator.
        } else {
            // IGNORE
        }
    }

    // stopLocationUpdates: method used to stop requesting periodical location updates.
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) { return; }  // If not requesting updates, return.
        this.mFusedLocationProviderClient.removeLocationUpdates(this.mLocationCallback);  // Stop location updates.
        this.mRequestingLocationUpdates = false;  // update receiving locations indicator.
    }


    // positionsToSpeedList: convert list of positions to list of speeds.
    private ArrayList<Float> positionsToSpeedList() {
        ArrayList<Float> constructedSpeedList = new ArrayList<>();  // Initialize resulting speed list.
        for (int i = 1; i < this.positionList.size(); i++) {  // Compute speeds from distances.
            constructedSpeedList.add((float)(this.positionList.get(i).distanceTo(this.positionList.get(i-1))/
                    ((this.positionList.get(i).getElapsedRealtimeNanos() - this.positionList.get(i-1).getElapsedRealtimeNanos())*1.0e-9)));
        }
        return constructedSpeedList;
    }


    // ### getters used for testing ###

    public int getState() {
        return this.trackingState;
    }

    public long getDuration() {
       return this.durationAccumulator;
    }

    public double getDistance() {
       return this.distanceAccumulator;
    }

    public double getPace() {
        return this.pace;
    }

    // ### /getters provider for testing ###
}
