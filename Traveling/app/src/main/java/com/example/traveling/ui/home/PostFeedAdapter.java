package com.example.traveling.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostFeedAdapter extends RecyclerView.Adapter<PostFeedAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(@NonNull Post post);
    }

    private final List<Post> items = new ArrayList<>();
    private final OnPostClickListener onPostClickListener;

    public PostFeedAdapter(@NonNull OnPostClickListener onPostClickListener) {
        this.onPostClickListener = onPostClickListener;
    }

    public void submitList(@NonNull List<Post> posts) {
        items.clear();
        items.addAll(posts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_feed, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = items.get(position);
        holder.bind(post, onPostClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        private final TextView author;
        private final TextView location;
        private final TextView caption;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.post_author);
            location = itemView.findViewById(R.id.post_location);
            caption = itemView.findViewById(R.id.post_caption);
        }

        void bind(@NonNull Post post, @NonNull OnPostClickListener onPostClickListener) {
            author.setText(post.getAuthor().getUsername());
            location.setText(post.getLocationName());
            caption.setText(post.getCaption());
            itemView.setOnClickListener(v -> onPostClickListener.onPostClick(post));
        }
    }
}


