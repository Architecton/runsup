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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import si.uni_lj.fri.pbd2019.runsup.model.Friend;

public class FriendsSearchResultsAdapter extends ArrayAdapter<Friend> {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    FriendsSearchResultsAdapter(Context context, ArrayList<Friend> messages) {
        super(context, 0, messages);
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {

        // Get the data item for this position.
        Friend searchResultNxt = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_friends_searchresults, parent, false);
        }

        // Lookup view for data population.
        TextView searchResultName = convertView.findViewById(R.id.textview_searchresult_name);
        ImageView userProfileImage = convertView.findViewById(R.id.imageview_searchresult_profile_image);

        // Populate the data into the template view using the data object.
        // searchResultName.setText(String.format("%s\n%s", searchResultNxt.getName(), searchResultNxt.getDateJoined().toString()));
        searchResultName.setText(String.format("%s\n Joined: %s", searchResultNxt.getName(), simpleDateFormat.format(searchResultNxt.getDateJoined())));
        Glide
                .with(userProfileImage.getContext())
                .load(searchResultNxt.getProfileImageUrl())
                .centerCrop()
                .override(150, 150)
                .into(userProfileImage);

        // Return the completed view to render on screen.
        return convertView;
    }
}
