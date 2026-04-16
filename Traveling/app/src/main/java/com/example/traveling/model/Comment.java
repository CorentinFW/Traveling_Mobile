package com.example.traveling.model;

import androidx.annotation.NonNull;

public class Comment {
    private final String id;
    private final User author;
    private final String text;
    private final long createdAtEpochMillis;

    public Comment(@NonNull String id, @NonNull User author, @NonNull String text, long createdAtEpochMillis) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.createdAtEpochMillis = createdAtEpochMillis;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public User getAuthor() {
        return author;
    }

    @NonNull
    public String getText() {
        return text;
    }

    public long getCreatedAtEpochMillis() {
        return createdAtEpochMillis;
    }
}


