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
import com.example.traveling.travelshare.domain.TravelShareUser;
import com.example.traveling.travelshare.domain.TravelShareUserRepository;

import java.util.ArrayList;
import java.util.List;

public class TravelShareSearchFragment extends Fragment {

    private TravelSharePostRepository postRepository;
    private TravelShareUserRepository userRepository;
    private ArrayAdapter<String> adapter;
    private final List<SearchResultItem> latestResults = new ArrayList<>();

    public TravelShareSearchFragment() {
        super(R.layout.fragment_travelshare_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postRepository = TravelShareDataProvider.postRepository();
        userRepository = TravelShareDataProvider.userRepository();

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
            SearchResultItem item = latestResults.get(position);
            if (item.type == SearchResultType.USER) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openUserProfile(resolveProfileDisplayName(item.user), item.user.getId());
                }
                return;
            }
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openPostDetail(item.post.getId());
            }
        });

        clearResults();
    }

    private void runSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            clearResults();
            return;
        }
        List<TravelSharePost> posts = postRepository.searchPosts(query);
        List<TravelShareUser> users = userRepository.searchUsers(query);
        latestResults.clear();

        List<String> rows = new ArrayList<>();
        for (TravelShareUser user : users) {
            latestResults.add(SearchResultItem.forUser(user));
            rows.add(buildUserPreview(user));
        }
        for (TravelSharePost post : posts) {
            latestResults.add(SearchResultItem.forPost(post));
            rows.add(buildPostPreview(post));
        }

        adapter.clear();
        adapter.addAll(rows);
        adapter.notifyDataSetChanged();
    }

    private void clearResults() {
        latestResults.clear();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    private String buildPostPreview(TravelSharePost post) {
        return post.getAuthorName() + " - " + post.getLocationName()
                + "\n" + post.getDescription()
                + "\n" + getString(R.string.travelshare_post_social_preview, post.getLikeCount(), post.getCommentCount());
    }

    private String buildUserPreview(TravelShareUser user) {
        return getString(R.string.travelshare_search_profile_prefix)
                + " " + user.getFullName()
                + " (@" + user.getPseudo() + ")"
                + "\n" + getString(R.string.travelshare_search_profile_friend_count, user.getFriendIds().size());
    }


    private String resolveProfileDisplayName(TravelShareUser user) {
        if (user == null) {
            return "";
        }
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            return user.getFirstName().trim();
        }
        if (user.getPseudo() != null && !user.getPseudo().trim().isEmpty()) {
            return user.getPseudo().trim();
        }
        return user.getFullName() == null ? "" : user.getFullName().trim();
    }

    private enum SearchResultType {
        USER,
        POST
    }

    private static class SearchResultItem {
        private final SearchResultType type;
        private final TravelShareUser user;
        private final TravelSharePost post;

        private SearchResultItem(SearchResultType type, TravelShareUser user, TravelSharePost post) {
            this.type = type;
            this.user = user;
            this.post = post;
        }

        private static SearchResultItem forUser(TravelShareUser user) {
            return new SearchResultItem(SearchResultType.USER, user, null);
        }

        private static SearchResultItem forPost(TravelSharePost post) {
            return new SearchResultItem(SearchResultType.POST, null, post);
        }
    }
}
