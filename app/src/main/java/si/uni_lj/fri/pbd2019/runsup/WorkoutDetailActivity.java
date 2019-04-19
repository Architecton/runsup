package si.uni_lj.fri.pbd2019.runsup;

import android.content.Intent;
import android.content.res.Resources;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Date;

import si.uni_lj.fri.pbd2019.runsup.helpers.MainHelper;

public class WorkoutDetailActivity extends AppCompatActivity {

    // ### PROPERTIES ###

    // resource used by the activity
    public static Resources resources;

    // UI components
    private EditText shareText;
    private Button confirmShareButton;
    private Button facebookShareButton;
    private Button emailShareButton;
    private Button googlePlusShareButton;
    private Button twitterShareButton;

    // workout parameters
    private int sportActivity;
    private long duration;
    private double calories;
    private double distance;
    private double avgPace;

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

    // onKeyDown: override default action when user presses the back button
    // Present stopwatch in initial state.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            Intent stopwatchActivityIntent = new Intent(WorkoutDetailActivity.this, StopwatchActivity.class);
            WorkoutDetailActivity.this.startActivity(stopwatchActivityIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
