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

import si.uni_lj.fri.pbd2019.runsup.model.FriendRequest;

public class FriendRequestsAdapter extends ArrayAdapter<FriendRequest> {

    public FriendRequestsAdapter(Context context, ArrayList<FriendRequest> requests) {
        super(context, 0, requests);
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {

        // Get the data item for this position.
        FriendRequest reqNext = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_friend_requests, parent, false);
        }

        // Lookup view for data population.
        TextView requestName = convertView.findViewById(R.id.textview_friendrequest_name);
        ImageView userProfileImage = convertView.findViewById(R.id.imageview_friendrequest_profile_image);

        // Populate the data into the template view using the data object
        requestName.setText(reqNext.getName());
        Glide
                .with(userProfileImage.getContext())
                .load(reqNext.getProfileImageUrl())
                .centerCrop()
                .override(150, 150)
                .into(userProfileImage);

        // Return the completed view to render on screen.
        return convertView;
    }
}
