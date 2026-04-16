package com.example.traveling;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.traveling.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment introuvable");
        }
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        binding.bottomNavigation.setOnItemReselectedListener(item -> {
            // Evite de réempiler inutilement la même destination.
        });

        binding.topAppBar.setNavigationOnClickListener(v -> openProfile());
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            CharSequence label = destination.getLabel();
            binding.topAppBar.setTitle(label != null ? label : getString(R.string.app_name));
        });

        applySystemBarInsets();
    }

    private void openProfile() {
        if (navController.getCurrentDestination() == null
                || navController.getCurrentDestination().getId() != R.id.profile_screen) {
            navController.navigate(R.id.profile_screen);
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
}
