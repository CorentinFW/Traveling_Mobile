package com.example.traveling.travelshare.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TravelShareUser {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String pseudo;
    private final List<String> friendIds = new ArrayList<>();

    public TravelShareUser(String id, String firstName, String lastName, String pseudo, List<String> friendIds) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pseudo = pseudo;
        if (friendIds != null) {
            for (String friendId : friendIds) {
                addUniqueFriend(friendId);
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            return lastName == null ? "" : lastName.trim();
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            return firstName.trim();
        }
        return firstName.trim() + " " + lastName.trim();
    }

    public List<String> getFriendIds() {
        return Collections.unmodifiableList(friendIds);
    }

    public boolean isFriendWith(String otherUserId) {
        if (otherUserId == null) {
            return false;
        }
        return friendIds.contains(otherUserId.trim());
    }

    public boolean addFriendId(String friendId) {
        return addUniqueFriend(friendId);
    }

    public boolean removeFriendId(String friendId) {
        if (friendId == null) {
            return false;
        }
        return friendIds.remove(friendId.trim());
    }

    private boolean addUniqueFriend(String friendId) {
        if (friendId == null) {
            return false;
        }
        String normalizedFriendId = friendId.trim();
        if (normalizedFriendId.isEmpty() || friendIds.contains(normalizedFriendId) || normalizedFriendId.equals(id)) {
            return false;
        }
        friendIds.add(normalizedFriendId);
        return true;
    }
}

