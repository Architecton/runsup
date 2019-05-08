package si.uni_lj.fri.pbd2019.runsup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {


    // ### PROPERTIES ###

    public static final String TAG  = LoginActivity.class.getSimpleName();
    private SharedPreferences preferences;                                  // shared preferences
    public static final String STATE_PREF_NAME = "state";                   // name of preferences state
    private long userId;                                                    // user's id
    private GoogleSignInClient mGoogleSignInClient;                         // Google sign in client
    static final int RC_SIGN_IN = 1;                                        // request code for intent

    // ### /PROPERTIES ###


    // onCreate: called when activity created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);  // Set content view.
        this.preferences = getSharedPreferences(STATE_PREF_NAME, MODE_PRIVATE);  // Get shared preferences.

        // Check if user logged in.
        if (preferences.getBoolean("userSignedIn", false)) {
            // User is logged in.

            // Get id of logged in user.
            this.userId = preferences.getLong("userId", -1);

            // Render layout of logged in user.

        } else {

            // User is not logged in.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });
        }

    }


    // onActivityResult: after the called activity finishes, the system calls your activity's onActivityResult() method.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);  // Call method of superclass.

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // handleSignInResult: method called with results of sign in.
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);  // Get account from results.
            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }


    // onStart: method called when the activity is started.
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.d(TAG, "Last signed in account found");
            updateUI(account);
        }
    }


    // updateUI: update user interface depending on whether user is signed in
    private void updateUI(GoogleSignInAccount account) {
        // TODO
    }
}
