package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class TravelShareSearchFragment extends Fragment {

    private TravelSharePostRepository postRepository;
    private ArrayAdapter<String> adapter;
    private final List<TravelSharePost> latestResults = new ArrayList<>();

    public TravelShareSearchFragment() {
        super(R.layout.fragment_travelshare_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postRepository = TravelShareDataProvider.postRepository();

        EditText input = view.findViewById(R.id.travelshare_search_input);
        Button button = view.findViewById(R.id.travelshare_search_button);
        ListView results = view.findViewById(R.id.travelshare_search_results);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        results.setAdapter(adapter);

        button.setOnClickListener(v -> runSearch(input.getText().toString()));

        results.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position < 0 || position >= latestResults.size()) {
                return;
            }
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openPostDetail(latestResults.get(position).getId());
            }
        });

        runSearch("");
    }

    private void runSearch(String query) {
        List<TravelSharePost> posts = postRepository.searchPosts(query);
        latestResults.clear();
        latestResults.addAll(posts);

        List<String> rows = new ArrayList<>();
        for (TravelSharePost post : posts) {
            rows.add(buildPostPreview(post));
        }

        adapter.clear();
        adapter.addAll(rows);
        adapter.notifyDataSetChanged();
    }

    private String buildPostPreview(TravelSharePost post) {
        return post.getAuthorName() + " - " + post.getLocationName()
                + "\n" + post.getDescription()
                + "\n" + getString(R.string.travelshare_post_social_preview, post.getLikeCount(), post.getCommentCount());
    }
}
