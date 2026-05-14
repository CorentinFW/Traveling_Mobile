package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelShareUser;
import com.example.traveling.travelshare.domain.TravelShareFriendRequestResult;
import com.example.traveling.travelshare.domain.TravelShareUserRepository;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Firestore-backed implementation of TravelShareUserRepository.
 * This implementation performs synchronous (blocking) calls using Tasks.await for simplicity.
 * Callers should avoid invoking these methods on the Android main thread.
 */
public class FirestoreTravelShareUserRepository implements TravelShareUserRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final InMemoryTravelShareUserRepository fallbackRepository = new InMemoryTravelShareUserRepository();
    private static final String USERS_COLLECTION = "users";
    private static final String FRIEND_REQUESTS_COLLECTION = "friendRequests";

    @Override
    public TravelShareFriendRequestResult sendFriendRequestDetailedByIds(String requesterUserId, String targetUserId) {
        if (requesterUserId == null || requesterUserId.trim().isEmpty()) {
            return TravelShareFriendRequestResult.REQUESTER_NOT_FOUND;
        }
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            return TravelShareFriendRequestResult.TARGET_NOT_FOUND;
        }

        String requesterId = requesterUserId.trim();
        String targetId = targetUserId.trim();
        if (requesterId.equals(targetId)) {
            return TravelShareFriendRequestResult.SELF_REQUEST;
        }

        try {
            TravelShareUser requester = getUserById(requesterId);
            TravelShareUser target = getUserById(targetId);
            if (requester == null || target == null) {
                return requester == null ? TravelShareFriendRequestResult.REQUESTER_NOT_FOUND : TravelShareFriendRequestResult.TARGET_NOT_FOUND;
            }
            if (requester.isFriendWith(target.getId())) {
                return TravelShareFriendRequestResult.ALREADY_FRIENDS;
            }

            String requestId = requester.getId() + "_" + target.getId();
            DocumentSnapshot existing = Tasks.await(
                    firestore.collection(FRIEND_REQUESTS_COLLECTION).document(requestId).get(),
                    5,
                    TimeUnit.SECONDS
            );
            if (existing.exists()) {
                String status = existing.getString("status");
                if ("pending".equalsIgnoreCase(status)) {
                    return TravelShareFriendRequestResult.ALREADY_PENDING;
                }
                if ("accepted".equalsIgnoreCase(status)) {
                    return TravelShareFriendRequestResult.ALREADY_FRIENDS;
                }
                if ("rejected".equalsIgnoreCase(status)) {
                    return TravelShareFriendRequestResult.FAILED;
                }
            }

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("fromUserId", requester.getId());
            requestData.put("toUserId", target.getId());
            requestData.put("status", "pending");
            requestData.put("createdAt", com.google.firebase.Timestamp.now());

            Tasks.await(
                    firestore.collection(FRIEND_REQUESTS_COLLECTION).document(requestId).set(requestData),
                    5,
                    TimeUnit.SECONDS
            );
            return TravelShareFriendRequestResult.SENT;
        } catch (Exception e) {
            return fallbackRepository.sendFriendRequestDetailedByIds(requesterUserId, targetUserId);
        }
    }

    @Override
    public List<TravelShareUser> getUsers() {
        try {
            QuerySnapshot snap = Tasks.await(firestore.collection(USERS_COLLECTION).get(), 5, TimeUnit.SECONDS);
            List<TravelShareUser> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snap) {
                result.add(mapDoc(doc));
            }
            return result.isEmpty() ? fallbackRepository.getUsers() : result;
        } catch (Exception e) {
            return fallbackRepository.getUsers();
        }
    }

    @Override
    public TravelShareUser getUserById(String userId) {
        if (userId == null) return null;
        try {
            DocumentSnapshot doc = Tasks.await(firestore.collection(USERS_COLLECTION).document(userId).get(), 5, TimeUnit.SECONDS);
            if (doc.exists()) {
                return mapDoc(doc);
            }
        } catch (Exception ignored) {
        }
        return fallbackRepository.getUserById(userId);
    }

    @Override
    public TravelShareUser getUserByDisplayName(String displayName) {
        if (displayName == null) return null;
        try {
            String normalized = displayName.trim();
            // Prefer pseudo field, fallback to displayName field
            QuerySnapshot snap = Tasks.await(firestore.collection(USERS_COLLECTION).whereEqualTo("pseudo", normalized).get(), 5, TimeUnit.SECONDS);
            if (!snap.isEmpty()) {
                return mapDoc(snap.getDocuments().get(0));
            }
            snap = Tasks.await(firestore.collection(USERS_COLLECTION).whereEqualTo("displayName", normalized).get(), 5, TimeUnit.SECONDS);
            if (!snap.isEmpty()) {
                return mapDoc(snap.getDocuments().get(0));
            }

            // Fallback for legacy/seeded data where display comes from firstName/fullName.
            List<TravelShareUser> allUsers = getUsers();
            for (TravelShareUser user : allUsers) {
                if (equalsIgnoreCase(user.getId(), normalized)
                        || equalsIgnoreCase(user.getPseudo(), normalized)
                        || equalsIgnoreCase(user.getFirstName(), normalized)
                        || equalsIgnoreCase(user.getLastName(), normalized)
                        || equalsIgnoreCase(user.getFullName(), normalized)) {
                    return user;
                }
            }
        } catch (Exception ignored) {
        }
        return fallbackRepository.getUserByDisplayName(displayName);
    }

    @Override
    public List<TravelShareUser> getFriendsOf(String displayName) {
        TravelShareUser user = getUserByDisplayName(displayName);
        if (user == null) return Collections.emptyList();
        List<TravelShareUser> friends = new ArrayList<>();
        for (String friendId : user.getFriendIds()) {
            TravelShareUser f = getUserById(friendId);
            if (f != null) friends.add(f);
        }
        return friends;
    }

    @Override
    public List<TravelShareUser> searchUsers(String query) {
        List<TravelShareUser> users = getUsers();
        if (query == null || query.trim().isEmpty()) {
            return users;
        }
        String normalized = query.trim().toLowerCase();
        List<TravelShareUser> matches = new ArrayList<>();
        for (TravelShareUser user : users) {
            if (containsIgnoreCase(user.getId(), normalized)
                    || containsIgnoreCase(user.getFirstName(), normalized)
                    || containsIgnoreCase(user.getLastName(), normalized)
                    || containsIgnoreCase(user.getPseudo(), normalized)
                    || containsIgnoreCase(user.getFullName(), normalized)) {
                matches.add(user);
            }
        }
        return matches;
    }

    @Override
    public TravelShareFriendRequestResult sendFriendRequestDetailed(String requesterDisplayName, String targetDisplayName) {
        TravelShareUser requester = resolveUserForRequest(requesterDisplayName);
        TravelShareUser target = resolveUserForRequest(targetDisplayName);
        if (requester == null || target == null) {
            return requester == null ? TravelShareFriendRequestResult.REQUESTER_NOT_FOUND : TravelShareFriendRequestResult.TARGET_NOT_FOUND;
        }
        return sendFriendRequestDetailedByIds(requester.getId(), target.getId());
    }

    @Override
    public boolean acceptFriendRequest(String targetDisplayName, String requesterDisplayName) {
        TravelShareUser target = resolveUserForRequest(targetDisplayName);
        TravelShareUser requester = resolveUserForRequest(requesterDisplayName);
        if (target == null || requester == null) {
            return false;
        }

        String requestId = requester.getId() + "_" + target.getId();
        try {
            DocumentSnapshot request = Tasks.await(
                    firestore.collection(FRIEND_REQUESTS_COLLECTION).document(requestId).get(),
                    5,
                    TimeUnit.SECONDS
            );
            if (!request.exists() || !"pending".equalsIgnoreCase(request.getString("status"))) {
                return false;
            }

            Tasks.await(
                    firestore.collection(USERS_COLLECTION).document(target.getId())
                            .update("friendIds", FieldValue.arrayUnion(requester.getId())),
                    5,
                    TimeUnit.SECONDS
            );
            Tasks.await(
                    firestore.collection(USERS_COLLECTION).document(requester.getId())
                            .update("friendIds", FieldValue.arrayUnion(target.getId())),
                    5,
                    TimeUnit.SECONDS
            );
            Tasks.await(
                    firestore.collection(FRIEND_REQUESTS_COLLECTION).document(requestId).update("status", "accepted"),
                    5,
                    TimeUnit.SECONDS
            );
            return true;
        } catch (Exception e) {
            return fallbackRepository.acceptFriendRequest(targetDisplayName, requesterDisplayName);
        }
    }

    @Override
    public List<TravelShareUser> getPendingFriendRequests(String displayName) {
        TravelShareUser target = getUserByDisplayName(displayName);
        if (target == null) {
            return Collections.emptyList();
        }
        try {
            QuerySnapshot snap = Tasks.await(
                    firestore.collection(FRIEND_REQUESTS_COLLECTION)
                            .whereEqualTo("toUserId", target.getId())
                            .whereEqualTo("status", "pending")
                            .get(),
                    5,
                    TimeUnit.SECONDS
            );
            List<TravelShareUser> pending = new ArrayList<>();
            for (QueryDocumentSnapshot requestDoc : snap) {
                String requesterId = requestDoc.getString("fromUserId");
                TravelShareUser requester = getUserById(requesterId);
                if (requester != null) {
                    pending.add(requester);
                }
            }
            return pending;
        } catch (Exception e) {
            return fallbackRepository.getPendingFriendRequests(displayName);
        }
    }

    private TravelShareUser mapDoc(DocumentSnapshot doc) {
        String id = doc.getId();
        String firstName = safeGetString(doc.getData(), "firstName");
        String lastName = safeGetString(doc.getData(), "lastName");
        String pseudo = safeGetString(doc.getData(), "pseudo");
        Object rawFriends = doc.getData() != null ? doc.getData().get("friendIds") : null;
        List<String> friendIds = new ArrayList<>();
        if (rawFriends instanceof List) {
            for (Object o : (List<?>) rawFriends) {
                if (o != null) friendIds.add(String.valueOf(o));
            }
        }
        return new TravelShareUser(id, firstName, lastName, pseudo, friendIds);
    }

    private String safeGetString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private boolean equalsIgnoreCase(String value, String expected) {
        return value != null && expected != null && value.equalsIgnoreCase(expected);
    }

    private TravelShareUser resolveUserForRequest(String displayValue) {
        TravelShareUser resolved = getUserByDisplayName(displayValue);
        if (resolved != null) {
            return resolved;
        }

        String localPart = extractLocalPart(displayValue);
        if (localPart != null) {
            resolved = getUserByDisplayName(localPart);
            if (resolved != null) {
                return resolved;
            }
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            resolved = getUserByDisplayName(firebaseUser.getDisplayName());
            if (resolved != null) {
                return resolved;
            }
            resolved = getUserByDisplayName(firebaseUser.getEmail());
            if (resolved != null) {
                return resolved;
            }
            String firebaseEmailLocalPart = extractLocalPart(firebaseUser.getEmail());
            if (firebaseEmailLocalPart != null) {
                resolved = getUserByDisplayName(firebaseEmailLocalPart);
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return null;
    }

    private String extractLocalPart(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("@")) {
            trimmed = trimmed.substring(1);
        }
        int atIndex = trimmed.indexOf('@');
        if (atIndex > 0) {
            return trimmed.substring(0, atIndex);
        }
        return trimmed;
    }
}

