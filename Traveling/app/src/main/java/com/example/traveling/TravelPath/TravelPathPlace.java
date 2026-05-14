package com.example.traveling.TravelPath;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.Map;

public class TravelPathPlace {

    private final String name;
    private final String theme;
    private final String imageUrl;
    @Nullable
    private final Double latitude;
    @Nullable
    private final Double longitude;
    @Nullable
    private final Double price;
    @Nullable
    private final Double star;
    private String id;
    private String sourceCollection;

    public TravelPathPlace(@NonNull String name, @NonNull String theme, @Nullable String imageUrl) {
        this(name, theme, imageUrl, null, null, null, null);
    }

    public TravelPathPlace(
            @NonNull String name,
            @NonNull String theme,
            @Nullable String imageUrl,
            @Nullable Double latitude,
            @Nullable Double longitude
    ) {
        this(name, theme, imageUrl, latitude, longitude, null, null);
    }

    public TravelPathPlace(
            @NonNull String name,
            @NonNull String theme,
            @Nullable String imageUrl,
            @Nullable Double latitude,
            @Nullable Double longitude,
            @Nullable Double price,
            @Nullable Double star
    ) {
        this.name = name;
        this.theme = theme;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
        this.star = star;
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

    @Nullable
    public Double getLatitude() {
        return latitude;
    }

    @Nullable
    public Double getLongitude() {
        return longitude;
    }

    @Nullable
    public Double getPrice() {
        return price;
    }

    @Nullable
    public Double getStar() {
        return star;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public String getSourceCollection() {
        return sourceCollection;
    }

    public void setSourceCollection(@Nullable String sourceCollection) {
        this.sourceCollection = sourceCollection;
    }

    @Nullable
    public String getReference() {
        if (id == null || id.trim().isEmpty() || sourceCollection == null || sourceCollection.trim().isEmpty()) {
            return null;
        }
        return sourceCollection.trim() + "/" + id.trim();
    }

    @NonNull
    public static TravelPathPlace fromDocument(@NonNull DocumentSnapshot document) {
        String name = document.getString("name");
        String theme = document.getString("theme");
        String image = document.getString("image");
        Double latitude = readLatitude(document);
        Double longitude = readLongitude(document);
        Double price = toDouble(document.get("price"));
        Double star = toDouble(document.get("star"));

        return new TravelPathPlace(
                (name == null || name.trim().isEmpty()) ? "Lieu-dit" : name,
                (theme == null || theme.trim().isEmpty()) ? "" : theme,
                image,
                latitude,
                longitude,
                price,
                star
        );
    }

    @Nullable
    private static Double readLatitude(@NonNull DocumentSnapshot document) {
        Double direct = readNumberField(document, "latitude");
        if (direct != null) {
            return direct;
        }
        direct = readNumberField(document, "lat");
        if (direct != null) {
            return direct;
        }

        GeoPoint geoPoint = document.getGeoPoint("position");
        if (geoPoint != null) {
            return geoPoint.getLatitude();
        }

        Map<String, Object> position = readMapField(document, "position");
        if (position == null) {
            return null;
        }
        Object latitude = position.get("latitude");
        if (latitude == null) {
            latitude = position.get("lat");
        }
        return toDouble(latitude);
    }

    @Nullable
    private static Double readLongitude(@NonNull DocumentSnapshot document) {
        Double direct = readNumberField(document, "longitude");
        if (direct != null) {
            return direct;
        }
        direct = readNumberField(document, "lng");
        if (direct != null) {
            return direct;
        }
        direct = readNumberField(document, "lon");
        if (direct != null) {
            return direct;
        }

        GeoPoint geoPoint = document.getGeoPoint("position");
        if (geoPoint != null) {
            return geoPoint.getLongitude();
        }

        Map<String, Object> position = readMapField(document, "position");
        if (position == null) {
            return null;
        }
        Object longitude = position.get("longitude");
        if (longitude == null) {
            longitude = position.get("lng");
        }
        if (longitude == null) {
            longitude = position.get("lon");
        }
        return toDouble(longitude);
    }

    @Nullable
    private static Double readNumberField(@NonNull DocumentSnapshot document, @NonNull String key) {
        Object raw = document.get(key);
        return toDouble(raw);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Map<String, Object> readMapField(@NonNull DocumentSnapshot document, @NonNull String key) {
        Object raw = document.get(key);
        if (raw instanceof Map<?, ?>) {
            return (Map<String, Object>) raw;
        }
        return null;
    }

    @Nullable
    private static Double toDouble(@Nullable Object raw) {
        if (raw instanceof Number) {
            return ((Number) raw).doubleValue();
        }
        if (raw instanceof String) {
            try {
                return Double.parseDouble(((String) raw).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
