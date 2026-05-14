package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.traveling.TravelPath.TravelPathAuthFragment;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;
import com.example.traveling.travelshare.ui.TravelShareAddPostFragment;
import com.example.traveling.travelshare.ui.TravelShareAuthFragment;
import com.example.traveling.travelshare.ui.TravelShareHomeFragment;
import com.example.traveling.travelshare.ui.TravelShareItineraryFragment;
import com.example.traveling.travelshare.ui.TravelShareMessagesFragment;
import com.example.traveling.travelshare.ui.TravelShareNotificationsFragment;
import com.example.traveling.travelshare.ui.TravelSharePostDetailFragment;
import com.example.traveling.travelshare.ui.TravelShareProfileFragment;
import com.example.traveling.travelshare.ui.TravelShareSearchFragment;
import com.example.traveling.travelshare.ui.navigation.TravelShareBottomNavConfig;
import com.example.traveling.travelshare.ui.navigation.TravelShareNavItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TravelShareSessionRepository sessionRepository;
    private Button sessionButton;
    private ImageButton notificationsButton;
    private final Map<Integer, TravelShareNavItem> navItemsById = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        logFirebaseProjectInfo();

        // Enable Firestore-backed TravelShare providers so TravelShare data is persisted to Firebase.
        try {
            TravelShareDataProvider.useFirestore();
        } catch (Exception e) {
            Log.w("TravelShare", "Firestore provider switch failed", e);
        }

        sessionRepository = TravelShareDataProvider.sessionRepository();
        sessionButton = findViewById(R.id.travelshare_session_button);
        notificationsButton = findViewById(R.id.travelshare_notifications_button);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        sessionButton.setOnClickListener(v -> openSessionScreen());
        notificationsButton.setOnClickListener(v -> openNotificationsScreen());
        refreshSessionButton();

        BottomNavigationView bottomNavigationView = findViewById(R.id.travelshare_bottom_nav);
        List<TravelShareNavItem> navItems = TravelShareBottomNavConfig.buildItems();
        TravelShareBottomNavConfig.inflateMenu(bottomNavigationView, navItems);
        navItemsById.clear();
        for (TravelShareNavItem navItem : navItems) {
            navItemsById.put(navItem.getItemId(), navItem);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switchToTab(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.travelshare_nav_home);
        }
    }

    public void refreshSessionButton() {
        syncTravelShareSessionWithFirebaseAuth();
        if (sessionRepository.isAuthenticated()) {
            sessionButton.setText(R.string.travelshare_auth_profile);
        } else {
            sessionButton.setText(R.string.travelshare_auth_login);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSessionButton();
    }

    public void openPostDetail(String postId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, TravelSharePostDetailFragment.newInstance(postId))
                .addToBackStack("post_detail")
                .commit();
    }

    public void openUserProfile(String displayName) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, TravelShareProfileFragment.newInstance(displayName))
                .addToBackStack("profile")
                .commit();
    }

    private void openSessionScreen() {
        syncTravelShareSessionWithFirebaseAuth();
        Fragment fragment = sessionRepository.isAuthenticated()
                ? new TravelShareProfileFragment()
                : new TravelPathAuthFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, fragment)
                .addToBackStack("session")
                .commit();
    }

    private void openNotificationsScreen() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, new TravelShareNotificationsFragment())
                .addToBackStack("notifications")
                .commit();
    }

    private void syncTravelShareSessionWithFirebaseAuth() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || sessionRepository.isAuthenticated()) {
            return;
        }

        String displayName = firebaseUser.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = firebaseUser.getEmail();
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "Voyageur";
        }

        sessionRepository.login(displayName);
    }

    private void switchToTab(int itemId) {
        if (itemId == R.id.travelshare_nav_itinerary) {
            startActivity(new Intent(this, TravelPathActivity.class));
            finish();
            return;
        }

        TravelShareNavItem navItem = navItemsById.get(itemId);
        if (navItem == null) {
            return;
        }

        Fragment fragment = navItem.createFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, fragment)
                .commit();
    }

    private void logFirebaseProjectInfo() {
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            FirebaseOptions options = app.getOptions();
            Log.i(
                    "TravelShare",
                    "Firebase projectId=" + options.getProjectId()
                            + ", appId=" + options.getApplicationId()
                            + ", gcmSenderId=" + options.getGcmSenderId()
            );
        } catch (IllegalStateException e) {
            Log.w("TravelShare", "FirebaseApp not initialized", e);
        }
    }
}