package si.uni_lj.fri.pbd2019.runsup;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

import si.uni_lj.fri.pbd2019.runsup.fragments.AboutFragment;
import si.uni_lj.fri.pbd2019.runsup.fragments.HistoryFragment;
import si.uni_lj.fri.pbd2019.runsup.fragments.StopwatchFragment;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;
import si.uni_lj.fri.pbd2019.runsup.settings.SettingsActivity;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // ### PROPERTIES ###
    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int FRAGMENT_STOPWATCH = 0;
    public static final int FRAGMENT_HISTORY = 1;
    public static final int FRAGMENT_ABOUT = 2;

    private Uri userImageUri;
    private String userFullName;

    private FragmentManager fragmentManager;
    public int currentFragment;

    private SharedPreferences preferences;

    public static final String STATE_PREF_NAME = "state";

    public static MainActivity mainActivity;

    private StopwatchFragment stopwatchFragment;
    private HistoryFragment historyFragment;
    private AboutFragment aboutFragment;

    private ImageView userImage;
    private TextView userName;

    private boolean accountDataSet;

    private DatabaseHelper dh;

    public User currentUser;

    // ### /PROPERTIES ###

    // onCreate: method called when the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("AHMAD", "ONCREATE");
        super.onCreate(savedInstanceState);

        // Initialize fragment instances.
        this.stopwatchFragment = new StopwatchFragment();
        this.aboutFragment = new AboutFragment();
        this.historyFragment = new HistoryFragment();

        // Save context.
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

        this.dh = new DatabaseHelper(this);

        // Set default fragment (StopwatchFragment).
        this.fragmentManager.beginTransaction().add(R.id.main_fragment_container, this.stopwatchFragment).commit();
        this.currentFragment = FRAGMENT_STOPWATCH;

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if necessary preferences values exist. If not, set defaults.
        if (!preferences.contains("unit") && !getIntent().hasExtra("unit")) {
            preferences.edit().putInt("unit", Constant.UNITS_KM).apply();
        } else if (!preferences.contains("unit") && getIntent().hasExtra("unit")) {
            preferences.edit().putInt("unit", getIntent().getIntExtra("unit", Constant.UNITS_KM)).apply();
        }

        if(!preferences.contains("location_permission")) {
            preferences.edit().putString("location_permission", "false").apply();
        }

        if(!preferences.contains("weight")) {
           preferences.edit().putInt("weight", Constant.DEFAULT_WEIGHT).apply();
        }

        if (!preferences.contains("age")) {
           preferences.edit().putInt("age", Constant.DEFAULT_AGE).apply();
        }
    }


    // onStart: method called when activity becomes visible to user.
    @Override
    protected void onStart() {
        Log.d("AHMAD", "ONSTART");
        super.onStart();

        // Check if user signed in.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Get user's photo url and user's full name.
            this.userImageUri = account.getPhotoUrl();
            this.userFullName = String.format("%s %s", account.getGivenName(), account.getFamilyName());
            this.preferences.edit().putBoolean("userSignedIn", true).apply();
            this.preferences.edit().putLong("userId", account.getId().hashCode()).apply();

            if (!this.accountDataSet && this.userImage != null && this.userName != null) {
                Glide
                        .with(MainActivity.this)
                        .load(this.userImageUri)
                        .centerCrop()
                        .override(150, 150)
                        .into(this.userImage);
                this.userImage.setImageURI(this.userImageUri);
                this.userName.setText(this.userFullName);
                this.accountDataSet = true;
            }

            // ### Set user ###

            // Check if user in database
            this.currentUser = null;
            User existingUser = null;
            try {
                List<User> res = dh.userDao()
                        .queryBuilder()
                        .where()
                        .eq("accId", account.getId().hashCode())
                        .query();

                if (res.size() > 0) {
                    existingUser = res.get(0);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            // If user not found in database, add to database.
            if (existingUser == null) {
                User newUser = new User(account.getId());
                try {
                    dh.userDao().create(newUser);
                    this.currentUser = newUser;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                this.currentUser = existingUser;
            }


        } else {
            if (this.userImage != null && this.userName != null) {
                this.userImage.setImageResource(R.mipmap.iconfinder_unknown2_628287);
                this.userName.setText(getString(R.string.all_unknownuser));
            }

            // Set user.
            this.currentUser = new User("anonymous");
        }

        if (getIntent().hasExtra("loadHistory")) {
            // load AboutFragment
            this.fragmentManager.beginTransaction().replace(R.id.main_fragment_container, this.historyFragment).addToBackStack(null).commit();
            currentFragment = FRAGMENT_HISTORY;
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
        getMenuInflater().inflate(R.menu.stopwatch_shared, menu);

        // user's profile image and full name.
        this.userImage = findViewById(R.id.menu_loggedInUserImage);
        this.userName = findViewById(R.id.menu_loggedInUserFullName);

        // If user logged in, set profile image and full name.
        if (this.userImageUri != null && this.userFullName != null && !accountDataSet)  {
            Glide
                    .with(MainActivity.this)
                    .load(userImageUri)
                    .centerCrop()
                    .override(150,150)
                    .into(userImage);
            userImage.setImageURI(this.userImageUri);
            userName.setText(this.userFullName);
            this.accountDataSet = true;
        }

        // Set on click listeners.
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start LoginActivity.
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                if (currentFragment == FRAGMENT_HISTORY) {
                    loginIntent.putExtra("fromHistory", true);
                }
                MainActivity.this.startActivity(loginIntent);
            }
        });
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start LoginActivity.
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                if (currentFragment == FRAGMENT_HISTORY) {
                    loginIntent.putExtra("fromHistory", true);
                }
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

        if (id == R.id.stopwatchfragment_menuitem_settings) {
            // Start SettingsActivity
            Intent settingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(settingsActivityIntent);
        } else if (id == R.id.stopwatchfragment_menuitem_sync) {
            // TODO
        } else if (id == R.id.historyfragment_menuitem_delete_history) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete History")
                    .setMessage("Are you sure you want to delete your local history? You cannot undo this action.")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                new DatabaseHelper(MainActivity.this).clearWorkoutTables();
                                fragmentManager.beginTransaction().detach(historyFragment).attach(historyFragment).commit();
                                currentFragment = FRAGMENT_HISTORY;
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, null)  // Do nothing if user selects cancel.
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
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
                this.fragmentManager.beginTransaction().replace(R.id.main_fragment_container, this.stopwatchFragment).addToBackStack(null).commit();
                this.currentFragment = FRAGMENT_STOPWATCH;
            }

        } else if (id == R.id.nav_history) {
            // load HistoryFragment
            if (currentFragment != FRAGMENT_HISTORY) {

                // load AboutFragment
                this.fragmentManager.beginTransaction().replace(R.id.main_fragment_container, this.historyFragment).addToBackStack(null).commit();
                currentFragment = FRAGMENT_HISTORY;
            }
        } else if (id == R.id.nav_settings) {
            // Start SettingsActivity.
            Intent settingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(settingsActivityIntent);
        } else if (id == R.id.nav_about) {
            // If current fragment not AboutFragment, set AboutFragment.
            if (currentFragment != FRAGMENT_ABOUT) {

                // load AboutFragment
                this.fragmentManager.beginTransaction().replace(R.id.main_fragment_container, this.aboutFragment).addToBackStack(null).commit();
                currentFragment = FRAGMENT_ABOUT;
            }
        }

        // Close drawer.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    // onKeyDown: override default action when user presses the back button
    // If on stopwatch fragment, close application.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && currentFragment == FRAGMENT_STOPWATCH) {
            finishAffinity();
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    // onPause: method called when activity paused.
    @Override
    public void onPause() {
        super.onPause();  // Call method of superclass.
    }

    @Override
    protected void onResume() {
        Log.d("AHMAD", "ONRESUME");
        super.onResume();
    }
}
