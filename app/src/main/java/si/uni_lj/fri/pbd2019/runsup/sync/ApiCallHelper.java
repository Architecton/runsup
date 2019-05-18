package si.uni_lj.fri.pbd2019.runsup.sync;

import android.annotation.SuppressLint;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.gson.Gson;
import com.j256.ormlite.stmt.QueryBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import si.uni_lj.fri.pbd2019.runsup.MainActivity;
import si.uni_lj.fri.pbd2019.runsup.model.GpsPoint;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;

class ApiCallHelper {

    public static String TAG = ApiCallHelper.class.getSimpleName();

    private String baseUrl;
    OkHttpClient client;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private DatabaseHelper dh;

    ApiCallHelper(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = new OkHttpClient();
        this.dh = new DatabaseHelper(MainActivity.mainActivity);
    }

    void logInOrSignUpAndSync(final long userId, final long[] presentWorkoutIds, final User user) {

        RequestBody requestBody = new FormBody.Builder()
                .add("accId", Long.toString(userId))
                .build();
        final Request request = new Request.Builder()
                .post(requestBody)
                .url(baseUrl + "/users/login")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    CloudLoginResponse res = gson.fromJson(response.body().string(), CloudLoginResponse.class);
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity).edit().putString("jwt", res.getToken()).apply();
                    getUserWorkoutIds(userId, presentWorkoutIds, user, res.getToken());
                } else {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("accId", Long.toString(userId))
                            .build();
                    final Request request = new Request.Builder()
                            .post(requestBody)
                            .url(baseUrl + "/users")
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            logInOrSignUpAndSync(userId, presentWorkoutIds, user);
                        }
                    });
                }
            }
        });
    }

    void postWorkoutToCloud(String workoutJson, long userId, String jwt) {
        RequestBody body = RequestBody.create(JSON, workoutJson);
        final Request request = new Request.Builder()
                .post(body)
                .addHeader("cache-control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization" , "Bearer " + jwt)
                .url(baseUrl + "/workouts/" + Long.toString(userId))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully uploaded workout.");
                } else {
                    Log.d(TAG, "Failed to uploaded workout.");
                }
            }
        });
    }

    void getWorkoutFromCloud(final User user, long workoutId, String jwt) {

        final Request request = new Request.Builder()
                .get()
                .addHeader("cache-control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization" , "Bearer " + jwt)
                .url(baseUrl + "/workouts/" + Long.toString(user.getId()) + "/" + Long.toString(workoutId))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    processWorkoutFromCloud(response.body().string(), user);
                } else {
                    Log.d(TAG, "Error receiving workout from cloud");
                }
            }
        });
    }

    private void processWorkoutFromCloud(String json, User user) {
        Gson gson = new Gson();
        CloudDataWorkout receivedData = gson.fromJson(json, CloudDataWorkout.class);
        Workout reconstruction = receivedData.toWorkout(user);
        try {
            dh.workoutDao().create(reconstruction);
            for (CloudDataGpsPoint point : receivedData.getGpsPoints()) {
                GpsPoint gpsPointNxt = point.toGpsPoint(reconstruction);
                dh.gpsPointDao().create(gpsPointNxt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getUserWorkoutIds(long userId, final long[] presentWorkoutIds, final User user, final String jwt) {
        final Request request = new Request.Builder()
                .get()
                .addHeader("cache-control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization" , "Bearer " + jwt)
                .url(baseUrl + "/workouts/" + Long.toString(userId))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    handleWorkoutIdsListResponse(response.body().string(), presentWorkoutIds, user, jwt);
                } else {

                }
            }
        });
    }

    private void handleWorkoutIdsListResponse(String json, long[] presentworkoutIds, User user, String jwt) {
        try {
            JSONArray arr = new JSONArray(json);
            long[] workoutIndicesUser = new long[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                workoutIndicesUser[i] = arr.getLong(i);
            }

            HashSet<Long> presentHere = new HashSet<>();
            HashSet<Long> presentHere2 = new HashSet<>();
            HashSet<Long> presentCloud = new HashSet<>();
            HashSet<Long> presentCloud2 = new HashSet<>();
            for (int i = 0; i < workoutIndicesUser.length; i++) {
                presentCloud.add(workoutIndicesUser[i]);
                presentCloud2.add(workoutIndicesUser[i]);
            }
            for (int i = 0; i < presentworkoutIds.length; i++) {
                presentHere.add(presentworkoutIds[i]);
                presentHere2.add(presentworkoutIds[i]);
            }

            // Get ids of workouts missing from here.
            presentCloud.removeAll(presentHere);

            // Get indices of workouts missing in cloud.
            presentHere2.removeAll(presentCloud2);


            // Upload workouts missing in cloud.
            for (long id : presentCloud) {
                getWorkoutFromCloud(user, id, jwt);
            }

            // Download workouts missing here.
            for (long id : presentHere2) {
                postWorkoutToCloud(packWorkoutForSending(id, user.getId()), user.getId(), jwt);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private String packWorkoutForSending(long workoutId, long userId) throws SQLException {
        DatabaseHelper dh = new DatabaseHelper(MainActivity.mainActivity);
        List<Workout> workouts = dh.workoutDao()
                .queryBuilder()
                .where()
                .eq("id", workoutId)
                .query();

        if (workouts.size() > 0) {
            Workout workoutSelected = workouts.get(0);

            CloudDataWorkout workoutCloudData = new CloudDataWorkout();
            workoutCloudData.set_id(workoutSelected.getId());
            workoutCloudData.setCreated(workoutSelected.getCreated().toString());
            workoutCloudData.setDistance(workoutSelected.getDistance());
            workoutCloudData.setDuration(workoutSelected.getDuration());
            workoutCloudData.setLastUpdate(workoutSelected.getLastUpdate().toString());
            workoutCloudData.setStatus(workoutSelected.getStatus());
            workoutCloudData.setTitle(workoutSelected.getTitle());
            workoutCloudData.setTotalCalories(workoutSelected.getTotalCalories());
            workoutCloudData.setSportActivity(workoutSelected.getSportActivity());
            workoutCloudData.setPaceAvg(workoutSelected.getPaceAvg());

            QueryBuilder<Workout, Long> workoutQb = dh.workoutDao().queryBuilder();
            QueryBuilder<GpsPoint, Long> gpspointQb = dh.gpsPointDao().queryBuilder();
            workoutQb.where().eq("id", workoutSelected.getId());
            List<GpsPoint> workoutGpsPoints = gpspointQb.join(workoutQb).query();

            ArrayList<CloudDataGpsPoint> gpsPointsCloudData = new ArrayList<CloudDataGpsPoint>(workoutGpsPoints.size());
            for (GpsPoint point : workoutGpsPoints) {
                CloudDataGpsPoint gpsPointNxt = new CloudDataGpsPoint();

                gpsPointNxt.set_id(point.getId());
                gpsPointNxt.setCreated(point.getCreated().toString());
                gpsPointNxt.setDuration(point.getDuration());
                gpsPointNxt.setLastUpdate(point.getLastUpdate().toString());
                gpsPointNxt.setLatitude(point.getLatitude());
                gpsPointNxt.setLongitude(point.getLongitude());
                gpsPointNxt.setPace(point.getPace());
                gpsPointNxt.setPauseFlag(point.getPauseFlag());
                gpsPointNxt.setSessionNumber(point.getSessionNumber());
                gpsPointNxt.setSpeed(point.getSpeed());
                gpsPointNxt.setTotalCalories(point.getTotalCalories());
                gpsPointsCloudData.add(gpsPointNxt);
            }

            workoutCloudData.setGpsPoints(gpsPointsCloudData);
            Gson gson = new Gson();
            return gson.toJson(workoutCloudData);
        } else {
            return "";
        }
    }

}
