package com.example.traveling.TravelPath;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

public class TravelPathPlace {

    private final String name;
    private final String theme;
    private final String imageUrl;

    public TravelPathPlace(@NonNull String name, @NonNull String theme, @Nullable String imageUrl) {
        this.name = name;
        this.theme = theme;
        this.imageUrl = imageUrl;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getTheme() {
        return theme;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    @NonNull
    public static TravelPathPlace fromDocument(@NonNull DocumentSnapshot document) {
        String name = document.getString("name");
        String theme = document.getString("theme");
        String image = document.getString("image");

        return new TravelPathPlace(
                (name == null || name.trim().isEmpty()) ? "Lieu-dit" : name,
                (theme == null || theme.trim().isEmpty()) ? "" : theme,
                image
        );
    }
}

