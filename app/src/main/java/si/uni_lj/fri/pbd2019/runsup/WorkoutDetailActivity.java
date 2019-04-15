package si.uni_lj.fri.pbd2019.runsup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class WorkoutDetailActivity extends AppCompatActivity {

    // onCreate: method called when activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Call onCreate method of superclass.
        setContentView(R.layout.activity_workout_detail);  // Set content of activity.
        Intent intent = getIntent();  // Get intent and unpack extras into methods that format and display data on UI.
        setDuration(intent.getLongExtra("duration", 0));
        setSportActivity(intent.getIntExtra("sportActivity", -1));
        setCalories(intent.getDoubleExtra("calories", 0.0));
        setDistance(intent.getDoubleExtra("distance", 0.0));
        setPace(intent.getDoubleExtra("pace", 0.0));
    }

    // onPause: method called when this activity is paused.
    @Override
    public void onPause() {
        super.onPause();
    }



    // ### METHODS FOR FORMATTING THE UI ###

    public void setSportActivity(int sportActivity) {
        TextView sportActivityText = findViewById(R.id.textview_workoutdetail_sportactivity);
        sportActivityText.setText(MainHelper.getSportActivityName(sportActivity));
    }

    public void setDuration(long duration) {
        TextView durationText = findViewById(R.id.textview_workoutdetail_valueduration);
        durationText.setText(MainHelper.formatDuration(duration));
    }

    public void setCalories(double calories) {
        TextView caloriesText = findViewById(R.id.textview_workoutdetail_valuecalories);
        caloriesText.setText(MainHelper.formatCalories(calories));
    }

    public void setDistance(double distance) {
        TextView distanceText = findViewById(R.id.textview_workoutdetail_valuedistance);
        distanceText.setText(MainHelper.formatDistance(distance));
    }

    public void setPace(double avgPace) {
        TextView paceText = findViewById(R.id.textview_workoutdetail_valueavgpace);
        paceText.setText(MainHelper.formatPace(avgPace));
    }

    // ### /METHODS FOR FORMATTING THE UI ###
}
