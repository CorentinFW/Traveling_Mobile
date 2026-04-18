package com.example.traveling.travelshare.data;

import androidx.annotation.Nullable;

import com.example.traveling.travelshare.domain.TravelShareConversation;
import com.example.traveling.travelshare.domain.TravelShareMessage;
import com.example.traveling.travelshare.domain.TravelShareMessageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTravelShareMessageRepository implements TravelShareMessageRepository {

    private final List<TravelShareConversation> conversations = new ArrayList<>();
    private final Map<String, List<TravelShareMessage>> messagesByConversationId = new HashMap<>();
    private int nextMessageId = 100;

    public InMemoryTravelShareMessageRepository() {
        seedConversations();
    }

    @Override
    public List<TravelShareConversation> getConversations() {
        return new ArrayList<>(conversations);
    }

    @Override
    public TravelShareConversation getConversationById(String conversationId) {
        for (TravelShareConversation conversation : conversations) {
            if (conversation.getId().equals(conversationId)) {
                return conversation;
            }
        }
        return null;
    }

    @Override
    public List<TravelShareMessage> getMessages(String conversationId) {
        List<TravelShareMessage> messages = messagesByConversationId.get(conversationId);
        if (messages == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(messages);
    }

    @Override
    public TravelShareMessage sendMessage(String conversationId, String senderName, String text) {
        TravelShareConversation conversation = getConversationById(conversationId);
        if (conversation == null || text == null || text.trim().isEmpty()) {
            return null;
        }

        String normalizedSender = normalizeSender(senderName);
        String normalizedText = text.trim();

        TravelShareMessage createdMessage = new TravelShareMessage(
                String.valueOf(nextMessageId++),
                conversationId,
                normalizedSender,
                normalizedText,
                "Maintenant"
        );

        List<TravelShareMessage> messages = messagesByConversationId.get(conversationId);
        if (messages == null) {
            messages = new ArrayList<>();
            messagesByConversationId.put(conversationId, messages);
        }
        messages.add(createdMessage);

        conversation.updateLastMessage(normalizedText, "Maintenant");
        moveConversationToTop(conversationId);

        return createdMessage;
    }

    private void seedConversations() {
        TravelShareConversation dmLina = new TravelShareConversation(
                "c1",
                "Lina",
                false,
                "Tu as des photos de Kyoto ?",
                "Aujourd'hui"
        );
        TravelShareConversation groupRoadTrip = new TravelShareConversation(
                "c2",
                "Groupe RoadTrip Europe",
                true,
                "Yanis: On part vendredi matin",
                "Hier"
        );
        TravelShareConversation dmCamille = new TravelShareConversation(
                "c3",
                "Camille",
                false,
                "Top ton post sur Tokyo",
                "2 j"
        );

        conversations.add(dmLina);
        conversations.add(groupRoadTrip);
        conversations.add(dmCamille);

        messagesByConversationId.put("c1", buildMessages(
                message("1", "c1", "Lina", "Tu as des photos de Kyoto ?", "09:12"),
                message("2", "c1", "Moi", "Oui, je te les envoie ce soir", "09:20")
        ));
        messagesByConversationId.put("c2", buildMessages(
                message("3", "c2", "Nora", "On prend une voiture a Barcelone", "Hier"),
                message("4", "c2", "Yanis", "On part vendredi matin", "Hier")
        ));
        messagesByConversationId.put("c3", buildMessages(
                message("5", "c3", "Camille", "Top ton post sur Tokyo", "Lun")
        ));
    }

    private List<TravelShareMessage> buildMessages(TravelShareMessage... messages) {
        List<TravelShareMessage> list = new ArrayList<>();
        for (TravelShareMessage message : messages) {
            list.add(message);
        }
        return list;
    }

    private TravelShareMessage message(String id, String conversationId, String sender, String text, String atLabel) {
        return new TravelShareMessage(id, conversationId, sender, text, atLabel);
    }

    private String normalizeSender(@Nullable String senderName) {
        if (senderName == null || senderName.trim().isEmpty()) {
            return "Voyageur";
        }
        return senderName.trim();
    }

    private void moveConversationToTop(String conversationId) {
        TravelShareConversation conversation = getConversationById(conversationId);
        if (conversation == null) {
            return;
        }
        conversations.remove(conversation);
        conversations.add(0, conversation);
    }
}

