package com.example.traveling.TravelPath;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.MainActivity;
import com.example.traveling.R;
import com.google.firebase.auth.FirebaseAuth;

public class TravelPathMainFragment extends Fragment {

    private static final String KEY_CURRENT_SCREEN = "travelpath_current_screen";
    private static final String KEY_SCREEN_HISTORY = "travelpath_screen_history";
    private static final String KEY_PLACE_DETAIL_NAME = "travelpath_place_detail_name";
    private static final String KEY_PLACE_DETAIL_LAT = "travelpath_place_detail_lat";
    private static final String KEY_PLACE_DETAIL_LNG = "travelpath_place_detail_lng";
    private static final int MAX_HISTORY_SIZE = 5;

    private enum Screen {
        AUTH,
        WELCOME,
        PREFERENCES,
        RESULTS,
        DURATION_BUDGET,
        VISIT_DATE,
        EFFORT,
        SUMMARY,
        ITINERARY,
        MY_ROUTES,
        PLACE_DETAIL
    }

    private final Deque<Screen> screenHistory = new ArrayDeque<>();
    private Screen currentScreen;
    @Nullable
    private TravelPathPlace currentDetailPlace;

    public TravelPathMainFragment() {
        super(R.layout.fragment_travelpath_main);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupFooterActions(view);

        if (!isUserAuthenticated()) {
            screenHistory.clear();
            currentScreen = Screen.AUTH;
            replaceInjectedScreen(currentScreen);
            return;
        }

        if (savedInstanceState == null) {
            currentScreen = Screen.WELCOME;
            replaceInjectedScreen(currentScreen);
            return;
        }

        restoreNavigationState(savedInstanceState);
        if (getChildFragmentManager().findFragmentById(R.id.travelpath_content_container) == null) {
            replaceInjectedScreen(currentScreen);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentScreen != null) {
            outState.putString(KEY_CURRENT_SCREEN, currentScreen.name());
        }

        if (currentDetailPlace != null) {
            outState.putString(KEY_PLACE_DETAIL_NAME, currentDetailPlace.getName());
            if (currentDetailPlace.getLatitude() != null) {
                outState.putDouble(KEY_PLACE_DETAIL_LAT, currentDetailPlace.getLatitude());
            }
            if (currentDetailPlace.getLongitude() != null) {
                outState.putDouble(KEY_PLACE_DETAIL_LNG, currentDetailPlace.getLongitude());
            }
        }

        ArrayList<String> serializedHistory = new ArrayList<>();
        for (Screen screen : screenHistory) {
            serializedHistory.add(screen.name());
        }
        outState.putStringArrayList(KEY_SCREEN_HISTORY, serializedHistory);
    }

    private void setupFooterActions(@NonNull View rootView) {
        LinearLayout homeButton = rootView.findViewById(R.id.travelpath_footer_home);
        LinearLayout backButton = rootView.findViewById(R.id.travelpath_footer_back);
        LinearLayout consultButton = rootView.findViewById(R.id.travelpath_footer_consult);

        homeButton.setOnClickListener(v -> goBackToMainActivityScreen());
        backButton.setOnClickListener(v -> navigateToPreviousInjectedScreen());
        consultButton.setOnClickListener(v -> navigateToScreen(Screen.MY_ROUTES));
    }

    public void showPreferencesScreen() {
        navigateToScreen(Screen.PREFERENCES);
    }

    public void onAuthenticationSucceeded() {
        screenHistory.clear();
        currentScreen = Screen.WELCOME;
        replaceInjectedScreen(currentScreen);
    }

    public void showResultsScreen() {
        navigateToScreen(Screen.RESULTS);
    }

    public void showDurationBudgetScreen() {
        navigateToScreen(Screen.DURATION_BUDGET);
    }

    public void showVisitDateScreen() {
        navigateToScreen(Screen.VISIT_DATE);
    }

    public void showEffortScreen() {
        navigateToScreen(Screen.EFFORT);
    }

    public void showSummaryScreen() {
        navigateToScreen(Screen.SUMMARY);
    }

    public void showItineraryScreen() {
        navigateToScreen(Screen.ITINERARY);
    }

    public void showPlaceDetailScreen(@NonNull TravelPathPlace place) {
        currentDetailPlace = place;
        navigateToCustomScreen(Screen.PLACE_DETAIL, TravelPathPlaceDetailFragment.newInstance(place));
    }

    private void goBackToMainActivityScreen() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void navigateToPreviousInjectedScreen() {
        if (!isUserAuthenticated()) {
            currentScreen = Screen.AUTH;
            replaceInjectedScreen(currentScreen);
            return;
        }

        if (screenHistory.isEmpty()) {
            return;
        }

        Screen previousScreen = screenHistory.removeLast();
        currentScreen = previousScreen;
        if (currentScreen != Screen.PLACE_DETAIL) {
            currentDetailPlace = null;
        }
        replaceInjectedScreen(previousScreen);
    }

    private void navigateToScreen(@NonNull Screen targetScreen) {
        if (targetScreen != Screen.AUTH && !isUserAuthenticated()) {
            targetScreen = Screen.AUTH;
        }

        if (targetScreen == currentScreen) {
            return;
        }

        if (targetScreen != Screen.PLACE_DETAIL) {
            currentDetailPlace = null;
        }

        if (currentScreen != null) {
            screenHistory.addLast(currentScreen);
            while (screenHistory.size() > MAX_HISTORY_SIZE) {
                screenHistory.removeFirst();
            }
        }

        currentScreen = targetScreen;
        replaceInjectedScreen(targetScreen);
    }

    private void navigateToCustomScreen(@NonNull Screen targetScreen, @NonNull Fragment targetFragment) {
        if (targetScreen == currentScreen) {
            return;
        }

        if (currentScreen != null) {
            screenHistory.addLast(currentScreen);
            while (screenHistory.size() > MAX_HISTORY_SIZE) {
                screenHistory.removeFirst();
            }
        }

        currentScreen = targetScreen;
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.travelpath_content_container, targetFragment)
                .commit();
    }

    private void replaceInjectedScreen(@NonNull Screen screen) {
        Fragment fragment = createFragmentFor(screen);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.travelpath_content_container, fragment)
                .commit();
    }

    @NonNull
    private Fragment createFragmentFor(@NonNull Screen screen) {
        if (screen == Screen.AUTH) {
            return new TravelPathAuthFragment();
        }
        if (screen == Screen.ITINERARY) {
            return new TravelPathItineraryFragment();
        }
        if (screen == Screen.SUMMARY) {
            return new TravelPathSummaryFragment();
        }
        if (screen == Screen.EFFORT) {
            return new TravelPathEffortFragment();
        }
        if (screen == Screen.VISIT_DATE) {
            return new TravelPathVisitDateFragment();
        }
        if (screen == Screen.DURATION_BUDGET) {
            return new TravelPathDurationBudgetFragment();
        }
        if (screen == Screen.RESULTS) {
            return new TravelPathResultsFragment();
        }
        if (screen == Screen.PREFERENCES) {
            return new TravelPathPreferencesFragment();
        }
        if (screen == Screen.MY_ROUTES) {
            return new TravelPathMyRoutesFragment();
        }
        if (screen == Screen.PLACE_DETAIL) {
            if (currentDetailPlace != null) {
                return TravelPathPlaceDetailFragment.newInstance(currentDetailPlace);
            }
            return new TravelPathPlaceDetailFragment();
        }
        return new TravelPathWelcomeFragment();
    }

    private void restoreNavigationState(@NonNull Bundle savedInstanceState) {
        String currentScreenName = savedInstanceState.getString(KEY_CURRENT_SCREEN, Screen.WELCOME.name());
        currentScreen = parseScreen(currentScreenName, Screen.WELCOME);
        String detailName = savedInstanceState.getString(KEY_PLACE_DETAIL_NAME, "");
        if (!detailName.isEmpty()) {
            Double detailLat = savedInstanceState.containsKey(KEY_PLACE_DETAIL_LAT)
                    ? savedInstanceState.getDouble(KEY_PLACE_DETAIL_LAT)
                    : null;
            Double detailLng = savedInstanceState.containsKey(KEY_PLACE_DETAIL_LNG)
                    ? savedInstanceState.getDouble(KEY_PLACE_DETAIL_LNG)
                    : null;
            currentDetailPlace = new TravelPathPlace(detailName, "", null, detailLat, detailLng);
        }

        if (!isUserAuthenticated()) {
            currentScreen = Screen.AUTH;
            screenHistory.clear();
            currentDetailPlace = null;
            return;
        }

        screenHistory.clear();
        ArrayList<String> serializedHistory = savedInstanceState.getStringArrayList(KEY_SCREEN_HISTORY);
        if (serializedHistory == null) {
            return;
        }

        for (String screenName : serializedHistory) {
            screenHistory.addLast(parseScreen(screenName, Screen.WELCOME));
        }
        while (screenHistory.size() > MAX_HISTORY_SIZE) {
            screenHistory.removeFirst();
        }
    }

    @NonNull
    private Screen parseScreen(@Nullable String screenName, @NonNull Screen fallback) {
        if (screenName == null) {
            return fallback;
        }

        try {
            return Screen.valueOf(screenName);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private boolean isUserAuthenticated() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
}

