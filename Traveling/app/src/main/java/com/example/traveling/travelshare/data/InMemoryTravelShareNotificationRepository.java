package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.domain.TravelShareNotification;
import com.example.traveling.travelshare.domain.TravelShareNotificationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryTravelShareNotificationRepository implements TravelShareNotificationRepository {
    private final List<TravelShareNotification> notifications = new ArrayList<>();

    public InMemoryTravelShareNotificationRepository() {
        // Ajouter quelques fausses données par défaut
        addNotification("current_user", "LIKE", "Alice a aimé votre post.");
        addNotification("current_user", "COMMENT", "Bob a commenté: 'Super voyage !'");
        addNotification("current_user", "INVITE", "Charlie vous a invité dans le groupe 'Gros Voyageurs'.");
    }

    @Override
    public List<TravelShareNotification> getNotificationsForUser(String userId) {
        List<TravelShareNotification> result = new ArrayList<>();
        for (TravelShareNotification n : notifications) {
            if (n.getUserId().equals(userId)) {
                result.add(n);
            }
        }
        // Tri du plus récent au plus ancien
        result.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return result;
    }

    @Override
    public void markAsRead(String notificationId) {
        for (TravelShareNotification n : notifications) {
            if (n.getId().equals(notificationId)) {
                n.setRead(true);
                break;
            }
        }
    }

    @Override
    public void addNotification(String userId, String type, String content) {
        String id = UUID.randomUUID().toString();
        TravelShareNotification n = new TravelShareNotification(id, userId, type, content, System.currentTimeMillis(), false);
        notifications.add(n);
    }
}