package si.uni_lj.fri.pbd2019.runsup.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import si.uni_lj.fri.pbd2019.runsup.R;

import static android.content.Context.MODE_PRIVATE;
import static si.uni_lj.fri.pbd2019.runsup.MainActivity.STATE_PREF_NAME;

public class AboutFragment extends Fragment {

    private SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        this.preferences = getActivity().getSharedPreferences(STATE_PREF_NAME, MODE_PRIVATE);
        return inflater.inflate(R.layout.fragment_about, parent, false);
    }


    // oncCreate: method called when the activity is created
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    // onCreateOptionsMenu: called when options menu created.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.stopwatch_shared, menu);
        if (!preferences.getBoolean("userSignedIn", false)) {
            MenuItem menuItem = menu.findItem(R.id.stopwatchfragment_menuitem_sync);
            menuItem.setVisible(false);
        }
    }

}
