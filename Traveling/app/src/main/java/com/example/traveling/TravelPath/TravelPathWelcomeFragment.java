package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;

public class TravelPathWelcomeFragment extends Fragment {

    public TravelPathWelcomeFragment() {
        super(R.layout.fragment_travelpath_welcome);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View startButton = view.findViewById(R.id.travelpath_start_button);
        startButton.setOnClickListener(v -> {
            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showPreferencesScreen();
            }
        });
    }
}

