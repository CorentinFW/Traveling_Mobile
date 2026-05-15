package com.example.traveling.travelshare.data;

import android.util.Log;

import com.example.traveling.travelshare.domain.TravelSharePost;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Firestore-backed implementation of TravelSharePostRepository.
 * Uses blocking calls (Tasks.await). Call off the main thread.
 */
public class FirestoreTravelSharePostRepository implements TravelSharePostRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final InMemoryTravelSharePostRepository fallbackRepository = new InMemoryTravelSharePostRepository();
    private static final String POSTS_COLLECTION = "posts";

    @Override
    public List<TravelSharePost> getFeedPosts() {
        try {
            QuerySnapshot snap = Tasks.await(firestore.collection(POSTS_COLLECTION).get(), 5, TimeUnit.SECONDS);
            List<TravelSharePost> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snap) {
                result.add(mapDoc(doc));
            }
            if (!result.isEmpty()) {
                return result;
            }
            return getFallbackPosts();
        } catch (Exception e) {
            Log.w("TravelShare", "Firestore getFeedPosts failed", e);
            return getFallbackPosts();
        }
    }

    @Override
    public List<TravelSharePost> searchPosts(String query) {
        if (query == null || query.trim().isEmpty()) return getFeedPosts();
        String q = query.trim().toLowerCase();
        List<TravelSharePost> all = getFeedPosts();
        List<TravelSharePost> filtered = new ArrayList<>();
        for (TravelSharePost p : all) {
            if ((p.getDescription() != null && p.getDescription().toLowerCase().contains(q)) ||
                    (p.getLocationName() != null && p.getLocationName().toLowerCase().contains(q)) ||
                    (p.getAuthorName() != null && p.getAuthorName().toLowerCase().contains(q))) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    @Override
    public List<TravelSharePost> getPostsByAuthor(String authorName) {
        if (authorName == null) return new ArrayList<>();
        try {
            QuerySnapshot snap = Tasks.await(firestore.collection(POSTS_COLLECTION).whereEqualTo("authorName", authorName).get(), 5, TimeUnit.SECONDS);
            List<TravelSharePost> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snap) result.add(mapDoc(doc));
            if (!result.isEmpty()) {
                return result;
            }
            return filterByAuthor(getFallbackPosts(), authorName);
        } catch (Exception e) {
            Log.w("TravelShare", "Firestore getPostsByAuthor failed", e);
            return filterByAuthor(getFallbackPosts(), authorName);
        }
    }

    @Override
    public List<TravelSharePost> getPostsByAuthorId(String authorId) {
        if (authorId == null) return new ArrayList<>();
        try {
            QuerySnapshot snap = Tasks.await(
                    firestore.collection(POSTS_COLLECTION).whereEqualTo("authorId", authorId).get(),
                    5,
                    TimeUnit.SECONDS
            );
            List<TravelSharePost> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snap) {
                result.add(mapDoc(doc));
            }
            if (!result.isEmpty()) {
                return result;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            Log.w("TravelShare", "Firestore getPostsByAuthorId failed", e);
            return new ArrayList<>();
        }
    }

    @Override
    public TravelSharePost getPostById(String postId) {
        if (postId == null) return null;
        try {
            DocumentSnapshot doc = Tasks.await(firestore.collection(POSTS_COLLECTION).document(postId).get(), 5, TimeUnit.SECONDS);
            if (doc.exists()) return mapDoc(doc);
        } catch (Exception e) {
            Log.w("TravelShare", "Firestore getPostById failed", e);
        }
        return fallbackRepository.getPostById(postId);
    }

    @Override
    public TravelSharePost createPost(String authorName, String locationName, String description, String period, String howToGetThere) {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.w("TravelShare", "Firestore createPost skipped: unauthenticated user");
                return null;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("authorId", currentUser.getUid());
            data.put("authorName", authorName);
            data.put("locationName", locationName);
            data.put("description", description);
            data.put("period", period);
            data.put("howToGetThere", howToGetThere);
            data.put("likeCount", 0);
            data.put("commentCount", 0);
            data.put("reportCount", 0);
            data.put("likedBy", new ArrayList<String>());
            data.put("createdAt", Timestamp.now());
            DocumentSnapshot ref = Tasks.await(firestore.collection(POSTS_COLLECTION).add(data).continueWithTask(task -> firestore.collection(POSTS_COLLECTION).document(task.getResult().getId()).get()));
            return mapDoc(ref);
        } catch (Exception e) {
            Log.w("TravelShare", "Firestore createPost failed", e);
            return null;
        }
    }

    @Override
    public boolean toggleLike(String postId) {
        if (postId == null) return false;
        String currentDisplay = TravelShareDataProvider.sessionRepository().getDisplayName();
        if (currentDisplay == null) currentDisplay = "anonymous";
        try {
            DocumentSnapshot doc = Tasks.await(firestore.collection(POSTS_COLLECTION).document(postId).get(), 5, TimeUnit.SECONDS);
            if (!doc.exists()) return false;
            List<String> likedBy = (List<String>) doc.get("likedBy");
            if (likedBy == null) likedBy = new ArrayList<>();
            boolean nowLiked;
            if (likedBy.contains(currentDisplay)) {
                likedBy.remove(currentDisplay);
                nowLiked = false;
            } else {
                likedBy.add(currentDisplay);
                nowLiked = true;
            }
            Map<String, Object> update = new HashMap<>();
            update.put("likedBy", likedBy);
            update.put("likeCount", likedBy.size());
            Tasks.await(firestore.collection(POSTS_COLLECTION).document(postId).update(update), 5, TimeUnit.SECONDS);
            return nowLiked;
        } catch (Exception e) {
            Log.w("TravelShare", "Firestore toggleLike failed", e);
            return false;
        }
    }

    @Override
    public int reportPost(String postId) {
        if (postId == null) return 0;
        try {
            DocumentSnapshot doc = Tasks.await(firestore.collection(POSTS_COLLECTION).document(postId).get(), 5, TimeUnit.SECONDS);
            long current = 0;
            if (doc.exists()) {
                Number n = doc.getLong("reportCount");
                if (n != null) current = n.longValue();
            }
            long updated = current + 1;
            Tasks.await(firestore.collection(POSTS_COLLECTION).document(postId).update("reportCount", updated), 5, TimeUnit.SECONDS);
            return (int) updated;
        } catch (Exception e) {
            Log.w("TravelShare", "Firestore reportPost failed", e);
            return 0;
        }
    }

    @Override
    public int addComment(String postId, String commentText) {
        if (postId == null) return 0;
        String currentDisplay = TravelShareDataProvider.sessionRepository().getDisplayName();
        if (currentDisplay == null) currentDisplay = "anonymous";
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.w("TravelShare", "Firestore addComment skipped: unauthenticated user");
                return 0;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("authorId", currentUser.getUid());
            data.put("authorDisplayName", currentDisplay);
            data.put("text", commentText);
            data.put("createdAt", Timestamp.now());
            Tasks.await(firestore.collection(POSTS_COLLECTION).document(postId).collection("comments").add(data), 5, TimeUnit.SECONDS);
            // increment commentCount
            Tasks.await(firestore.collection(POSTS_COLLECTION).document(postId).update("commentCount", com.google.firebase.firestore.FieldValue.increment(1)), 5, TimeUnit.SECONDS);
            DocumentSnapshot doc = Tasks.await(firestore.collection(POSTS_COLLECTION).document(postId).get(), 5, TimeUnit.SECONDS);
            Number n = doc.getLong("commentCount");
            return n == null ? 0 : n.intValue();
        } catch (Exception e) {
            Log.w("TravelShare", "Firestore addComment failed", e);
            return 0;
        }
    }

    private TravelSharePost mapDoc(DocumentSnapshot doc) {
        String id = doc.getId();
        String authorId = doc.getString("authorId");
        String authorName = doc.getString("authorName");
        String locationName = doc.getString("locationName");
        String description = doc.getString("description");
        String period = doc.getString("period");
        String howToGetThere = doc.getString("howToGetThere");
        Number like = doc.getLong("likeCount");
        Number comment = doc.getLong("commentCount");
        int likeCount = like == null ? 0 : like.intValue();
        int commentCount = comment == null ? 0 : comment.intValue();
        return new TravelSharePost(id, authorId, authorName, locationName, description, period, howToGetThere, likeCount, commentCount);
    }

    private List<TravelSharePost> getFallbackPosts() {
        // Keep fallback aligned with current requirement: 1 post per seeded user.
        List<TravelSharePost> all = fallbackRepository.getFeedPosts();
        List<TravelSharePost> out = new ArrayList<>();
        addFirstForAuthor(all, out, "Corentin");
        addFirstForAuthor(all, out, "Lina");
        addFirstForAuthor(all, out, "Mehdi");
        addFirstForAuthor(all, out, "Camille");
        addFirstForAuthor(all, out, "Nora");
        return out;
    }

    private List<TravelSharePost> filterByAuthor(List<TravelSharePost> posts, String authorName) {
        List<TravelSharePost> out = new ArrayList<>();
        for (TravelSharePost post : posts) {
            if (post.getAuthorName() != null && post.getAuthorName().equalsIgnoreCase(authorName.trim())) {
                out.add(post);
            }
        }
        return out;
    }

    private void addFirstForAuthor(List<TravelSharePost> source, List<TravelSharePost> target, String authorName) {
        for (TravelSharePost post : source) {
            if (post.getAuthorName() != null && post.getAuthorName().equalsIgnoreCase(authorName)) {
                target.add(post);
                return;
            }
        }
    }
}
