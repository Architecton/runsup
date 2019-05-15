package si.uni_lj.fri.pbd2019.runsup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.helpers.MainHelper;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;

public class HistoryListAdapter extends ArrayAdapter<Workout> {

    public HistoryListAdapter(Context context, ArrayList<Workout> workouts) {
        super(context, 0, workouts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

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

        // Populate the data into the template view using the data object
        // tvName.setText(user);
        workoutTitle.setText(workoutNxt.getTitle());
        workoutTime.setText(workoutNxt.getCreated().toString());
        workoutSportActivity.setText(String.format("%s %s | %s | %s | %s",
                HistoryStringFormatter.formatDuration(workoutNxt.getDuration()),
                HistoryStringFormatter.formatSportActivity(workoutNxt.getSportActivity()),
                HistoryStringFormatter.formatDistance(workoutNxt.getDistance()),
                HistoryStringFormatter.formatCalories(workoutNxt.getTotalCalories()),
                HistoryStringFormatter.formatPace(workoutNxt.getPaceAvg())));
        // Return the completed view to render on screen
        return convertView;
    }

    public static class HistoryStringFormatter {
        public static String formatDuration(long duration) {
            return MainHelper.formatDuration(duration);
        }

        public static String formatSportActivity(int sportActivity) {
            return MainHelper.getSportActivityName(sportActivity);
        }

        public static String formatDistance(double distance) {
            return MainHelper.formatDistance(distance);
        }

        public static String formatCalories(double calories) {
            return MainHelper.formatCalories(calories);
        }

        public static String formatPace(double pace) {
            return MainHelper.formatPace(pace);
        }
    }
}