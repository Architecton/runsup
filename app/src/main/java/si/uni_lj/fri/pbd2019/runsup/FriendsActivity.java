package si.uni_lj.fri.pbd2019.runsup;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.model.Friend;
import si.uni_lj.fri.pbd2019.runsup.model.FriendRequest;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.sync.FriendsSearchHelper;

public class FriendsActivity extends AppCompatActivity {

    FriendsSearchHelper fsh;
    User currentUser;
    String currentUserName;
    String currentUserProfileImageUrl;
    Context ctx;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        this.currentUser = (User) getIntent().getSerializableExtra("currentUser");
        this.currentUserName = getIntent().getStringExtra("name");
        this.currentUserProfileImageUrl = getIntent().getStringExtra("profileImageUrl");

        this.ctx = this;
        this.fsh = new FriendsSearchHelper(Constant.BASE_CLOUD_URL);

        this.pref = PreferenceManager.getDefaultSharedPreferences(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start FriendsSearchActivity.
                Intent friendsSearchActivityIntent = new Intent(FriendsActivity.this, FriendsSearchActivity.class);
                friendsSearchActivityIntent.putExtra("currentUser", currentUser);
                friendsSearchActivityIntent.putExtra("name", currentUserName);
                friendsSearchActivityIntent.putExtra("profileImageUrl", currentUserProfileImageUrl);
                FriendsActivity.this.startActivity(friendsSearchActivityIntent);
            }
        });
    }

    public interface GetFetchFriendsRequestResponse {
        void response(ArrayList<FriendRequest> res);
    }

    public interface GetFetchFriendsResponse {
        void response(ArrayList<Friend> res);
    }

    public interface GetJwtRequestResponse {
        void response(String jwt);
    }

    public interface GetAcceptFriendRequestResponse {
        void response(boolean res);
    }

    public interface GetSendMessageRequestResponse {
        void response(boolean res);
    }

    public interface GetUnfriendRequestResponse {
        void response(boolean res);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ListView listViewRequests = findViewById(R.id.listview_friendrequests);
        final ListView listViewFriends = findViewById(R.id.listview_friends);
        if (!pref.contains("jwt")) {
            fsh.getJwt(currentUser.getId(), currentUserName, currentUserProfileImageUrl, new GetJwtRequestResponse() {
                @Override
                public void response(final String jwt) {
                    // Create the adapter to convert the array to views and attach to a ListView instance.
                    fsh.fetchFriendRequests(currentUser.getId(), jwt, new GetFetchFriendsRequestResponse() {
                        @Override
                        public void response(final ArrayList<FriendRequest> res) {
                            final FriendRequestsAdapter adapter = new FriendRequestsAdapter(FriendsActivity.this, res);
                            // Set up click listener for adapter items.
                            listViewRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle(R.string.alerttitle_respond_friendrequest);
                                    builder.setMessage(R.string.alertmessage_respond_friendrequest);
                                    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            fsh.acceptFriendRequest(currentUser.getId(), res.get(position).getIdUser(), jwt, new GetAcceptFriendRequestResponse() {
                                                @Override
                                                public void response(boolean res) {
                                                    if (res) {
                                                        FriendsActivity.this.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                AlertDialog alertDialog = new AlertDialog.Builder(FriendsActivity.this).create();
                                                                alertDialog.setTitle(getString(R.string.alerttitle_friendrequest_success));
                                                                alertDialog.setMessage(getString(R.string.alertmessage_friendrequest_success));
                                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                                        new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                dialog.dismiss();
                                                                                finish();
                                                                                startActivity(getIntent());
                                                                            }
                                                                        });
                                                                alertDialog.show();
                                                            }
                                                        });
                                                    } else {
                                                        FriendsActivity.this.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                AlertDialog alertDialog = new AlertDialog.Builder(FriendsActivity.this).create();
                                                                alertDialog.setTitle(getString(R.string.alerttitle_friendrequest_error));
                                                                alertDialog.setMessage(getString(R.string.alertmessage_friendrequest_message));
                                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                                        new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                dialog.dismiss();
                                                                                finish();
                                                                                startActivity(getIntent());
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

                            FriendsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listViewRequests.setAdapter(adapter);
                                }
                            });
                        }
                    });

                }
            });
        } else {
            // Create the adapter to convert the array to views and attach to a ListView instance.
            fsh.fetchFriendRequests(currentUser.getId(), pref.getString("jwt", ""), new GetFetchFriendsRequestResponse() {
                @Override
                public void response(final ArrayList<FriendRequest> res) {
                    final FriendRequestsAdapter adapter = new FriendRequestsAdapter(FriendsActivity.this, res);

                    // Set up click listener for adapter items.
                    listViewRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                            builder.setTitle(R.string.dialogtitle_friend_request);
                            builder.setMessage(R.string.dialogmessage_friend_request);
                            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fsh.acceptFriendRequest(currentUser.getId(), res.get(position).getIdUser(), pref.getString("jwt", ""), new GetAcceptFriendRequestResponse() {
                                        @Override
                                        public void response(boolean res) {
                                            if (res) {
                                                FriendsActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AlertDialog alertDialog = new AlertDialog.Builder(FriendsActivity.this).create();
                                                        alertDialog.setTitle(getString(R.string.alerttitle_friendrequest_success));
                                                        alertDialog.setMessage(getString(R.string.alertmessage_friendrequest_success));
                                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                        finish();
                                                                        startActivity(getIntent());
                                                                    }
                                                                });
                                                        alertDialog.show();
                                                    }
                                                });
                                            } else {
                                                FriendsActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AlertDialog alertDialog = new AlertDialog.Builder(FriendsActivity.this).create();
                                                        alertDialog.setTitle(getString(R.string.alerttitle_friendrequest_error));
                                                        alertDialog.setMessage(getString(R.string.alertmessage_friendrequest_message));
                                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                        finish();
                                                                        startActivity(getIntent());
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

                    FriendsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listViewRequests.setAdapter(adapter);
                        }
                    });
                }
            });
        }


        if (!pref.contains("jwt")) {
            fsh.getJwt(currentUser.getId(), currentUserName, currentUserProfileImageUrl, new GetJwtRequestResponse() {
                @Override
                public void response(final String jwt) {
                    fsh.fetchFriends(currentUser.getId(), jwt, new GetFetchFriendsResponse() {
                        @Override
                        public void response(final ArrayList<Friend> res) {
                            final FriendsAdapter adapter = new FriendsAdapter(FriendsActivity.this, res);
                            FriendsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listViewFriends.setAdapter(adapter);
                                }
                            });
                            listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle(res.get(position).getName());
                                    builder.setIcon(R.drawable.bicycle);
                                    builder.setMessage(getString(R.string.alertmessage_sendclick_message));
                                    builder.setPositiveButton(getString(R.string.alertoption_send), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            AlertDialog.Builder alert = new AlertDialog.Builder(FriendsActivity.this);
                                            final EditText edittext = new EditText(FriendsActivity.this);
                                            alert.setMessage(getString(R.string.alertmessage_sendmessage));
                                            alert.setTitle(getString(R.string.alerttitle_sendmessage));
                                            alert.setView(edittext);
                                            alert.setPositiveButton(getString(R.string.send), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    String content = edittext.getText().toString();
                                                    fsh.sendMessage(currentUser.getId(), res.get(position).getFriendUserId(), jwt, content, new GetSendMessageRequestResponse() {
                                                        @Override
                                                        public void response(boolean res) {
                                                            // TODO
                                                        }
                                                    });
                                                }
                                            });

                                            alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    // what ever you want to do with No option.
                                                }
                                            });

                                            alert.show();
                                        }
                                    });
                                    builder.setNegativeButton(getString(R.string.alrertbutton_unfriend), new DialogInterface.OnClickListener() {
                                        @Override public void onClick(DialogInterface dialog, int which) {

                                            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);

                                            builder.setTitle(getString(R.string.confirm));
                                            builder.setMessage(getString(R.string.confirm_unfriend));

                                            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                                                public void onClick(DialogInterface dialog, int which) {
                                                    fsh.unfriend(currentUser.getId(), res.get(position).getFriendUserId(), jwt, new GetUnfriendRequestResponse() {
                                                        @Override
                                                        public void response(boolean res) {
                                                            if (res) {
                                                                FriendsActivity.this.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        AlertDialog alertDialog = new AlertDialog.Builder(FriendsActivity.this).create();
                                                                        alertDialog.setTitle(getString(R.string.success));
                                                                        alertDialog.setMessage(getString(R.string.alertmessage_unfriend));
                                                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                                                new DialogInterface.OnClickListener() {
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        adapter.clear();
                                                                                        adapter.notifyDataSetChanged();
                                                                                        dialog.dismiss();
                                                                                    }
                                                                                });
                                                                        alertDialog.show();
                                                                    }
                                                                });
                                                            } else {
                                                                FriendsActivity.this.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        AlertDialog alertDialog = new AlertDialog.Builder(FriendsActivity.this).create();
                                                                        alertDialog.setTitle(getString(R.string.error));
                                                                        alertDialog.setMessage(getString(R.string.alertmessage_unfriend_error));
                                                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                                                new DialogInterface.OnClickListener() {
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        adapter.clear();
                                                                                        adapter.notifyDataSetChanged();
                                                                                        dialog.dismiss();
                                                                                    }
                                                                                });
                                                                        alertDialog.show();
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });

                                                    dialog.dismiss();
                                                }
                                            });
                                            builder.setNegativeButton(getString(R.string.cancel), null);
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                        }
                                    });
                                    builder.create().show(); // Create the Dialog and display it to the user
                                }
                            });
                        }
                    });
                }
            });
        } else {
            fsh.fetchFriends(currentUser.getId(), pref.getString("jwt", ""), new GetFetchFriendsResponse() {
                @Override
                public void response(final ArrayList<Friend> res) {
                    final FriendsAdapter adapter = new FriendsAdapter(FriendsActivity.this, res);
                    FriendsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listViewFriends.setAdapter(adapter);
                        }
                    });
                    listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                            builder.setTitle(res.get(position).getName());
                            builder.setIcon(R.drawable.bicycle);
                            builder.setMessage(R.string.alertmessage_sendclick_message);
                            builder.setPositiveButton(R.string.alertoption_send, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(FriendsActivity.this);
                                    final EditText edittext = new EditText(FriendsActivity.this);
                                    alert.setTitle(R.string.alerttitle_sendmessage);
                                    alert.setMessage(R.string.alertmessage_sendmessage);
                                    alert.setView(edittext);
                                    alert.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            String content = edittext.getText().toString();
                                            fsh.sendMessage(currentUser.getId(), res.get(position).getId(), pref.getString("jwt", ""), content, new GetSendMessageRequestResponse() {
                                                @Override
                                                public void response(boolean res) {
                                                    // TODO
                                                }
                                            });
                                        }
                                    });
                                    alert.setNegativeButton(getString(R.string.no), null);
                                    alert.show();
                                }
                            });
                            builder.setNegativeButton(R.string.alrertbutton_unfriend, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);

                                    builder.setTitle(R.string.confirm);
                                    builder.setMessage(R.string.confirm_unfriend);

                                    builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int which) {
                                            fsh.unfriend(currentUser.getId(), res.get(position).getFriendUserId(), pref.getString("jwt", ""), new GetUnfriendRequestResponse() {
                                                @Override
                                                public void response(boolean res) {
                                                    if (res) {
                                                        FriendsActivity.this.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                AlertDialog alertDialog = new AlertDialog.Builder(FriendsActivity.this).create();
                                                                alertDialog.setTitle(getString(R.string.success));
                                                                alertDialog.setMessage(getString(R.string.alertmessage_unfriend));
                                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                                        new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                adapter.clear();
                                                                                adapter.notifyDataSetChanged();
                                                                                dialog.dismiss();
                                                                            }
                                                                        });
                                                                alertDialog.show();
                                                            }
                                                        });
                                                    } else {
                                                        FriendsActivity.this.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                AlertDialog alertDialog = new AlertDialog.Builder(FriendsActivity.this).create();
                                                                alertDialog.setTitle(getString(R.string.error));
                                                                alertDialog.setMessage(getString(R.string.alertmessage_unfriend_error));
                                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.yes),
                                                                        new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                adapter.clear();
                                                                                adapter.notifyDataSetChanged();
                                                                                dialog.dismiss();
                                                                            }
                                                                        });
                                                                alertDialog.show();
                                                            }
                                                        });
                                                    }
                                                }
                                            });

                                            dialog.dismiss();
                                        }
                                    });
                                    builder.setNegativeButton(getString(R.string.cancel), null);
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            });
                            builder.create().show();
                        }
                    });
                }
            });

        }
    }

}
