package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelShareMessageRepository;
import com.example.traveling.travelshare.domain.TravelShareNotificationRepository;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;
import com.example.traveling.travelshare.domain.TravelShareUserRepository;

public final class TravelShareDataProvider {

    // Default to in-memory providers. Call useFirestore(true) to switch to Firestore-backed implementations.
    private static TravelSharePostRepository POST_REPOSITORY = new InMemoryTravelSharePostRepository();
    private static TravelShareMessageRepository MESSAGE_REPOSITORY = new InMemoryTravelShareMessageRepository();
    private static TravelShareSessionRepository SESSION_REPOSITORY = new InMemoryTravelShareSessionRepository();
    private static TravelShareNotificationRepository NOTIFICATION_REPOSITORY = new InMemoryTravelShareNotificationRepository();
    private static TravelShareUserRepository USER_REPOSITORY = null;

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

    public static TravelShareUserRepository userRepository() {
        if (USER_REPOSITORY == null) {
            // default to in-memory if not initialized
            USER_REPOSITORY = new InMemoryTravelShareUserRepository();
        }
        return USER_REPOSITORY;
    }

    /**
     * Switch the TravelShare data providers to use Firestore-backed implementations.
     * Call during app startup (off the main thread) if Firestore is available.
     */
    public static void useFirestore() {
        try {
            POST_REPOSITORY = new FirestoreTravelSharePostRepository();
            MESSAGE_REPOSITORY = new FirestoreTravelShareMessageRepository();
            NOTIFICATION_REPOSITORY = new InMemoryTravelShareNotificationRepository(); // keep notifications in-memory for now
            USER_REPOSITORY = new FirestoreTravelShareUserRepository();
            // SESSION_REPOSITORY remains the same: session is local but may integrate with Firebase Auth elsewhere
        } catch (Throwable t) {
            // If Firestore classes are not available at runtime, keep in-memory providers and log if possible.
            // Avoid crashing the app during automatic switch.
        }
    }
}
