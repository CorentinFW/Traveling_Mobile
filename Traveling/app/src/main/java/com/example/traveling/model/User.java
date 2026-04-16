package com.example.traveling.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class User {
    private final String id;
    private final String username;
    private final String displayName;
    private final String avatarUrl;

    public User(@NonNull String id, @NonNull String username, @NonNull String displayName, @Nullable String avatarUrl) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }
}


