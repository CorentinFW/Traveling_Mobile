package com.example.traveling.travelshare.data;

import android.util.Log;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight seeder: creates five users, one post per user, sample conversations,
 * and a sample pending friend request.
 * Intended to run once on app startup if Firestore is empty. Runs on a background thread.
 */
public class TravelShareFirestoreSeeder {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void seedIfEmpty() {
        seedIfMissing();
    }

    public void seedIfMissing() {
        new Thread(() -> {
            try {
                // Ensure users u1..u5 exist.
                ensureUser("u1", "Corentin", "Dupont", "corentin", new String[]{"u2","u4","u5"});
                ensureUser("u2", "Lina", "Martin", "lina", new String[]{"u1","u4"});
                ensureUser("u3", "Mehdi", "Benali", "mehdi", new String[]{"u5"});
                ensureUser("u4", "Camille", "Laurent", "camille", new String[]{"u1","u2","u5"});
                ensureUser("u5", "Nora", "Diallo", "nora", new String[]{"u1","u4","u3"});

                // Keep exactly the requested seed set: 1 seed post per seed user.
                ensurePost("p1","u1","Corentin","Tokyo","Nuit neon et ramen","Automne 2025","Marche facile");
                ensurePost("p2","u2","Lina","New York","Rues, cafes et concerts","Ete 2024","Metro local");
                ensurePost("p3","u3","Mehdi","Reykjavik","Aurores boreales","Automne 2024","Vol direct");
                ensurePost("p4","u4","Camille","Paris","Musees et bistros","Printemps 2025","Metro");
                ensurePost("p5","u5","Nora","Rome","Fontaines et ruelles","Automne 2024","A pied");
                pruneLegacySeedPosts();

                ensureConversation("c1", new String[]{"corentin","lina"}, "Tu as des photos de Tokyo ?");
                ensureConversation("c2", new String[]{"corentin","camille"}, "On organise le prochain week-end ?");
                ensureConversation("c3", new String[]{"corentin","nora"}, "J'ai repere un spot incroyable a Reykjavik");

                ensureFriendRequestSample();

            } catch (Exception e) {
                Log.w("TravelShare", "Firestore seeding failed", e);
            }
        }).start();
    }

    private void ensureUser(String id, String firstName, String lastName, String pseudo, String[] friends) throws Exception {
        DocumentReference ref = firestore.collection("users").document(id);
        boolean exists = Boolean.TRUE.equals(Tasks.await(ref.get(), 5, TimeUnit.SECONDS).exists());
        if (!exists) {
            createUser(id, firstName, lastName, pseudo, friends);
        }
    }

    private void ensurePost(String postId, String authorId, String authorName, String location, String description, String period, String howTo) throws Exception {
        DocumentReference ref = firestore.collection("posts").document(postId);
        boolean exists = Boolean.TRUE.equals(Tasks.await(ref.get(), 5, TimeUnit.SECONDS).exists());
        if (!exists) {
            createPost(postId, authorId, authorName, location, description, period, howTo);
        }
    }

    private void ensureConversation(String id, String[] participants, String initialMessage) throws Exception {
        DocumentReference ref = firestore.collection("conversations").document(id);
        boolean exists = Boolean.TRUE.equals(Tasks.await(ref.get(), 5, TimeUnit.SECONDS).exists());
        if (!exists) {
            createConversation(id, participants, initialMessage);
        }
    }

    private void ensureFriendRequestSample() throws Exception {
        DocumentReference ref = firestore.collection("friendRequests").document("u3_u1");
        boolean exists = Boolean.TRUE.equals(Tasks.await(ref.get(), 5, TimeUnit.SECONDS).exists());
        if (!exists) {
            createFriendRequestSample();
        }
    }

    private void pruneLegacySeedPosts() throws Exception {
        // Remove old oversized seed set (p6..p15) while keeping user-created posts untouched.
        for (int i = 6; i <= 15; i++) {
            String postId = "p" + i;
            DocumentReference ref = firestore.collection("posts").document(postId);
            boolean exists = Boolean.TRUE.equals(Tasks.await(ref.get(), 5, TimeUnit.SECONDS).exists());
            if (exists) {
                Tasks.await(ref.delete(), 5, TimeUnit.SECONDS);
            }
        }
    }

    private void createUser(String id, String firstName, String lastName, String pseudo, String[] friends) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("pseudo", pseudo);
        List<String> friendIds = new ArrayList<>();
        if (friends != null) {
            Collections.addAll(friendIds, friends);
        }
        data.put("friendIds", friendIds);
        Tasks.await(firestore.collection("users").document(id).set(data),5, TimeUnit.SECONDS);
    }

    private void createPost(String postId, String authorId, String authorName, String location, String description, String period, String howTo) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("authorId", authorId);
        data.put("authorName", authorName);
        data.put("locationName", location);
        data.put("description", description);
        data.put("period", period);
        data.put("howToGetThere", howTo);
        data.put("likeCount", 0);
        data.put("commentCount", 0);
        data.put("reportCount", 0);
        data.put("likedBy", new ArrayList<String>());
        data.put("createdAt", Timestamp.now());
        Tasks.await(firestore.collection("posts").document(postId).set(data),5, TimeUnit.SECONDS);
    }

    private void createConversation(String id, String[] participants, String initialMessage) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("isGroup", false);
        List<String> parts = new ArrayList<>();
        Collections.addAll(parts, participants);
        data.put("participants", parts);
        data.put("lastMessage", initialMessage);
        data.put("lastUpdated", Timestamp.now());
        DocumentReference ref = firestore.collection("conversations").document(id);
        Tasks.await(ref.set(data),5, TimeUnit.SECONDS);
        // add initial message
        Map<String, Object> msg = new HashMap<>();
        msg.put("senderName", participants[0]);
        msg.put("text", initialMessage);
        msg.put("createdAt", Timestamp.now());
        Tasks.await(ref.collection("messages").add(msg),5, TimeUnit.SECONDS);
    }

    private void createFriendRequestSample() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("fromUserId", "u3");
        data.put("toUserId", "u1");
        data.put("status", "pending");
        data.put("createdAt", Timestamp.now());
        String id = "u3_u1";
        Tasks.await(firestore.collection("friendRequests").document(id).set(data), 5, TimeUnit.SECONDS);
    }
}
