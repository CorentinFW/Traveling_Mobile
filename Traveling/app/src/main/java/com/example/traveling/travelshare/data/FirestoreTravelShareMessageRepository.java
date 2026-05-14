package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelShareConversation;
import com.example.traveling.travelshare.domain.TravelShareGroup;
import com.example.traveling.travelshare.domain.TravelShareMessage;
import com.example.traveling.travelshare.domain.TravelShareMessageRepository;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Minimal Firestore implementation for conversations and groups.
 * Blocking calls via Tasks.await — do not call on Android main thread.
 */
public class FirestoreTravelShareMessageRepository implements TravelShareMessageRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static final String CONVERSATIONS_COLLECTION = "conversations";

    @Override
    public List<TravelShareConversation> getConversations() {
        String current = TravelShareDataProvider.sessionRepository().getDisplayName();
        if (current == null) return new ArrayList<>();
        try {
            QuerySnapshot snap = Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).whereArrayContains("participants", current).get(), 5, TimeUnit.SECONDS);
            List<TravelShareConversation> out = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snap) {
                out.add(mapConversation(doc));
            }
            return out;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public TravelShareConversation getConversationById(String conversationId) {
        try {
            DocumentSnapshot doc = Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId).get(), 5, TimeUnit.SECONDS);
            if (doc.exists()) return mapConversation(doc);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public List<TravelShareMessage> getMessages(String conversationId) {
        if (conversationId == null) return new ArrayList<>();
        try {
            QuerySnapshot snap = Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId).collection("messages").get(), 5, TimeUnit.SECONDS);
            List<TravelShareMessage> msgs = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                msgs.add(mapMessage(conversationId, d));
            }
            return msgs;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public TravelShareMessage sendMessage(String conversationId, String senderName, String text) {
        if (conversationId == null) return null;
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("senderName", senderName);
            data.put("text", text);
            data.put("createdAt", Timestamp.now());
            DocumentSnapshot added = Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId).collection("messages").add(data).continueWithTask(task -> firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId).collection("messages").document(task.getResult().getId()).get()));
            // update last message
            Map<String, Object> upd = new HashMap<>();
            upd.put("lastMessage", text);
            upd.put("lastUpdated", Timestamp.now());
            Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId).update(upd), 5, TimeUnit.SECONDS);
            return mapMessage(conversationId, added);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<TravelShareGroup> getGroups() {
        try {
            QuerySnapshot snap = Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).whereEqualTo("isGroup", true).get(), 5, TimeUnit.SECONDS);
            List<TravelShareGroup> out = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                out.add(mapGroup(d));
            }
            return out;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public TravelShareGroup getGroupById(String groupId) {
        try {
            DocumentSnapshot doc = Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).document(groupId).get(), 5, TimeUnit.SECONDS);
            if (doc.exists()) return mapGroup(doc);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public TravelShareGroup createGroup(String name, String ownerName, List<String> initialMembers) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("isGroup", true);
            data.put("name", name);
            data.put("ownerName", ownerName);
            List<String> participants = new ArrayList<>();
            if (initialMembers != null) participants.addAll(initialMembers);
            if (!participants.contains(ownerName)) participants.add(ownerName);
            data.put("participants", participants);
            data.put("createdAt", Timestamp.now());
            DocumentSnapshot added = Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).add(data).continueWithTask(task -> firestore.collection(CONVERSATIONS_COLLECTION).document(task.getResult().getId()).get()));
            return mapGroup(added);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean addMemberToGroup(String groupId, String memberName) {
        try {
            DocumentSnapshot doc = Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).document(groupId).get(), 5, TimeUnit.SECONDS);
            if (!doc.exists()) return false;
            List<String> participants = (List<String>) doc.get("participants");
            if (participants == null) participants = new ArrayList<>();
            if (participants.contains(memberName)) return false;
            participants.add(memberName);
            Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).document(groupId).update("participants", participants), 5, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeMemberFromGroup(String groupId, String memberName) {
        try {
            DocumentSnapshot doc = Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).document(groupId).get(), 5, TimeUnit.SECONDS);
            if (!doc.exists()) return false;
            List<String> participants = (List<String>) doc.get("participants");
            if (participants == null || !participants.contains(memberName)) return false;
            participants.remove(memberName);
            Tasks.await(firestore.collection(CONVERSATIONS_COLLECTION).document(groupId).update("participants", participants), 5, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TravelShareMessage sendGroupMessage(String groupId, String senderName, String text) {
        return sendMessage(groupId, senderName, text);
    }

    private TravelShareConversation mapConversation(DocumentSnapshot doc) {
        String id = doc.getId();
        String display = doc.getString("lastMessage");
        boolean isGroup = Boolean.TRUE.equals(doc.getBoolean("isGroup"));
        String title = doc.getString("name");
        String other = doc.getString("ownerName");
        return new TravelShareConversation(id, title == null ? other : title, isGroup, display == null ? "" : display, "");
    }

    private TravelShareMessage mapMessage(String conversationId, DocumentSnapshot doc) {
        String id = doc.getId();
        String sender = doc.getString("senderName");
        String text = doc.getString("text");
        com.google.firebase.Timestamp ts = doc.getTimestamp("createdAt");
        String time = ts == null ? "" : ts.toDate().toString();
        return new TravelShareMessage(id, conversationId, sender, text, time);
    }

    private TravelShareGroup mapGroup(DocumentSnapshot doc) {
        String id = doc.getId();
        String name = doc.getString("name");
        String owner = doc.getString("ownerName");
        List<String> members = (List<String>) doc.get("participants");
        if (members == null) members = new ArrayList<>();
        return new TravelShareGroup(id, name == null ? "Groupe" : name, owner == null ? "" : owner, members);
    }
}


