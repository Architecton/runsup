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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import si.uni_lj.fri.pbd2019.runsup.sync.CloudContentUpdatesFetchHelper;
import si.uni_lj.fri.pbd2019.runsup.sync.CloudSyncHelper;
import si.uni_lj.fri.pbd2019.runsup.sync.FriendsSearchHelper;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // ### PROPERTIES ###
    public static final String TAG = MainActivity.class.getSimpleName();  // TAG

    // codes used to indicate current fragment
    public static final int FRAGMENT_STOPWATCH = 0;
    public static final int FRAGMENT_HISTORY = 1;
    public static final int FRAGMENT_ABOUT = 2;

    // user image uri and user's full name
    public Uri userImageUri;
    public String userFullName;

    // fragment manager instance
    private FragmentManager fragmentManager;

    // drawer layout of the MainActivity
    DrawerLayout mDrawerLayout;

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


    NavigationView navView;
    private DrawerLayout.DrawerListener drawerListener;


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

        // Check if user signed in
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        // If user signed in, set account data.
        // Else add data for unknown user.
        if (account != null) {
            this.userFullName = account.getDisplayName();
            // this.userFullName ="tralala";
            if (account.getPhotoUrl() != null) {
                this.userImageUri = account.getPhotoUrl();
            } else {
                this.userImageUri = Uri.parse("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRZ594qCi5NafFyv4R69-Fpq_8IbbVQ1RtL208R8uM-RErUEUNp");
            }
        } else {
            this.userImageUri = Uri.parse("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRZ594qCi5NafFyv4R69-Fpq_8IbbVQ1RtL208R8uM-RErUEUNp");
            this.userFullName = getString(R.string.all_unknownuser);
        }


        // Set onOpen listeners for drawer.
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.removeDrawerListener(this.drawerListener);
        this.drawerListener = new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerOpened(View drawerView) {
                // Find user's profile image and full name in drawer.
                ImageView imageViewUserImage = findViewById(R.id.menu_loggedInUserImage);
                TextView textViewUserFullName = findViewById(R.id.menu_loggedInUserFullName);

                // Load profile image and set full name.
                Glide
                        .with(MainActivity.this)
                        .load(userImageUri)
                        .centerCrop()
                        .override(150, 150)
                        .into(imageViewUserImage);
                textViewUserFullName.setText(userFullName);

                // Set on click listeners.
                imageViewUserImage.setOnClickListener(new View.OnClickListener() {
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
                textViewUserFullName.setOnClickListener(new View.OnClickListener() {
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
            }
            @Override
            public void onDrawerClosed(View drawerView) {}
            @Override
            public void onDrawerStateChanged(int newState) {}
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}
        };

        // Set listener.
        this.mDrawerLayout.addDrawerListener(this.drawerListener);

        // Get navigation view.
        this.navView = findViewById(R.id.nav_view);


        // Check if user signed in.
        if (account != null) {

            // Set shared preferences' values.
            this.preferences.edit().putBoolean("userSignedIn", true).apply();
            this.preferences.edit().putLong("userId", account.getId().hashCode()).apply();

            // Check if user in database.
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

            // Remove drawer menu items that should only be visible to signed in users.
            this.navView.getMenu().findItem(R.id.nav_friends).setVisible(false);
            this.navView.getMenu().findItem(R.id.nav_shared_workouts).setVisible(false);
            this.navView.getMenu().findItem(R.id.nav_messaging).setVisible(false);
            this.currentUser = new User("anonymous");
        }

        // If intent has extra that specifies history fragment to be loaded, load it.
        if (getIntent().hasExtra("loadHistory")) {
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
        return true;
    }

    private void notifySyncResults(final boolean success) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    // Notify user of successful synchronization with the cloud.
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.alerttitle_sync_success)
                            .setMessage(R.string.alertmessage_sync_success)
                            .setPositiveButton(R.string.yes, null)
                            .setIcon(R.drawable.checked)
                            .show();
                } else {
                    // Notify user of unsuccessful synchronization with the cloud.
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.alerttitle_sync_fail)
                            .setMessage(R.string.alertmessage_sync_fail)
                            .setPositiveButton(R.string.yes, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });
    }


    // onOptionsItemSelected: Handle action bar item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Get id of clicked item.
        final int id = item.getItemId();

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
                csh.syncWithCloud(Constant.BASE_CLOUD_URL, currentUser, this.userFullName, this.userImageUri.toString(), new SyncCompleted() {
                    @Override
                    public void syncCompleted(boolean success) {
                        if (success) {
                            // If currently on history fragment, refresh it.
                            if (currentFragment == FRAGMENT_HISTORY) {
                                fragmentManager.beginTransaction().detach(historyFragment).attach(historyFragment).commit();
                                currentFragment = FRAGMENT_HISTORY;
                            }
                            notifySyncResults(true);
                        } else {
                            notifySyncResults(false);
                        }
                    }
                });
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

        } else if (id == R.id.nav_friends) {
            // Start FriendsActivity.
            Intent friendsActivityIntent = new Intent(MainActivity.this, FriendsActivity.class);
            friendsActivityIntent.putExtra("currentUser", this.currentUser);
            friendsActivityIntent.putExtra("name", this.userFullName);
            friendsActivityIntent.putExtra("profileImageUrl", this.userImageUri.toString());
            MainActivity.this.startActivity(friendsActivityIntent);
        } else if (id == R.id.nav_messaging) {
            final FriendsSearchHelper fsh = new FriendsSearchHelper(Constant.BASE_CLOUD_URL);
            if (!preferences.contains("jwt")) {
                fsh.getJwt(currentUser.getId(), userFullName, userImageUri.toString(), new FriendsActivity.GetJwtRequestResponse() {
                    @Override
                    public void response(final String jwt) {
                        fsh.getFriendLastMessageId(currentUser.getId(), jwt, new CloudContentUpdatesFetchHelper.GetLastMessageIdRequestResponse() {
                            @Override
                            public void response(Long id) {
                                if (id != -1) {
                                    Intent messagingActivityIntent = new Intent(MainActivity.this, MessagingActivity.class);
                                    messagingActivityIntent.putExtra("idHere", currentUser.getId());
                                    messagingActivityIntent.putExtra("idOther", id);
                                    messagingActivityIntent.putExtra("profileImageUrl", userImageUri.toString());
                                    messagingActivityIntent.putExtra("userName", userFullName);
                                    messagingActivityIntent.putExtra("jwt", jwt);
                                    MainActivity.this.startActivity(messagingActivityIntent);
                                } else {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, R.string.messaging_no_friends_toast, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            } else {
                fsh.getFriendLastMessageId(currentUser.getId(), preferences.getString("jwt", ""), new CloudContentUpdatesFetchHelper.GetLastMessageIdRequestResponse() {
                    @Override
                    public void response(Long id) {
                        if (id != -1) {
                            Intent messagingActivityIntent = new Intent(MainActivity.this, MessagingActivity.class);
                            messagingActivityIntent.putExtra("idHere", currentUser.getId());
                            messagingActivityIntent.putExtra("idOther", id);
                            messagingActivityIntent.putExtra("profileImageUrl", userImageUri);
                            messagingActivityIntent.putExtra("userName", userFullName);
                            messagingActivityIntent.putExtra("jwt", preferences.getString("jwt", ""));
                            MainActivity.this.startActivity(messagingActivityIntent);
                        } else {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, R.string.messaging_no_friends_toast, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }
        } else if (id == R.id.nav_shared_workouts) {
            Toast.makeText(MainActivity.this, R.string.future_impl, Toast.LENGTH_LONG).show();
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

    public interface SyncCompleted {
        void syncCompleted(boolean success);
    }
}
