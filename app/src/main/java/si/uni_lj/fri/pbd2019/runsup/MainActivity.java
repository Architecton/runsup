package si.uni_lj.fri.pbd2019.runsup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.jetbrains.annotations.NotNull;

import si.uni_lj.fri.pbd2019.runsup.fragments.StopwatchFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // ### PROPERTIES ###
    public static final String TAG = MainActivity.class.getSimpleName();
    private ImageView userImage;
    private TextView userName;
    private Uri userImageUri;
    private String userFullName;

    private FragmentManager fragmentManager;
    // ### /PROPERTIES ###


    // onCreate: method called when the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        // Initialize fragmentManager instance.
        this.fragmentManager = getSupportFragmentManager();

        // Set default fragment (StopwatchFragment).
        StopwatchFragment fragment = new StopwatchFragment();
        this.fragmentManager.beginTransaction().add(R.id.main_fragment_container, fragment).commit();


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
        this.userImage = findViewById(R.id.menu_loggedInUserImage);
        this.userName = findViewById(R.id.menu_loggedInUserFullName);

        // If user logged in, set profile image and full name.
        if (this.userImageUri != null && this.userFullName != null)  {
            this.userImage.setImageURI(this.userImageUri);
            this.userName.setText(this.userFullName);
        }

        // Set on click listeners.
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Profile image clicked.");
                // Start LoginActivity.
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(loginIntent);
            }
        });
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "User's name clicked.");
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

        // If user clicked on settings...
        if (id == R.id.action_settings) {
            return true;
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
            Log.d(TAG, "workout menu item selected.");
            // load StopwatchFragment
        } else if (id == R.id.nav_history) {
            Log.d(TAG, "history menu item selected.");
            // load HistoryFragment
        } else if (id == R.id.nav_settings) {
            Log.d(TAG, "settings menu item selected.");
            // open SettingsActivity
        } else if (id == R.id.nav_about) {
            Log.d(TAG, "about menu item selected.");
            // load AboutFragment
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
