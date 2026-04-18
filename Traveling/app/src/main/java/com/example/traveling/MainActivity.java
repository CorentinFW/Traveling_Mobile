package com.example.traveling;

import android.os.Bundle;
import android.util.SparseArray;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;
import com.example.traveling.travelshare.ui.TravelShareAuthFragment;
import com.example.traveling.travelshare.ui.TravelSharePostDetailFragment;
import com.example.traveling.travelshare.ui.TravelShareProfileFragment;
import com.example.traveling.travelshare.ui.navigation.TravelShareBottomNavConfig;
import com.example.traveling.travelshare.ui.navigation.TravelShareNavItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TravelShareSessionRepository sessionRepository;
    private Button sessionButton;
    private final SparseArray<TravelShareNavItem> navItemRegistry = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sessionRepository = TravelShareDataProvider.sessionRepository();
        sessionButton = findViewById(R.id.travelshare_session_button);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        sessionButton.setOnClickListener(v -> openSessionScreen());
        refreshSessionButton();

        BottomNavigationView bottomNavigationView = findViewById(R.id.travelshare_bottom_nav);
        setupBottomNavigation(bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            return switchToTab(item.getItemId());
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.travelshare_nav_home);
        }
    }

    public void refreshSessionButton() {
        if (sessionRepository.isAuthenticated()) {
            sessionButton.setText(R.string.travelshare_auth_profile);
        } else {
            sessionButton.setText(R.string.travelshare_auth_login);
        }
    }

    public void openPostDetail(String postId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, TravelSharePostDetailFragment.newInstance(postId))
                .addToBackStack("post_detail")
                .commit();
    }

    private void openSessionScreen() {
        Fragment fragment = sessionRepository.isAuthenticated()
                ? new TravelShareProfileFragment()
                : new TravelShareAuthFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, fragment)
                .addToBackStack("session")
                .commit();
    }

    private void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        List<TravelShareNavItem> navItems = TravelShareBottomNavConfig.buildItems();
        TravelShareBottomNavConfig.inflateMenu(bottomNavigationView, navItems);

        navItemRegistry.clear();
        for (TravelShareNavItem navItem : navItems) {
            navItemRegistry.put(navItem.getItemId(), navItem);
        }
    }

    private boolean switchToTab(int itemId) {
        TravelShareNavItem navItem = navItemRegistry.get(itemId);
        if (navItem == null) {
            return false;
        }

        Fragment fragment = navItem.createFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, fragment)
                .commit();

        return true;
    }
}