package si.uni_lj.fri.pbd2019.runsup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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

    public CompositeListener seekBarIntervalListener;

    private int numTicks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_stats);
        Intent intent = getIntent();

        // Initialize fragmentManager instance.
        this.fragmentManager = getSupportFragmentManager();

        // Extract parameters data from intent.
        ArrayList<Double> elevationByTick = (ArrayList<Double>) intent.getSerializableExtra("elevationByTick");
        elevationByTick = processElevation(elevationByTick);
        ArrayList<Double> caloriesByTick = (ArrayList<Double>) intent.getSerializableExtra("caloriesByTick");
        ArrayList<Double> paceByTick = (ArrayList<Double>) intent.getSerializableExtra(("paceByTick"));
        this.numTicks = paceByTick.size();

        // Initialize fragments.
        this.elevationParamsFragment = WorkoutParamsFragment.newInstance(elevationByTick, Constant.GRAPH_COLOR_GREEN, Constant.CHART_TYPE_ELEVATION);
        this.paceParamsFragment = WorkoutParamsFragment.newInstance(paceByTick, Constant.GRAPH_COLOR_BLUE, Constant.CHART_TYPE_PACE);
        this.caloriesParamsFragment = WorkoutParamsFragment.newInstance(caloriesByTick, Constant.GRAPH_COLOR_RED, Constant.CHART_TYPE_CALORIES);
    }

    // processElevation: replace zero values in list of elevations by nearest non-zero values.
    private ArrayList<Double> processElevation(ArrayList<Double> elevationByTick) {

        // Go over elevations.
        for (int i = 0; i < elevationByTick.size(); i++) {

            // If elevation 0, interpolate with closest non-zero value.
            if (elevationByTick.get(i) == 0) {
                int closestNonZeroBelow = -1;
                for (int j = i; j >= 0; j--) {
                    if (elevationByTick.get(j) > 0) {
                        closestNonZeroBelow = j;
                    }
                }
                int closestNonZeroAbove = -1;
                for (int j = i; j < elevationByTick.size(); j++) {
                    if (elevationByTick.get(j) > 0) {
                        closestNonZeroAbove = j;
                    }
                }

                if (closestNonZeroBelow >= 0 && closestNonZeroAbove >= 0) {
                    int indexFillElement = (Math.abs(i - closestNonZeroBelow) < Math.abs(i - closestNonZeroAbove)) ? closestNonZeroBelow : closestNonZeroAbove;
                    elevationByTick.set(i, elevationByTick.get(indexFillElement));
                } else if (closestNonZeroAbove >= 0) {
                    elevationByTick.set(i, elevationByTick.get(closestNonZeroAbove));
                } else if (closestNonZeroAbove >= 0) {
                    elevationByTick.set(i, elevationByTick.get(closestNonZeroAbove));
                }
            }
        }
        return elevationByTick;
    }

    // CompositeListener: OnSeekBarChangeListener implementation that is able to register
    // other listeners of this type and forward events to them.
    public class CompositeListener implements SeekBar.OnSeekBarChangeListener {
        private List<SeekBar.OnSeekBarChangeListener> registeredListeners = new ArrayList<>();

        public void registerListener (SeekBar.OnSeekBarChangeListener listener) {
            registeredListeners.add(listener);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            for (SeekBar.OnSeekBarChangeListener listener : registeredListeners) {
                listener.onProgressChanged(seekBar, progress, fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Initialize composite listener.
        this.seekBarIntervalListener = new CompositeListener();


        // Initialize seekBar instance.
        SeekBar seekBarInterval = findViewById(R.id.seekBar_interval);
        seekBarInterval.setMax(this.numTicks-10);
        seekBarInterval.setOnSeekBarChangeListener(this.seekBarIntervalListener);

        // Add fragments to containers.
        this.fragmentManager.beginTransaction().add(R.id.elevation_chart_container, this.elevationParamsFragment).commit();
        this.fragmentManager.beginTransaction().add(R.id.pace_chart_container, this.paceParamsFragment).commit();
        this.fragmentManager.beginTransaction().add(R.id.calories_chart_container, this.caloriesParamsFragment).commit();

        // Display toast with short instructions on position lock.
        Toast toast = Toast.makeText(this, getString(R.string.params_toast_instructions), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }
}
