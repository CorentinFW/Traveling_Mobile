package com.example.traveling.travelshare.domain;

import java.util.List;

public interface TravelShareMessageRepository {
    List<TravelShareConversation> getConversations();

    TravelShareConversation getConversationById(String conversationId);

    List<TravelShareMessage> getMessages(String conversationId);

    TravelShareMessage sendMessage(String conversationId, String senderName, String text);
}

