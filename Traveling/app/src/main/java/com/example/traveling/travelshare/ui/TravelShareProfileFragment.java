package com.example.traveling.travelshare.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
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
import com.example.traveling.travelshare.domain.TravelShareFriendRequestResult;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;
import com.example.traveling.travelshare.domain.TravelShareUser;
import com.example.traveling.travelshare.domain.TravelShareUserRepository;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TravelShareProfileFragment extends Fragment {

    private static final String ARG_PROFILE_DISPLAY_NAME = "profile_display_name";
    private TravelShareUserRepository userRepository;
    private TravelShareUser currentUserEntity;
    private TravelShareUser profileUserEntity;

    public TravelShareProfileFragment() {
        super(R.layout.fragment_travelshare_profile);
    }

    public static TravelShareProfileFragment newInstance(String displayName) {
        TravelShareProfileFragment fragment = new TravelShareProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROFILE_DISPLAY_NAME, displayName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TravelShareSessionRepository sessionRepository = TravelShareDataProvider.sessionRepository();
        TravelSharePostRepository postRepository = TravelShareDataProvider.postRepository();
        userRepository = TravelShareDataProvider.userRepository();

        TextView avatarView = view.findViewById(R.id.travelshare_profile_avatar);
        TextView nameView = view.findViewById(R.id.travelshare_profile_name_value);
        TextView postsCountView = view.findViewById(R.id.travelshare_profile_posts_count);
        TextView groupsCountView = view.findViewById(R.id.travelshare_profile_groups_count);
        ListView postsListView = view.findViewById(R.id.travelshare_profile_posts_list);
        Button logoutButton = view.findViewById(R.id.travelshare_profile_logout_button);
        Button addFriendButton = view.findViewById(R.id.travelshare_profile_add_friend_button);

        String currentUser = sessionRepository.getDisplayName();
        String profileUser = resolveProfileUser(currentUser);
        currentUserEntity = resolveCurrentUserEntity(currentUser);
        profileUserEntity = resolveUserEntity(profileUser);

        String displayedName = profileUserEntity == null ? profileUser : buildDisplayName(profileUserEntity);
        nameView.setText(displayedName);

        // Set avatar with initial
        String initial = "?";
        if (displayedName != null && !displayedName.trim().isEmpty()) {
            initial = displayedName.trim().substring(0, 1).toUpperCase(Locale.ROOT);
        }
        avatarView.setText(initial);

        int backgroundColor = MaterialColors.getColor(avatarView, com.google.android.material.R.attr.colorPrimaryContainer, Color.LTGRAY);
        int textColor = MaterialColors.getColor(avatarView, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.DKGRAY);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(backgroundColor);
        avatarView.setBackground(drawable);
        avatarView.setTextColor(textColor);

        // Load user's posts with tolerant aliases (pseudo, prenom, email local-part)
        List<TravelSharePost> userPosts = loadPostsForProfile(postRepository, userRepository, currentUser, profileUser);
        postsCountView.setText(String.valueOf(userPosts.size()));
        groupsCountView.setText("0"); // Will be updated in next iteration with group repo

        boolean isOwnProfile = isOwnProfile(currentUser, profileUser);
        logoutButton.setVisibility(isOwnProfile ? View.VISIBLE : View.GONE);
        addFriendButton.setVisibility(!isOwnProfile ? View.VISIBLE : View.GONE);

        if (!isOwnProfile) {
            addFriendButton.setOnClickListener(v -> openFriendRequestDialog());
        }

        TravelSharePostAdapter adapter = new TravelSharePostAdapter(requireContext(), userPosts, authorName -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openUserProfile(authorName);
            }
        });
        postsListView.setAdapter(adapter);
        postsListView.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position < 0 || position >= userPosts.size()) {
                return;
            }
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openPostDetail(userPosts.get(position).getId());
            }
        });

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

    private void openFriendRequestDialog() {
        if (currentUserEntity == null) {
            Toast.makeText(requireContext(), R.string.travelshare_friend_request_need_login, Toast.LENGTH_SHORT).show();
            return;
        }
        if (profileUserEntity == null) {
            Toast.makeText(requireContext(), "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.travelshare_friend_request_title)
                .setMessage(getString(R.string.travelshare_friend_request_message, buildDisplayName(profileUserEntity), profileUserEntity.getPseudo()))
                .setPositiveButton(R.string.travelshare_friend_request_send, (dialog, which) -> {
                    TravelShareFriendRequestResult result = userRepository.sendFriendRequestDetailedByIds(
                            currentUserEntity.getId(),
                            profileUserEntity.getId()
                    );
                    Toast.makeText(requireContext(), getFriendRequestResultMessage(result), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private String getFriendRequestResultMessage(TravelShareFriendRequestResult result) {
        if (result == null) {
            return getString(R.string.travelshare_friend_request_failed);
        }
        switch (result) {
            case SENT:
                return getString(R.string.travelshare_friend_request_sent);
            case ALREADY_PENDING:
                return getString(R.string.travelshare_friend_request_already_pending);
            case ALREADY_FRIENDS:
                return getString(R.string.travelshare_friend_request_already_friends);
            case SELF_REQUEST:
                return getString(R.string.travelshare_friend_request_self);
            case REQUESTER_NOT_FOUND:
            case TARGET_NOT_FOUND:
            case FAILED:
            default:
                return getString(R.string.travelshare_friend_request_failed);
        }
    }

    private String resolveProfileUser(String currentUser) {
        Bundle args = getArguments();
        if (args == null) {
            return currentUser;
        }
        String requestedUser = args.getString(ARG_PROFILE_DISPLAY_NAME);
        if (requestedUser == null || requestedUser.trim().isEmpty()) {
            return currentUser;
        }
        return requestedUser.trim();
    }

    private boolean isOwnProfile(String currentUser, String profileUser) {
        if (currentUser == null || profileUser == null) {
            return false;
        }
        return currentUser.trim().equalsIgnoreCase(profileUser.trim());
    }

    private TravelShareUser resolveUserEntity(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return resolveUserEntityFromFirebase();
        }
        String normalized = displayName.trim();
        TravelShareUser resolved = userRepository.getUserByDisplayName(displayName);
        if (resolved != null) {
            return resolved;
        }
        List<TravelShareUser> matches = userRepository.searchUsers(normalized);
        if (matches != null && !matches.isEmpty()) {
            return matches.get(0);
        }

        String localPart = extractLocalPart(normalized);
        if (localPart != null && !localPart.equalsIgnoreCase(normalized)) {
            resolved = userRepository.getUserByDisplayName(localPart);
            if (resolved != null) {
                return resolved;
            }
            matches = userRepository.searchUsers(localPart);
            if (matches != null && !matches.isEmpty()) {
                return matches.get(0);
            }
        }

        resolved = resolveUserEntityFromFirebase();
        if (resolved != null) {
            return resolved;
        }

        return null;
    }

    private TravelShareUser resolveCurrentUserEntity(String displayName) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            TravelShareUser byUid = userRepository.getUserById(uid);
            if (byUid != null) {
                return byUid;
            }
            TravelShareUser byDisplayName = resolveUserEntityDirect(auth.getCurrentUser().getDisplayName());
            if (byDisplayName != null) {
                return byDisplayName;
            }
            TravelShareUser byEmail = resolveUserEntityDirect(auth.getCurrentUser().getEmail());
            if (byEmail != null) {
                return byEmail;
            }
            TravelShareUser byEmailLocalPart = resolveUserEntityDirect(extractLocalPart(auth.getCurrentUser().getEmail()));
            if (byEmailLocalPart != null) {
                return byEmailLocalPart;
            }
        }

        TravelShareUser fallback = resolveUserEntity(displayName);
        if (fallback != null) {
            return fallback;
        }
        return null;
    }

    private TravelShareUser resolveUserEntityDirect(String candidate) {
        if (candidate == null || candidate.trim().isEmpty()) {
            return null;
        }
        String normalized = candidate.trim();
        TravelShareUser resolved = userRepository.getUserByDisplayName(normalized);
        if (resolved != null) {
            return resolved;
        }
        List<TravelShareUser> matches = userRepository.searchUsers(normalized);
        if (matches != null && !matches.isEmpty()) {
            return matches.get(0);
        }
        String localPart = extractLocalPart(normalized);
        if (localPart != null && !localPart.equalsIgnoreCase(normalized)) {
            resolved = userRepository.getUserByDisplayName(localPart);
            if (resolved != null) {
                return resolved;
            }
            matches = userRepository.searchUsers(localPart);
            if (matches != null && !matches.isEmpty()) {
                return matches.get(0);
            }
        }
        return null;
    }

    private TravelShareUser resolveUserEntityFromFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        String firebaseDisplayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String firebaseEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        TravelShareUser resolved = resolveUserEntityDirect(firebaseDisplayName);
        if (resolved != null) {
            return resolved;
        }

        resolved = resolveUserEntityDirect(firebaseEmail);
        if (resolved != null) {
            return resolved;
        }

        return resolveUserEntityDirect(extractLocalPart(firebaseEmail));
    }

    private String buildDisplayName(TravelShareUser user) {
        if (user == null) {
            return "";
        }
        String fullName = user.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }
        if (user.getPseudo() != null && !user.getPseudo().trim().isEmpty()) {
            return user.getPseudo().trim();
        }
        return user.getId() == null ? "" : user.getId().trim();
    }

    private List<TravelSharePost> loadPostsForProfile(
            TravelSharePostRepository postRepository,
            TravelShareUserRepository userRepository,
            String currentUser,
            String profileUser
    ) {
        Set<String> aliases = buildAuthorAliases(userRepository, currentUser, profileUser);
        Map<String, TravelSharePost> unique = new LinkedHashMap<>();

        for (String alias : aliases) {
            List<TravelSharePost> byAuthor = postRepository.getPostsByAuthor(alias);
            for (TravelSharePost post : byAuthor) {
                unique.put(post.getId(), post);
            }
        }

        if (!unique.isEmpty()) {
            return new ArrayList<>(unique.values());
        }

        // Fallback: filtre sur le feed complet si la requete auteur stricte ne renvoie rien.
        List<TravelSharePost> fromFeed = new ArrayList<>();
        for (TravelSharePost post : postRepository.getFeedPosts()) {
            String author = post.getAuthorName();
            if (matchesAnyAlias(author, aliases)) {
                fromFeed.add(post);
            }
        }
        return fromFeed;
    }

    private Set<String> buildAuthorAliases(TravelShareUserRepository userRepository, String currentUser, String profileUser) {
        Set<String> aliases = new LinkedHashSet<>();
        addAlias(aliases, profileUser);

        TravelShareUser profile = userRepository.getUserByDisplayName(profileUser);
        if (profile != null) {
            addAlias(aliases, profile.getPseudo());
            addAlias(aliases, profile.getFirstName());
            addAlias(aliases, profile.getFullName());
        }

        if (currentUser != null && currentUser.equalsIgnoreCase(profileUser)) {
            addAlias(aliases, currentUser);
            addAlias(aliases, extractLocalPart(currentUser));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && currentUser != null && currentUser.equalsIgnoreCase(profileUser)) {
            addAlias(aliases, firebaseUser.getUid());
            addAlias(aliases, firebaseUser.getDisplayName());
            addAlias(aliases, firebaseUser.getEmail());
            addAlias(aliases, extractLocalPart(firebaseUser.getEmail()));
        }
        return aliases;
    }

    private boolean matchesAnyAlias(String author, Set<String> aliases) {
        if (author == null) {
            return false;
        }
        String normalizedAuthor = author.trim().toLowerCase(Locale.ROOT);
        for (String alias : aliases) {
            if (alias == null) {
                continue;
            }
            String normalizedAlias = alias.trim().toLowerCase(Locale.ROOT);
            if (normalizedAlias.equals(normalizedAuthor)) {
                return true;
            }
        }
        return false;
    }

    private void addAlias(Set<String> aliases, String value) {
        if (value == null) {
            return;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        aliases.add(trimmed);
    }

    private String extractLocalPart(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        int atIndex = trimmed.indexOf('@');
        if (atIndex > 0) {
            return trimmed.substring(0, atIndex);
        }
        return trimmed;
    }
}

