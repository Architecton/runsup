package si.uni_lj.fri.pbd2019.runsup;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Chronometer;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Date;

public class TrackerService extends Service {

    // Service must use Google Play Location Services and FusedLocationProviderClient for
    // location updates.

    // Min. time between updates should be 3 seconds.
    // Min. distance change between updates should be 10 meters.

    /*
    Commands accepted by the service:

    si.uni_lj.fri.pbd2019.runsup.COMMAND_START - starts a new workout activity.
    si.uni_lj.fri.pbd2019.runsup.COMMAND_CONTINUE - continue paused workout.
    si.uni_lj.fri.pbd2019.runsup.COMMAND_PAUSE - pause running workout.
    si.uni_lj.fri.pbd2019.runsup.COMMAND_STOP - absolutely stop (end) workout.
    */

    /*
    To save the battery of the device, location updates must be disabled, when the service is destroyed (not running).
    */


    // ## PROPERTIES ##

    public static final String TAG = TrackerService.class.getName();

    private final long BROADCAST_PERIOD = 1000;  // The broadcast period in milliseconds.
    private final long MIN_TIME_BETWEEN_UPDATES = 3000;  // Minimum delta time between updates.
    private final long MIN_DISTANCE_CHANGE_BETWEEN_UPDATES = 10;  // Minimum delta distance between updates.
    private final long MIN_DIST_CHANGE = 2;  // Minimum distance between current and next location to store next location.

    private FusedLocationProviderClient mFusedLocationProviderClient;  // Instance that allows interaction with the API.
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    Location mCurrentLocation;
    String mLastUpdateTime;
    boolean mRequestingLocationUpdates;


    private Handler h;  // handler
    private Runnable r;  // runnable
    private volatile boolean broadcasting; // volatile boolean specifying whether the service is broadcasting.

    private ArrayList<Location> positionList;  // List for storing locations.
    private ArrayList<Float> speedList;  // List for storing speeds.
    private long distanceAccumulator;
    private long durationAccumulator;
    long prevTimeMeas;

    private int sportActivity;
    private int trackingState;

    private boolean firstMeasAfterPause;

    private final IBinder mBinder = new LocalBinder();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        /* Anonymous BroadcastReceiver instance that receives commands */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();  // Get broadcasted action.
            switch (action) {
                case Constant.COMMAND_START:
                    Log.d(TAG, "COMMAND_START received");

                    durationAccumulator = 0;  // Initialize duration accumulator.

                    sportActivity = Constant.RUNNING;
                    trackingState = Constant.STATE_RUNNING;
                    firstMeasAfterPause = false;

                    prevTimeMeas = SystemClock.elapsedRealtime();
                    startLocationUpdates();

                    // it starts to send a broadcast intent every second with action si.uni_lj.fri.pbd2019.runsup.TICK including following parameters:
                    broadcasting = true;
                    h.postDelayed(r, BROADCAST_PERIOD);
                    break;
                case Constant.COMMAND_CONTINUE:
                    // it starts to send a broadcast intent every second with action si.uni_lj.fri.pbd2019.runsup.TICK including following parameters:
                    Log.d(TAG, "COMMAND_CONTINUE received");

                    prevTimeMeas = SystemClock.elapsedRealtime();  // Set time measurement to this moment.
                    startLocationUpdates();
                    firstMeasAfterPause = true;
                    broadcasting = true;
                    h.postDelayed(r, BROADCAST_PERIOD);
                    break;
                case Constant.COMMAND_PAUSE:
                    Log.d(TAG, "COMMAND_PAUSE received");
                    stopLocationUpdates();
                    broadcasting = false;
                    break;
                case Constant.COMMAND_STOP:
                    Log.d(TAG, "COMMAND_STOP received");
                    stopLocationUpdates();
                    broadcasting = false;
                    break;
            }
        }
    };

    // ## /PROPERTIES ##



    @Override
    public void onCreate() {
        /* onCreate: method called when this service is created. */

        super.onCreate();

        // Instantiate mFusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        durationAccumulator = 0;  // Initialize duration accumulator.

        IntentFilter filter = new IntentFilter();  // Instantiate IntentFilter.
        filter.addAction("1");
        registerReceiver(receiver, filter);  // Register receiver.

        createLocationCallback();
        createLocationRequest();

        r = new Runnable() {
            @Override
            public void run() {

                // Initialize intent to broadcast.
                Intent toSend = new Intent();
                toSend.setAction("si.uni_lj.fri.pbd2019.runsup.TICK");
                durationAccumulator += prevTimeMeas - SystemClock.elapsedRealtime();
                toSend.putExtra("duration", 1.0e-3 * durationAccumulator);
                toSend.putExtra("distance", distanceAccumulator);

                // Compute pace.
                double pace = 0.0;
                // If list of positions is not empty and if last position update less than threshold ago, compute pace from speed.
                if (!positionList.isEmpty() && SystemClock.elapsedRealtime() - positionList.get(positionList.size()-1).getElapsedRealtimeNanos()*1.0e-6 < MIN_TIME_BETWEEN_UPDATES*2) {
                    if (!speedList.isEmpty()) {
                       pace = speedList.get(speedList.size() - 1)*(1000.0/60.0);
                    }
                }

                toSend.putExtra("pace", pace);

                // Compute cumulative number of calories used until now.
                double caloriesNxt = 0.0;
                if (speedList.size() >= 2) {
                    caloriesNxt = SportActivities.countCalories(sportActivity, 60, speedList, durationAccumulator*1.0e-3*Math.pow(60.0, -2.0));
                }

                toSend.putExtra("calories", caloriesNxt);
                toSend.putExtra("state", trackingState);
                toSend.putExtra("sportActivity", sportActivity);
                toSend.putExtra("positionList", positionList);
                sendBroadcast(toSend);

                if (broadcasting) {
                    Log.d(TAG, "Broadcasting intent...");
                    h.postDelayed(this, BROADCAST_PERIOD);
                }
            }
        };

    }

    @Override
    public void onDestroy() {
        // onDestroy: method called when this service is destroyed.
        super.onDestroy();
        unregisterReceiver(receiver);  // Unregister receiver.
        stopLocationUpdates();  // Disable location updates.
    }

    public class LocalBinder extends Binder {
        public TrackerService getService() {
            return TrackerService.this;
        }
    }

    protected void createLocationRequest() {
        // Instantiate and initialize new LocationRequest.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MIN_TIME_BETWEEN_UPDATES);
        mLocationRequest.setFastestInterval(MIN_TIME_BETWEEN_UPDATES);
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_BETWEEN_UPDATES);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location nxtLocation = locationResult.getLastLocation();

                // If distance from previous location point greater than 2 meters, add new
                // position to locations list.
                if (mCurrentLocation != null && mCurrentLocation.distanceTo(nxtLocation) < MIN_DIST_CHANGE) {
                    return;
                } else {
                    // If previous location exists, compute speed in m/s.
                    if (mCurrentLocation != null) {
                        if (firstMeasAfterPause && mCurrentLocation.distanceTo(nxtLocation) > 100) {
                            firstMeasAfterPause = false;
                        } else {
                            firstMeasAfterPause = false;
                            double delta_time = 1.0e-9 * (nxtLocation.getElapsedRealtimeNanos() - mCurrentLocation.getElapsedRealtimeNanos());
                            speedList.add((float) (mCurrentLocation.distanceTo(nxtLocation) / delta_time));  // Compute speed in m/s.
                            distanceAccumulator += mCurrentLocation.distanceTo(nxtLocation);  // Add distance to distance accumulator.
                        }
                    }

                    // Set current location to last retrieved location and set update time.
                    mCurrentLocation = nxtLocation;
                    mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                    positionList.add(mCurrentLocation);  // Add location to list of locations.
                }
            }
        };
    }

    protected void startLocationUpdates() {
        /* startLocationUpdates: start receiving location updates. */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }


    private void stopLocationUpdates() {
        // stopLocationUpdates: stop receiving location updates.
        if (!mRequestingLocationUpdates) { return; }  // If not requesting updates, return.
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);  // Stop location updates.
    }


    // ### getters and binder provider for testing ###

    public int getState() {
        return -1;
    }

    public long getDuration() {
       return -1;
    }

    public double getDistance() {
       return -1.0;
    }

    public double getPace() {
        return -1.0;
    }

    // @androidx.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    // ### /getters and binder provider for testing ###

}
