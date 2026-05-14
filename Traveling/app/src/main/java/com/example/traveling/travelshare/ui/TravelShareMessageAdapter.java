package com.example.traveling.travelshare.ui;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.traveling.R;
import com.example.traveling.travelshare.domain.TravelShareConversation;
import com.example.traveling.travelshare.domain.TravelShareGroup;
import com.google.android.material.color.MaterialColors;
import java.util.List;
import java.util.Locale;
public class TravelShareMessageAdapter extends ArrayAdapter<Object> {
    private final String currentUserName;
    public TravelShareMessageAdapter(@NonNull Context context, @NonNull List<Object> objects, String currentUserName) {
        super(context, 0, objects);
        this.currentUserName = currentUserName;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_travelshare_message, parent, false);
        }
        Object item = getItem(position);
         TextView avatarSingle = convertView.findViewById(R.id.avatar_single);
         RelativeLayout avatarGroupContainer = convertView.findViewById(R.id.avatar_group_container);
         TextView mini1 = convertView.findViewById(R.id.avatar_mini_1);
         TextView mini2 = convertView.findViewById(R.id.avatar_mini_2);
         TextView mini3 = convertView.findViewById(R.id.avatar_mini_3);
         TextView title = convertView.findViewById(R.id.message_title);
        TextView preview = convertView.findViewById(R.id.message_preview);
        TextView activity = convertView.findViewById(R.id.message_activity);
        if (item instanceof TravelShareConversation) {
            TravelShareConversation conversation = (TravelShareConversation) item;
            avatarSingle.setVisibility(View.VISIBLE);
            avatarGroupContainer.setVisibility(View.GONE);
            title.setText(conversation.getTitle());
            preview.setText(conversation.getLastMessagePreview());
            activity.setText(conversation.getLastActivityLabel());
            setConversationAvatar(avatarSingle, conversation.getTitle());
        } else if (item instanceof TravelShareGroup) {
            TravelShareGroup group = (TravelShareGroup) item;
            avatarSingle.setVisibility(View.GONE);
            avatarGroupContainer.setVisibility(View.VISIBLE);
            List<String> members = group.getMemberNames();
            int count = 0;
            mini1.setVisibility(View.GONE);
            mini2.setVisibility(View.GONE);
            mini3.setVisibility(View.GONE);
            for (String member : members) {
                if (member.equals(currentUserName)) continue;
                if (count == 0) {
                    mini1.setVisibility(View.VISIBLE);
                    setMemberAvatar(mini1, member);
                } else if (count == 1) {
                    mini2.setVisibility(View.VISIBLE);
                    setMemberAvatar(mini2, member);
                } else if (count == 2) {
                    mini3.setVisibility(View.VISIBLE);
                    setMemberAvatar(mini3, member);
                }
                count++;
                if (count >= 3) break;
            }
            // Fallback if no other members (or just yourself + 1, maybe show less)
            if (count == 0) {
                mini1.setVisibility(View.VISIBLE);
                mini1.setText("?");
            }
            title.setText(group.getName());
            preview.setText(group.getLastMessagePreview());
            activity.setText(group.getLastActivityLabel());
        }
        return convertView;
    }

    private void setConversationAvatar(TextView avatarView, String title) {
        String initial = "?";
        if (title != null && !title.trim().isEmpty()) {
            initial = title.trim().substring(0, 1).toUpperCase(Locale.ROOT);
        }
        avatarView.setText(initial);

        int backgroundColor = MaterialColors.getColor(avatarView, com.google.android.material.R.attr.colorPrimaryContainer, Color.LTGRAY);
        int textColor = MaterialColors.getColor(avatarView, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.DKGRAY);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(backgroundColor);
        avatarView.setBackground(drawable);
        avatarView.setTextColor(textColor);
    }

    private void setMemberAvatar(TextView avatarView, String memberName) {
        String initial = "?";
        if (memberName != null && !memberName.trim().isEmpty()) {
            initial = memberName.trim().substring(0, 1).toUpperCase(Locale.ROOT);
        }
        avatarView.setText(initial);

        int backgroundColor = MaterialColors.getColor(avatarView, com.google.android.material.R.attr.colorSecondaryContainer, Color.LTGRAY);
        int textColor = MaterialColors.getColor(avatarView, com.google.android.material.R.attr.colorOnSecondaryContainer, Color.DKGRAY);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(backgroundColor);
        avatarView.setBackground(drawable);
        avatarView.setTextColor(textColor);
    }
}
