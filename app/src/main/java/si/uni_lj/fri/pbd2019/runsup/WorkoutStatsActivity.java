package si.uni_lj.fri.pbd2019.runsup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.fragments.StopwatchFragment;
import si.uni_lj.fri.pbd2019.runsup.fragments.WorkoutParamsFragment;

public class WorkoutStatsActivity extends AppCompatActivity {

    // fragments
    private Fragment elevationParamsFragment;
    private Fragment paceParamsFragment;
    private Fragment caloriesParamsFragment;

    // fragment containers
    private FrameLayout layoutElevationParams;
    private FrameLayout layoutPaceParams;
    private FrameLayout layoutCaloriesParams;

    // fragment manager instance
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_stats);
        Intent intent = getIntent();

        // Initialize fragmentManager instance.
        this.fragmentManager = getSupportFragmentManager();

        // Extract parameters data from intent.
        ArrayList<Double> elevationByTick = (ArrayList<Double>) intent.getSerializableExtra("elevationByTick");
        ArrayList<Double> caloriesByTick = (ArrayList<Double>) intent.getSerializableExtra("caloriesByTick");
        ArrayList<Double> paceByTick = (ArrayList<Double>) intent.getSerializableExtra(("paceByTick"));
        long duration = intent.getLongExtra("duration", -1);

        // Initialize fragments.
        this.elevationParamsFragment = WorkoutParamsFragment.newInstance(elevationByTick, Constant.GRAPH_COLOR_GREEN);
        this.paceParamsFragment = WorkoutParamsFragment.newInstance(paceByTick, Constant.GRAPH_COLOR_BLUE);
        this.elevationParamsFragment = WorkoutParamsFragment.newInstance(caloriesByTick, Constant.GRAPH_COLOR_RED);



    }


    @Override
    protected void onStart() {
        super.onStart();

        Fragment stopwatchFragment = new StopwatchFragment();

        // Add fragments to containers.
        // this.fragmentManager.beginTransaction().add(R.id.elevation_chart_container, this.elevationParamsFragment).commit();
        Fragment test = new WorkoutParamsFragment();
        this.fragmentManager.beginTransaction().add(R.id.pace_chart_container, test).commit();
        // this.fragmentManager.beginTransaction().add(R.id.calories_chart_container, this.caloriesParamsFragment).commit();
    }
}
