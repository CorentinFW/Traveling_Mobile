package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class TravelPathResultsFragment extends Fragment {

    private GoogleMap googleMap;

    public TravelPathResultsFragment() {
        super(R.layout.fragment_travelpath_results);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupMap();

        View continueButton = view.findViewById(R.id.travelpath_continue_button);
        continueButton.setOnClickListener(v -> {
            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showDurationBudgetScreen();
            }
        });
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
        Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.travelpath_map_container);
        if (!(mapFragment instanceof SupportMapFragment)) {
            return;
        }

        ((SupportMapFragment) mapFragment).getMapAsync(map -> {
            googleMap = map;
            TravelPathMapLocationHelper.centerMapOnUserOrFallback(this, map);
        });
    }
}

