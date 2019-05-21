package si.uni_lj.fri.pbd2019.runsup.stats;

import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import si.uni_lj.fri.pbd2019.runsup.MainActivity;
import si.uni_lj.fri.pbd2019.runsup.model.GpsPoint;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;

public class DataForStatsRetriever {

    private DatabaseHelper dh;
    private List<GpsPoint> gpsPoints;
    private long idWorkout;

    public DataForStatsRetriever(long idWorkout) throws SQLException {
        this.dh = new DatabaseHelper(MainActivity.mainActivity);
        this.idWorkout = idWorkout;

        // Find GPS points corresponding to selected workout.
        QueryBuilder<Workout, Long> workoutQb = this.dh.workoutDao().queryBuilder();
        QueryBuilder<GpsPoint, Long> gpspointQb = this.dh.gpsPointDao().queryBuilder();
        workoutQb.where().eq("id", idWorkout);
        gpsPoints = gpspointQb.join(workoutQb).query();

        // Order GPS points by date.
        Collections.sort(gpsPoints, new Comparator<GpsPoint>(){
            public int compare(GpsPoint p1, GpsPoint p2) {
                return p1.getCreated().compareTo(p2.getCreated());
            }
        });
    }

    // retreiveDuration: retrieve duration of workout with specified id
    public long retrieveDuration() throws SQLException {

        // Find workout with specified id.
        List<Workout> res = this.dh.workoutDao()
                .queryBuilder()
                .where()
                .eq("id", this.idWorkout)
                .query();

        // Return duration of workout.
        if (res != null && res.size() > 0) {
            return res.get(0).getDuration();
        } else {
            return -1L;
        }
    }


    // retreiveCalories: retrieve calories for each tick received during workout.
    public ArrayList<Double> retrieveCaloriesByTick() {


        // Get list of calories with respect to tick.
        ArrayList<Double> caloriesWithRespectToTick = new ArrayList<>(this.gpsPoints.size());
        for (GpsPoint p : this.gpsPoints) {
            caloriesWithRespectToTick.add(p.getTotalCalories());
        }

        return caloriesWithRespectToTick;
    }

    // retrieveElevation: retrieve elevation for each tick received during workout.
    public ArrayList<Double> retrieveElevationByTick() {

        // Get list of elevations with respect to tick.
        ArrayList<Double> elevationsWithRespectToTick = new ArrayList<>(this.gpsPoints.size());
        for (GpsPoint p : gpsPoints) {
            elevationsWithRespectToTick.add(p.getTotalCalories());
        }

        // Store elevations in ArrayList instance and return it.
        return elevationsWithRespectToTick;
    }

    // retrievePace: retrieve pace for each tick received during workout.
    public ArrayList<Double> retrievePaceByTick() {

        // Get list of paces with respect to tick.
        ArrayList<Double> elevationsWithRespectToTick = new ArrayList<>(this.gpsPoints.size());
        for (GpsPoint p : this.gpsPoints) {
            elevationsWithRespectToTick.add(p.getPace());
        }

        // Store paces in ArrayList instance and return it.
        return elevationsWithRespectToTick;
    }

}
