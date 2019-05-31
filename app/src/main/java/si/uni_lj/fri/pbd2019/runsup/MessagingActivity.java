package si.uni_lj.fri.pbd2019.runsup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.model.Message;
import si.uni_lj.fri.pbd2019.runsup.settings.SettingsActivity;
import si.uni_lj.fri.pbd2019.runsup.sync.CloudContentUpdatesFetchHelper;
import si.uni_lj.fri.pbd2019.runsup.sync.FriendsSearchHelper;

public class MessagingActivity extends AppCompatActivity {

    public static final String TAG = MessagingActivity.class.getSimpleName();

    private long idHere;
    private long idOther;
    private String profileImageUrl;
    private String userName;
    private String jwt;
    private ListView listViewMessages;
    private MessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Intent intent = getIntent();
        this.idHere = intent.getLongExtra("idHere", -1L);
        this.idOther = intent.getLongExtra("idOther", -1L);
        this.profileImageUrl = intent.getStringExtra("profileImageUrl");
        this.userName = intent.getStringExtra("userName");
        this.jwt = intent.getStringExtra("jwt");
    }

    @Override
    protected void onStart() {
        super.onStart();
        final FriendsSearchHelper fsh = new FriendsSearchHelper(Constant.BASE_CLOUD_URL);
        getIntent().getLongArrayExtra("friendId");
        this.listViewMessages = findViewById(R.id.listview_messages);

        fsh.fetchMessages(idHere, idOther, jwt, new CloudContentUpdatesFetchHelper.FetchMessagesResponse() {
            @Override
            public void response(ArrayList<Message> messages) {
                adapter = new MessagesAdapter(MessagingActivity.this, messages, idHere);
                listViewMessages.setAdapter(adapter);
                listViewMessages.setSelection(adapter.getCount() - 1);
            }
        });

        final EditText messageField = findViewById(R.id.edittext_send_message);
        FloatingActionButton fab = findViewById(R.id.fab_send_message);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fsh.sendMessage(idHere, idOther, messageField.getText().toString(), profileImageUrl, userName, jwt, new CloudContentUpdatesFetchHelper.SendMessageResponse() {
                    @Override
                    public void response(boolean result) {
                        if (result) {
                            fsh.fetchMessages(idHere, idOther, jwt, new CloudContentUpdatesFetchHelper.FetchMessagesResponse() {
                                @Override
                                public void response(final ArrayList<Message> messages) {
                                    MessagingActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.clear();
                                            adapter.addAll(messages);
                                            listViewMessages.setAdapter(adapter);
                                            listViewMessages.setSelection(adapter.getCount() - 1);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    // onCreateOptionsMenu: called when options menu is created
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.messages, menu);
        return true;
    }

    public interface GetDeleteConversationRequestResponse {
        void response(boolean result);
    }


    // onOptionsItemSelected: Handle action bar item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Get id of selected item.
        int id = item.getItemId();

        // Handle selection.
        if (id == R.id.messages_menuitem_settings) {
            Intent settingsActivityIntent = new Intent(MessagingActivity.this, SettingsActivity.class);
            MessagingActivity.this.startActivity(settingsActivityIntent);
        } else if (id == R.id.messages_menuitem_delete) {
            // Prompt user to confirm intention to delete activity.
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alerttitle_delete_workout)
                    .setMessage(R.string.alertmessage_delete_workout)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            FriendsSearchHelper fsh = new FriendsSearchHelper(Constant.BASE_CLOUD_URL);
                            fsh.deleteConversation(idHere, idOther, jwt, new GetDeleteConversationRequestResponse() {
                                @Override
                                public void response(final boolean result) {
                                    MessagingActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (result) {
                                                Toast.makeText(MessagingActivity.this, "Conversation Successfully Deleted",
                                                        Toast.LENGTH_LONG).show();
                                                adapter.clear();
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(MessagingActivity.this, "Error Deleting Conversation",
                                                        Toast.LENGTH_LONG).show();
                                            }
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
        return super.onOptionsItemSelected(item);
    }
}
