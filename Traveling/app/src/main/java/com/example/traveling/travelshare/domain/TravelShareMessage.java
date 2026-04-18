package com.example.traveling.travelshare.domain;

public class TravelShareMessage {

    private final String id;
    private final String conversationId;
    private final String senderName;
    private final String text;
    private final String sentAtLabel;

    public TravelShareMessage(String id, String conversationId, String senderName, String text, String sentAtLabel) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderName = senderName;
        this.text = text;
        this.sentAtLabel = sentAtLabel;
    }

    public String getId() {
        return id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getText() {
        return text;
    }

    public String getSentAtLabel() {
        return sentAtLabel;
    }
}

