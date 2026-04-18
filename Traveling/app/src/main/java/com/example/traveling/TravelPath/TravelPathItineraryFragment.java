package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class TravelPathItineraryFragment extends Fragment {

    private GoogleMap googleMap;

    public TravelPathItineraryFragment() {
        super(R.layout.fragment_travelpath_itinerary);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMap();
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
        Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.travelpath_itinerary_map);
        if (!(mapFragment instanceof SupportMapFragment)) {
            return;
        }

        ((SupportMapFragment) mapFragment).getMapAsync(map -> {
            googleMap = map;
            TravelPathMapLocationHelper.centerMapOnUserOrFallback(this, map);
        });
    }
}

