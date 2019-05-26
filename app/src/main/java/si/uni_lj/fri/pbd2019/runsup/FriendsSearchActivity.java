package si.uni_lj.fri.pbd2019.runsup;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import si.uni_lj.fri.pbd2019.runsup.model.Friend;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.sync.FriendsSearchHelper;

public class FriendsSearchActivity extends AppCompatActivity {


    FriendsSearchHelper fsh;
    User currentUser;
    String currentUserName;
    String currentUserProfileImageUrl;
    ArrayList<Friend> searchRes;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_search);
        this.fsh = new FriendsSearchHelper(Constant.BASE_CLOUD_URL);
        this.currentUser = (User) getIntent().getSerializableExtra("currentUser");
        this.currentUserName = getIntent().getStringExtra("name");
        this.currentUserProfileImageUrl = getIntent().getStringExtra("profileImageUrl");
        this.pref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final EditText editText = findViewById(R.id.friendssearch_edittext_search);
        final ListView listView = findViewById(R.id.listview_friendssearch);
        editText.addTextChangedListener(
                new TextWatcher() {
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    private Timer timer = new Timer();
                    private final long DELAY = 300; // milliseconds

                    @Override
                    public void afterTextChanged(final Editable s) {
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        fsh.searchForFriends(s.toString(), currentUser, currentUserName, currentUserProfileImageUrl, new GetFriendsSearchResponse() {
                                            @Override
                                            public void getSearchResultsList(final ArrayList<Friend> searchResults) {
                                                searchRes = searchResults;
                                                if (searchResults != null) {
                                                    // Remove current user from search results.
                                                    for (int i = 0; i < searchResults.size(); i++) {
                                                        if (searchResults.get(i).getFriendUserId() == currentUser.getId()) {
                                                            searchResults.remove(i);
                                                        }
                                                    }
                                                    FriendsSearchActivity.this.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            // Create the adapter to convert the array to views and attach to a ListView instance.
                                                            FriendsSearchResultsAdapter adapter = new FriendsSearchResultsAdapter(FriendsSearchActivity.this, searchResults);
                                                            listView.setAdapter(adapter);
                                                            if (searchResults.size() == 0) {
                                                                findViewById(R.id.textView_friendssearch_no_results).setVisibility(View.VISIBLE);
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    FriendsSearchActivity.this.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            findViewById(R.id.textView_friendssearch_no_results).setVisibility(View.VISIBLE);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                },
                                DELAY
                        );
                    }
                }
        );

        // Set up click listener for adapter items.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsSearchActivity.this);
                builder.setTitle(R.string.dialogtitle_friend_request);
                builder.setMessage(R.string.dialogmessage_friend_request);
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fsh.sendFriendRequest(currentUser.getId(), searchRes.get(position).getFriendUserId(),
                                currentUserName, currentUserProfileImageUrl,
                                pref.getString("jwt", ""),
                                new GetSendFriendRequestResponse() {
                            @Override
                            public void getResponse(boolean success) {
                                if (success) {
                                    FriendsSearchActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog alertDialog = new AlertDialog.Builder(FriendsSearchActivity.this).create();
                                            alertDialog.setTitle(getString(R.string.alerttitle_friendrequest_success));
                                            alertDialog.setMessage(getString(R.string.alertmessage_message_success_sent));
                                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            alertDialog.show();
                                        }
                                    });
                                } else {
                                    FriendsSearchActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog alertDialog = new AlertDialog.Builder(FriendsSearchActivity.this).create();
                                            alertDialog.setTitle(getString(R.string.alerttitle_friendrequest_error));
                                            alertDialog.setMessage("Friend request already sent!");
                                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            alertDialog.show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });

                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });


    }

    public interface GetFriendsSearchResponse {
        void getSearchResultsList(ArrayList<Friend> searchResults);
    }

    public interface GetSendFriendRequestResponse {
        void getResponse(boolean success);
    }
}
