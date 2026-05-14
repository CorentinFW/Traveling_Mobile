package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;

public class TravelPathSummaryFragment extends Fragment {

    private static final String PREFS_PREFERENCES = "travelpath_preferences_state";
    private static final String KEY_SELECTED_CATEGORY_IDS = "selected_category_ids";

    private static final String PREFS_DURATION_BUDGET = "travelpath_duration_budget_state";
    private static final String KEY_VISIT_START_POSITION = "visit_start_position";
    private static final String KEY_BUDGET_MIN = "budget_min";
    private static final String KEY_BUDGET_MAX = "budget_max";

    private static final String PREFS_VISIT_DATE = "travelpath_visit_date_state";
    private static final String KEY_YEAR = "visit_year";
    private static final String KEY_MONTH = "visit_month";
    private static final String KEY_DAY = "visit_day";

    private static final String PREFS_EFFORT = "travelpath_effort_state";
    private static final String KEY_EFFORT_ID = "effort_id";

    private static final String PREFS_SUMMARY = "travelpath_summary_state";
    private static final String KEY_ROUTE_TYPE_POSITION = "route_type_position";

    private static final String PREFS_RESULTS = "travelpath_results_state";
    private static final String KEY_SELECTED_PLACE_KEYS = "selected_place_keys";
    private static final String KEY_SELECTED_PLACE_ENTRIES = "selected_place_entries";
    private static final String ENTRY_SEPARATOR = "::";

    private final TravelPathItineraryPlanner itineraryPlanner = new TravelPathItineraryPlanner();
    private final TravelPathPlaceRepository placeRepository = new TravelPathPlaceRepository();

    private TextView activitiesValue;
    private TextView budgetValue;
    private TextView durationValue;
    private TextView effortValue;
    private Spinner routeTypeSpinner;

    public TravelPathSummaryFragment() {
        super(R.layout.fragment_travelpath_summary);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        restoreRouteTypeSelection();
        renderSummaryValues();
        setupSelectedPlaceButton(view);
        setupContinueButton(view);
    }

    private void bindViews(@NonNull View rootView) {
        activitiesValue = rootView.findViewById(R.id.travelpath_summary_activities_value);
        budgetValue = rootView.findViewById(R.id.travelpath_summary_budget_value);
        durationValue = rootView.findViewById(R.id.travelpath_summary_duration_value);
        effortValue = rootView.findViewById(R.id.travelpath_summary_effort_value);
        routeTypeSpinner = rootView.findViewById(R.id.travelpath_route_type_spinner);
    }

    private void restoreRouteTypeSelection() {
        SharedPreferences summaryPrefs = getPreferences(PREFS_SUMMARY);
        int savedPosition = summaryPrefs.getInt(KEY_ROUTE_TYPE_POSITION, 0);
        int itemCount = routeTypeSpinner.getCount();
        if (itemCount <= 0) {
            return;
        }
        routeTypeSpinner.setSelection(Math.max(0, Math.min(savedPosition, itemCount - 1)));
    }

    private void renderSummaryValues() {
        renderActivities();
        renderBudget();
        renderDurationAndDate();
        renderEffort();
    }

    private void renderActivities() {
        SharedPreferences prefs = getPreferences(PREFS_PREFERENCES);
        Set<String> selectedCategoryIds = prefs.getStringSet(KEY_SELECTED_CATEGORY_IDS, new HashSet<>());
        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            return;
        }

        List<String> labels = new ArrayList<>();
        for (String categoryId : selectedCategoryIds) {
            String label = mapCategoryIdToLabel(categoryId);
            if (!label.isEmpty()) {
                labels.add(label);
            }
        }
        if (!labels.isEmpty()) {
            activitiesValue.setText(joinWithComma(labels));
        }
    }

    private void renderBudget() {
        SharedPreferences prefs = getPreferences(PREFS_DURATION_BUDGET);
        int min = prefs.getInt(KEY_BUDGET_MIN, -1);
        int max = prefs.getInt(KEY_BUDGET_MAX, -1);
        if (min < 0 || max < 0) {
            return;
        }
        min = Math.max(0, min);
        max = Math.max(0, max);

        String formatted = String.format(
                Locale.getDefault(),
                "%d-%d%s",
                min,
                max,
                getString(R.string.travelpath_budget_currency)
        );
        budgetValue.setText(formatted);
    }

    private void renderDurationAndDate() {
        String visitStart = readVisitStartLabel();
        String dateLabel = readSavedVisitDateLabel();

        if (visitStart.isEmpty() && dateLabel.isEmpty()) {
            return;
        }
        if (visitStart.isEmpty()) {
            durationValue.setText(dateLabel);
            return;
        }
        if (dateLabel.isEmpty()) {
            durationValue.setText(visitStart);
            return;
        }
        durationValue.setText(String.format(Locale.getDefault(), "%s, %s", visitStart, dateLabel));
    }

    @NonNull
    private String readVisitStartLabel() {
        SharedPreferences prefs = getPreferences(PREFS_DURATION_BUDGET);
        int position = prefs.getInt(KEY_VISIT_START_POSITION, -1);
        if (position < 0) {
            return "";
        }

        String[] values = getResources().getStringArray(R.array.travelpath_visit_start_options);
        if (position >= values.length) {
            return "";
        }
        return values[position];
    }

    @NonNull
    private String readSavedVisitDateLabel() {
        SharedPreferences prefs = getPreferences(PREFS_VISIT_DATE);
        if (!prefs.contains(KEY_YEAR) || !prefs.contains(KEY_MONTH) || !prefs.contains(KEY_DAY)) {
            return "";
        }

        int year = prefs.getInt(KEY_YEAR, 0);
        int month = prefs.getInt(KEY_MONTH, 0) + 1;
        int day = prefs.getInt(KEY_DAY, 0);
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month, year);
    }

    private void renderEffort() {
        SharedPreferences prefs = getPreferences(PREFS_EFFORT);
        String effortIdName = prefs.getString(KEY_EFFORT_ID, "");
        String effortLabel = mapEffortIdToLabel(effortIdName);
        if (!effortLabel.isEmpty()) {
            effortValue.setText(effortLabel);
        }
    }

    private void setupContinueButton(@NonNull View rootView) {
        Button continueButton = rootView.findViewById(R.id.travelpath_continue_button);
        continueButton.setOnClickListener(v -> {
            saveRouteTypeSelection();
            generateItineraryAndContinue();
        });
    }

    private void setupSelectedPlaceButton(@NonNull View rootView) {
        LinearLayout selectedPlaceButton = rootView.findViewById(R.id.travelpath_selected_place_button);
        selectedPlaceButton.setOnClickListener(v -> showSelectedPlacesDialog());
    }

    private void showSelectedPlacesDialog() {
        List<String> selectedPlaces = readSelectedPlaceLabels();
        if (selectedPlaces.isEmpty()) {
            Toast.makeText(requireContext(), R.string.travelpath_selected_places_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < selectedPlaces.size(); i++) {
            if (i > 0) {
                messageBuilder.append("\n");
            }
            messageBuilder.append("- ").append(selectedPlaces.get(i));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.travelpath_selected_places_dialog_title)
                .setMessage(messageBuilder.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @NonNull
    private List<String> readSelectedPlaceLabels() {
        SharedPreferences resultsPrefs = getPreferences(PREFS_RESULTS);
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

        if (!labels.isEmpty()) {
            return labels;
        }

        // Compatibilite avec les sauvegardes plus anciennes: on tente au moins d'afficher une liste.
        Set<String> legacyKeys = resultsPrefs.getStringSet(KEY_SELECTED_PLACE_KEYS, new HashSet<>());
        for (String legacyKey : legacyKeys) {
            if (legacyKey == null || legacyKey.trim().isEmpty()) {
                continue;
            }
            labels.add(legacyKey);
        }
        return labels;
    }

    private void generateItineraryAndContinue() {
        itineraryPlanner.generateItinerary(requireContext(), placeRepository, new TravelPathItineraryPlanner.ItineraryCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> itinerary) {
                if (!isAdded()) {
                    return;
                }
                if (itinerary.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.travelpath_itinerary_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (usesFallback(itinerary)) {
                    Toast.makeText(requireContext(), R.string.travelpath_fallback_used, Toast.LENGTH_SHORT).show();
                }
                TravelPathItineraryStore.saveItinerary(requireContext(), itinerary);
                navigateToItinerary();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.travelpath_results_load_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean usesFallback(@NonNull List<TravelPathPlace> itinerary) {
        for (TravelPathPlace place : itinerary) {
            if ("fallback".equals(place.getSourceCollection())) {
                return true;
            }
        }
        return false;
    }

    private void navigateToItinerary() {
        Fragment parent = getParentFragment();
        if (parent instanceof TravelPathMainFragment) {
            ((TravelPathMainFragment) parent).showItineraryScreen();
        }
    }

    private void saveRouteTypeSelection() {
        getPreferences(PREFS_SUMMARY)
                .edit()
                .putInt(KEY_ROUTE_TYPE_POSITION, routeTypeSpinner.getSelectedItemPosition())
                .apply();
    }

    @NonNull
    private String mapCategoryIdToLabel(@Nullable String categoryId) {
        if (categoryId == null) {
            return "";
        }

        switch (categoryId) {
            case "travelpath_category_restaurant":
                return getString(R.string.travelpath_category_restaurant);
            case "travelpath_category_culture":
                return getString(R.string.travelpath_category_culture);
            case "travelpath_category_monument":
                return getString(R.string.travelpath_category_monument);
            case "travelpath_category_leisure":
                return getString(R.string.travelpath_category_leisure);
            case "travelpath_category_shopping":
                return getString(R.string.travelpath_category_shopping);
            case "travelpath_category_events":
                return getString(R.string.travelpath_category_events);
            default:
                return "";
        }
    }

    @NonNull
    private String mapEffortIdToLabel(@Nullable String effortIdName) {
        if (effortIdName == null) {
            return "";
        }

        switch (effortIdName) {
            case "travelpath_effort_low":
                return getString(R.string.travelpath_effort_low);
            case "travelpath_effort_medium":
                return getString(R.string.travelpath_effort_medium);
            case "travelpath_effort_high":
                return getString(R.string.travelpath_effort_high);
            default:
                return "";
        }
    }

    @NonNull
    private String joinWithComma(@NonNull List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    @NonNull
    private SharedPreferences getPreferences(@NonNull String prefsName) {
        Context context = requireContext().getApplicationContext();
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }
}
