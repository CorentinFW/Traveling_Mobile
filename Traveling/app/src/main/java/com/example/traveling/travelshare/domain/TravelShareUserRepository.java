package com.example.traveling.travelshare.domain;

import java.util.List;

@SuppressWarnings("unused")
public interface TravelShareUserRepository {

    List<TravelShareUser> getUsers();

    TravelShareUser getUserById(String userId);

    TravelShareUser getUserByDisplayName(String displayName);

    List<TravelShareUser> getFriendsOf(String displayName);

    List<TravelShareUser> searchUsers(String query);

    TravelShareFriendRequestResult sendFriendRequestDetailedByIds(String requesterUserId, String targetUserId);

    TravelShareFriendRequestResult sendFriendRequestDetailed(String requesterDisplayName, String targetDisplayName);

    default boolean sendFriendRequest(String requesterDisplayName, String targetDisplayName) {
        return sendFriendRequestDetailed(requesterDisplayName, targetDisplayName) == TravelShareFriendRequestResult.SENT;
    }

    boolean acceptFriendRequest(String targetDisplayName, String requesterDisplayName);

    List<TravelShareUser> getPendingFriendRequests(String displayName);
}


