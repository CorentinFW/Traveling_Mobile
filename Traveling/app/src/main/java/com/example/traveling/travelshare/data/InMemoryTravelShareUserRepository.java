package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelShareUser;
import com.example.traveling.travelshare.domain.TravelShareFriendRequestResult;
import com.example.traveling.travelshare.domain.TravelShareUserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class InMemoryTravelShareUserRepository implements TravelShareUserRepository {

    private final List<TravelShareUser> users = new ArrayList<>(Arrays.asList(
            new TravelShareUser("u1", "Corentin", "Dupont", "corentin", Arrays.asList("u2", "u4", "u5")),
            new TravelShareUser("u2", "Lina", "Martin", "lina", Arrays.asList("u1", "u4")),
            new TravelShareUser("u3", "Mehdi", "Benali", "mehdi", Collections.singletonList("u5")),
            new TravelShareUser("u4", "Camille", "Laurent", "camille", Arrays.asList("u1", "u2", "u5")),
            new TravelShareUser("u5", "Nora", "Diallo", "nora", Arrays.asList("u1", "u4", "u3"))
    ));
    private final Map<String, Set<String>> pendingByTargetUserId = new HashMap<>();

    @Override
    public List<TravelShareUser> getUsers() {
        return new ArrayList<>(users);
    }

    @Override
    public TravelShareUser getUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        String normalizedId = userId.trim();
        for (TravelShareUser user : users) {
            if (user.getId().equalsIgnoreCase(normalizedId)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public TravelShareUser getUserByDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }

        String normalizedDisplayName = displayName.trim();
        for (TravelShareUser user : users) {
            if (matchesDisplayName(user, normalizedDisplayName)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public List<TravelShareUser> getFriendsOf(String displayName) {
        TravelShareUser user = getUserByDisplayName(displayName);
        if (user == null) {
            return Collections.emptyList();
        }

        List<TravelShareUser> friends = new ArrayList<>();
        for (String friendId : user.getFriendIds()) {
            TravelShareUser friend = getUserById(friendId);
            if (friend != null) {
                friends.add(friend);
            }
        }
        return friends;
    }

    @Override
    public List<TravelShareUser> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getUsers();
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
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
    public TravelShareFriendRequestResult sendFriendRequestDetailedByIds(String requesterUserId, String targetUserId) {
        TravelShareUser requester = getUserById(requesterUserId);
        TravelShareUser target = getUserById(targetUserId);
        if (requester == null || target == null) {
            return requester == null ? TravelShareFriendRequestResult.REQUESTER_NOT_FOUND : TravelShareFriendRequestResult.TARGET_NOT_FOUND;
        }
        if (requester.getId().equals(target.getId())) {
            return TravelShareFriendRequestResult.SELF_REQUEST;
        }
        if (requester.isFriendWith(target.getId())) {
            return TravelShareFriendRequestResult.ALREADY_FRIENDS;
        }
        Set<String> pending = pendingByTargetUserId.computeIfAbsent(target.getId(), key -> new HashSet<>());
        if (!pending.add(requester.getId())) {
            return TravelShareFriendRequestResult.ALREADY_PENDING;
        }
        return TravelShareFriendRequestResult.SENT;
    }

    @Override
    public TravelShareFriendRequestResult sendFriendRequestDetailed(String requesterDisplayName, String targetDisplayName) {
        TravelShareUser requester = getUserByDisplayName(requesterDisplayName);
        TravelShareUser target = getUserByDisplayName(targetDisplayName);
        if (requester == null || target == null) {
            return requester == null ? TravelShareFriendRequestResult.REQUESTER_NOT_FOUND : TravelShareFriendRequestResult.TARGET_NOT_FOUND;
        }
        if (requester.getId().equals(target.getId())) {
            return TravelShareFriendRequestResult.SELF_REQUEST;
        }
        if (requester.isFriendWith(target.getId())) {
            return TravelShareFriendRequestResult.ALREADY_FRIENDS;
        }
        Set<String> pending = pendingByTargetUserId.computeIfAbsent(target.getId(), key -> new HashSet<>());
        if (!pending.add(requester.getId())) {
            return TravelShareFriendRequestResult.ALREADY_PENDING;
        }
        return TravelShareFriendRequestResult.SENT;
    }

    @Override
    public boolean acceptFriendRequest(String targetDisplayName, String requesterDisplayName) {
        TravelShareUser target = getUserByDisplayName(targetDisplayName);
        TravelShareUser requester = getUserByDisplayName(requesterDisplayName);
        if (target == null || requester == null) {
            return false;
        }

        Set<String> pending = pendingByTargetUserId.get(target.getId());
        if (pending == null || !pending.remove(requester.getId())) {
            return false;
        }

        boolean requesterUpdated = requester.addFriendId(target.getId());
        boolean targetUpdated = target.addFriendId(requester.getId());
        return requesterUpdated || targetUpdated;
    }

    @Override
    public List<TravelShareUser> getPendingFriendRequests(String displayName) {
        TravelShareUser target = getUserByDisplayName(displayName);
        if (target == null) {
            return Collections.emptyList();
        }
        Set<String> pending = pendingByTargetUserId.get(target.getId());
        if (pending == null || pending.isEmpty()) {
            return Collections.emptyList();
        }
        List<TravelShareUser> requesters = new ArrayList<>();
        for (String requesterId : pending) {
            TravelShareUser requester = getUserById(requesterId);
            if (requester != null) {
                requesters.add(requester);
            }
        }
        return requesters;
    }

    private boolean matchesDisplayName(TravelShareUser user, String displayName) {
        String lowerDisplayName = displayName.toLowerCase(Locale.ROOT);
        return user.getId().equalsIgnoreCase(displayName)
                || user.getFirstName().equalsIgnoreCase(displayName)
                || user.getLastName().equalsIgnoreCase(displayName)
                || user.getPseudo().equalsIgnoreCase(displayName)
                || user.getFullName().equalsIgnoreCase(displayName)
                || user.getFirstName().toLowerCase(Locale.ROOT).equals(lowerDisplayName)
                || user.getPseudo().toLowerCase(Locale.ROOT).equals(lowerDisplayName)
                || user.getFullName().toLowerCase(Locale.ROOT).equals(lowerDisplayName);
    }

    private boolean containsIgnoreCase(String value, String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }
}



