package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelShareMessageRepository;
import com.example.traveling.travelshare.domain.TravelShareNotificationRepository;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;

public final class TravelShareDataProvider {

    private static final TravelSharePostRepository POST_REPOSITORY = new InMemoryTravelSharePostRepository();
    private static final TravelShareMessageRepository MESSAGE_REPOSITORY = new InMemoryTravelShareMessageRepository();
    private static final TravelShareSessionRepository SESSION_REPOSITORY = new InMemoryTravelShareSessionRepository();
    private static final TravelShareNotificationRepository NOTIFICATION_REPOSITORY = new InMemoryTravelShareNotificationRepository();

    private TravelShareDataProvider() {
    }

    public static TravelSharePostRepository postRepository() {
        return POST_REPOSITORY;
    }

    public static TravelShareMessageRepository messageRepository() {
        return MESSAGE_REPOSITORY;
    }

    public static TravelShareSessionRepository sessionRepository() {
        return SESSION_REPOSITORY;
    }

    public static TravelShareNotificationRepository notificationRepository() {
        return NOTIFICATION_REPOSITORY;
    }
}
