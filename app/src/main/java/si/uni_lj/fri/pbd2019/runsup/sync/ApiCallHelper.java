package si.uni_lj.fri.pbd2019.runsup.sync;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.j256.ormlite.stmt.QueryBuilder;

import org.json.JSONArray;
import org.json.JSONException;

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
import si.uni_lj.fri.pbd2019.runsup.FriendsSearchActivity;
import si.uni_lj.fri.pbd2019.runsup.MainActivity;
import si.uni_lj.fri.pbd2019.runsup.model.Friend;
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

    void logInOrSignUpAndSync(final long userId, final long[] presentWorkoutIds, final User user, final String name, final String profileImageUrl, final MainActivity.SyncCompleted syncCompleted) {

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
                syncCompleted.syncCompleted(false);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    CloudLoginResponse res = gson.fromJson(response.body().string(), CloudLoginResponse.class);
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity).edit().putString("jwt", res.getToken()).apply();
                    getUserWorkoutIds(userId, presentWorkoutIds, user, res.getToken(), syncCompleted);
                } else {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("accId", Long.toString(userId))
                            .add("name", name)
                            .add("profileImageUrl", profileImageUrl)
                            .build();
                    final Request request = new Request.Builder()
                            .post(requestBody)
                            .url(baseUrl + "/users")
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            syncCompleted.syncCompleted(false);
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            logInOrSignUpAndSync(userId, presentWorkoutIds, user, name, profileImageUrl, syncCompleted);
                        }
                    });
                }
            }
        });
    }

    void postWorkoutToCloud(String workoutJson, long userId, String jwt, final TransferCompleted transferCompleted) {
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
                    transferCompleted.onTransferCompleted(true);
                } else {
                    Log.d(TAG, "Failed to uploaded workout.");
                    transferCompleted.onTransferCompleted(false);
                }
            }
        });
    }

    private void getWorkoutFromCloud(final User user, long workoutId, String jwt, final TransferCompleted transferCompleted) {

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
                    transferCompleted.onTransferCompleted(true);
                } else {
                    Log.d(TAG, "Error receiving workout from cloud");
                    transferCompleted.onTransferCompleted(false);
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

    public void getUserWorkoutIds(long userId, final long[] presentWorkoutIds,
                                  final User user, final String jwt,
                                  final MainActivity.SyncCompleted syncCompleted) {
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
                syncCompleted.syncCompleted(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    handleWorkoutIdsListResponse(response.body().string(), presentWorkoutIds, user, jwt, syncCompleted);
                } else {
                    syncCompleted.syncCompleted(false);
                }
            }
        });
    }

    private interface TransferCompleted {
        void onTransferCompleted(boolean successful);
    }

    private void handleWorkoutIdsListResponse(String json, long[] presentworkoutIds,
                                              User user, String jwt,
                                              final MainActivity.SyncCompleted syncCompleted) {
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

            final int numToTransfer = presentHere2.size() + presentCloud.size();
            final int[] numTransferred = new int[1];

            // Upload workouts missing in cloud.
            for (long id : presentCloud) {
                getWorkoutFromCloud(user, id, jwt, new TransferCompleted() {
                    @Override
                    public void onTransferCompleted(boolean successful) {
                        numTransferred[0]++;
                        if (numTransferred[0] >= numToTransfer) {
                            syncCompleted.syncCompleted(true);
                        }
                    }
                });
            }

            // Download workouts missing here.
            for (long id : presentHere2) {
                postWorkoutToCloud(packWorkoutForSending(id, user.getId()), user.getId(), jwt, new TransferCompleted() {
                    @Override
                    public void onTransferCompleted(boolean successful) {
                        numTransferred[0]++;
                        if (numTransferred[0] >= numToTransfer) {
                            syncCompleted.syncCompleted(true);
                        }
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
            syncCompleted.syncCompleted(false);
        } catch (SQLException e) {
            e.printStackTrace();
            syncCompleted.syncCompleted(false);
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
                gpsPointNxt.setElevation(point.getElevation());
                gpsPointsCloudData.add(gpsPointNxt);
            }

            workoutCloudData.setGpsPoints(gpsPointsCloudData);
            Gson gson = new Gson();
            return gson.toJson(workoutCloudData);
        } else {
            return "";
        }
    }

    void loginOrSignupAndSearchFriends(final String searchInput, final User user, final String name, final String profileImageUrl, final FriendsSearchActivity.GetFriendsSearchResponse getFriendsSearchResponse) {

        // Check if already logged in to back-end
        if (PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity).contains("jwt")) {
            searchForFriends(searchInput, PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity).getString("jwt", ""), getFriendsSearchResponse);
        } else {
            RequestBody requestBody = new FormBody.Builder()
                    .add("accId", Long.toString(user.getId()))
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
                        searchForFriends(searchInput, res.getToken(), getFriendsSearchResponse);
                    } else {
                        RequestBody requestBody = new FormBody.Builder()
                                .add("accId", Long.toString(user.getId()))
                                .add("name", name)
                                .add("profileImageUrl", profileImageUrl)
                                .build();
                        final Request request = new Request.Builder()
                                .post(requestBody)
                                .url(baseUrl + "/users")
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                // Return failure signal.
                                getFriendsSearchResponse.getSearchResultsList(null);
                            }

                            @Override
                            public void onResponse(Call call, Response response) {
                                loginOrSignupAndSearchFriends(searchInput, user, name, profileImageUrl, getFriendsSearchResponse);
                            }
                        });
                    }
                }
            });
        }
    }

    private void searchForFriends(String searchInput, String jwt, final FriendsSearchActivity.GetFriendsSearchResponse getFriendsSearchResponse) {
        final Request request = new Request.Builder()
                .get()
                .addHeader("cache-control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization" , "Bearer " + jwt)
                .url(baseUrl + "/friends/" + searchInput)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Return failure signal.
                getFriendsSearchResponse.getSearchResultsList(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    String json = response.body().string();
                    ArrayList<Friend> reconstruction;
                    if (json.charAt(0) == '[') {
                        CloudDataFriend[] receivedData = gson.fromJson(json, CloudDataFriend[].class);
                        reconstruction = new ArrayList<>(receivedData.length);
                        for (CloudDataFriend receivedDatum : receivedData) {
                            reconstruction.add(receivedDatum.toFriend());
                        }
                    } else {
                        reconstruction = new ArrayList<>(1);
                        reconstruction.add(gson.fromJson(json, CloudDataFriend.class).toFriend());
                    }
                    getFriendsSearchResponse.getSearchResultsList(reconstruction);
                } else {
                    // Return failure signal.
                    getFriendsSearchResponse.getSearchResultsList(null);
                }
            }
        });
    }

    void sendFriendRequest(final long idFriend, String jwt, FriendsSearchActivity.GetSendFriendRequestResponse getSendFriendRequestResponse) {

        RequestBody requestBody = new FormBody.Builder()
                .add("idFriend", Long.toString(idFriend))
                .build();

        final Request request = new Request.Builder()
                .post(requestBody)
                .addHeader("cache-control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization" , "Bearer " + jwt)
                .url(baseUrl + "/friends/" + Long.toString(idFriend) + "/request")
                .build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Show message that friend request successfully sent.
                } else {
                    // Show message that there was an error sending friend request.
                }
            }
        });
    }

    void acceptFriendRequest(final long idFriend, String jwt, FriendsSearchActivity.GetAcceptFriendRequestResponse getAcceptFriendRequestResponse) {
        RequestBody requestBody = new FormBody.Builder()
                .add("idFriend", Long.toString(idFriend))
                .build();

        final Request request = new Request.Builder()
                .post(requestBody)
                .addHeader("cache-control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization" , "Bearer " + jwt)
                .url(baseUrl + "/friends/" + Long.toString(idFriend) + "/accept")
                .build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Show message that friend request successfully sent.
                } else {
                    // Show message that there was an error sending friend request.
                }
            }
        });
    }
}
