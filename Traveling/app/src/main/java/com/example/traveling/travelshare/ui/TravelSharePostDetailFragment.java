package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelSharePost;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;

public class TravelSharePostDetailFragment extends Fragment {

    private static final String ARG_POST_ID = "arg_post_id";

    public TravelSharePostDetailFragment() {
        super(R.layout.fragment_travelshare_post_detail);
    }

    public static TravelSharePostDetailFragment newInstance(String postId) {
        TravelSharePostDetailFragment fragment = new TravelSharePostDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String postId = getArguments() != null ? getArguments().getString(ARG_POST_ID) : null;
        if (postId == null) {
            Toast.makeText(requireContext(), R.string.travelshare_post_not_found, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        TravelSharePostRepository postRepository = TravelShareDataProvider.postRepository();
        TravelShareSessionRepository sessionRepository = TravelShareDataProvider.sessionRepository();

        new Thread(() -> {
            TravelSharePost loadedPost = postRepository.getPostById(postId);
            requireActivity().runOnUiThread(() -> bindPost(view, postId, loadedPost, postRepository, sessionRepository));
        }).start();
    }

    private void bindPost(
            View view,
            String postId,
            TravelSharePost loadedPost,
            TravelSharePostRepository postRepository,
            TravelShareSessionRepository sessionRepository
    ) {
        if (!isAdded()) {
            return;
        }
        if (loadedPost == null) {
            Toast.makeText(requireContext(), R.string.travelshare_post_not_found, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        final TravelSharePost[] postHolder = new TravelSharePost[]{loadedPost};

        TextView title = view.findViewById(R.id.travelshare_detail_title);
        TextView author = view.findViewById(R.id.travelshare_detail_author);
        TextView description = view.findViewById(R.id.travelshare_detail_description);
        TextView period = view.findViewById(R.id.travelshare_detail_period);
        TextView howTo = view.findViewById(R.id.travelshare_detail_how_to);
        TextView social = view.findViewById(R.id.travelshare_detail_social);
        EditText commentInput = view.findViewById(R.id.travelshare_detail_comment_input);
        Button likeButton = view.findViewById(R.id.travelshare_detail_like_button);
        Button reportButton = view.findViewById(R.id.travelshare_detail_report_button);
        Button commentButton = view.findViewById(R.id.travelshare_detail_comment_button);

        author.setText(getString(R.string.travelshare_post_author_format, postHolder[0].getAuthorName()));
        title.setText(postHolder[0].getLocationName());
        description.setText(postHolder[0].getDescription());
        period.setText(getString(R.string.travelshare_post_period_format, postHolder[0].getPeriod()));
        howTo.setText(getString(R.string.travelshare_post_how_to_format, postHolder[0].getHowToGetThere()));

        refreshButtons(postHolder[0], social, likeButton, commentInput, commentButton, sessionRepository);

        likeButton.setOnClickListener(v -> new Thread(() -> {
            postRepository.toggleLike(postId);
            TravelSharePost updated = postRepository.getPostById(postId);
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                if (updated != null) {
                    postHolder[0] = updated;
                }
                refreshButtons(postHolder[0], social, likeButton, commentInput, commentButton, sessionRepository);
            });
        }).start());

        reportButton.setOnClickListener(v -> new Thread(() -> {
            int reportCount = postRepository.reportPost(postId);
            TravelSharePost updated = postRepository.getPostById(postId);
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                if (updated != null) {
                    postHolder[0] = updated;
                }
                refreshButtons(postHolder[0], social, likeButton, commentInput, commentButton, sessionRepository);
                Toast.makeText(
                        requireContext(),
                        getString(R.string.travelshare_report_done, reportCount),
                        Toast.LENGTH_SHORT
                ).show();
            });
        }).start());

        commentButton.setOnClickListener(v -> {
            if (!sessionRepository.isAuthenticated()) {
                Toast.makeText(requireContext(), R.string.travelshare_comment_need_login, Toast.LENGTH_SHORT).show();
                return;
            }

            String commentText = commentInput.getText().toString();
            if (commentText.trim().isEmpty()) {
                Toast.makeText(requireContext(), R.string.travelshare_comment_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                int commentCount = postRepository.addComment(postId, commentText);
                TravelSharePost updated = postRepository.getPostById(postId);
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    commentInput.setText("");
                    if (updated != null) {
                        postHolder[0] = updated;
                    }
                    refreshButtons(postHolder[0], social, likeButton, commentInput, commentButton, sessionRepository);
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.travelshare_comment_added, commentCount),
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }).start();
        });
    }

    private void refreshButtons(
            TravelSharePost post,
            TextView socialView,
            Button likeButton,
            EditText commentInput,
            Button commentButton,
            TravelShareSessionRepository sessionRepository
    ) {
        socialView.setText(getString(
                R.string.travelshare_post_social_detail,
                post.getLikeCount(),
                post.getCommentCount(),
                post.getReportCount()
        ));

        likeButton.setText(post.isLikedByCurrentUser()
                ? R.string.travelshare_action_unlike
                : R.string.travelshare_action_like);

        boolean canComment = sessionRepository.isAuthenticated();
        commentInput.setEnabled(canComment);
        commentButton.setEnabled(canComment);
        if (!canComment) {
            commentInput.setHint(R.string.travelshare_comment_need_login);
        }
    }
}
