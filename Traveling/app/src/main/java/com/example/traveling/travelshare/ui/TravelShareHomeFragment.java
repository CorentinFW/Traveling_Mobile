package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.MainActivity;
import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelSharePost;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;

import java.util.ArrayList;
import java.util.List;

public class TravelShareHomeFragment extends Fragment {

    public TravelShareHomeFragment() {
        super(R.layout.fragment_travelshare_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = view.findViewById(R.id.travelshare_home_list);
        TravelSharePostRepository postRepository = TravelShareDataProvider.postRepository();
        List<TravelSharePost> posts = postRepository.getFeedPosts();

        List<String> rows = new ArrayList<>();
        for (TravelSharePost post : posts) {
            rows.add(buildPostPreview(post));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                rows
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            TravelSharePost selectedPost = posts.get(position);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openPostDetail(selectedPost.getId());
            }
        });
    }

    private String buildPostPreview(TravelSharePost post) {
        return post.getAuthorName() + " - " + post.getLocationName()
                + "\n" + post.getDescription()
                + "\n" + getString(R.string.travelshare_post_social_preview, post.getLikeCount(), post.getCommentCount());
    }
}
