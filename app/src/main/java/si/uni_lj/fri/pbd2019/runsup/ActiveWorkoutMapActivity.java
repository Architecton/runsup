package si.uni_lj.fri.pbd2019.runsup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class ActiveWorkoutMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ### PROPERTIES ###

    public static final String TAG = ActiveWorkoutMapActivity.class.getSimpleName();
    private GoogleMap mMap;
    private IntentFilter filter;
    private Marker currentMarker;
    private LatLng currentLocation;
    private ArrayList<Location> positions;

    // lockPosition: inidicator whether position is locked.
    private boolean lockPosition;


    // interval at which to redraw the trail.
    private final int REDRAW_INTERVAL = 15;


    // ## BROADCAST RECEIVER ##
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        // Count received updates from service.
        int receiveCounter = 0;

        /* Anonymous BroadcastReceiver instance that receives commands */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Increment counter of received broadcasts.
            receiveCounter += 1;

            // Save received ArrayList of locations.
            Location positionNxt = intent.getParcelableExtra("position");
            positions.add(positionNxt);

            // If positions array is not null, draw trail.
            if (positions != null) {
                // Draw trail
                if (receiveCounter % REDRAW_INTERVAL == 0) {
                    for (int i = 1; i < positions.size(); i++) {
                        mMap.addPolyline(new PolylineOptions()
                                .add(
                                        new LatLng(positions.get(i-1).getLatitude(),
                                                positions.get(i-1).getLongitude()),
                                        new LatLng(positions.get(i).getLatitude(),
                                                positions.get(i).getLongitude()))
                                .width(5.0f)
                                .color(Color.RED));
                    }
                }

                // Get current location and set marker
                currentLocation = new LatLng(positionNxt.getLatitude(), positionNxt.getLongitude());

                // Add marker at user's current location (remove previous one if it exists).
                if (currentMarker != null) {
                    animateMarker(currentMarker, currentLocation, false);
                } else {
                    currentMarker = mMap.addMarker(new MarkerOptions()
                            .position(currentLocation)
                            .title("Your Current Location"));
                }

                // If user locked position
                if (lockPosition) {

                    // Move camera to current location of user.
                    CameraUpdate center = CameraUpdateFactory.newLatLng(currentLocation);
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(15f);

                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);

                }
            }

        }
    };

    private boolean receiverRegistered;

    // ## /BROADCAST RECEIVER ##


    // ### /PROPERTIES ###

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_workout_map);  // Set content.

        // Get SupportMapFragment instance and get map (onMapReady gets called).
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_activeworkoutmap_map);
        mapFragment.getMapAsync(this);

        this.positions = new ArrayList<>();

        // ## INTENT FILTER INITIALIZATION ##
        this.filter = new IntentFilter();
        this.filter.addAction(Constant.TICK);  // Register action.
        registerReceiver(this.receiver, this.filter);  // Register receiver.
        this.receiverRegistered = true;
        // ## INTENT FILTER INITIALIZATION ##

        // Lock camera position onto current location marker.
        this.lockPosition = true;

        // Display toast with short instructions on position lock.
        Toast toast = Toast.makeText(this, getString(R.string.workoutmap_toast_instructions), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set on-click listener to back button.
        Button backButton = findViewById(R.id.button_activeworkoutmap_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    // onBackPressed: called when the back button is pressed.
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // If broadcast receiver registered, unregister it.
        if (this.receiverRegistered) {
            unregisterReceiver(this.receiver);
            this.receiverRegistered = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // If broadcast receiver registered, unregister it.
        if (this.receiverRegistered) {
            unregisterReceiver(this.receiver);
            this.receiverRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If broadcast receiver not registered, register it.
        if (!this.receiverRegistered) {
            registerReceiver(this.receiver, this.filter);
            this.receiverRegistered = true;
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        // If broadcast receiver not registered, register it.
        if (!this.receiverRegistered) {
            registerReceiver(this.receiver, this.filter);
            this.receiverRegistered = true;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Initialize map instance.
        this.mMap = map;

        // set on-click listener - when user clicks on map, toggle location lock.
        this.mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lockPosition = !lockPosition;
            }
        });
    }


    // animateMarker: animate the transition of a marker from one place to another.
    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();  // Initialize Handler instance.
        final long start = SystemClock.uptimeMillis();  // Save start time.
        Projection proj = mMap.getProjection();  // Get projection to map.
        Point startPoint = proj.toScreenLocation(marker.getPosition());  // Get start point of animation.
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);  // Get start point LatLng instance.
        final long duration = 500;  // Animation duration.
        final Interpolator interpolator = new LinearInterpolator();  // Instantiate linear interpolator to interpolate line through points.
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;  // Get elapsed time.
                float t = interpolator.getInterpolation((float) elapsed  // Get interpolation.
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));  // Set position of marker.
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
}
