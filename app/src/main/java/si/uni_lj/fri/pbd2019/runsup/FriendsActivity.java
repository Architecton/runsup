package si.uni_lj.fri.pbd2019.runsup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.model.Friend;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.sync.FriendsSearchHelper;

public class FriendsActivity extends AppCompatActivity {

    FriendsSearchHelper fsh;
    User currentUser;
    String currentUserName;
    String currentUserProfileImageUrl;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        this.currentUser = (User) getIntent().getSerializableExtra("currentUser");
        this.currentUserName = getIntent().getStringExtra("name");
        this.currentUserProfileImageUrl = getIntent().getStringExtra("profileImageUrl");

        this.ctx = this;
        this.fsh = new FriendsSearchHelper(Constant.BASE_CLOUD_URL);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start FriendsActivity.
                Intent friendsSearchActivityIntent = new Intent(FriendsActivity.this, FriendsSearchActivity.class);
                friendsSearchActivityIntent.putExtra("currentUser", currentUser);
                friendsSearchActivityIntent.putExtra("name", currentUserName);
                friendsSearchActivityIntent.putExtra("profileImageUrl", currentUserProfileImageUrl);
                FriendsActivity.this.startActivity(friendsSearchActivityIntent);
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
        // Create the adapter to convert the array to views and attach to a ListView instance.
        ArrayList<Friend> friends = new ArrayList<>();
        FriendsAdapter adapter = new FriendsAdapter(this, friends);
        ListView listView = findViewById(R.id.listview_friends);
        listView.setAdapter(adapter);

    }

}
