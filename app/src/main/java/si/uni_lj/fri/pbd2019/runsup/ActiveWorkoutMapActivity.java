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
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

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


    // ## BROADCAST RECEIVER ##
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        // TODO do not redraw path every time.

        /* Anonymous BroadcastReceiver instance that receives commands */
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Update from service received.");

            // Save received ArrayList of locations.
            ArrayList<Location> positions = intent.<Location>getParcelableArrayListExtra("positionList");

            // Draw trail.
            for (int i = 1; i < positions.size(); i++) {
                mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(positions.get(i-1).getLatitude(), positions.get(i-1).getLongitude()),
                                new LatLng(positions.get(i).getLatitude(), positions.get(i).getLongitude()))
                        .width(5.0f)
                        .color(Color.RED));
            }

            // Get current location and set marker
            LatLng currentLocation = new LatLng(positions.get(positions.size()-1).getLatitude(), positions.get(positions.size()-1).getLongitude());

            // Add marker at user's current location.
            if (currentMarker != null) {
                currentMarker.remove();
                currentMarker = null;
            }
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Your Current Location"));


            // Move camera to current location of user. TODO zoom only for first received broadcast.
            CameraUpdate center=
                    CameraUpdateFactory.newLatLng(currentLocation);
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15f);

            mMap.moveCamera(center);
            mMap.animateCamera(zoom);

        }
    };
    // ## /BROADCAST RECEIVER ##


    // ### /PROPERTIES ###

    // called when activity created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_workout_map);  // Set content.

        // Get SupportMapFragment instance and get map (onMapReady gets called)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_activeworkoutmap_map);
        mapFragment.getMapAsync(this);

        // ## INTENT FILTER INITIALIZATION ##
        this.filter = new IntentFilter();
        this.filter.addAction(Constant.TICK);  // Register action.
        registerReceiver(receiver, filter);  // Register receiver.
        // ## INTENT FILTER INITIALIZATION ##
    }


    // called when map is ready.
    @Override
    public void onMapReady(GoogleMap map) {

        this.mMap = map;  // initialize map instance.
        Log.d(TAG, "onMapReady called");
        Marker testMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
        LatLng testLatLng = new LatLng(39.0392, 125.7625);
        animateMarker(testMarker, testLatLng, false);
    }


    // animateMarker: animate the transition of a marker from one place to another.
    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();  // Initialize Handler instance.
        final long start = SystemClock.uptimeMillis();  // Save start time.
        Projection proj = mMap.getProjection();  // Get projection to map.
        Point startPoint = proj.toScreenLocation(marker.getPosition());  // Get start point of animation.
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);  // Get start point LatLng instance.
        final long duration = 500;  // Animation duration.
        final Interpolator interpolator = new LinearInterpolator();  // Instantiate linear interpolator to iterpolate line through points.
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
