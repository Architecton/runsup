package si.uni_lj.fri.pbd2019.runsup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.helpers.MainHelper;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;

import static android.content.Context.MODE_PRIVATE;

public class HistoryListAdapter extends ArrayAdapter<Workout> {

    public HistoryListAdapter(Context context, ArrayList<Workout> workouts) {
        super(context, 0, workouts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SharedPreferences preferences = getContext().getSharedPreferences(Constant.STATE_PREF_NAME, MODE_PRIVATE);
        String distUnitsAbbr = (preferences.getInt("unit", Constant.UNITS_KM) == Constant.UNITS_KM)
                ?
                getContext().getString(R.string.all_labeldistanceunitkilometers)
                :
                getContext().getString(R.string.all_labeldistanceunitmiles);
        String paceUnitsAbbr = (preferences.getInt("unit", Constant.UNITS_KM) == Constant.UNITS_KM)
                ?
                getContext().getString(R.string.all_labelpaceunitkilometers)
                :
                getContext().getString(R.string.all_labelpaceunitmiles);
        boolean convertToMi = preferences.getInt("unit", Constant.UNITS_KM) != Constant.UNITS_KM;

        // Get the data item for this position
        Workout workoutNxt = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_history, parent, false);
        }
        // Lookup view for data population
        TextView workoutTitle = (TextView) convertView.findViewById(R.id.textview_history_title);
        TextView workoutTime = (TextView) convertView.findViewById(R.id.textview_history_datetime);
        TextView workoutSportActivity = (TextView) convertView.findViewById(R.id.textview_history_sportactivity);
        ImageView workoutIcon = (ImageView) convertView.findViewById(R.id.imageview_history_icon);

        // Populate the data into the template view using the data object
        // tvName.setText(user);
        workoutTitle.setText(workoutNxt.getTitle());
        workoutTime.setText(workoutNxt.getCreated().toString());
        workoutSportActivity.setText(String.format("%s %s | %s | %s | %s",
                HistoryStringFormatter.formatDuration(workoutNxt.getDuration()),
                HistoryStringFormatter.formatSportActivity(workoutNxt.getSportActivity()),
                HistoryStringFormatter.formatDistance((convertToMi)
                        ? MainHelper.kmToMi(workoutNxt.getDistance())
                        : workoutNxt.getDistance()) + " " + distUnitsAbbr,
                HistoryStringFormatter.formatCalories(workoutNxt.getTotalCalories()) + " " + getContext().getString(R.string.all_labelcaloriesunit),
                "avg " + HistoryStringFormatter.formatPace((convertToMi)
                        ? MainHelper.minpkmToMinpmi(workoutNxt.getPaceAvg())
                        : workoutNxt.getPaceAvg()) + " " + paceUnitsAbbr
        ));

        switch (workoutNxt.getSportActivity()) {
            case Constant.CYCLING:
                workoutIcon.setImageResource(R.drawable.bicycle);
                break;
            case Constant.RUNNING:
                workoutIcon.setImageResource(R.drawable.running);
                break;
            case Constant.WALKING:
                workoutIcon.setImageResource(R.drawable.pedestrian_walking);
                break;
        }

        // Return the completed view to render on screen
        return convertView;
    }

    public static class HistoryStringFormatter {
        static String formatDuration(long duration) {
            return MainHelper.formatDuration(Math.round(duration*1e-3));
        }

        static String formatSportActivity(int sportActivity) {
            return MainHelper.getSportActivityName(sportActivity);
        }

        static String formatDistance(double distance) {
            return MainHelper.formatDistance(distance);
        }

        static String formatCalories(double calories) {
            return MainHelper.formatCalories(calories);
        }

        static String formatPace(double pace) {
            return MainHelper.formatPace(pace);
        }
    }
}