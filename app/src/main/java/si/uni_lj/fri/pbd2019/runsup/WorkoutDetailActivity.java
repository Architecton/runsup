package si.uni_lj.fri.pbd2019.runsup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class WorkoutDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);
        Intent intent = getIntent();
        setDuration(intent.getLongExtra("duration", 0));
        setSportActivity(intent.getIntExtra("sportActivity", -1));
        setCalories(intent.getDoubleExtra("calories", 0.0));
        setDistance(intent.getDoubleExtra("distance", 0.0));
        setPace(intent.getDoubleExtra("pace", 0.0));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

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

}
