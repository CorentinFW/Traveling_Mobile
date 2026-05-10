package com.example.traveling.travelshare.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.MainActivity;
import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelSharePost;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TravelShareProfileFragment extends Fragment {

    public TravelShareProfileFragment() {
        super(R.layout.fragment_travelshare_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TravelShareSessionRepository sessionRepository = TravelShareDataProvider.sessionRepository();
        TravelSharePostRepository postRepository = TravelShareDataProvider.postRepository();

        TextView avatarView = view.findViewById(R.id.travelshare_profile_avatar);
        TextView nameView = view.findViewById(R.id.travelshare_profile_name_value);
        TextView postsCountView = view.findViewById(R.id.travelshare_profile_posts_count);
        TextView groupsCountView = view.findViewById(R.id.travelshare_profile_groups_count);
        ListView postsListView = view.findViewById(R.id.travelshare_profile_posts_list);
        Button logoutButton = view.findViewById(R.id.travelshare_profile_logout_button);

        String currentUser = sessionRepository.getDisplayName();
        nameView.setText(currentUser);

        // Set avatar with initial
        String initial = "?";
        if (currentUser != null && !currentUser.trim().isEmpty()) {
            initial = currentUser.trim().substring(0, 1).toUpperCase(Locale.ROOT);
        }
        avatarView.setText(initial);

        int backgroundColor = MaterialColors.getColor(avatarView, com.google.android.material.R.attr.colorPrimaryContainer, Color.LTGRAY);
        int textColor = MaterialColors.getColor(avatarView, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.DKGRAY);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(backgroundColor);
        avatarView.setBackground(drawable);
        avatarView.setTextColor(textColor);

        // Load user's posts
        List<TravelSharePost> userPosts = postRepository.getPostsByAuthor(currentUser);
        postsCountView.setText(String.valueOf(userPosts.size()));
        groupsCountView.setText("0"); // Will be updated in next iteration with group repo

        // Display user posts in list
        List<String> postTitles = new ArrayList<>();
        for (TravelSharePost post : userPosts) {
            postTitles.add(post.getLocationName() + " - " + post.getDescription().substring(0, Math.min(30, post.getDescription().length())));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                postTitles
        );
        postsListView.setAdapter(adapter);

        // Logout button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            sessionRepository.logout();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).refreshSessionButton();
            }

            Toast.makeText(requireContext(), R.string.travelshare_auth_logout_success, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}

