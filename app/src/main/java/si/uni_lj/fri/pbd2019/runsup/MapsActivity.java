package si.uni_lj.fri.pbd2019.runsup;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // ### PROPERTIES ###

    public static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private ArrayList<Location> positions;

    // ### /PROPERTIES ###


    // onCreate: called when activity created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content of activity.
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_maps_map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();  // Get intent and unpack extras into methods that format and display data on UI.

        // Get list of lists of positions from intent.
        this.positions = (ArrayList<Location>) intent.getSerializableExtra("positions");
    }


    // create_trail: create line that connects all locations.
    private void create_trail(ArrayList<Location> positionsLast) {

        // initialize counter of pauses and breaks (used to enumerate markers)
        int pauseCounter = 1;
        int breakCounter = 1;

        // Go over locations in final list of locations.
        for (int i = 1; i < positionsLast.size(); i++) {

            // If change in position less than 100 meters, add trail segment to current session.
            if (positionsLast.get(i-1).distanceTo(positionsLast.get(i)) < Constant.PAUSE_DIST_CHANGE_THRESH) {
                mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(positionsLast.get(i-1).getLatitude(), positionsLast.get(i-1).getLongitude()),
                                new LatLng(positionsLast.get(i).getLatitude(), positionsLast.get(i).getLongitude()))
                        .width(5.0f)
                        .color(Color.RED));

                // If location has pause flag set to 1, mark pause location.
                if (positionsLast.get(i-1).getExtras() != null &&
                        positionsLast.get(i-1).getExtras().getByte("pauseFlag", (byte)0) == (byte)1) {

                    // Add marker to end of previous session (before pausing).
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(positionsLast.get(i-1).getLatitude(), positionsLast.get(i-1).getLongitude()))
                            .title(String.format("Break %d", breakCounter)));
                    breakCounter += 1;  // Increment breaks counter.
                }
            } else {
                // If change in positions more than 100 meters, mark new session.

                // Add marker to end of previous session (before pausing).
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(positionsLast.get(i-1).getLatitude(), positionsLast.get(i-1).getLongitude()))
                        .title(String.format("Pause %d", pauseCounter)));

                // Add marker to start of new session (after continuing).
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(positionsLast.get(i).getLatitude(), positionsLast.get(i).getLongitude()))
                        .title(String.format("Continue %d", pauseCounter)));

                pauseCounter += 1;  // Increment pause counter.
            }
        }

        if (positionsLast.size() > 0) {
            // Get start and end position of route.
            LatLng endPos = new LatLng(positionsLast.get(positionsLast.size() - 1).getLatitude(),
                    positionsLast.get(positionsLast.size() - 1).getLongitude());

            LatLng startPos = new LatLng(positionsLast.get(0).getLatitude(),
                    positionsLast.get(0).getLongitude());


            // Mark start and finish of workout.
            mMap.addMarker(new MarkerOptions()
                    .position(startPos)
                    .title("Start of Workout")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start)));

            mMap.addMarker(new MarkerOptions()
                    .position(endPos)
                    .title("End of Workout")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.racing_flag)));
        }

    }


    // onMapReady: callback executed when the map is ready.
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Get map instance.
        this.mMap = googleMap;

        // Draw trail created from locations.
        this.create_trail(this.positions);

        if (this.positions.size() > 0) {
            // Zoom enough to see full route.
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Location pos : this.positions) {
                builder.include(new LatLng(pos.getLatitude(), pos.getLongitude()));
            }
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10);
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.animateCamera(cu);
        }
    }

}
