package si.uni_lj.fri.pbd2019.runsup.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import si.uni_lj.fri.pbd2019.runsup.R;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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
    }

}
