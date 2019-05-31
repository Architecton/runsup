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

    private long idHere;

    MessagesAdapter(Context context, ArrayList<Message> messages, long idHere) {
        super(context, 0, messages);
        this.idHere = idHere;
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {

        // Get the data item for this position.
        Message messageNxt = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_messages, parent, false);
        }

        if (messageNxt.getIdReceiver() == this.idHere) {
            convertView.setBackground(App.getAppResources().getDrawable(R.color.colorAccent));
        } else {
            convertView.setBackground(App.getAppResources().getDrawable(R.color.colorPrimary));
        }

        // Lookup view for data population.
        TextView name = convertView.findViewById(R.id.textview_messaging_friends_name);
        TextView content = convertView.findViewById(R.id.textview_messaging_message);
        ImageView profileImage = convertView.findViewById(R.id.imageview_message_profileimage);

        Glide
            .with(App.getAppContext())
            .load(messageNxt.getProfileImageUri())
            .centerCrop()
            .override(150, 150)
            .into(profileImage);

        name.setText(messageNxt.getSenderName());
        content.setText(messageNxt.getContent());

        // Return the completed view to render on screen.
        return convertView;
    }

}
