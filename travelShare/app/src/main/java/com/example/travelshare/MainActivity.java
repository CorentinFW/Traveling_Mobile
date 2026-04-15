package com.example.travelshare;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelshare.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_SCREEN = "key_screen";
    private static final String KEY_BOTTOM_SCREEN = "key_bottom_screen";

    private ActivityMainBinding binding;
    private Screen currentScreen = Screen.HOME;
    private Screen bottomScreen = Screen.HOME;
    private boolean suppressBottomNavigationCallback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.topAppBar.setNavigationOnClickListener(v -> openProfile());
        applySystemBarInsets();

        binding.bottomNavigation.setOnItemSelectedListener(this::onBottomNavigationSelected);

        if (savedInstanceState != null) {
            currentScreen = Screen.valueOf(savedInstanceState.getString(KEY_SCREEN, Screen.HOME.name()));
            bottomScreen = Screen.valueOf(savedInstanceState.getString(KEY_BOTTOM_SCREEN, Screen.HOME.name()));
        }

        showScreen(currentScreen, false);
        selectBottomItem(bottomScreen);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SCREEN, currentScreen.name());
        outState.putString(KEY_BOTTOM_SCREEN, bottomScreen.name());
    }

    private boolean onBottomNavigationSelected(@NonNull android.view.MenuItem item) {
        if (suppressBottomNavigationCallback) {
            return true;
        }

        if (item.getItemId() == R.id.navigation_home) {
            showScreen(Screen.HOME, false);
            return true;
        } else if (item.getItemId() == R.id.navigation_search) {
            showScreen(Screen.SEARCH, false);
            return true;
        } else if (item.getItemId() == R.id.navigation_messages) {
            showScreen(Screen.MESSAGES, false);
            return true;
        } else if (item.getItemId() == R.id.navigation_add) {
            showScreen(Screen.ADD, false);
            return true;
        } else if (item.getItemId() == R.id.navigation_itinerary) {
            showScreen(Screen.ITINERARY, false);
            return true;
        }
        return false;
    }

    private void openProfile() {
        showScreen(Screen.PROFILE, false);
    }

    private void showScreen(@NonNull Screen screen, boolean updateBottomSelection) {
        currentScreen = screen;

        if (screen != Screen.PROFILE) {
            bottomScreen = screen;
        }

        Fragment fragment;
        String title;

        switch (screen) {
            case SEARCH:
                fragment = PlaceholderFragment.newInstance(
                        getString(R.string.title_search),
                        getString(R.string.subtitle_search),
                        getString(R.string.placeholder_search)
                );
                title = getString(R.string.title_search);
                break;
            case MESSAGES:
                fragment = PlaceholderFragment.newInstance(
                        getString(R.string.title_messages),
                        getString(R.string.subtitle_messages),
                        getString(R.string.placeholder_messages)
                );
                title = getString(R.string.title_messages);
                break;
            case ADD:
                fragment = PlaceholderFragment.newInstance(
                        getString(R.string.title_add),
                        getString(R.string.subtitle_add),
                        getString(R.string.placeholder_add)
                );
                title = getString(R.string.title_add);
                break;
            case ITINERARY:
                fragment = PlaceholderFragment.newInstance(
                        getString(R.string.title_itinerary),
                        getString(R.string.subtitle_itinerary),
                        getString(R.string.placeholder_itinerary)
                );
                title = getString(R.string.title_itinerary);
                break;
            case PROFILE:
                fragment = PlaceholderFragment.newInstance(
                        getString(R.string.title_profile),
                        getString(R.string.subtitle_profile),
                        getString(R.string.placeholder_profile)
                );
                title = getString(R.string.title_profile);
                break;
            case HOME:
            default:
                fragment = PlaceholderFragment.newInstance(
                        getString(R.string.title_home),
                        getString(R.string.subtitle_home),
                        getString(R.string.placeholder_home)
                );
                title = getString(R.string.title_home);
                break;
        }

        binding.topAppBar.setTitle(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        if (updateBottomSelection) {
            selectBottomItem(bottomScreen);
        }
    }

    private void selectBottomItem(@NonNull Screen screen) {
        int itemId;
        switch (screen) {
            case SEARCH:
                itemId = R.id.navigation_search;
                break;
            case MESSAGES:
                itemId = R.id.navigation_messages;
                break;
            case ADD:
                itemId = R.id.navigation_add;
                break;
            case ITINERARY:
                itemId = R.id.navigation_itinerary;
                break;
            case HOME:
            default:
                itemId = R.id.navigation_home;
                break;
        }
        BottomNavigationView bottomNavigationView = binding.bottomNavigation;
        if (bottomNavigationView.getSelectedItemId() == itemId) {
            return;
        }

        suppressBottomNavigationCallback = true;
        try {
            bottomNavigationView.setSelectedItemId(itemId);
        } finally {
            suppressBottomNavigationCallback = false;
        }
    }

    private void applySystemBarInsets() {
        final View root = binding.main;
        final int rootStart = root.getPaddingStart();
        final int rootTop = root.getPaddingTop();
        final int rootEnd = root.getPaddingEnd();
        final int rootBottom = root.getPaddingBottom();

        final View bottomNavigation = binding.bottomNavigation;
        final int navStart = bottomNavigation.getPaddingStart();
        final int navTop = bottomNavigation.getPaddingTop();
        final int navEnd = bottomNavigation.getPaddingEnd();
        final int navBottom = bottomNavigation.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            view.setPaddingRelative(
                    rootStart + systemBars.left,
                    rootTop + systemBars.top,
                    rootEnd + systemBars.right,
                    rootBottom
            );

            bottomNavigation.setPaddingRelative(
                    navStart,
                    navTop,
                    navEnd,
                    navBottom + systemBars.bottom
            );

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(root);
    }

    private enum Screen {
        HOME,
        SEARCH,
        MESSAGES,
        ADD,
        ITINERARY,
        PROFILE
    }
}