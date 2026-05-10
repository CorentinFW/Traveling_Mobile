package com.example.traveling.travelshare.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TravelShareGroup {

    private final String id;
    private final String name;
    private final String ownerName;
    private final List<String> memberNames = new ArrayList<>();
    private final List<TravelShareMessage> messages = new ArrayList<>();
    private String lastMessagePreview;
    private String lastActivityLabel;

    public TravelShareGroup(String id, String name, String ownerName, List<String> initialMembers) {
        this(id, name, ownerName, initialMembers, "Groupe cree", "Maintenant");
    }

    public TravelShareGroup(
            String id,
            String name,
            String ownerName,
            List<String> initialMembers,
            String lastMessagePreview,
            String lastActivityLabel
    ) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
        if (initialMembers != null) {
            for (String member : initialMembers) {
                addUniqueMember(member);
            }
        }
        addUniqueMember(ownerName);
        this.lastMessagePreview = lastMessagePreview;
        this.lastActivityLabel = lastActivityLabel;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public List<String> getMemberNames() {
        return Collections.unmodifiableList(memberNames);
    }

    public List<TravelShareMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public String getLastActivityLabel() {
        return lastActivityLabel;
    }

    public boolean addMember(String memberName) {
        if (!addUniqueMember(memberName)) {
            return false;
        }
        updateLastActivity(memberName.trim() + " a rejoint le groupe", "Maintenant");
        return true;
    }

    public boolean removeMember(String memberName) {
        if (memberName == null) {
            return false;
        }
        String normalizedMember = memberName.trim();
        if (normalizedMember.isEmpty() || ownerName.equals(normalizedMember)) {
            return false;
        }
        boolean removed = memberNames.remove(normalizedMember);
        if (removed) {
            updateLastActivity(normalizedMember + " a quitte le groupe", "Maintenant");
        }
        return removed;
    }

    public TravelShareMessage addMessage(String senderName, String text) {
        String normalizedText = text == null ? "" : text.trim();
        String normalizedSender = senderName == null || senderName.trim().isEmpty()
                ? "Voyageur"
                : senderName.trim();
        TravelShareMessage message = new TravelShareMessage(
                id + "-msg-" + (messages.size() + 1),
                id,
                normalizedSender,
                normalizedText,
                "Maintenant"
        );
        messages.add(message);
        updateLastActivity(normalizedSender + ": " + normalizedText, "Maintenant");
        return message;
    }

    public void updateLastActivity(String preview, String activityLabel) {
        if (preview != null && !preview.trim().isEmpty()) {
            lastMessagePreview = preview.trim();
        }
        if (activityLabel != null && !activityLabel.trim().isEmpty()) {
            lastActivityLabel = activityLabel.trim();
        }
    }

    private boolean addUniqueMember(String memberName) {
        if (memberName == null) {
            return false;
        }
        String normalizedMember = memberName.trim();
        if (normalizedMember.isEmpty() || memberNames.contains(normalizedMember)) {
            return false;
        }
        memberNames.add(normalizedMember);
        return true;
    }
}


