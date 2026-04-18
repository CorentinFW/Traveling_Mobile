package com.example.traveling.travelshare.domain;

import java.util.List;

public interface TravelSharePostRepository {
    List<TravelSharePost> getFeedPosts();

    List<TravelSharePost> searchPosts(String query);

    TravelSharePost getPostById(String postId);

    TravelSharePost createPost(
            String authorName,
            String locationName,
            String description,
            String period,
            String howToGetThere
    );

    boolean toggleLike(String postId);

    int reportPost(String postId);

    int addComment(String postId, String commentText);
}
