package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.traveling.R;

public class TravelPathItineraryPlanner {

    private static final String PREFS_PREFERENCES = "travelpath_preferences_state";
    private static final String KEY_SELECTED_CATEGORY_IDS = "selected_category_ids";

    private static final String PREFS_DURATION_BUDGET = "travelpath_duration_budget_state";
    private static final String KEY_DURATION_ID = "duration_id";
    private static final String KEY_MULTIPLE_DAYS_VALUE = "multiple_days_value";
    private static final String KEY_BUDGET_MIN = "budget_min";
    private static final String KEY_BUDGET_MAX = "budget_max";

    private static final String PREFS_RESULTS = "travelpath_results_state";
    private static final String KEY_SELECTED_PLACE_ENTRIES = "selected_place_entries";
    private static final String ENTRY_SEPARATOR = "::";

    private static final String PREFS_SUMMARY = "travelpath_summary_state";
    private static final String KEY_ROUTE_TYPE_POSITION = "route_type_position";

    public interface ItineraryCallback {
        void onSuccess(@NonNull List<TravelPathPlace> itinerary);

        void onError(@NonNull Exception exception);
    }

    public void generateItinerary(
            @NonNull Context context,
            @NonNull TravelPathPlaceDataSource placeRepository,
            @NonNull ItineraryCallback callback
    ) {
        SharedPreferences durationPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_DURATION_BUDGET, Context.MODE_PRIVATE);

        int budgetMin = durationPrefs.getInt(KEY_BUDGET_MIN, -1);
        int budgetMax = durationPrefs.getInt(KEY_BUDGET_MAX, -1);

        int targetCount = resolveTargetCount(context, durationPrefs);
        List<String> selectedPlaceLabels = readSelectedPlaceLabels(context);
        Set<String> selectedThemes = readSelectedThemes(context);
        String routeType = readRouteType(context);

        placeRepository.loadAllPlaces(new TravelPathPlaceRepository.LoadCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> places) {
                List<TravelPathPlace> selectedPlaces = matchSelectedPlaces(places, selectedPlaceLabels);
                if (!selectedPlaces.isEmpty()) {
                    List<TravelPathPlace> budgetedSelected = applyBudgetFilter(selectedPlaces, budgetMin, budgetMax);
                    if (!budgetedSelected.isEmpty()) {
                        List<TravelPathPlace> ordered = orderByNearestNeighbor(budgetedSelected);
                        callback.onSuccess(ordered);
                        return;
                    }
                    // TODO: si budget insuffisant, ajuster le budget ou prevenir l'utilisateur.
                }

                List<TravelPathPlace> filtered = filterByTheme(places, selectedThemes);
                filtered = applyBudgetFilter(filtered, budgetMin, budgetMax);
                List<TravelPathPlace> pool = new ArrayList<>(filtered);
                Collections.shuffle(pool);

                List<TravelPathPlace> itinerary = new ArrayList<>();
                for (TravelPathPlace place : pool) {
                    if (itinerary.size() >= targetCount) {
                        break;
                    }
                    itinerary.add(place);
                }

                if (itinerary.isEmpty()) {
                    itinerary = buildFallbackItinerary(filtered, targetCount);
                }

                itinerary = applyRouteTypeOrdering(routeType, itinerary);
                callback.onSuccess(itinerary);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    @NonNull
    private List<TravelPathPlace> filterByTheme(
            @NonNull List<TravelPathPlace> places,
            @NonNull Set<String> selectedThemes
    ) {
        if (selectedThemes.isEmpty()) {
            return new ArrayList<>(places);
        }

        List<TravelPathPlace> filtered = new ArrayList<>();
        for (TravelPathPlace place : places) {
            if (selectedThemes.contains(normalizeText(place.getTheme()))) {
                filtered.add(place);
            }
        }
        return filtered;
    }

    @NonNull
    private List<TravelPathPlace> matchSelectedPlaces(
            @NonNull List<TravelPathPlace> places,
            @NonNull List<String> selectedLabels
    ) {
        if (selectedLabels.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, TravelPathPlace> byName = new HashMap<>();
        for (TravelPathPlace place : places) {
            byName.put(normalizeText(place.getName()), place);
        }

        List<TravelPathPlace> selected = new ArrayList<>();
        for (String label : selectedLabels) {
            TravelPathPlace place = byName.get(normalizeText(label));
            if (place != null) {
                selected.add(place);
            }
        }
        return selected;
    }

    private void removeByKey(@NonNull List<TravelPathPlace> pool, @NonNull List<TravelPathPlace> selected) {
        if (pool.isEmpty() || selected.isEmpty()) {
            return;
        }
        Set<String> selectedKeys = new HashSet<>();
        for (TravelPathPlace place : selected) {
            selectedKeys.add(buildPlaceKey(place));
        }
        pool.removeIf(place -> selectedKeys.contains(buildPlaceKey(place)));
    }

    @NonNull
    private List<TravelPathPlace> buildFallbackItinerary(@NonNull List<TravelPathPlace> places, int targetCount) {
        List<TravelPathPlace> fallback = new ArrayList<>(places);
        Collections.shuffle(fallback);
        int count = Math.min(targetCount, fallback.size());
        return new ArrayList<>(fallback.subList(0, count));
    }

    @NonNull
    private List<TravelPathPlace> applyRouteTypeOrdering(
            @NonNull String routeType,
            @NonNull List<TravelPathPlace> itinerary
    ) {
        if (itinerary.isEmpty()) {
            return itinerary;
        }

        String normalizedType = normalizeText(routeType);
        if (normalizedType.contains("simple")) {
            itinerary.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
            return itinerary;
        }
        if (normalizedType.contains("equilibre")) {
            return interleaveByTheme(itinerary);
        }
        return itinerary;
    }

    @NonNull
    private List<TravelPathPlace> interleaveByTheme(@NonNull List<TravelPathPlace> itinerary) {
        Map<String, List<TravelPathPlace>> byTheme = new HashMap<>();
        for (TravelPathPlace place : itinerary) {
            String themeKey = normalizeText(place.getTheme());
            byTheme.computeIfAbsent(themeKey, key -> new ArrayList<>()).add(place);
        }

        List<TravelPathPlace> ordered = new ArrayList<>();
        boolean hasMore = true;
        int index = 0;
        List<String> themes = new ArrayList<>(byTheme.keySet());
        Collections.sort(themes);

        while (hasMore) {
            hasMore = false;
            for (String theme : themes) {
                List<TravelPathPlace> list = byTheme.get(theme);
                if (list == null || index >= list.size()) {
                    continue;
                }
                ordered.add(list.get(index));
                hasMore = true;
            }
            index++;
        }
        return ordered;
    }

    @NonNull
    private List<String> readSelectedPlaceLabels(@NonNull Context context) {
        SharedPreferences resultsPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_RESULTS, Context.MODE_PRIVATE);
        Set<String> rawEntries = resultsPrefs.getStringSet(KEY_SELECTED_PLACE_ENTRIES, new HashSet<>());
        List<String> labels = new ArrayList<>();
        for (String rawEntry : rawEntries) {
            int separatorIndex = rawEntry.indexOf(ENTRY_SEPARATOR);
            if (separatorIndex <= 0 || separatorIndex >= rawEntry.length() - ENTRY_SEPARATOR.length()) {
                continue;
            }
            String label = rawEntry.substring(separatorIndex + ENTRY_SEPARATOR.length()).trim();
            if (!label.isEmpty()) {
                labels.add(label);
            }
        }
        Collections.sort(labels, String.CASE_INSENSITIVE_ORDER);
        return labels;
    }

    @NonNull
    private Set<String> readSelectedThemes(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_PREFERENCES, Context.MODE_PRIVATE);
        Set<String> selectedCategoryIds = prefs.getStringSet(KEY_SELECTED_CATEGORY_IDS, new HashSet<>());
        Set<String> themes = new HashSet<>();
        if (selectedCategoryIds == null) {
            return themes;
        }
        for (String categoryId : selectedCategoryIds) {
            String label = mapCategoryIdToLabel(context, categoryId);
            if (!TextUtils.isEmpty(label)) {
                themes.add(normalizeText(label));
            }
        }
        return themes;
    }

    @NonNull
    private String readRouteType(@NonNull Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_SUMMARY, Context.MODE_PRIVATE);
        int position = prefs.getInt(KEY_ROUTE_TYPE_POSITION, 0);
        String[] values = context.getResources().getStringArray(R.array.travelpath_route_type_options);
        if (position < 0 || position >= values.length) {
            return values[0];
        }
        return values[position];
    }

    private int resolveTargetCount(@NonNull Context context, @NonNull SharedPreferences durationPrefs) {
        String durationId = durationPrefs.getString(KEY_DURATION_ID, "");
        String halfDayId = context.getResources().getResourceEntryName(R.id.travelpath_duration_half_day);
        String fullDayId = context.getResources().getResourceEntryName(R.id.travelpath_duration_full_day);
        String multipleDaysId = context.getResources().getResourceEntryName(R.id.travelpath_duration_multiple_days);

        if (multipleDaysId.equals(durationId)) {
            int days = parsePositiveInt(durationPrefs.getString(KEY_MULTIPLE_DAYS_VALUE, ""), 2);
            int count = days * 5;
            return Math.min(10, Math.max(5, count));
        }
        if (fullDayId.equals(durationId)) {
            return 5;
        }
        if (halfDayId.equals(durationId)) {
            return 3;
        }
        return 5;
    }

    private int parsePositiveInt(@Nullable String raw, int fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return Math.max(1, fallback);
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value <= 0 ? Math.max(1, fallback) : value;
        } catch (NumberFormatException ignored) {
            return Math.max(1, fallback);
        }
    }

    @NonNull
    private String mapCategoryIdToLabel(@NonNull Context context, @Nullable String categoryId) {
        if (categoryId == null) {
            return "";
        }

        switch (categoryId) {
            case "travelpath_category_restaurant":
                return context.getString(R.string.travelpath_category_restaurant);
            case "travelpath_category_culture":
                return context.getString(R.string.travelpath_category_culture);
            case "travelpath_category_monument":
                return context.getString(R.string.travelpath_category_monument);
            case "travelpath_category_leisure":
                return context.getString(R.string.travelpath_category_leisure);
            case "travelpath_category_shopping":
                return context.getString(R.string.travelpath_category_shopping);
            case "travelpath_category_events":
                return context.getString(R.string.travelpath_category_events);
            default:
                return "";
        }
    }

    @NonNull
    private String buildPlaceKey(@NonNull TravelPathPlace place) {
        return normalizeText(place.getName()) + "|" + normalizeText(place.getTheme());
    }

    @NonNull
    private String normalizeText(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{M}+", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }

    @NonNull
    private List<TravelPathPlace> orderByNearestNeighbor(@NonNull List<TravelPathPlace> selected) {
        List<TravelPathPlace> withCoords = new ArrayList<>();
        List<TravelPathPlace> withoutCoords = new ArrayList<>();

        for (TravelPathPlace place : selected) {
            if (place.getLatitude() != null && place.getLongitude() != null) {
                withCoords.add(place);
            } else {
                withoutCoords.add(place);
            }
        }

        if (withCoords.size() <= 1) {
            List<TravelPathPlace> ordered = new ArrayList<>(withCoords);
            ordered.addAll(withoutCoords);
            return ordered;
        }

        List<TravelPathPlace> ordered = new ArrayList<>();
        TravelPathPlace current = withCoords.remove(0);
        ordered.add(current);

        while (!withCoords.isEmpty()) {
            int closestIndex = 0;
            double closestDistance = Double.MAX_VALUE;
            for (int i = 0; i < withCoords.size(); i++) {
                TravelPathPlace candidate = withCoords.get(i);
                double distance = distanceKm(current, candidate);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestIndex = i;
                }
            }
            current = withCoords.remove(closestIndex);
            ordered.add(current);
        }

        ordered.addAll(withoutCoords);
        return ordered;
    }

    private double distanceKm(@NonNull TravelPathPlace from, @NonNull TravelPathPlace to) {
        if (from.getLatitude() == null || from.getLongitude() == null
                || to.getLatitude() == null || to.getLongitude() == null) {
            return Double.MAX_VALUE;
        }

        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c;
    }

    @NonNull
    private List<TravelPathPlace> applyBudgetFilter(
            @NonNull List<TravelPathPlace> places,
            int budgetMin,
            int budgetMax
    ) {
        if (budgetMin < 0 || budgetMax < 0) {
            return new ArrayList<>(places);
        }
        int safeMin = Math.max(0, budgetMin);
        int safeMax = Math.max(0, budgetMax);
        if (safeMin == 0 && safeMax == 0) {
            return new ArrayList<>(places);
        }
        if (safeMax < safeMin) {
            safeMax = safeMin;
        }

        List<TravelPathPlace> filtered = new ArrayList<>();
        for (TravelPathPlace place : places) {
            Double price = place.getPrice();
            if (price == null) {
                continue;
            }
            if (price >= safeMin && price <= safeMax) {
                filtered.add(place);
            }
        }
        return filtered;
    }
}
