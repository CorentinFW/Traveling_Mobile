package com.example.traveling.travelshare.domain;

import java.util.Objects;

public class TravelShareNotification {
    private final String id;
    private final String userId; // User who receives the notification
    private final String type; // LIKE, COMMENT, INVITE, etc.
    private final String content;
    private final long timestamp;
    private boolean isRead;

    public TravelShareNotification(String id, String userId, String type, String content, long timestamp, boolean isRead) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelShareNotification that = (TravelShareNotification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
