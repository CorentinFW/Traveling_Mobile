package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelSharePost;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class InMemoryTravelSharePostRepository implements TravelSharePostRepository {

    private final List<TravelSharePost> posts = new ArrayList<>(Arrays.asList(
            new TravelSharePost(
                    "1",
                    "Lina",
                    "Annecy",
                    "Lac turquoise au lever du soleil",
                    "Ete 2025",
                    "Bus SIBRA + 12 min a pied",
                    18,
                    3
            ),
            new TravelSharePost(
                    "2",
                    "Mehdi",
                    "Lisbonne",
                    "Tram jaune et rues pavees dans l'Alfama",
                    "Printemps 2024",
                    "Metro + tram 28",
                    42,
                    7
            ),
            new TravelSharePost(
                    "3",
                    "Camille",
                    "Tokyo",
                    "Nuit neon a Shibuya et ramen incroyable",
                    "Automne 2025",
                    "JR Yamanote - station Shibuya",
                    64,
                    12
            ),
            new TravelSharePost(
                    "4",
                    "Yanis",
                    "Marrakech",
                    "Souk colore et epices partout",
                    "Hiver 2024",
                    "Taxi depuis la medina",
                    27,
                    5
            ),
            new TravelSharePost(
                    "5",
                    "Nora",
                    "Reykjavik",
                    "Road trip volcan et cascades en Islande",
                    "Ete 2023",
                    "Location voiture 4x4 depuis Keflavik",
                    51,
                    9
            )
    ));

    private int nextId = 6;

    @Override
    public List<TravelSharePost> getFeedPosts() {
        return new ArrayList<>(posts);
    }

    @Override
    public List<TravelSharePost> searchPosts(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            return getFeedPosts();
        }

        List<TravelSharePost> matches = new ArrayList<>();
        for (TravelSharePost post : posts) {
            String haystack = (post.getAuthorName() + " " + post.getLocationName() + " " + post.getDescription())
                    .toLowerCase(Locale.ROOT);
            if (haystack.contains(normalizedQuery)) {
                matches.add(post);
            }
        }

        return matches;
    }

    @Override
    public TravelSharePost getPostById(String postId) {
        for (TravelSharePost post : posts) {
            if (post.getId().equals(postId)) {
                return post;
            }
        }
        return null;
    }

    @Override
    public TravelSharePost createPost(
            String authorName,
            String locationName,
            String description,
            String period,
            String howToGetThere
    ) {
        TravelSharePost newPost = new TravelSharePost(
                String.valueOf(nextId++),
                safe(authorName, "Voyageur"),
                safe(locationName, "Lieu inconnu"),
                safe(description, "Photo de voyage"),
                safe(period, "Periode non precisee"),
                safe(howToGetThere, "Infos transport a venir"),
                0,
                0
        );

        posts.add(0, newPost);
        return newPost;
    }

    @Override
    public boolean toggleLike(String postId) {
        TravelSharePost post = getPostById(postId);
        if (post == null) {
            return false;
        }
        post.toggleLike();
        return post.isLikedByCurrentUser();
    }

    @Override
    public int reportPost(String postId) {
        TravelSharePost post = getPostById(postId);
        if (post == null) {
            return 0;
        }
        post.report();
        return post.getReportCount();
    }

    @Override
    public int addComment(String postId, String commentText) {
        TravelSharePost post = getPostById(postId);
        if (post == null) {
            return 0;
        }

        if (commentText != null && !commentText.trim().isEmpty()) {
            post.addComment();
        }
        return post.getCommentCount();
    }

    private String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
