package si.uni_lj.fri.pbd2019.runsup;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Field;

public class LoginActivity extends AppCompatActivity {


    // ### PROPERTIES ###

    public static final String TAG  = LoginActivity.class.getSimpleName();  // TAG
    private SharedPreferences preferences;                                  // shared preferences
    private GoogleSignInClient mGoogleSignInClient;                         // Google sign in client
    static final int RC_SIGN_IN = 1;                                        // request code for intent

    // ### /PROPERTIES ###


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);  // Set content view.
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);  // Get shared preferences.

        // User is not logged in.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Set Google sign in client.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check if user logged in.
        if (!preferences.getBoolean("userSignedIn", false)) {
            // If user signed in, add click listener for sign in button.
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
            account = GoogleSignIn.getLastSignedInAccount(this);
            this.preferences.edit().putBoolean("userSignedIn", true).apply();
            if (account != null && account.getId() != null) {
                this.preferences.edit().putLong("userId", account.getId().hashCode()).apply();
            }

            // Signed in successfully, show authenticated UI.
            updateUiLoggedIn(account);
        } catch (ApiException e) {

            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUiLoggedIn(null);
        }
    }

    // setNumberPickerTextColor: auxiliary method used to set the colors of the number picker.
    public static void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return;
                }
                catch(NoSuchFieldException e) {
                    Log.w(TAG, e);
                }
                catch(IllegalAccessException e) {
                    Log.w(TAG, e);
                }
                catch(IllegalArgumentException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Get Google account. If null, user is not signed in.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            updateUiLoggedIn(account);
        } else {
            this.preferences.edit().putBoolean("userSignedIn", false).apply();
            this.preferences.edit().remove("userId").apply();
        }

        // Get items from view.
        NumberPicker npWeight = findViewById(R.id.numberPicker_set_weight);
        NumberPicker npAge = findViewById(R.id.numberPicker_set_age);
        Button startButton = findViewById(R.id.login_start_button);
        TextView signedInConfirmationTextView = findViewById(R.id.textview_signed_in_confirmation);

        // Set NumberPicker instances text colors.
        setNumberPickerTextColor(npAge, Color.WHITE);
        setNumberPickerTextColor(npWeight, Color.WHITE);

        // Add values to NumberPicker instances.
        String[] weights = new String[500];
        for(int i = 0; i < weights.length; i++) {
            weights[i] = i + 1 + " kg";
        }
        String[] ages = new String[200];

        for(int i = 0; i < ages.length; i++) {
            ages[i] = Integer.toString(i + 1);
        }

        // Set NumberPicker instances values.
        npWeight.setMinValue(1);
        npWeight.setMaxValue(500);
        npWeight.setWrapSelectorWheel(false);
        npWeight.setDisplayedValues(weights);
        npWeight.setValue(preferences.getInt("weight", Constant.DEFAULT_WEIGHT));

        npAge.setMinValue(1);
        npAge.setMaxValue(200);
        npAge.setWrapSelectorWheel(false);
        npAge.setDisplayedValues(ages);
        npAge.setValue(preferences.getInt("age", Constant.DEFAULT_AGE));


        // Set value change listeners to NumberPicker instances.
        npWeight.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // Set selected value.
                preferences.edit().putInt("weight", newVal).apply();
            }
        });

        npAge.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // set selected value.
                preferences.edit().putInt("age", newVal).apply();
            }
        });

        // Set onClick listener that listens for clicks to start button.
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainActivity.
                Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(mainActivityIntent);
            }
        });

        // Set click listener for button that logs the user out.
        signedInConfirmationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Prompt user to confirm intention to log out.
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle(R.string.alterttitle_login_activity_signout)
                        .setMessage(R.string.alert_sign_out_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit().clear().apply();
                                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                        preferences.edit().putBoolean("userSignedIn", false).apply();
                                        updateUiLoggedIn(null);
                                        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                                                startActivityForResult(signInIntent, RC_SIGN_IN);
                                            }
                                        });
                                    }
                                });

                            }
                        })
                        .setNegativeButton(R.string.no, null)  // Do nothing if user selects cancel.
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }


    // onKeyDown: override default action when user presses the back button.
    // Present updated history if user came from history.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getIntent().hasExtra("fromHistory")) {
            Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
            mainActivityIntent.putExtra("loadHistory", true);
            LoginActivity.this.startActivity(mainActivityIntent);
            return true;
        } else {
            Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
            LoginActivity.this.startActivity(mainActivityIntent);
            return true;
        }
    }


    // updateUI: update user interface depending on whether user is signed in
    private void updateUiLoggedIn(GoogleSignInAccount account) {
        if (account != null) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.textview_signed_in_confirmation).setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.textview_signed_in_confirmation).setVisibility(View.GONE);
        }
    }
}
