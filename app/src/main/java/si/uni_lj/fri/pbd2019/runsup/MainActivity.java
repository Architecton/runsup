package si.uni_lj.fri.pbd2019.runsup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.j256.ormlite.dao.Dao;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.fragments.AboutFragment;
import si.uni_lj.fri.pbd2019.runsup.fragments.HistoryFragment;
import si.uni_lj.fri.pbd2019.runsup.fragments.StopwatchFragment;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;
import si.uni_lj.fri.pbd2019.runsup.settings.SettingsActivity;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // ### PROPERTIES ###
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int FRAGMENT_STOPWATCH = 0;
    private static final int FRAGMENT_HISTORY = 1;
    private static final int FRAGMENT_ABOUT = 2;

    private Uri userImageUri;
    private String userFullName;

    private FragmentManager fragmentManager;
    private int currentFragment;

    public static final String STATE_PREF_NAME = "state";

    public static MainActivity mainActivity;

    // ### /PROPERTIES ###

    // onCreate: method called when the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = this;

        setContentView(R.layout.activity_main);  // Set layout content.
        Toolbar toolbar = findViewById(R.id.toolbar);  // Get toolbar.
        setSupportActionBar(toolbar);  // Get support action for toolbar.

        // Initialization of drawer.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Get navigation view instance.
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize fragmentManager instance.
        this.fragmentManager = getSupportFragmentManager();

        // Set default fragment (StopwatchFragment).
        StopwatchFragment fragment = new StopwatchFragment();
        this.fragmentManager.beginTransaction().add(R.id.main_fragment_container, fragment).commit();
        this.currentFragment = FRAGMENT_STOPWATCH;

        SharedPreferences preferences = getSharedPreferences(STATE_PREF_NAME, MODE_PRIVATE);

        // Check if necessary preferences values exist. If not, set defaults.
        if (!preferences.contains("pref_units_value")) {
            preferences.edit().putString("pref_units_value", "km").apply();
        }
        if(!preferences.contains("pref_location_access_value")) {
            preferences.edit().putString("pref_location_access_value", "false").apply();
        }
    }


    // onStart: method called when activity becomes visible to user.
    @Override
    protected void onStart() {
        super.onStart();

        // Check if user signed in.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {

            // Get user's photo url and user's full name.
            this.userImageUri = account.getPhotoUrl();
            this.userFullName = String.format("%s %s", account.getGivenName(), account.getFamilyName());
        }
    }

    // onBackPressed: method called when back button pressed.
    @Override
    public void onBackPressed() {

        // If drawer is open, close drawer.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    // onCreateOptionsMenu: method called when options menu created.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // user's profile image and full name.
        ImageView userImage = findViewById(R.id.menu_loggedInUserImage);
        TextView userName = findViewById(R.id.menu_loggedInUserFullName);

        // If user logged in, set profile image and full name.
        if (this.userImageUri != null && this.userFullName != null)  {
            Glide
                    .with(MainActivity.this)
                    .load(userImageUri)
                    .centerCrop()
                    .override(150,150)
                    .into(userImage);
            userImage.setImageURI(this.userImageUri);
            userName.setText(this.userFullName);
        }

        // Set on click listeners.
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start LoginActivity.
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(loginIntent);
            }
        });
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start LoginActivity.
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(loginIntent);
            }
        });
        return true;
    }


    // onOptionsItemSelected: Handle action bar item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Get id of clicked item.
        int id = item.getItemId();

        if (id == R.id.stopwatchfragment_menuitem_settings || id == R.id.historyfragment_menuitem_settings) {
            // Start SettingsActivity
            Intent settingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(settingsActivityIntent);
        } else if (id == R.id.stopwatchfragment_menuitem_sync) {
            // TODO
        } else if (id == R.id.historyfragment_menuitem_delete_history) {
            // TODO
        }
        return super.onOptionsItemSelected(item);
    }


    // onNavigationItemSelected: handle navigation view item clicks
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem item) {

        // Get id of selected item.
        int id = item.getItemId();

        // Switch on selected item's id.
        if (id == R.id.nav_workout) {

            // If current fragment not StopwatchFragment, set StopwatchFragment.
            if (currentFragment != FRAGMENT_STOPWATCH) {
                // load StopwatchFragment
                StopwatchFragment fragment = new StopwatchFragment();
                this.fragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).addToBackStack(null).commit();
                this.currentFragment = FRAGMENT_STOPWATCH;
            }

        } else if (id == R.id.nav_history) {
            Log.d(TAG, "history menu item selected.");
            // load HistoryFragment
            if (currentFragment != FRAGMENT_HISTORY) {

                // load AboutFragment
                HistoryFragment fragment = new HistoryFragment();
                this.fragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).addToBackStack(null).commit();
                currentFragment = FRAGMENT_HISTORY;
            }
        } else if (id == R.id.nav_settings) {
            Log.d(TAG, "settings menu item selected.");
            // Start SettingsActivity.
            Intent settingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(settingsActivityIntent);
        } else if (id == R.id.nav_about) {
            Log.d(TAG, "about menu item selected.");
            // If current fragment not AboutFragment, set AboutFragment.
            if (currentFragment != FRAGMENT_ABOUT) {

                // load AboutFragment
                AboutFragment fragment = new AboutFragment();
                this.fragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).addToBackStack(null).commit();
                currentFragment = FRAGMENT_ABOUT;
            }
        }

        // Close drawer.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // onPause: method called when activity paused.
    @Override
    public void onPause() {
        super.onPause();  // Call method of superclass.
    }
}
