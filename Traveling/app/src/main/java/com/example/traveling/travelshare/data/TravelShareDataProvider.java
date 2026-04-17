package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelSharePostRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;

public final class TravelShareDataProvider {

    private static final TravelSharePostRepository POST_REPOSITORY = new InMemoryTravelSharePostRepository();
    private static final TravelShareSessionRepository SESSION_REPOSITORY = new InMemoryTravelShareSessionRepository();

    private TravelShareDataProvider() {
    }

    public static TravelSharePostRepository postRepository() {
        return POST_REPOSITORY;
    }

    public static TravelShareSessionRepository sessionRepository() {
        return SESSION_REPOSITORY;
    }
}
