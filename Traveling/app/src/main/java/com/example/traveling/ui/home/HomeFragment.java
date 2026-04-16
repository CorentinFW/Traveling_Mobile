package com.example.traveling.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.data.FakePostRepository;
import com.example.traveling.model.Post;

import java.util.List;

public class HomeFragment extends Fragment {

    private final FakePostRepository repository = FakePostRepository.getInstance();
    private PostFeedAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.home_feed_list);
        TextView emptyState = view.findViewById(R.id.home_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PostFeedAdapter(post -> openPostDetail(post.getId()));
        recyclerView.setAdapter(adapter);

        List<Post> feed = repository.getFeedPosts();
        adapter.submitList(feed);

        boolean hasItems = !feed.isEmpty();
        recyclerView.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    private void openPostDetail(@NonNull String postId) {
        Bundle args = new Bundle();
        args.putString("post_id", postId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_navigation_home_to_post_detail_screen, args);
    }
}


