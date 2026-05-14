package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TravelPathResultsFragment extends Fragment {

    private static final String PREFS_NAME = "travelpath_results_state";
    private static final String KEY_SELECTED_PLACE_KEYS = "selected_place_keys";
    private static final String KEY_SELECTED_PLACE_ENTRIES = "selected_place_entries";
    private static final String ENTRY_SEPARATOR = "::";

    private GoogleMap googleMap;
    private TravelPathPlaceDataSource placeRepository = new TravelPathPlaceRepository();
    private LinearLayout resultsContainer;
    private TextView resultsCount;
    private Spinner filterSpinner;
    private Spinner sortSpinner;
    private SearchView searchView;
    private int activeSearchRequestId = 0;
    private boolean suppressSearchCallbacks = false;
    private boolean mapEnabled = true;
    private String currentQuery = "";
    private List<TravelPathPlace> sourcePlaces = new ArrayList<>();
    private Set<String> selectedPlaceKeys = new HashSet<>();
    private Map<String, String> selectedPlaceLabelsByKey = new HashMap<>();

    public TravelPathResultsFragment() {
        super(R.layout.fragment_travelpath_results);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resultsContainer = view.findViewById(R.id.travelpath_results_injection_container);
        resultsCount = view.findViewById(R.id.travelpath_results_count);
        filterSpinner = view.findViewById(R.id.travelpath_filter_spinner);
        sortSpinner = view.findViewById(R.id.travelpath_sort_spinner);
        searchView = view.findViewById(R.id.travelpath_search_view);
        selectedPlaceKeys = new HashSet<>(getPreferences().getStringSet(KEY_SELECTED_PLACE_KEYS, new HashSet<>()));
        selectedPlaceLabelsByKey = readSelectedPlaceEntries();

        if (mapEnabled) {
            setupMap();
        }
        setupSearch();
        setupFilterAndSort();
        setupRefresh(view);
        loadPlaces();

        View continueButton = view.findViewById(R.id.travelpath_continue_button);
        continueButton.setOnClickListener(v -> {
            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showDurationBudgetScreen();
            }
        });
    }

    private void loadPlaces() {
        refreshRandomPlaces(false);
    }

    private void setupSearch() {
        if (searchView == null) {
            return;
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (suppressSearchCallbacks) {
                    return true;
                }
                executeSearch(query != null ? query : "");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (suppressSearchCallbacks) {
                    return true;
                }
                executeSearch(newText != null ? newText : "");
                return true;
            }
        });
    }

    private void setupFilterAndSort() {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Filtre/tri s'appliquent uniquement aux resultats deja charges dans la page.
                applyFilterAndSortAndRender();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilterAndSortAndRender();
            }
        };

        if (filterSpinner != null) {
            filterSpinner.setOnItemSelectedListener(listener);
        }
        if (sortSpinner != null) {
            sortSpinner.setOnItemSelectedListener(listener);
        }
    }

    private void executeSearch(@NonNull String rawQuery) {
        if (resultsContainer == null || resultsCount == null) {
            return;
        }

        final int requestId = ++activeSearchRequestId;
        String query = rawQuery.trim();
        currentQuery = query;

        resultsContainer.removeAllViews();
        resultsCount.setText(
                query.isEmpty()
                        ? getString(R.string.travelpath_results_loading)
                        : getString(R.string.travelpath_results_searching)
        );

        TravelPathPlaceRepository.LoadCallback callback = new TravelPathPlaceRepository.LoadCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> places) {
                if (!isAdded() || requestId != activeSearchRequestId) {
                    return;
                }

                sourcePlaces = new ArrayList<>(places);
                List<TravelPathPlace> displayedPlaces = buildDisplayedPlaces();
                renderPlaces(displayedPlaces);
                notifyIfFallbackUsed(places);

                if (displayedPlaces.isEmpty() && !query.isEmpty()) {
                    resultsCount.setText(getString(R.string.travelpath_results_no_match, query));
                } else {
                    resultsCount.setText(getString(R.string.travelpath_results_count_format, displayedPlaces.size()));
                }
            }

            @Override
            public void onError(@NonNull Exception exception) {
                if (!isAdded() || requestId != activeSearchRequestId) {
                    return;
                }

                resultsCount.setText(getString(R.string.travelpath_results_load_error));
                Toast.makeText(requireContext(), R.string.travelpath_results_load_error, Toast.LENGTH_SHORT).show();
            }
        };

        if (query.isEmpty()) {
            placeRepository.loadPlaces(callback);
        } else {
            placeRepository.searchPlacesByName(query, callback);
        }
    }

    private void setupRefresh(@NonNull View rootView) {
        ImageButton refreshButton = rootView.findViewById(R.id.travelpath_refresh_button);
        if (refreshButton != null) {
            refreshButton.setOnClickListener(v -> refreshRandomPlaces(true));
        }
    }

    private void refreshRandomPlaces(boolean clearSearch) {
        if (resultsContainer == null || resultsCount == null) {
            return;
        }

        if (clearSearch && searchView != null) {
            suppressSearchCallbacks = true;
            searchView.setQuery("", false);
            suppressSearchCallbacks = false;
            currentQuery = "";
        }

        final int requestId = ++activeSearchRequestId;
        resultsContainer.removeAllViews();
        resultsCount.setText(getString(R.string.travelpath_results_loading));

        String selectedTheme = mapFilterSelectionToTheme();
        placeRepository.loadRandomPlaces(selectedTheme, new TravelPathPlaceRepository.LoadCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> places) {
                if (!isAdded() || requestId != activeSearchRequestId) {
                    return;
                }

                sourcePlaces = new ArrayList<>(places);
                applyFilterAndSortAndRender();
                notifyIfFallbackUsed(places);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                if (!isAdded() || requestId != activeSearchRequestId) {
                    return;
                }

                resultsCount.setText(getString(R.string.travelpath_results_load_error));
                Toast.makeText(requireContext(), R.string.travelpath_results_load_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderPlaces(@NonNull List<TravelPathPlace> places) {
        if (resultsContainer == null || !isAdded()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        resultsContainer.removeAllViews();
        for (TravelPathPlace place : places) {
            View item = inflater.inflate(R.layout.item_travelpath_result, resultsContainer, false);
            ImageView imageView = item.findViewById(R.id.travelpath_result_image);
            TextView title = item.findViewById(R.id.travelpath_result_title);
            TextView theme = item.findViewById(R.id.travelpath_result_theme);
            TextView location = item.findViewById(R.id.travelpath_result_location);
            LinearLayout addContainer = item.findViewById(R.id.travelpath_result_add_container);
            CheckBox favoriteToggle = item.findViewById(R.id.travelpath_result_favorite_toggle);
            title.setText(place.getName());
            theme.setText(getString(R.string.travelpath_result_theme_format, place.getTheme()));
            location.setText(getString(R.string.travelpath_result_location_value));
            loadPlaceImage(imageView, place.getImageUrl());

            String placeKey = buildSavedPlaceKey(place);
            favoriteToggle.setChecked(selectedPlaceKeys.contains(placeKey));

            addContainer.setOnClickListener(v -> {
                boolean nextCheckedState = !favoriteToggle.isChecked();
                favoriteToggle.setChecked(nextCheckedState);
                updateSavedPlaceSelection(placeKey, place.getName(), nextCheckedState);
            });
            favoriteToggle.setOnClickListener(v -> updateSavedPlaceSelection(placeKey, place.getName(), favoriteToggle.isChecked()));

            item.setOnClickListener(v -> {
                Fragment parent = getParentFragment();
                if (parent instanceof TravelPathMainFragment) {
                    ((TravelPathMainFragment) parent).showPlaceDetailScreen(place);
                }
            });

            resultsContainer.addView(item);
        }
    }

    private void loadPlaceImage(@NonNull ImageView imageView, @Nullable String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }
        if (imageUrl.startsWith("gs://")) {
            FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl)
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        if (isAdded()) {
                            Glide.with(this).load(uri).into(imageView);
                        }
                    });
            return;
        }
        Glide.with(this).load(imageUrl).into(imageView);
    }

    private void updateSavedPlaceSelection(@NonNull String placeKey, @NonNull String placeName, boolean isSelected) {
        if (isSelected) {
            selectedPlaceKeys.add(placeKey);
            selectedPlaceLabelsByKey.put(placeKey, placeName);
        } else {
            selectedPlaceKeys.remove(placeKey);
            selectedPlaceLabelsByKey.remove(placeKey);
        }
        getPreferences().edit()
                .putStringSet(KEY_SELECTED_PLACE_KEYS, new HashSet<>(selectedPlaceKeys))
                .putStringSet(KEY_SELECTED_PLACE_ENTRIES, serializeSelectedPlaceEntries())
                .apply();
    }

    @NonNull
    private Map<String, String> readSelectedPlaceEntries() {
        Set<String> rawEntries = getPreferences().getStringSet(KEY_SELECTED_PLACE_ENTRIES, new HashSet<>());
        Map<String, String> entries = new HashMap<>();
        for (String rawEntry : rawEntries) {
            int separatorIndex = rawEntry.indexOf(ENTRY_SEPARATOR);
            if (separatorIndex <= 0 || separatorIndex >= rawEntry.length() - ENTRY_SEPARATOR.length()) {
                continue;
            }
            String key = rawEntry.substring(0, separatorIndex);
            String label = rawEntry.substring(separatorIndex + ENTRY_SEPARATOR.length());
            if (!key.isEmpty() && !label.isEmpty()) {
                entries.put(key, label);
            }
        }
        return entries;
    }

    @NonNull
    private Set<String> serializeSelectedPlaceEntries() {
        Set<String> serializedEntries = new HashSet<>();
        for (Map.Entry<String, String> entry : selectedPlaceLabelsByKey.entrySet()) {
            serializedEntries.add(entry.getKey() + ENTRY_SEPARATOR + entry.getValue());
        }
        return serializedEntries;
    }

    @NonNull
    private String buildSavedPlaceKey(@NonNull TravelPathPlace place) {
        return normalizeText(place.getName()) + "|" + normalizeText(place.getTheme());
    }

    @NonNull
    private SharedPreferences getPreferences() {
        Context context = requireContext().getApplicationContext();
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void applyFilterAndSortAndRender() {
        List<TravelPathPlace> displayedPlaces = buildDisplayedPlaces();
        renderPlaces(displayedPlaces);
        notifyIfFallbackUsed(displayedPlaces);

        if (displayedPlaces.isEmpty() && !TextUtils.isEmpty(currentQuery)) {
            resultsCount.setText(getString(R.string.travelpath_results_no_match, currentQuery));
        } else {
            resultsCount.setText(getString(R.string.travelpath_results_count_format, displayedPlaces.size()));
        }
    }

    private void notifyIfFallbackUsed(@NonNull List<TravelPathPlace> places) {
        for (TravelPathPlace place : places) {
            if ("fallback".equals(place.getSourceCollection())) {
                Toast.makeText(requireContext(), R.string.travelpath_fallback_used, Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    @NonNull
    private List<TravelPathPlace> buildDisplayedPlaces() {
        List<TravelPathPlace> displayed = new ArrayList<>(sourcePlaces);

        String selectedTheme = mapFilterSelectionToTheme();
        if (!TextUtils.isEmpty(selectedTheme)) {
            String normalizedTheme = normalizeText(selectedTheme);
            displayed.removeIf(place -> !normalizeText(place.getTheme()).equals(normalizedTheme));
        }

        int sortMode = sortSpinner != null ? sortSpinner.getSelectedItemPosition() : 0;
        if (sortMode == 1) {
            displayed.sort(Comparator.comparing(TravelPathPlace::getName, String.CASE_INSENSITIVE_ORDER));
        } else if (sortMode == 2) {
            displayed.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(b.getName(), a.getName()));
        } else if (sortMode == 3) {
            displayed.sort(Comparator
                    .comparing(TravelPathPlace::getTheme, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(TravelPathPlace::getName, String.CASE_INSENSITIVE_ORDER));
        } else if (sortMode == 4) {
            displayed.sort((a, b) -> {
                int byTheme = String.CASE_INSENSITIVE_ORDER.compare(b.getTheme(), a.getTheme());
                if (byTheme != 0) {
                    return byTheme;
                }
                return String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName());
            });
        }

        return displayed;
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

    @Nullable
    private String mapFilterSelectionToTheme() {
        if (filterSpinner == null) {
            return null;
        }

        int selected = filterSpinner.getSelectedItemPosition();
        switch (selected) {
            case 2:
                return "Restaurant";
            case 3:
                return "Culture";
            case 4:
                return "Monument";
            case 5:
                return "Loisir";
            case 6:
                return "Shopping";
            case 7:
                return "Evenements";
            default:
                return null;
        }
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
        Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.travelpath_map_fragment);
        if (!(mapFragment instanceof SupportMapFragment)) {
            return;
        }

        ((SupportMapFragment) mapFragment).getMapAsync(map -> {
            googleMap = map;
            TravelPathMapLocationHelper.centerMapOnUserOrFallback(this, map);
        });
    }

    void setPlaceDataSourceForTest(@NonNull TravelPathPlaceDataSource placeDataSource) {
        this.placeRepository = placeDataSource;
    }

    void setMapEnabledForTest(boolean enabled) {
        this.mapEnabled = enabled;
    }
}
