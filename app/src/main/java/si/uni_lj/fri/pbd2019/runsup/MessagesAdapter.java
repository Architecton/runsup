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

import si.uni_lj.fri.pbd2019.runsup.model.Message;

public class MessagesAdapter extends ArrayAdapter<Message> {

    public MessagesAdapter(Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {

        // Get the data item for this position.
        Message messageNxt = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_history, parent, false);  // TODO
        }

        // Lookup view for data population.
        TextView messageContent = convertView.findViewById(R.id.textview_messages_content);
        TextView sender = convertView.findViewById(R.id.textview_messages_sender);
        TextView dateSent = convertView.findViewById(R.id.textview_messages_date);
        ImageView userProfileImage = convertView.findViewById(R.id.imageview_messages_sender_profile_image);

        // Populate the data into the template view using the data object
        messageContent.setText(messageNxt.getContent());
        sender.setText("TODO");
        dateSent.setText("TODO");
        Glide
                .with(userProfileImage.getContext())
                .load(messageNxt.getProfileImageUri())
                .centerCrop()
                .override(150, 150)
                .into(userProfileImage);

        // Return the completed view to render on screen.
        return convertView;
    }
}
