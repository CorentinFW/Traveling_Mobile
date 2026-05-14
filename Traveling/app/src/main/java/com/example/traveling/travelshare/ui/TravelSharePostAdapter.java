package com.example.traveling.travelshare.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.traveling.R;
import com.example.traveling.travelshare.domain.TravelSharePost;
import com.google.android.material.color.MaterialColors;

import java.util.List;
import java.util.Locale;

public class TravelSharePostAdapter extends BaseAdapter {

    public interface OnAuthorClickListener {
        void onAuthorClicked(String authorName);
    }

    private final LayoutInflater inflater;
    private final List<TravelSharePost> posts;
    private final OnAuthorClickListener onAuthorClickListener;

    public TravelSharePostAdapter(@NonNull Context context, @NonNull List<TravelSharePost> posts) {
        this(context, posts, null);
    }

    public TravelSharePostAdapter(
            @NonNull Context context,
            @NonNull List<TravelSharePost> posts,
            OnAuthorClickListener onAuthorClickListener
    ) {
        this.inflater = LayoutInflater.from(context);
        this.posts = posts;
        this.onAuthorClickListener = onAuthorClickListener;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public TravelSharePost getItem(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_travelshare_post, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TravelSharePost post = getItem(position);
        holder.bind(convertView.getContext(), post, position == 0, onAuthorClickListener);
        return convertView;
    }

    private static class ViewHolder {
        private final TextView avatarView;
        private final TextView authorView;
        private final TextView locationView;
        private final TextView descriptionView;
        private final TextView metaView;
        private final TextView statsView;
        private final TextView highlightView;

        ViewHolder(View root) {
            avatarView = root.findViewById(R.id.travelshare_post_avatar);
            authorView = root.findViewById(R.id.travelshare_post_author);
            locationView = root.findViewById(R.id.travelshare_post_location);
            descriptionView = root.findViewById(R.id.travelshare_post_description);
            metaView = root.findViewById(R.id.travelshare_post_meta);
            statsView = root.findViewById(R.id.travelshare_post_stats);
            highlightView = root.findViewById(R.id.travelshare_post_highlight);
        }

        void bind(Context context, TravelSharePost post, boolean highlight, OnAuthorClickListener onAuthorClickListener) {
            authorView.setText(post.getAuthorName());
            locationView.setText(post.getLocationName());
            descriptionView.setText(post.getDescription());
            metaView.setText(context.getString(R.string.travelshare_post_feed_meta,
                    context.getString(R.string.travelshare_post_period_format, post.getPeriod())));
            statsView.setText(context.getString(R.string.travelshare_post_feed_stats,
                    post.getLikeCount(), post.getCommentCount(), post.getReportCount()));

            if (highlight) {
                highlightView.setVisibility(View.VISIBLE);
            } else {
                highlightView.setVisibility(View.GONE);
            }

            setAvatarStyle(post.getAuthorName());

            View.OnClickListener authorClick = v -> {
                if (onAuthorClickListener != null) {
                    onAuthorClickListener.onAuthorClicked(post.getAuthorName());
                }
            };
            avatarView.setOnClickListener(authorClick);
            authorView.setOnClickListener(authorClick);
        }

        private void setAvatarStyle(String authorName) {
            String initial = "?";
            if (authorName != null && !authorName.trim().isEmpty()) {
                initial = authorName.trim().substring(0, 1).toUpperCase(Locale.ROOT);
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
    }
}


