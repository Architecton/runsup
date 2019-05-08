package si.uni_lj.fri.pbd2019.runsup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.helpers.MainHelper;

public class HistoryListAdapter extends ArrayAdapter<String> {

    // View lookup cache
    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView time;
        TextView activity;
    }

    private static class WorkoutData {

        // ### PROPERTIES ###
        private int activity;
        private double distance;
        private int calories;
        private double pace;
        // ### /PROPERTIES ###

        // constructor
        private WorkoutData(int activity, double distance, int calories, double pace) {
            this.activity = activity;
            this.distance = distance;
            this.calories = calories;
            this.pace = pace;
        }
    }

    private static class WorkoutInfoParser {

        private static int getWorkoutIntCode(String workoutName) {
            switch (workoutName) {
                case "Running":
                    return Constant.RUNNING;
                case "Cycling":
                    return Constant.CYCLING;
                case "Walking":
                    return Constant.WALKING;
                default:
                    return -1;
            }
        }

        private static WorkoutData getWorkoutDataInstanceFromString(String workoutInfo) {
            String[] split = workoutInfo.split(" ");

            return new WorkoutData(WorkoutInfoParser.getWorkoutIntCode(split[1]), Double.parseDouble(split[3]), Integer.parseInt(split[6]), Double.parseDouble(split[10]));
        }
    }

    public HistoryListAdapter(Context context, ArrayList<String> users) {
        super(context, R.layout.adapter_history, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String workoutInfo = getItem(position);
        WorkoutData dataNxt = WorkoutInfoParser.getWorkoutDataInstanceFromString(workoutInfo);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());

            convertView = inflater.inflate(R.layout.adapter_history, parent, false);
            // viewHolder.name = convertView.findViewById(R.id.tvName);
            // viewHolder.home = convertView.findViewById(R.id.tvHome);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Populate the data from the data object via the viewHolder object
        // into the template view.
        // viewHolder.icon
        viewHolder.activity.setText(MainHelper.getSportActivityName(dataNxt.activity));

        // Return the completed view to render on screen
        return convertView;
    }
}