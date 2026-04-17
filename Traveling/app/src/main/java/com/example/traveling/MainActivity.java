package com.example.traveling;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;
import com.example.traveling.travelshare.ui.TravelShareAddPostFragment;
import com.example.traveling.travelshare.ui.TravelShareAuthFragment;
import com.example.traveling.travelshare.ui.TravelShareHomeFragment;
import com.example.traveling.travelshare.ui.TravelShareItineraryFragment;
import com.example.traveling.travelshare.ui.TravelShareMessagesFragment;
import com.example.traveling.travelshare.ui.TravelSharePostDetailFragment;
import com.example.traveling.travelshare.ui.TravelShareProfileFragment;
import com.example.traveling.travelshare.ui.TravelShareSearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TravelShareSessionRepository sessionRepository;
    private Button sessionButton;

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
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switchToTab(item.getItemId());
            return true;
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

    private void switchToTab(int itemId) {
        Fragment fragment;

        if (itemId == R.id.travelshare_nav_home) {
            fragment = new TravelShareHomeFragment();
        } else if (itemId == R.id.travelshare_nav_search) {
            fragment = new TravelShareSearchFragment();
        } else if (itemId == R.id.travelshare_nav_messages) {
            fragment = new TravelShareMessagesFragment();
        } else if (itemId == R.id.travelshare_nav_add) {
            fragment = new TravelShareAddPostFragment();
        } else {
            fragment = new TravelShareItineraryFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, fragment)
                .commit();
    }
}