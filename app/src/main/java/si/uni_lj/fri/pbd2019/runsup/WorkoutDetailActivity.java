package si.uni_lj.fri.pbd2019.runsup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.Date;

import si.uni_lj.fri.pbd2019.runsup.fragments.StopwatchFragment;
import si.uni_lj.fri.pbd2019.runsup.helpers.MainHelper;



public class WorkoutDetailActivity extends AppCompatActivity implements OnMapReadyCallback{

    // ### PROPERTIES ###

    // Tag of class.
    public static final String TAG = WorkoutDetailActivity.class.getSimpleName();

    // resource used by the activity
    public static Resources resources;

    // UI components
    private EditText shareText;
    private Button confirmShareButton;
    private Button facebookShareButton;
    private Button emailShareButton;
    private Button googlePlusShareButton;
    private Button twitterShareButton;
    private Button displayWorkoutMapButton;
    private GoogleMap mMap;

    // workout parameters
    private int sportActivity;
    private long duration;
    private double calories;
    private double distance;
    private double avgPace;

    private ArrayList<ArrayList<Location>> positions;

    // Date when the activity ended
    private String dateEnd;

    // ### /PROPERTIES ###



    // onCreate: method called when activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Call onCreate method of superclass.
        this.dateEnd = DateFormat.getTimeInstance().format(new Date());  // Set date when activity ended.
        setContentView(R.layout.activity_workout_detail);  // Set content of activity.
        resources = getResources();  // Initialize resources.
        Intent intent = getIntent();  // Get intent and unpack extras into methods that format and display data on UI.
        this.setDuration(intent.getLongExtra("duration", 0));
        this.setSportActivity(intent.getIntExtra("sportActivity", -1));
        this.setCalories(intent.getDoubleExtra("calories", 0.0));
        this.setDistance(intent.getDoubleExtra("distance", 0.0));
        this.setPace(intent.getDoubleExtra("pace", 0.0));
        this.positions = (ArrayList<ArrayList<Location>>) intent.getSerializableExtra("finalPositionsList");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_workoutdetail_map);
        mapFragment.getMapAsync(this);
    }

    // onStart: method called when the activity UI becomes visible to the user.
    @Override
    protected void onStart() {
        super.onStart();  // Call onStart method of superclass.

        // Initialize UI elements.
        this.shareText = findViewById(R.id.share_message);
        this.facebookShareButton = findViewById(R.id.button_workoutdetail_fbsharebtn);
        this.emailShareButton = findViewById(R.id.button_workoutdetail_emailshare);
        this.googlePlusShareButton = findViewById(R.id.button_workoutdetail_gplusshare);
        this.twitterShareButton = findViewById(R.id.button_workoutdetail_twittershare);
        this.confirmShareButton = findViewById(R.id.confirm_share_button);

        // TODO
        // this.displayWorkoutMapButton = findViewById(R.id.button_workoutdetail_showmap);
        /*
        displayWorkoutMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MapsActivity and pass locations to it.
                Intent workoutMapIntent = new Intent(WorkoutDetailActivity.this, MapsActivity.class);
                workoutMapIntent.putExtra("finalPositionsList", positions);
                WorkoutDetailActivity.this.startActivity(workoutMapIntent);
            }
        });
        */

        // Set moment as end of workout.
        this.setActivityDate(this.dateEnd);  // Set date of end of activity.
    }

    // onPause: method called when this activity is paused.
    @Override
    public void onPause() {
        super.onPause();
    }


    // displayShareText: display EditText field for user to input text to share.
    public void displayShareText(View view) {
        this.shareText.setVisibility(View.VISIBLE);
        this.confirmShareButton.setVisibility(View.VISIBLE);
        this.shareText.setText(String.format(getString(R.string.workout_share_description),
                MainHelper.getSportActivityName(this.sportActivity),
                MainHelper.formatDistance(this.distance), getString(R.string.distance_unit),
                MainHelper.formatDuration(this.duration)));
    }

    // ### METHODS FOR FORMATTING THE UI ###

    // setSportActivity: format and display text on UI that indicates the sport activity
    // also set property value.
    public void setSportActivity(int sportActivity) {
        TextView sportActivityText = findViewById(R.id.textview_workoutdetail_sportactivity);
        sportActivityText.setText(MainHelper.getSportActivityName(sportActivity));
        this.sportActivity = sportActivity;
    }

    // setDuration: format and display text on UI that indicates the duration of the workout
    // also set property value.
    public void setDuration(long duration) {
        TextView durationText = findViewById(R.id.textview_workoutdetail_valueduration);
        durationText.setText(MainHelper.formatDuration(duration));
        this.duration = duration;
    }

    // setCalories: format and display text on UI that indicates the calories burnt during the workout.
    // also set property value.
    public void setCalories(double calories) {
        TextView caloriesText = findViewById(R.id.textview_workoutdetail_valuecalories);
        caloriesText.setText(MainHelper.formatCaloriesWithUnits(calories));
        this.calories = calories;
    }

    // setDistance: format and display text on UI that indicates the distance of the workout.
    // also set property value.
    public void setDistance(double distance) {
        TextView distanceText = findViewById(R.id.textview_workoutdetail_valuedistance);
        distanceText.setText(MainHelper.formatDistanceWithUnits(distance));
        this.distance = distance;
    }

    // setPace: format and display text on UI that indicates the average pace of the workout.
    // also set property value.
    public void setPace(double avgPace) {
        TextView paceText = findViewById(R.id.textview_workoutdetail_valueavgpace);
        paceText.setText(MainHelper.formatPaceWithUnits(avgPace));
        this.avgPace = avgPace;
    }

    // setActivityDate: format and display text on UI that indicates the date of the end of the workout.
    public void setActivityDate(String endDate) {
        TextView activityDate = findViewById(R.id.textview_workoutdetail_activitydate);
        activityDate.setText(endDate);
    }

    // ### /METHODS FOR FORMATTING THE UI ###

    // called when map is ready.
    @Override
    public void onMapReady(GoogleMap map) {

        // Initialize map instance.
        this.mMap = map;
        this.mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // Start MapsActivity and pass locations to it.
                Intent workoutMapIntent = new Intent(WorkoutDetailActivity.this, MapsActivity.class);
                workoutMapIntent.putExtra("finalPositionsList", positions);
                WorkoutDetailActivity.this.startActivity(workoutMapIntent);
            }
        });

        // Get last list of locations.
        ArrayList<Location> positionsLast = positions.get(positions.size()-1);

        // Get starting and end positions.
        Location startPos = positionsLast.get(positionsLast.size()-1);
        Location endPos = positionsLast.get(0);

        // Mark starting and end position.
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(startPos.getLatitude(), startPos.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_small)));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(endPos.getLatitude(), endPos.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.racing_flag_small)));


        // Create route trail.
        for (int i = 1; i < positionsLast.size(); i++) {
            mMap.addPolyline(new PolylineOptions()
                    .add(
                            new LatLng(positionsLast.get(i-1).getLatitude(),
                                    positionsLast.get(i-1).getLongitude()),
                            new LatLng(positionsLast.get(i).getLatitude(),
                                    positionsLast.get(i).getLongitude()))
                    .width(5.0f)
                    .color(Color.RED));
        }

        // Zoom enough to see start and end of route.
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(startPos.getLatitude(), startPos.getLongitude()));
        builder.include(new LatLng(endPos.getLatitude(), endPos.getLongitude()));
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels / 5;
        int padding = (int) (width * 0.1);
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);
    }


    // onKeyDown: override default action when user presses the back button
    // Present stopwatch in initial state.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent stopwatchActivityIntent = new Intent(WorkoutDetailActivity.this, StopwatchFragment.class);
            WorkoutDetailActivity.this.startActivity(stopwatchActivityIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // onCreateOptionsMenu: called when options menu is created
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.workoutdetail, menu);
        return true;
    }


    // onOptionsItemSelected: Handle action bar item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Get id of selected item.
        int id = item.getItemId();

        // Handle selection.
        if (id == R.id.workoutdetail_menuitem_settings) {
            // Start settings activity

            // TODO
            // Intent settingsIntent = new Intent(WorkoutDetailActivity.this, SettingsActivity.class);
            // WorkoutDetailActivity.this.startActivity(settingsIntent);
        } else if (id == R.id.workoutdetail_menuitem_delete) {

            // Prompt user to confirm intention to delete activity.
            new AlertDialog.Builder(this)
                    .setTitle("Delete Workout")
                    .setMessage("Are you sure you want to delete this workout? You cannot undo this action.")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO delete workout and destroy activity.
                        }
                    })
                    .setNegativeButton(R.string.no, null)  // Do nothing if user selects cancel.
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}

