package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TravelPathResultsFragment extends Fragment {

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
            TextView title = item.findViewById(R.id.travelpath_result_title);
            TextView theme = item.findViewById(R.id.travelpath_result_theme);
            TextView location = item.findViewById(R.id.travelpath_result_location);
            title.setText(place.getName());
            theme.setText(getString(R.string.travelpath_result_theme_format, place.getTheme()));
            location.setText(getString(R.string.travelpath_result_location_value));
            resultsContainer.addView(item);
        }
    }

    private void applyFilterAndSortAndRender() {
        List<TravelPathPlace> displayedPlaces = buildDisplayedPlaces();
        renderPlaces(displayedPlaces);

        if (displayedPlaces.isEmpty() && !TextUtils.isEmpty(currentQuery)) {
            resultsCount.setText(getString(R.string.travelpath_results_no_match, currentQuery));
        } else {
            resultsCount.setText(getString(R.string.travelpath_results_count_format, displayedPlaces.size()));
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

