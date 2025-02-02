package si.uni_lj.fri.pbd2019.runsup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import si.uni_lj.fri.pbd2019.runsup.helpers.MainHelper;
import si.uni_lj.fri.pbd2019.runsup.model.Friend;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;
import si.uni_lj.fri.pbd2019.runsup.settings.SettingsActivity;
import si.uni_lj.fri.pbd2019.runsup.stats.DataForStatsRetriever;
import si.uni_lj.fri.pbd2019.runsup.sync.FriendsSearchHelper;


public class WorkoutDetailActivity extends AppCompatActivity implements OnMapReadyCallback{

    // ### PROPERTIES ###

    // Tag of class.
    public static final String TAG = WorkoutDetailActivity.class.getSimpleName();

    // ID of this workout.
    private long workoutId;

    // resource used by the activity
    public static Resources resources;

    private String userFullName;
    private long userId;
    private String userProfileImageUrl;

    // UI components
    private Button showParamsButton;
    private Button shareWorkoutButton;
    private TextView workoutTitleTextView;
    private GoogleMap mMap;

    // Workout data retriever.
    DataForStatsRetriever dsr;

    // workout parameters
    private int sportActivity;
    private long duration;
    private double calories;
    private double distance;
    private double avgPace;
    private String workoutTitle;

    private ArrayList<Location> positions;

    // Date when the activity ended
    private String dateEnd;

    private SharedPreferences sharedPreferences;

    private boolean convertUnits;

    private boolean titleSet;

    private DatabaseHelper dh;

    // ### /PROPERTIES ###



    // onCreate: method called when activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Call onCreate method of superclass.
        setContentView(R.layout.activity_workout_detail);  // Set content of activity.
        resources = getResources();  // Initialize resources.
        Intent intent = getIntent();  // Get intent and unpack extras into methods that format and display data on UI.

        // Get shared preferences.
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get indicator whether to convert units to miles.
        this.convertUnits = this.sharedPreferences.getInt("unit", Constant.UNITS_KM) == Constant.UNITS_KM;

        // Initialize database helper instance.
        this.dh = new DatabaseHelper(this);

        // Initialize values from intent extras.
        this.setDuration(intent.getLongExtra("duration", 0));
        this.setSportActivity(intent.getIntExtra("sportActivity", -1));
        this.setCalories(intent.getDoubleExtra("calories", 0.0));
        this.setDistance(intent.getDoubleExtra("distance", 0.0), convertUnits);
        this.setPace(intent.getDoubleExtra("pace", 0.0), convertUnits);
        this.workoutId = intent.getLongExtra("workoutId", -1L);
        this.userFullName = intent.getStringExtra("userFullName");
        this.userId = intent.getLongExtra("userId", -1L);
        this.userProfileImageUrl = intent.getStringExtra("userProfileImageUrl");


        // Initialize DataForStatsRetriever instance.
        try {
            this.dsr = new DataForStatsRetriever(this.workoutId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.positions = (ArrayList<Location>)intent.getSerializableExtra("positions");
        this.workoutTitle = getString(R.string.workoutdetail_workoutname_default);  // Set default workout name.
        this.titleSet = intent.hasExtra("titleSet");


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_workoutdetail_map);
        mapFragment.getMapAsync(this);

        // If intent has an indicator that activity started from history, instantiate current workout from database.
        if (intent.hasExtra("fromHistory")) {
            Workout workoutThis = null;
            try {
                workoutThis = dh
                        .workoutDao()
                        .queryBuilder()
                        .where()
                        .eq("id", this.workoutId)
                        .query()
                        .get(0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.dateEnd = DateFormat.getTimeInstance().format((workoutThis != null)
                    ? workoutThis.getLastUpdate()
                    : new Date());
        } else {
            this.dateEnd = DateFormat.getTimeInstance().format(new Date());  // Set date when activity ended.
        }
    }

    public interface GetShareWorkoutRequestResponse {
       void response(boolean result);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize UI elements.
        this.workoutTitleTextView = findViewById(R.id.textview_workoutdetail_workouttitle);
        this.shareWorkoutButton = findViewById(R.id.button_workoutdetail_send_to_friend);
        this.showParamsButton = findViewById(R.id.button_workoutdetail_show_params);


        if (this.userId == 0) {
            this.shareWorkoutButton.setVisibility(View.INVISIBLE);
        } else {

            // Set click listener for share button.
            this.shareWorkoutButton.setOnClickListener(new View.OnClickListener() {
                FriendsSearchHelper fsh = new FriendsSearchHelper(Constant.BASE_CLOUD_URL);
                ListView listViewFriends;
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(WorkoutDetailActivity.this);
                    dialog.setContentView(R.layout.friends_share_dialog);
                    dialog.setTitle("Select Friend to Share Your Workout With");
                    if (sharedPreferences.contains("jwt")) {
                        fsh.fetchFriends(userId, sharedPreferences.getString("jwt", ""),
                                new FriendsActivity.GetFetchFriendsResponse() {
                                    @Override
                                    public void response(final ArrayList<Friend> res) {
                                        if (res != null) {
                                            final FriendsAdapter adapter = new FriendsAdapter(WorkoutDetailActivity.this, res);
                                            WorkoutDetailActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    listViewFriends = dialog.findViewById(R.id.listview_friends_share);
                                                    listViewFriends.setAdapter(adapter);
                                                    listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                            fsh.shareWorkout(userId, res.get(position).getFriendUserId(), workoutId, sharedPreferences.getString("jwt", ""), new GetShareWorkoutRequestResponse() {
                                                                @Override
                                                                public void response(boolean result) {
                                                                    if (result) {
                                                                        Toast.makeText(WorkoutDetailActivity.this,
                                                                                R.string.future_impl, Toast.LENGTH_LONG).show();
                                                                       // TODO
                                                                    } else {
                                                                        Toast.makeText(WorkoutDetailActivity.this,
                                                                                R.string.future_impl, Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                });
                    } else {
                        fsh.getJwt(userId, userFullName, userProfileImageUrl, new FriendsActivity.GetJwtRequestResponse() {
                            @Override
                            public void response(final String jwt) {
                                if (jwt != null && !jwt.equals("")) {
                                    fsh.fetchFriends(userId, jwt, new FriendsActivity.GetFetchFriendsResponse() {
                                        @Override
                                        public void response(final ArrayList<Friend> res) {
                                            if (res != null) {
                                                final FriendsAdapter adapter = new FriendsAdapter(WorkoutDetailActivity.this, res);
                                                WorkoutDetailActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        listViewFriends = dialog.findViewById(R.id.listview_friends_share);
                                                        listViewFriends.setAdapter(adapter);
                                                        listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                            @Override
                                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                                fsh.shareWorkout(userId, res.get(position).getFriendUserId(), workoutId, jwt, new GetShareWorkoutRequestResponse() {
                                                                    @Override
                                                                    public void response(boolean result) {
                                                                        if (result) {
                                                                            // TODO
                                                                        } else {
                                                                            // TODO
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                    dialog.show();
                }
            });
        }

        // If title of workout is set...
        if (this.titleSet) {
            try {
                Workout workoutThis = dh.workoutDao()
                        .queryBuilder()
                        .where()
                        .eq("id", this.workoutId)
                        .query().get(0);

                this.workoutTitleTextView.setText(workoutThis.getTitle());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // If less than 2 gps points, hide map preview fragment.
        if (this.positions.size() < 2) {
            findViewById(R.id.fragment_workoutdetail_map).setVisibility(View.INVISIBLE);
            findViewById(R.id.textview_workoutdetail_preview_not_available).setVisibility(View.VISIBLE);
        }

        // Set moment as end of workout.
        this.setActivityDate(this.dateEnd);  // Set date of end of activity.

        // Set on-click listener to listen for requests to change workout title.
        this.workoutTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WorkoutDetailActivity.this);
                builder.setTitle("Set Workout Title");
                final EditText input = new EditText(WorkoutDetailActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Set new workout title.
                        String workoutTitleNew = input.getText().toString();
                        workoutTitleTextView.setText(workoutTitleNew);
                        workoutTitle = workoutTitleNew;
                        try {
                            Workout workoutThis = dh
                                    .workoutDao()
                                    .queryBuilder()
                                    .where()
                                    .eq("id", workoutId)
                                    .query()
                                    .get(0);
                            workoutThis.setTitle(workoutTitle);
                            dh.workoutDao().update(workoutThis);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        // Set click listener for workout parameters button:
        this.showParamsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Double> caloriesByTick = dsr.retrieveCaloriesByTick();
                ArrayList<Double> paceByTick = dsr.retrievePaceByTick();
                ArrayList<Double> elevationByTick = dsr.retrieveElevationByTick();

                if (caloriesByTick != null & caloriesByTick.size() > 0 && paceByTick != null && paceByTick.size() > 0) {

                    // Initialize intent to start new activity and put additional data in extras.
                    Intent workoutStatsActivityIntent = new Intent(WorkoutDetailActivity.this, WorkoutStatsActivity.class);
                    workoutStatsActivityIntent.putExtra("caloriesByTick", caloriesByTick);
                    workoutStatsActivityIntent.putExtra("paceByTick", paceByTick);
                    workoutStatsActivityIntent.putExtra("elevationByTick", elevationByTick);
                    WorkoutDetailActivity.this.startActivity(workoutStatsActivityIntent);
                }
            }
        });

    }

    // onPause: method called when this activity is paused.
    @Override
    public void onPause() {
        super.onPause();
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
    public void setDistance(double distance, boolean convertUnits) {
        TextView distanceText = findViewById(R.id.textview_workoutdetail_valuedistance);
        distanceText.setText(MainHelper.formatDistance((convertUnits)
                ? MainHelper.kmToMi(distance) : distance)
                + " " + ((convertUnits)
                ? getString(R.string.all_labeldistanceunitmiles)
                : getString(R.string.all_labeldistanceunitkilometers)));
        this.distance = distance;
    }

    // setPace: format and display text on UI that indicates the average pace of the workout.
    // also set property value.
    public void setPace(double avgPace, boolean convertUnits) {
        if (Double.isNaN(avgPace) || Double.isInfinite(avgPace)) {
            avgPace = 0.0;
        }
        TextView paceText = findViewById(R.id.textview_workoutdetail_valueavgpace);
        paceText.setText(MainHelper.formatPace((convertUnits)
                ? MainHelper.minpkmToMinpmi(avgPace)
                : avgPace)
                + " " + ((convertUnits)
                ? getString(R.string.all_labelpaceunitmiles)
                : getString(R.string.all_labelpaceunitkilometers)));
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

        // Disable gestures.
        mMap.getUiSettings().setAllGesturesEnabled(false);

        this.mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // Start MapsActivity and pass locations to it.
                Intent workoutMapIntent = new Intent(WorkoutDetailActivity.this, MapsActivity.class);
                workoutMapIntent.putExtra("positions", positions);
                WorkoutDetailActivity.this.startActivity(workoutMapIntent);
            }
        });

        if (positions != null && positions.size() > 0) {

            // Get starting and end positions.
            Location endPos = positions.get(positions.size()-1);
            Location startPos = positions.get(0);

            if (endPos != null && startPos != null) {
                // Mark starting and end position.
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(startPos.getLatitude(), startPos.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_small)));

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(endPos.getLatitude(), endPos.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.racing_flag_small)));

                // Initialize pause and break counters.
                int pauseCounter = 1;
                int breakCounter = 1;

                // Create route trail.
                for (int i = 1; i < this.positions.size(); i++) {
                    if (this.positions.get(i-1).distanceTo(this.positions.get(i)) < Constant.PAUSE_DIST_CHANGE_THRESH) {
                        mMap.addPolyline(new PolylineOptions()
                                .add(
                                        new LatLng(positions.get(i - 1).getLatitude(),
                                                positions.get(i - 1).getLongitude()),
                                        new LatLng(positions.get(i).getLatitude(),
                                                positions.get(i).getLongitude()))
                                .width(5.0f)
                                .color(Color.RED));
                           // If location has pause flag set to 1, mark pause location.
                        if (this.positions.get(i-1).getExtras() != null &&
                                this.positions.get(i-1).getExtras().getByte("pauseFlag", (byte)0) == (byte)1) {

                            // Add marker to end of previous session (before pausing).
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(this.positions.get(i - 1).getLatitude(), this.positions.get(i - 1).getLongitude()))
                                    .title(String.format(Locale.getDefault(),"Break %d", breakCounter)));
                            breakCounter += 1;  // Increment breaks counter.
                        }
                    } else {
                        // Add marker to end of previous session (before pausing).
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(this.positions.get(i-1).getLatitude(), this.positions.get(i-1).getLongitude()))
                                .title(String.format(Locale.getDefault(), "Pause %d", pauseCounter)));

                        // Add marker to start of new session (after continuing).
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(this.positions.get(i).getLatitude(), this.positions.get(i).getLongitude()))
                                .title(String.format(Locale.getDefault(), "Continue %d", pauseCounter)));

                        pauseCounter += 1;  // Increment pause counter.
                    }

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
        }
    }

    // onKeyDown: override default action when user presses the back button
    // Present stopwatch_shared in initial state.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent mainActivityIntent = new Intent(WorkoutDetailActivity.this, MainActivity.class);
            mainActivityIntent.putExtra("unit",
                    (convertUnits
                            ? Constant.UNITS_MI
                            : Constant.UNITS_KM));
            if (getIntent().hasExtra("fromHistory")) {
                mainActivityIntent.putExtra("loadHistory", true);
            }
            WorkoutDetailActivity.this.startActivity(mainActivityIntent);
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
            // Start SettingsActivity
            Intent settingsActivityIntent = new Intent(WorkoutDetailActivity.this, SettingsActivity.class);
            WorkoutDetailActivity.this.startActivity(settingsActivityIntent);
        } else if (id == R.id.workoutdetail_menuitem_delete) {

            // Prompt user to confirm intention to delete activity.
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alerttitle_delete_workout)
                    .setMessage(R.string.alertmessage_delete_workout)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                DeleteBuilder<Workout, Long> deleteBuilder = new DatabaseHelper(WorkoutDetailActivity.this).workoutDao().deleteBuilder();
                                deleteBuilder.where().eq("id", workoutId);
                                deleteBuilder.delete();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, null)  // Do nothing if user selects cancel.
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}

