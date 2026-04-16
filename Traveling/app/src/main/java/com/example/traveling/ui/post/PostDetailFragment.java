package com.example.traveling.ui.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.data.FakePostRepository;
import com.example.traveling.model.Post;

public class PostDetailFragment extends Fragment {

    private final FakePostRepository repository = FakePostRepository.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView author = view.findViewById(R.id.detail_author);
        TextView location = view.findViewById(R.id.detail_location);
        TextView caption = view.findViewById(R.id.detail_caption);
        TextView tags = view.findViewById(R.id.detail_tags);

        String postId = null;
        Bundle args = getArguments();
        if (args != null) {
            postId = args.getString("post_id");
        }

        if (postId == null) {
            showNotFound(author, location, caption, tags);
            return;
        }

        Post post = repository.getPostById(postId);
        if (post == null) {
            showNotFound(author, location, caption, tags);
            return;
        }

        author.setText(post.getAuthor().getDisplayName());
        location.setText(getString(R.string.detail_location_format, post.getLocationName()));
        caption.setText(post.getCaption());
        tags.setText(getString(R.string.detail_tags_format, TextUtils.join(", ", post.getTags())));
    }

    private void showNotFound(
            @NonNull TextView author,
            @NonNull TextView location,
            @NonNull TextView caption,
            @NonNull TextView tags
    ) {
        author.setText(R.string.detail_not_found_title);
        location.setText(R.string.detail_not_found_message);
        caption.setText("");
        tags.setText("");
    }
}



