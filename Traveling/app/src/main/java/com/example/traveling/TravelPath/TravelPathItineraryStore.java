package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class TravelPathItineraryStore {

    private static final String PREFS_NAME = "travelpath_itinerary_state";
    private static final String KEY_ITINERARY_ENTRIES = "itinerary_entries";
    private static final String ENTRY_SEPARATOR = "\n";
    private static final String FIELD_SEPARATOR = "||";

    private TravelPathItineraryStore() {
        // Utility class.
    }

    public static void saveItinerary(@NonNull Context context, @NonNull List<TravelPathPlace> places) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < places.size(); i++) {
            TravelPathPlace place = places.get(i);
            if (i > 0) {
                builder.append(ENTRY_SEPARATOR);
            }
            builder.append(safeValue(place.getName()))
                    .append(FIELD_SEPARATOR)
                    .append(safeValue(place.getTheme()))
                    .append(FIELD_SEPARATOR)
                    .append(place.getLatitude() != null ? place.getLatitude() : "")
                    .append(FIELD_SEPARATOR)
                    .append(place.getLongitude() != null ? place.getLongitude() : "")
                    .append(FIELD_SEPARATOR)
                    .append(place.getPrice() != null ? place.getPrice() : "")
                    .append(FIELD_SEPARATOR)
                    .append(place.getStar() != null ? place.getStar() : "");
        }

        getPreferences(context).edit()
                .putString(KEY_ITINERARY_ENTRIES, builder.toString())
                .apply();
    }

    @NonNull
    public static List<TravelPathPlace> loadItinerary(@NonNull Context context) {
        String raw = getPreferences(context).getString(KEY_ITINERARY_ENTRIES, "");
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] entries = raw.split(ENTRY_SEPARATOR);
        List<TravelPathPlace> places = new ArrayList<>();
        for (String entry : entries) {
            TravelPathPlace place = parseEntry(entry);
            if (place != null) {
                places.add(place);
            }
        }
        return places;
    }

    public static void clear(@NonNull Context context) {
        getPreferences(context).edit().remove(KEY_ITINERARY_ENTRIES).apply();
    }

    @Nullable
    private static TravelPathPlace parseEntry(@NonNull String entry) {
        String[] fields = entry.split("\\|\\|", -1);
        if (fields.length < 2) {
            return null;
        }

        String name = fields[0];
        String theme = fields[1];
        Double lat = parseDouble(fields.length > 2 ? fields[2] : "");
        Double lng = parseDouble(fields.length > 3 ? fields[3] : "");
        Double price = parseDouble(fields.length > 4 ? fields[4] : "");
        Double star = parseDouble(fields.length > 5 ? fields[5] : "");

        if (name.trim().isEmpty()) {
            return null;
        }
        return new TravelPathPlace(name, theme, null, lat, lng, price, star);
    }

    @Nullable
    private static Double parseDouble(@Nullable String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @NonNull
    private static String safeValue(@Nullable String value) {
        return value == null ? "" : value.replace(ENTRY_SEPARATOR, " ").replace(FIELD_SEPARATOR, " ");
    }

    @NonNull
    private static SharedPreferences getPreferences(@NonNull Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
