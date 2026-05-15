package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelSharePost;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InMemoryTravelSharePostRepository implements TravelSharePostRepository {

    private final List<TravelSharePost> posts = seedPosts();

    private int nextGeneratedId = 16;

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
    public List<TravelSharePost> getPostsByAuthor(String authorName) {
        List<TravelSharePost> result = new ArrayList<>();
        if (authorName == null || authorName.trim().isEmpty()) {
            return result;
        }

        String normalizedAuthor = authorName.trim();
        for (TravelSharePost post : posts) {
            if (post.getAuthorName().equalsIgnoreCase(normalizedAuthor)) {
                result.add(post);
            }
        }
        return result;
    }

    @Override
    public List<TravelSharePost> getPostsByAuthorId(String authorId) {
        return getPostsByAuthor(authorId);
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

    @Override
    public TravelSharePost createPost(
            String authorName,
            String locationName,
            String description,
            String period,
            String howToGetThere
    ) {
        TravelSharePost createdPost = new TravelSharePost(
                String.valueOf(nextGeneratedId++),
                authorName == null || authorName.trim().isEmpty() ? "Voyageur" : authorName.trim(),
                locationName == null ? "" : locationName.trim(),
                description == null ? "" : description.trim(),
                period == null ? "" : period.trim(),
                howToGetThere == null ? "" : howToGetThere.trim(),
                0,
                0
        );

        // Most recent publication appears first in the feed.
        posts.add(0, createdPost);
        return createdPost;
    }

    private List<TravelSharePost> seedPosts() {
        List<TravelSharePost> seededPosts = new ArrayList<>();

        seededPosts.add(new TravelSharePost("1", "Corentin", "Tokyo", "Nuit neon a Shibuya et ramen incroyable", "Automne 2025", "JR Yamanote - station Shibuya", 64, 12));
        seededPosts.add(new TravelSharePost("2", "Corentin", "Kyoto", "Temples dores et atmosphere apaisante", "Printemps 2025", "Train rapide depuis Osaka", 41, 8));
        seededPosts.add(new TravelSharePost("3", "Corentin", "Paris", "Balade au bord de la Seine au coucher du soleil", "Ete 2024", "Metro ligne 1", 22, 4));

        seededPosts.add(new TravelSharePost("4", "Lina", "Annecy", "Lac turquoise au lever du soleil", "Ete 2025", "Bus SIBRA + 12 min a pied", 18, 3));
        seededPosts.add(new TravelSharePost("5", "Lina", "Lisbonne", "Tram jaune et rues pavees dans l'Alfama", "Printemps 2024", "Metro + tram 28", 42, 7));
        seededPosts.add(new TravelSharePost("6", "Lina", "Porto", "Pont Dom Luis au coucher du soleil", "Automne 2024", "Train regional + marche", 35, 6));

        seededPosts.add(new TravelSharePost("7", "Mehdi", "Marrakech", "Souk colore et epices partout", "Hiver 2024", "Taxi depuis la medina", 27, 5));
        seededPosts.add(new TravelSharePost("8", "Mehdi", "Essaouira", "Vent marin et remparts face a l'ocean", "Printemps 2025", "Bus direct depuis Marrakech", 16, 2));
        seededPosts.add(new TravelSharePost("9", "Mehdi", "Chefchaouen", "Ruelles bleues et ambiance paisible", "Ete 2023", "Bus local + marche", 38, 9));

        seededPosts.add(new TravelSharePost("10", "Camille", "Reykjavik", "Road trip volcan et cascades en Islande", "Ete 2023", "Location voiture 4x4 depuis Keflavik", 51, 9));
        seededPosts.add(new TravelSharePost("11", "Camille", "Oslo", "Fjords, musees et architecture scandinave", "Hiver 2024", "Metro + bateau", 29, 4));
        seededPosts.add(new TravelSharePost("12", "Camille", "Seoul", "Nuit animee a Hongdae et cafes design", "Automne 2025", "Metro ligne 2", 46, 11));

        seededPosts.add(new TravelSharePost("13", "Nora", "Bali", "Rizieres vertes et plages au lever du jour", "Printemps 2024", "Scooter depuis Ubud", 57, 10));
        seededPosts.add(new TravelSharePost("14", "Nora", "Santorini", "Domes blancs et mer bleu profond", "Ete 2025", "Ferry + bus local", 31, 6));
        seededPosts.add(new TravelSharePost("15", "Nora", "Rome", "Fontaines, piazzas et gelato a chaque coin de rue", "Automne 2024", "Metro + marche", 44, 8));

        return seededPosts;
    }
}
