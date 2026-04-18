package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    private final TravelPathRouteRepository routeRepository = new TravelPathRouteRepository();

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
            saveCurrentRouteToFirestoreAndContinue();
        });
    }

    private void saveCurrentRouteToFirestoreAndContinue() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            navigateToItinerary();
            return;
        }

        TravelPathRoute route = buildRouteFromCurrentState();
        routeRepository.saveRoute(user.getUid(), route, new TravelPathRouteRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.travelpath_db_route_saved, Toast.LENGTH_SHORT).show();
                navigateToItinerary();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.travelpath_db_route_save_error, Toast.LENGTH_SHORT).show();
                navigateToItinerary();
            }
        });
    }

    @NonNull
    private TravelPathRoute buildRouteFromCurrentState() {
        TravelPathRoute route = new TravelPathRoute();
        route.setRouteName(buildDefaultRouteName());
        route.setActivities(readText(activitiesValue));

        SharedPreferences durationPrefs = getPreferences(PREFS_DURATION_BUDGET);
        int budgetMin = Math.max(0, durationPrefs.getInt(KEY_BUDGET_MIN, 0));
        int budgetMax = Math.max(0, durationPrefs.getInt(KEY_BUDGET_MAX, budgetMin));
        route.setBudgetMin(budgetMin);
        route.setBudgetMax(Math.max(budgetMin, budgetMax));

        route.setVisitSummary(readText(durationValue));
        route.setEffort(readText(effortValue));
        route.setRouteType(readSelectedRouteType());
        route.setPlacesSummary(readText(activitiesValue));
        return route;
    }

    @NonNull
    private String buildDefaultRouteName() {
        String date = readSavedVisitDateLabel();
        if (date.isEmpty()) {
            return getString(R.string.travelpath_route_default_name_no_date);
        }
        return getString(R.string.travelpath_route_default_name_format, date);
    }

    @NonNull
    private String readSelectedRouteType() {
        Object selected = routeTypeSpinner.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    @NonNull
    private String readText(@Nullable TextView view) {
        if (view == null || view.getText() == null) {
            return "";
        }
        return view.getText().toString().trim();
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

