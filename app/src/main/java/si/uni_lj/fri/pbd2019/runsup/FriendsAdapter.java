package si.uni_lj.fri.pbd2019.runsup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.model.Friend;

public class FriendsAdapter extends ArrayAdapter<Friend> {

    FriendsAdapter(Context context, ArrayList<Friend> friends) {
        super(context, 0, friends);
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {

        // Get the data item for this position.
        Friend friendNxt = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_friends, parent, false);  // TODO
        }

        // Lookup view for data population.
        TextView friendName = convertView.findViewById(R.id.textview_friends_name);
        TextView friendsSince = convertView.findViewById(R.id.textview_friends_since);
        ImageView userProfileImage = convertView.findViewById(R.id.imageview_friends_profile_image);

        // Populate the data into the template view using the data object
        friendName.setText(friendNxt.getName());
        friendsSince.setText(friendNxt.getFriendsSinceFormatted().toString());
        Glide
                .with(userProfileImage.getContext())
                .load(friendNxt.getProfileImageUrl())
                .centerCrop()
                .override(150, 150)
                .into(userProfileImage);

        // Return the completed view to render on screen.
        return convertView;
    }

}
