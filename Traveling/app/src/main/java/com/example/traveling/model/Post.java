package com.example.traveling.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Post {
    private final String id;
    private final User author;
    private final String imageUri;
    private final String locationName;
    private final String caption;
    private final List<String> tags;
    private final List<Comment> comments;
    private final long createdAtEpochMillis;

    public Post(
            @NonNull String id,
            @NonNull User author,
            @Nullable String imageUri,
            @NonNull String locationName,
            @NonNull String caption,
            @NonNull List<String> tags,
            @NonNull List<Comment> comments,
            long createdAtEpochMillis
    ) {
        this.id = id;
        this.author = author;
        this.imageUri = imageUri;
        this.locationName = locationName;
        this.caption = caption;
        this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
        this.comments = Collections.unmodifiableList(new ArrayList<>(comments));
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

    @Nullable
    public String getImageUri() {
        return imageUri;
    }

    @NonNull
    public String getLocationName() {
        return locationName;
    }

    @NonNull
    public String getCaption() {
        return caption;
    }

    @NonNull
    public List<String> getTags() {
        return tags;
    }

    @NonNull
    public List<Comment> getComments() {
        return comments;
    }

    public long getCreatedAtEpochMillis() {
        return createdAtEpochMillis;
    }
}


