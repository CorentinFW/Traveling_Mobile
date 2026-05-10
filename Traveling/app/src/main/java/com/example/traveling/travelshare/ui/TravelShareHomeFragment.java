package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.MainActivity;
import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelSharePost;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;

import java.util.List;

public class TravelShareHomeFragment extends Fragment {

    public TravelShareHomeFragment() {
        super(R.layout.fragment_travelshare_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = view.findViewById(R.id.travelshare_home_list);
        TextView emptyView = view.findViewById(android.R.id.empty);
        TravelSharePostRepository postRepository = TravelShareDataProvider.postRepository();
        List<TravelSharePost> posts = postRepository.getFeedPosts();

        TravelSharePostAdapter adapter = new TravelSharePostAdapter(requireContext(), posts);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            TravelSharePost selectedPost = posts.get(position);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openPostDetail(selectedPost.getId());
            }
        });
    }
}
