package com.example.traveling.travelshare.domain;

public class TravelShareConversation {

    private final String id;
    private final String title;
    private final boolean groupConversation;
    private String lastMessagePreview;
    private String lastActivityLabel;

    public TravelShareConversation(
            String id,
            String title,
            boolean groupConversation,
            String lastMessagePreview,
            String lastActivityLabel
    ) {
        this.id = id;
        this.title = title;
        this.groupConversation = groupConversation;
        this.lastMessagePreview = lastMessagePreview;
        this.lastActivityLabel = lastActivityLabel;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isGroupConversation() {
        return groupConversation;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public String getLastActivityLabel() {
        return lastActivityLabel;
    }

    public void updateLastMessage(String preview, String activityLabel) {
        if (preview != null && !preview.trim().isEmpty()) {
            lastMessagePreview = preview.trim();
        }
        if (activityLabel != null && !activityLabel.trim().isEmpty()) {
            lastActivityLabel = activityLabel.trim();
        }
    }
}

