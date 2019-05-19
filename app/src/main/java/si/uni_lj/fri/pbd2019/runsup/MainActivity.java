package si.uni_lj.fri.pbd2019.runsup;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import si.uni_lj.fri.pbd2019.runsup.model.UserProfile;
import si.uni_lj.fri.pbd2019.runsup.model.config.DatabaseHelper;
import si.uni_lj.fri.pbd2019.runsup.settings.SettingsActivity;
import si.uni_lj.fri.pbd2019.runsup.sync.CloudSyncHelper;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // ### PROPERTIES ###
    public static final String TAG = MainActivity.class.getSimpleName();  // TAG

    // codes used to indicate current fragment
    public static final int FRAGMENT_STOPWATCH = 0;
    public static final int FRAGMENT_HISTORY = 1;
    public static final int FRAGMENT_ABOUT = 2;

    // user image uri and user's full name
    private Uri userImageUri;
    private String userFullName;

    // fragment manager instance
    private FragmentManager fragmentManager;

    // variable that holds the current set fragment.
    public int currentFragment;

    // shared preferences instance.
    private SharedPreferences preferences;

    // Context of this activity.
    public static MainActivity mainActivity;

    // fragments
    private StopwatchFragment stopwatchFragment;
    private HistoryFragment historyFragment;
    private AboutFragment aboutFragment;

    // User's image and user's name UI components.
    private ImageView userImage;
    private TextView userName;

    // variable that indicates whether the account data is set.
    private boolean accountDataSet;

    // DatabaseHelper instance.
    private DatabaseHelper dh;

    // Current user using the application.
    public User currentUser;

    // ### /PROPERTIES ###

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Initialize DatabaseHelper instance.
        this.dh = new DatabaseHelper(this);

        // Set default fragment (StopwatchFragment).
        this.fragmentManager.beginTransaction().add(R.id.main_fragment_container, this.stopwatchFragment).commit();
        this.currentFragment = FRAGMENT_STOPWATCH;

        // Get shared preferences.
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


    @Override
    protected void onStart() {
        super.onStart();

        // Check if user signed in.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {

            // Get user's photo url and user's full name.
            this.userImageUri = account.getPhotoUrl();
            this.userFullName = String.format("%s %s", account.getGivenName(), account.getFamilyName());
            this.preferences.edit().putBoolean("userSignedIn", true).apply();
            this.preferences.edit().putLong("userId", account.getId().hashCode()).apply();

            // Set user's avatar.
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

                // Create new user profile.
                UserProfile newUserProfile =
                        new UserProfile(newUser, preferences.getInt("weight",
                                Constant.DEFAULT_WEIGHT),
                                preferences.getInt("age", Constant.DEFAULT_AGE));
                newUserProfile.setId(account.getId().hashCode());
                try {
                    dh.userDao().create(newUser);
                    dh.userProfileDao().create(newUserProfile);
                    this.currentUser = newUser;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                this.currentUser = existingUser;

                // Get weight and age from current user.
                try {
                    List<UserProfile> res = dh.userProfileDao().queryBuilder().where().eq("id", this.currentUser.getAccId()).query();
                    if (res != null && res.size() > 0) {
                        // TODO
                        // preferences.edit().putInt("age", res.get(0).getAge()).apply();
                        // preferences.edit().putInt("weight", res.get(0).getWeight()).apply();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (this.userImage != null && this.userName != null) {
                this.userImage.setImageResource(R.mipmap.iconfinder_unknown2_628287);
                this.userName.setText(getString(R.string.all_unknownuser));
            }


            // Set user.
            this.currentUser = new User("anonymous");
        }

        // If intent has extra that specifies history fragment to be loaded, load it.
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // If user not logged int, hide cloud sync option. Else display it.
        if (currentUser != null && currentUser.getAccId() == "anonymous".hashCode()) {
            menu.findItem(R.id.stopwatchfragment_menuitem_sync).setVisible(false);
        } else if (currentUser != null && !menu.findItem(R.id.stopwatchfragment_menuitem_sync).isVisible()) {
            menu.findItem(R.id.stopwatchfragment_menuitem_sync).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

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

        // If user selected settings option...
        if (id == R.id.stopwatchfragment_menuitem_settings) {

            // Start SettingsActivity
            Intent settingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(settingsActivityIntent);

            // If user selected cloud sync option
        } else if (id == R.id.stopwatchfragment_menuitem_sync) {
            CloudSyncHelper csh = new CloudSyncHelper(Constant.BASE_CLOUD_URL);
            try {

                // Synchronize with cloud.
                csh.syncWithCloud(Constant.BASE_CLOUD_URL, currentUser);

                // If on history fragment, refresh fragment after 5 seconds.
                if (this.currentFragment == FRAGMENT_HISTORY) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fragmentManager.beginTransaction().detach(historyFragment).attach(historyFragment).commit();
                            currentFragment = FRAGMENT_HISTORY;
                        }
                    }, 5000);

                }

                // Notify user.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.alerttitle_sync)
                        .setMessage(R.string.alertmessage_sync)
                        .setPositiveButton(R.string.yes, null)
                        .setIcon(R.drawable.checked)
                        .show();
            } catch (SQLException e) {
                e.printStackTrace();
                new AlertDialog.Builder(this)
                        .setTitle(R.string.alerttitle_sync_error)
                        .setMessage(R.string.alertmessage_sync_error)
                        .setPositiveButton(R.string.yes, null)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }

            // If user chose to delete history.
        } else if (id == R.id.historyfragment_menuitem_delete_history) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alerttitle_delete_history)
                    .setMessage(R.string.alertmessage_delete_history)
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

            // If user chose to refresh fragment.
        } else if (id == R.id.historyfragment_menuitem_refresh) {
            // load AboutFragment
            this.fragmentManager.beginTransaction().detach(this.historyFragment).attach(this.historyFragment).commit();
            currentFragment = FRAGMENT_HISTORY;
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
        // If user chose to go to history fragment.
        } else if (id == R.id.nav_history) {
            // load HistoryFragment
            if (currentFragment != FRAGMENT_HISTORY) {

                // load AboutFragment
                this.fragmentManager.beginTransaction().replace(R.id.main_fragment_container, this.historyFragment).addToBackStack(null).commit();
                currentFragment = FRAGMENT_HISTORY;
            }

            // If user chose to go to settings.
        } else if (id == R.id.nav_settings) {
            // Start SettingsActivity.
            Intent settingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(settingsActivityIntent);

            // If user chose to load about fragment.
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

    // ## FOR TESTING ##
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
