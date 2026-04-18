package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;

public class TravelPathResultsFragment extends Fragment {

    public TravelPathResultsFragment() {
        super(R.layout.fragment_travelpath_results);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View continueButton = view.findViewById(R.id.travelpath_continue_button);
        continueButton.setOnClickListener(v -> {
            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showDurationBudgetScreen();
            }
        });
    }
}

