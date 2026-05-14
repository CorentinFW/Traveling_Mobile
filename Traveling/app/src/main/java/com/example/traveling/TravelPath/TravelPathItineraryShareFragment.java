package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;

public class TravelPathItineraryShareFragment extends Fragment {

    private static final String ARG_ROUTE_NAME = "arg_route_name";
    private static final String ARG_ACTIVITIES = "arg_activities";
    private static final String ARG_BUDGET_MIN = "arg_budget_min";
    private static final String ARG_BUDGET_MAX = "arg_budget_max";
    private static final String ARG_VISIT_SUMMARY = "arg_visit_summary";
    private static final String ARG_EFFORT = "arg_effort";
    private static final String ARG_ROUTE_TYPE = "arg_route_type";
    private static final String ARG_PLACES_SUMMARY = "arg_places_summary";

    private LinearLayout itineraryContainer;
    private TextView budgetValue;
    private TextView durationValue;
    private TextView effortValue;
    private TextView titleValue;
    private ImageButton prevPlaceButton;
    private ImageButton nextPlaceButton;
    private int currentPlaceIndex = 0;

    private GoogleMap googleMap;
    private final TravelPathPlaceRepository placeRepository = new TravelPathPlaceRepository();
    private List<TravelPathPlace> lastRenderedPlaces = new ArrayList<>();

    public TravelPathItineraryShareFragment() {
        super(R.layout.fragment_travelpath_itinerary_share);
    }

    @NonNull
    public static TravelPathItineraryShareFragment newInstance(@NonNull TravelPathRoute route) {
        TravelPathItineraryShareFragment fragment = new TravelPathItineraryShareFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROUTE_NAME, safeString(route.getRouteName()));
        args.putString(ARG_ACTIVITIES, safeString(route.getActivities()));
        args.putInt(ARG_BUDGET_MIN, route.getBudgetMin());
        args.putInt(ARG_BUDGET_MAX, route.getBudgetMax());
        args.putString(ARG_VISIT_SUMMARY, safeString(route.getVisitSummary()));
        args.putString(ARG_EFFORT, safeString(route.getEffort()));
        args.putString(ARG_ROUTE_TYPE, safeString(route.getRouteType()));
        args.putString(ARG_PLACES_SUMMARY, safeString(route.getPlacesSummary()));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        itineraryContainer = view.findViewById(R.id.travelpath_itinerary_injection_container);
        budgetValue = view.findViewById(R.id.travelpath_itinerary_budget_value);
        durationValue = view.findViewById(R.id.travelpath_itinerary_duration_value);
        effortValue = view.findViewById(R.id.travelpath_itinerary_effort_value);
        titleValue = view.findViewById(R.id.travelpath_itinerary_title);
        prevPlaceButton = view.findViewById(R.id.travelpath_itinerary_prev_button);
        nextPlaceButton = view.findViewById(R.id.travelpath_itinerary_next_button);

        setupMap();
        setupPlaceNavigation();

        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        String routeName = args.getString(ARG_ROUTE_NAME, "");
        int budgetMin = args.getInt(ARG_BUDGET_MIN, 0);
        int budgetMax = args.getInt(ARG_BUDGET_MAX, 0);
        String visitSummary = args.getString(ARG_VISIT_SUMMARY, "");
        String effort = args.getString(ARG_EFFORT, "");
        String placesSummary = args.getString(ARG_PLACES_SUMMARY, "");

        if (!routeName.trim().isEmpty()) {
            titleValue.setText(routeName.trim());
        }

        if (budgetMin > 0 || budgetMax > 0) {
            int normalizedMin = Math.max(0, budgetMin);
            int normalizedMax = Math.max(normalizedMin, budgetMax);
            String formatted = String.format(
                    Locale.getDefault(),
                    "%d-%d%s",
                    normalizedMin,
                    normalizedMax,
                    getString(R.string.travelpath_budget_currency)
            );
            budgetValue.setText(formatted);
        }

        if (!visitSummary.trim().isEmpty()) {
            durationValue.setText(visitSummary.trim());
        }

        if (!effort.trim().isEmpty()) {
            effortValue.setText(effort.trim());
        }

        renderPlaces(placesSummary);
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
            TravelPathMapLocationHelper.centerMapOnUserOrFallback(this, map);
            renderPlaceMarkers(lastRenderedPlaces);
            centerOnCurrentPlace(false);
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
        if (newIndex < 0 || newIndex >= lastRenderedPlaces.size()) {
            return;
        }
        currentPlaceIndex = newIndex;
        centerOnCurrentPlace(true);
        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        updateNavigationButton(prevPlaceButton, currentPlaceIndex > 0);
        updateNavigationButton(nextPlaceButton, currentPlaceIndex + 1 < lastRenderedPlaces.size());
    }

    private void updateNavigationButton(@Nullable ImageButton button, boolean enabled) {
        if (button == null) {
            return;
        }
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.4f);
    }

    private boolean centerOnCurrentPlace(boolean animate) {
        if (googleMap == null || lastRenderedPlaces.isEmpty()) {
            return false;
        }
        if (currentPlaceIndex < 0 || currentPlaceIndex >= lastRenderedPlaces.size()) {
            return false;
        }
        TravelPathPlace place = lastRenderedPlaces.get(currentPlaceIndex);
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

    private void renderPlaces(@Nullable String placesSummary) {
        if (itineraryContainer == null) {
            return;
        }

        List<String> placeNames = splitPlaces(placesSummary);
        if (placeNames.isEmpty()) {
            itineraryContainer.removeAllViews();
            return;
        }

        placeRepository.loadAllPlaces(new TravelPathPlaceRepository.LoadCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> places) {
                if (!isAdded()) {
                    return;
                }
                List<TravelPathPlace> ordered = buildOrderedPlaces(placeNames, places);
                renderPlaceList(ordered);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                if (!isAdded()) {
                    return;
                }
                renderPlaceList(buildFallbackPlaces(placeNames));
                Toast.makeText(requireContext(), R.string.travelpath_results_load_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderPlaceList(@NonNull List<TravelPathPlace> itinerary) {
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

        lastRenderedPlaces = new ArrayList<>(itinerary);
        currentPlaceIndex = 0;
        renderPlaceMarkers(lastRenderedPlaces);
        centerOnCurrentPlace(true);
        updateNavigationButtons();
    }

    private void renderPlaceMarkers(@NonNull List<TravelPathPlace> itinerary) {
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
    private List<TravelPathPlace> buildOrderedPlaces(
            @NonNull List<String> placeNames,
            @NonNull List<TravelPathPlace> candidates
    ) {
        Map<String, TravelPathPlace> byName = new HashMap<>();
        for (TravelPathPlace place : candidates) {
            String key = normalizeText(place.getName());
            if (!byName.containsKey(key)) {
                byName.put(key, place);
            }
        }

        List<TravelPathPlace> ordered = new ArrayList<>();
        for (String name : placeNames) {
            TravelPathPlace match = byName.get(normalizeText(name));
            ordered.add(match != null ? match : new TravelPathPlace(name, "", null));
        }
        return ordered;
    }

    @NonNull
    private List<TravelPathPlace> buildFallbackPlaces(@NonNull List<String> placeNames) {
        List<TravelPathPlace> fallback = new ArrayList<>();
        for (String name : placeNames) {
            fallback.add(new TravelPathPlace(name, "", null));
        }
        return fallback;
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

    @NonNull
    private String normalizeText(@NonNull String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{M}+", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }

    @NonNull
    private static String safeString(@Nullable String value) {
        return value == null ? "" : value;
    }

    @NonNull
    private List<String> splitPlaces(@Nullable String placesSummary) {
        List<String> places = new ArrayList<>();
        if (placesSummary == null || placesSummary.trim().isEmpty()) {
            return places;
        }
        String[] tokens = placesSummary.split(",");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                places.add(trimmed);
            }
        }
        return places;
    }
}
