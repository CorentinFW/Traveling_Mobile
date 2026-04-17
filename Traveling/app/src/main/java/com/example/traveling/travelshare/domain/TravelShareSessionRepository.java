package com.example.traveling.travelshare.domain;

public interface TravelShareSessionRepository {
    boolean isAuthenticated();

    String getDisplayName();

    void login(String displayName);

    void logout();
}

