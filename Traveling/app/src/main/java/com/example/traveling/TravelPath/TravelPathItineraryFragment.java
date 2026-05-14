package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;

public class TravelPathItineraryFragment extends Fragment {

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

    private GoogleMap googleMap;
    private LinearLayout itineraryContainer;
    private TextView budgetValue;
    private TextView durationValue;
    private TextView effortValue;
    private ImageButton prevPlaceButton;
    private ImageButton nextPlaceButton;
    private int currentPlaceIndex = 0;

    private final TravelPathPlaceRepository placeRepository = new TravelPathPlaceRepository();
    private final TravelPathItineraryPlanner itineraryPlanner = new TravelPathItineraryPlanner();
    private final TravelPathRouteRepository routeRepository = new TravelPathRouteRepository();

    public TravelPathItineraryFragment() {
        super(R.layout.fragment_travelpath_itinerary);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        itineraryContainer = view.findViewById(R.id.travelpath_itinerary_injection_container);
        budgetValue = view.findViewById(R.id.travelpath_itinerary_budget_value);
        durationValue = view.findViewById(R.id.travelpath_itinerary_duration_value);
        effortValue = view.findViewById(R.id.travelpath_itinerary_effort_value);
        prevPlaceButton = view.findViewById(R.id.travelpath_itinerary_prev_button);
        nextPlaceButton = view.findViewById(R.id.travelpath_itinerary_next_button);

        setupMap();
        renderSummaryValues();
        renderItinerary();
        setupActions(view);
        setupPlaceNavigation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != TravelPathMapLocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (googleMap != null && TravelPathMapLocationHelper.isLocationPermissionGranted(grantResults)) {
            TravelPathMapLocationHelper.centerMapOnUserOrFallback(this, googleMap);
        }
    }

    private void setupMap() {
        Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.travelpath_itinerary_map);
        if (!(mapFragment instanceof SupportMapFragment)) {
            return;
        }

        ((SupportMapFragment) mapFragment).getMapAsync(map -> {
            googleMap = map;
            renderItineraryMarkers(lastRenderedItinerary);
            if (!centerOnCurrentPlace(false)) {
                TravelPathMapLocationHelper.centerMapOnUserOrFallback(this, map);
            }
        });
    }

    private void setupPlaceNavigation() {
        if (prevPlaceButton != null) {
            prevPlaceButton.setOnClickListener(v -> moveToPlace(currentPlaceIndex - 1));
        }
        if (nextPlaceButton != null) {
            nextPlaceButton.setOnClickListener(v -> moveToPlace(currentPlaceIndex + 1));
        }
        updateNavigationButtons();
    }

    private void moveToPlace(int newIndex) {
        if (newIndex < 0 || newIndex >= lastRenderedItinerary.size()) {
            return;
        }
        currentPlaceIndex = newIndex;
        centerOnCurrentPlace(true);
        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        updateNavigationButton(prevPlaceButton, currentPlaceIndex > 0);
        updateNavigationButton(nextPlaceButton, currentPlaceIndex + 1 < lastRenderedItinerary.size());
    }

    private void updateNavigationButton(@Nullable ImageButton button, boolean enabled) {
        if (button == null) {
            return;
        }
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.4f);
    }

    private boolean centerOnCurrentPlace(boolean animate) {
        if (googleMap == null || lastRenderedItinerary.isEmpty()) {
            return false;
        }
        if (currentPlaceIndex < 0 || currentPlaceIndex >= lastRenderedItinerary.size()) {
            return false;
        }
        TravelPathPlace place = lastRenderedItinerary.get(currentPlaceIndex);
        Double lat = place.getLatitude();
        Double lng = place.getLongitude();
        if (lat == null || lng == null) {
            return false;
        }
        LatLng position = new LatLng(lat, lng);
        if (animate) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14f));
        }
        return true;
    }

    private void setupActions(@NonNull View rootView) {
        Button saveButton = rootView.findViewById(R.id.travelpath_save_button);
        Button recalcButton = rootView.findViewById(R.id.travelpath_recalculate_button);

        saveButton.setOnClickListener(v -> promptForRouteName());
        recalcButton.setOnClickListener(v -> regenerateItinerary());
    }

    private void promptForRouteName() {
        String defaultName = buildDefaultRouteName();
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(defaultName);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.travelpath_route_name_prompt_title)
                .setView(input)
                .setPositiveButton(R.string.travelpath_route_name_confirm, (dialog, which) -> {
                    String raw = input.getText() != null ? input.getText().toString().trim() : "";
                    String routeName = TextUtils.isEmpty(raw) ? defaultName : raw;
                    saveRoute(routeName);
                })
                .setNegativeButton(R.string.travelpath_route_name_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void saveRoute(@NonNull String routeName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), R.string.travelpath_db_auth_required, Toast.LENGTH_SHORT).show();
            return;
        }

        TravelPathRoute route = buildRouteFromCurrentState(routeName);
        routeRepository.saveRoute(user.getUid(), route, new TravelPathRouteRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) {
                    return;
                }
                clearTravelPathState();
                Toast.makeText(requireContext(), R.string.travelpath_db_route_saved, Toast.LENGTH_SHORT).show();
                Fragment parent = getParentFragment();
                if (parent instanceof TravelPathMainFragment) {
                    ((TravelPathMainFragment) parent).resetToMyRoutesScreen();
                }

            }

            @Override
            public void onError(@NonNull Exception exception) {
                if (!isAdded()) {
                    return;
                }
                Log.e("TravelPathItinerary", "Failed to save route", exception);
                String message = exception.getMessage();
                String fallback = getString(R.string.travelpath_db_route_save_error);
                Toast.makeText(requireContext(), message == null || message.trim().isEmpty() ? fallback : message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderSummaryValues() {
        renderBudget();
        renderDurationAndDate();
        renderEffort();
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

    private void renderItinerary() {
        List<TravelPathPlace> itinerary = TravelPathItineraryStore.loadItinerary(requireContext());
        if (itineraryContainer == null) {
            return;
        }

        itineraryContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        TravelPathPlace previousPlace = null;
        for (int index = 0; index < itinerary.size(); index++) {
            TravelPathPlace place = itinerary.get(index);
            View itemView = inflater.inflate(R.layout.item_travelpath_itinerary_step, itineraryContainer, false);
            TextView stepIndex = itemView.findViewById(R.id.travelpath_step_index);
            TextView stepName = itemView.findViewById(R.id.travelpath_step_place_name);
            TextView stepMeta = itemView.findViewById(R.id.travelpath_step_meta);
            TextView stepStarValue = itemView.findViewById(R.id.travelpath_step_star_value);

            stepIndex.setText(String.format(Locale.getDefault(), "%d.", index + 1));
            stepName.setText(place.getName());
            stepMeta.setText(formatDistanceLabel(previousPlace, place));
            stepStarValue.setText(formatStarLabel(place));

            itineraryContainer.addView(itemView);
            previousPlace = place;
        }

        lastRenderedItinerary = new ArrayList<>(itinerary);
        currentPlaceIndex = 0;
        renderItineraryMarkers(lastRenderedItinerary);
        centerOnCurrentPlace(true);
        updateNavigationButtons();
    }

    private List<TravelPathPlace> lastRenderedItinerary = new ArrayList<>();

    private void renderItineraryMarkers(@NonNull List<TravelPathPlace> itinerary) {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();
        for (TravelPathPlace place : itinerary) {
            Double lat = place.getLatitude();
            Double lng = place.getLongitude();
            if (lat == null || lng == null) {
                continue;
            }
            LatLng position = new LatLng(lat, lng);
            googleMap.addMarker(new MarkerOptions().position(position).title(place.getName()));
        }
    }

    @NonNull
    private String formatDistanceLabel(@Nullable TravelPathPlace previous, @NonNull TravelPathPlace current) {
        Double currentLat = current.getLatitude();
        Double currentLng = current.getLongitude();
        if (currentLat == null || currentLng == null) {
            return getString(R.string.travelpath_step_distance_unknown);
        }

        if (previous == null || previous.getLatitude() == null || previous.getLongitude() == null) {
            return getString(R.string.travelpath_step_distance_m_format, 0);
        }

        double distanceKm = computeDistanceKm(previous, current);
        if (distanceKm < 1.0) {
            int meters = Math.max(0, (int) Math.round(distanceKm * 1000));
            return getString(R.string.travelpath_step_distance_m_format, meters);
        }
        return getString(R.string.travelpath_step_distance_km_format, distanceKm);
    }

    @NonNull
    private String formatStarLabel(@NonNull TravelPathPlace place) {
        Double star = place.getStar();
        if (star == null) {
            return getString(R.string.travelpath_step_star_placeholder);
        }
        return String.format(Locale.getDefault(), "%.1f", star);
    }

    private double computeDistanceKm(@NonNull TravelPathPlace from, @NonNull TravelPathPlace to) {
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

    private void regenerateItinerary() {
        itineraryPlanner.generateItinerary(requireContext(), placeRepository, new TravelPathItineraryPlanner.ItineraryCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> itinerary) {
                if (!isAdded()) {
                    return;
                }
                TravelPathItineraryStore.saveItinerary(requireContext(), itinerary);
                renderItinerary();
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

    @NonNull
    private TravelPathRoute buildRouteFromCurrentState(@NonNull String routeName) {
        TravelPathRoute route = new TravelPathRoute();
        route.setRouteName(routeName);
        route.setActivities(buildActivitiesLabel());

        SharedPreferences durationPrefs = getPreferences(PREFS_DURATION_BUDGET);
        int budgetMin = Math.max(0, durationPrefs.getInt(KEY_BUDGET_MIN, 0));
        int budgetMax = Math.max(0, durationPrefs.getInt(KEY_BUDGET_MAX, budgetMin));
        route.setBudgetMin(budgetMin);
        route.setBudgetMax(Math.max(budgetMin, budgetMax));

        route.setVisitSummary(readDurationSummary());
        route.setEffort(readEffortLabel());
        route.setRouteType(readSelectedRouteType());
        route.setPlacesSummary(buildPlacesSummary());
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
        SharedPreferences prefs = getPreferences(PREFS_SUMMARY);
        int position = prefs.getInt(KEY_ROUTE_TYPE_POSITION, 0);
        String[] values = getResources().getStringArray(R.array.travelpath_route_type_options);
        if (position < 0 || position >= values.length) {
            return values[0];
        }
        return values[position];
    }

    @NonNull
    private String buildActivitiesLabel() {
        SharedPreferences prefs = getPreferences(PREFS_PREFERENCES);
        Set<String> selectedCategoryIds = prefs.getStringSet(KEY_SELECTED_CATEGORY_IDS, new HashSet<>());
        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            return "";
        }

        List<String> labels = new ArrayList<>();
        for (String categoryId : selectedCategoryIds) {
            String label = mapCategoryIdToLabel(categoryId);
            if (!label.isEmpty()) {
                labels.add(label);
            }
        }
        return joinWithComma(labels);
    }

    @NonNull
    private String buildPlacesSummary() {
        List<TravelPathPlace> itinerary = TravelPathItineraryStore.loadItinerary(requireContext());
        if (itinerary.isEmpty()) {
            return "";
        }
        List<String> names = new ArrayList<>();
        for (TravelPathPlace place : itinerary) {
            names.add(place.getName());
        }
        return joinWithComma(names);
    }

    @NonNull
    private String readDurationSummary() {
        String visitStart = readVisitStartLabel();
        String dateLabel = readSavedVisitDateLabel();

        if (visitStart.isEmpty() && dateLabel.isEmpty()) {
            return "";
        }
        if (visitStart.isEmpty()) {
            return dateLabel;
        }
        if (dateLabel.isEmpty()) {
            return visitStart;
        }
        return String.format(Locale.getDefault(), "%s, %s", visitStart, dateLabel);
    }

    @NonNull
    private String readEffortLabel() {
        SharedPreferences prefs = getPreferences(PREFS_EFFORT);
        String effortIdName = prefs.getString(KEY_EFFORT_ID, "");
        return mapEffortIdToLabel(effortIdName);
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

    private void clearTravelPathState() {
        Context context = requireContext().getApplicationContext();
        context.getSharedPreferences(PREFS_PREFERENCES, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(PREFS_DURATION_BUDGET, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(PREFS_RESULTS, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(PREFS_VISIT_DATE, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(PREFS_EFFORT, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(PREFS_SUMMARY, Context.MODE_PRIVATE).edit().clear().apply();
        TravelPathItineraryStore.clear(context);
    }
}
