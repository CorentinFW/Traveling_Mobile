package com.example.traveling.travelshare.domain;

import java.util.List;

public interface TravelShareMessageRepository {
    List<TravelShareConversation> getConversations();

    TravelShareConversation getConversationById(String conversationId);

    List<TravelShareMessage> getMessages(String conversationId);

    TravelShareMessage sendMessage(String conversationId, String senderName, String text);

    List<TravelShareGroup> getGroups();

    TravelShareGroup getGroupById(String groupId);

    TravelShareGroup createGroup(String name, String ownerName, List<String> initialMembers);

    boolean addMemberToGroup(String groupId, String memberName);

    boolean removeMemberFromGroup(String groupId, String memberName);

    TravelShareMessage sendGroupMessage(String groupId, String senderName, String text);
}

