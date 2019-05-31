package si.uni_lj.fri.pbd2019.runsup.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import si.uni_lj.fri.pbd2019.runsup.HistoryListAdapter;
import si.uni_lj.fri.pbd2019.runsup.MainActivity;
import si.uni_lj.fri.pbd2019.runsup.R;
import si.uni_lj.fri.pbd2019.runsup.WorkoutDetailActivity;
import si.uni_lj.fri.pbd2019.runsup.model.GpsPoint;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;

public class HistoryFragment extends Fragment {

    // ### PROPERTIES ###

    private User currentUser;

    // ### /PROPERTIES ###

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        setHasOptionsMenu(true);  // Fragment has an options menu.

        // Get shared preferences and current user.
        this.currentUser = ((MainActivity)getActivity()).currentUser;
        return inflater.inflate(R.layout.fragment_history, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Get list of workouts in database.
        final DatabaseHelper dh = new DatabaseHelper(getContext());
        final ArrayList<Workout> workouts = new ArrayList<>();
        try {
            QueryBuilder<User, Long> userQb = dh.userDao().queryBuilder();
            QueryBuilder<Workout, Long> workoutQb = dh.workoutDao().queryBuilder();
            userQb.where().eq("accId", currentUser.getAccId());
            List<Workout> userWorkouts = workoutQb.join(userQb).query();
            Collections.reverse(userWorkouts);

            final SwipeRefreshLayout pullToRefresh = getActivity().findViewById(R.id.pullToRefresh);
            pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getFragmentManager().beginTransaction().detach(HistoryFragment.this).attach(HistoryFragment.this).commit();
                    pullToRefresh.setRefreshing(false);
                }
            });

            // If workouts found in database, remove TextView instance that informs of
            // absence of workouts.
            if (userWorkouts.size() > 0) {
                getActivity()
                        .findViewById(R.id.textview_history_noHistoryData).setVisibility(View.GONE);
            }
            workouts.addAll(userWorkouts);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create the adapter to convert the array to views and attach to a ListView instance.
        HistoryListAdapter adapter = new HistoryListAdapter(getContext(), workouts);
        ListView listView = getActivity().findViewById(R.id.listview_history_workouts);
        listView.setAdapter(adapter);

        // Set up click listener for adapter items.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {try {
                    // Reconstruct positions from GPS points.
                    QueryBuilder<Workout, Long> workoutQb = dh.workoutDao().queryBuilder();
                    QueryBuilder<GpsPoint, Long> gpspointQb = dh.gpsPointDao().queryBuilder();
                    workoutQb.where().eq("id", workouts.get(position).getId());
                    List<GpsPoint> results = gpspointQb.join(workoutQb).query();
                    ArrayList<Location> reconstructedPositions = new ArrayList<>(results.size());
                    for (GpsPoint point : results) {
                        Location locationNxt = new Location("");
                        locationNxt.setLatitude(point.getLatitude());
                        locationNxt.setLongitude(point.getLongitude());
                        // Handle possible pause flags.
                        if (point.getPauseFlag() == (byte)1) {
                            Bundle flags = new Bundle();
                            flags.putByte("pauseFlag", (byte)1);
                            locationNxt.setExtras(flags);
                        }
                        reconstructedPositions.add(locationNxt);

                        // Initialize intent to start new activity and put additional data in extras.
                        // Display data concerning workout in WorkoutDetailActivity.
                        Intent workoutDetailsIntent = new Intent(getContext(), WorkoutDetailActivity.class);
                        workoutDetailsIntent.putExtra("sportActivity", workouts.get(position).getSportActivity()); //Optional parameters
                        workoutDetailsIntent.putExtra("duration", Math.round(workouts.get(position).getDuration() * 1e-3));
                        workoutDetailsIntent.putExtra("distance", workouts.get(position).getDistance());
                        workoutDetailsIntent.putExtra("pace", workouts.get(position).getPaceAvg());
                        workoutDetailsIntent.putExtra("calories", workouts.get(position).getTotalCalories());
                        workoutDetailsIntent.putExtra("positions", reconstructedPositions);
                        workoutDetailsIntent.putExtra("workoutId", workouts.get(position).getId());
                        workoutDetailsIntent.putExtra("titleSet", true);
                        workoutDetailsIntent.putExtra("fromHistory", true);
                        getContext().startActivity(workoutDetailsIntent);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history, menu);  // Inflate options menu.
    }

}
