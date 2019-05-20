package si.uni_lj.fri.pbd2019.runsup.stats;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.MainActivity;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;

public class DataForStatsRetriever {

    DatabaseHelper dh;

    public DataForStatsRetriever() {
        dh = new DatabaseHelper(MainActivity.mainActivity);
    }

    // retreiveDuration: retrieve duration of workout with specified id
    public ArrayList<Long> retreiveDuration(long idWorkout) {
        // Find workout with specified id.

        // Return total duration of workout in seconds.
        return null;
    }

    // retreiveCalories: retrieve calories for each tick received during workout.
    public ArrayList<Long> retreiveCalories(long idWorkout) {
        // Find workout with specific id.

        // Find corresponding gps points.

        // Order gps points by date.

        // Store calories in ArrayList instance and return it.
        return null;
    }

    // retreiveElevation: retrieve elevation for each tick received during workout.
    public ArrayList<Double> retrieveElevation(long idWorkout) {
        // Find workout with specific id.

        // Find corresponding gps points.

        // Order gps points by date.

        // Store elevations in ArrayList instance and return it.
        return null;
    }

    // retrievePace: retrieve pace for each tick received during workout.
    public ArrayList<Double> retrievePace(long idWorkout) {
        // Find workout with specific id.

        // Find corresponding gps points.

        // Order gps points by date.

        // Store paces in ArrayList instance and return it.

        return null;
    }

}
