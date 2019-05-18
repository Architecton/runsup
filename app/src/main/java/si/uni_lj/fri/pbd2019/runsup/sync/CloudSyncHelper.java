package si.uni_lj.fri.pbd2019.runsup.sync;

import android.util.Log;

import java.sql.SQLException;
import java.util.List;

import si.uni_lj.fri.pbd2019.runsup.MainActivity;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;

public class CloudSyncHelper {

    private String baseUrl;
    private ApiCallHelper apiCallHelper;

    public CloudSyncHelper(String baseUrl) {
       this.baseUrl = baseUrl;
       this.apiCallHelper = new ApiCallHelper(this.baseUrl);
    }


    public void syncWithCloud(String baseUrl, User currentUser) throws SQLException {
        List<Workout> presetWorkoutIdsRes = new DatabaseHelper(MainActivity.mainActivity)
                .workoutDao()
                .queryBuilder()
                .query();
        long[] presentWorkoutIds = new long[presetWorkoutIdsRes.size()];

        for (int i = 0; i < presetWorkoutIdsRes.size(); i++) {
            presentWorkoutIds[i] = presetWorkoutIdsRes.get(i).getId();
        }

        this.apiCallHelper.logInOrSignUpAndSync(currentUser.getId(), presentWorkoutIds, currentUser);
    }


}
