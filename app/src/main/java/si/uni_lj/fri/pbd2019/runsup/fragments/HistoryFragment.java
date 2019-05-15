package si.uni_lj.fri.pbd2019.runsup.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import si.uni_lj.fri.pbd2019.runsup.HistoryListAdapter;
import si.uni_lj.fri.pbd2019.runsup.R;
import si.uni_lj.fri.pbd2019.runsup.model.SyncLog;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;

import static android.content.Context.MODE_PRIVATE;
import static si.uni_lj.fri.pbd2019.runsup.settings.SettingsActivity.STATE_PREF_NAME;

public class HistoryFragment extends Fragment {

    // ### PROPERTIES ###

    private SharedPreferences preferences;

    // ### /PROPERTIES ###

    // onCreateView: method called when view is to be created.
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        setHasOptionsMenu(true);  // Fragment has an options menu.

        // Get shared preferences.
        this.preferences = getActivity().getSharedPreferences(STATE_PREF_NAME, MODE_PRIVATE);
        return inflater.inflate(R.layout.fragment_history, parent, false);
    }


    // onViewCreated: method called when view is created.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Construct the data source
        DatabaseHelper dh = new DatabaseHelper(getContext());
        ArrayList<Workout> workouts = new ArrayList<Workout>();
        try {
            Log.d("HEREIAM", "HERE");
            Dao<Workout, Long> workoutDao = dh.workoutDao();
            Log.d("BUR", Long.toString(workoutDao.countOf()));
            Iterator it = workoutDao.iterator();
            while(it.hasNext()) {
                Log.d("HEREIAM", "HERE2");
                workouts.add((Workout) it.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create the adapter to convert the array to views
        HistoryListAdapter adapter = new HistoryListAdapter(getContext(), workouts);
        // Attach the adapter to a ListView
        ListView listView = getActivity().findViewById(R.id.listview_history_workouts);
        listView.setAdapter(adapter);
    }

    // onCreateOptionsMenu: called when options menu created.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history, menu);  // Inflate options menu.

        // If user not signed in, hide synchronization option in menu.
        if (!preferences.getBoolean("userSignedIn", false)) {
            MenuItem menuItem = menu.findItem(R.id.stopwatchfragment_menuitem_sync);
            menuItem.setVisible(false);
        }
    }
}
