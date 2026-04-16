package com.example.traveling.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.traveling.model.Comment;
import com.example.traveling.model.Post;
import com.example.traveling.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FakePostRepository {

    private static final FakePostRepository INSTANCE = new FakePostRepository();

    private final List<Post> posts = new ArrayList<>();
    private long nextPostId = 1000L;

    private FakePostRepository() {
        seedPosts();
    }

    @NonNull
    public static FakePostRepository getInstance() {
        return INSTANCE;
    }

    @NonNull
    public synchronized List<Post> getFeedPosts() {
        return new ArrayList<>(posts);
    }

    @Nullable
    public synchronized Post getPostById(@NonNull String postId) {
        for (Post post : posts) {
            if (post.getId().equals(postId)) {
                return post;
            }
        }
        return null;
    }

    @NonNull
    public synchronized List<Post> searchPosts(@NonNull String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return getFeedPosts();
        }

        List<Post> results = new ArrayList<>();
        for (Post post : posts) {
            if (contains(post.getCaption(), normalized)
                    || contains(post.getLocationName(), normalized)
                    || contains(post.getAuthor().getUsername(), normalized)
                    || contains(post.getAuthor().getDisplayName(), normalized)
                    || containsAny(post.getTags(), normalized)) {
                results.add(post);
            }
        }
        return results;
    }

    @NonNull
    public synchronized Post addPost(
            @NonNull User author,
            @NonNull String locationName,
            @NonNull String caption,
            @NonNull List<String> tags
    ) {
        if (locationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le lieu est obligatoire");
        }
        if (caption.trim().isEmpty()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }

        String id = "local-" + nextPostId++;
        Post post = new Post(
                id,
                author,
                "local://image/" + id,
                locationName.trim(),
                caption.trim(),
                normalizeTags(tags),
                Collections.emptyList(),
                System.currentTimeMillis()
        );
        posts.add(0, post);
        return post;
    }

    public synchronized void resetForTests() {
        posts.clear();
        nextPostId = 1000L;
        seedPosts();
    }

    private void seedPosts() {
        User alice = new User("u1", "alice.nomad", "Alice Nomad", null);
        User yassine = new User("u2", "yass.trips", "Yassine Trips", null);
        User clara = new User("u3", "clara.roads", "Clara Roads", null);

        long now = System.currentTimeMillis();
        posts.add(new Post("p10", alice, "mock://p10", "Lisbonne", "Golden hour sur les toits.",
                Arrays.asList("city", "sunset", "portugal"),
                Collections.singletonList(new Comment("c1", yassine, "Magnifique vue.", now - 200_000L)),
                now - 1_000L));
        posts.add(new Post("p9", yassine, "mock://p9", "Chamonix", "Pause cafe face au Mont-Blanc.",
                Arrays.asList("mountain", "snow", "france"),
                Collections.emptyList(),
                now - 2_000L));
        posts.add(new Post("p8", clara, "mock://p8", "Marrakech", "Les couleurs du souk au matin.",
                Arrays.asList("market", "morocco", "street"),
                Collections.emptyList(),
                now - 3_000L));
        posts.add(new Post("p7", alice, "mock://p7", "Kyoto", "Temple calme apres la pluie.",
                Arrays.asList("temple", "japan", "culture"),
                Collections.emptyList(),
                now - 4_000L));
        posts.add(new Post("p6", yassine, "mock://p6", "Reykjavik", "Lumiere bleue et vent glacial.",
                Arrays.asList("iceland", "nature", "cold"),
                Collections.emptyList(),
                now - 5_000L));
        posts.add(new Post("p5", clara, "mock://p5", "Bali", "Scooter, rizieres et pluie tropicale.",
                Arrays.asList("asia", "nature", "island"),
                Collections.emptyList(),
                now - 6_000L));
        posts.add(new Post("p4", alice, "mock://p4", "Rome", "Fin de journee au Trastevere.",
                Arrays.asList("italy", "food", "street"),
                Collections.emptyList(),
                now - 7_000L));
        posts.add(new Post("p3", yassine, "mock://p3", "Istanbul", "Ferry du soir sur le Bosphore.",
                Arrays.asList("turkey", "water", "city"),
                Collections.emptyList(),
                now - 8_000L));
        posts.add(new Post("p2", clara, "mock://p2", "Seoul", "Neons et pluie fine en centre-ville.",
                Arrays.asList("korea", "night", "city"),
                Collections.emptyList(),
                now - 9_000L));
        posts.add(new Post("p1", alice, "mock://p1", "Cusco", "Altitude, murs incas et ciel immense.",
                Arrays.asList("peru", "history", "mountain"),
                Collections.emptyList(),
                now - 10_000L));
    }

    private static boolean contains(@Nullable String value, @NonNull String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private static boolean containsAny(@NonNull List<String> values, @NonNull String normalizedQuery) {
        for (String value : values) {
            if (contains(value, normalizedQuery)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private static List<String> normalizeTags(@NonNull List<String> rawTags) {
        List<String> cleaned = new ArrayList<>();
        for (String tag : rawTags) {
            String normalized = tag.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                cleaned.add(normalized);
            }
        }
        return cleaned;
    }
}



