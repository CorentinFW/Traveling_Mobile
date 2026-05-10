package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelShareSessionRepository;

public class InMemoryTravelShareSessionRepository implements TravelShareSessionRepository {

    private boolean authenticated;
    private String displayName;

    public InMemoryTravelShareSessionRepository() {
        authenticated = false;
        displayName = "Voyageur";
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void login(String displayName) {
        authenticated = true;
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.displayName = displayName.trim();
        }
    }

    @Override
    public void logout() {
        authenticated = false;
        displayName = "Voyageur";
    }
}

