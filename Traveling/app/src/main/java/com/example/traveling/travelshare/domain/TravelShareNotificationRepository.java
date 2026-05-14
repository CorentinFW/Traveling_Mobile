package com.example.traveling.travelshare.domain;

import java.util.List;

public interface TravelShareNotificationRepository {
    List<TravelShareNotification> getNotificationsForUser(String userId);
    void markAsRead(String notificationId);
    void addNotification(String userId, String type, String content);
}