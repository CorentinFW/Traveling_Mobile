package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class TravelPathPlaceDetailFragment extends Fragment {

    private static final String ARG_PLACE_NAME = "arg_place_name";
    private static final String ARG_PLACE_LATITUDE = "arg_place_latitude";
    private static final String ARG_PLACE_LONGITUDE = "arg_place_longitude";

    private static final LatLng DEFAULT_MONTPELLIER = new LatLng(43.6110, 3.8767);
    private static final float PLACE_ZOOM = 15f;

    public TravelPathPlaceDetailFragment() {
        super(R.layout.fragment_travelpath_place_detail);
    }

    @NonNull
    public static TravelPathPlaceDetailFragment newInstance(@NonNull TravelPathPlace place) {
        TravelPathPlaceDetailFragment fragment = new TravelPathPlaceDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLACE_NAME, place.getName());
        if (place.getLatitude() != null) {
            args.putDouble(ARG_PLACE_LATITUDE, place.getLatitude());
        }
        if (place.getLongitude() != null) {
            args.putDouble(ARG_PLACE_LONGITUDE, place.getLongitude());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView title = view.findViewById(R.id.travelpath_place_detail_title);
        title.setText(readPlaceName());

        androidx.fragment.app.Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.travelpath_place_map_container);
        if (!(mapFragment instanceof SupportMapFragment)) {
            return;
        }

        ((SupportMapFragment) mapFragment).getMapAsync(map -> {
            LatLng target = readPlaceLatLng();
            map.clear();
            map.addMarker(new MarkerOptions().position(target).title(readPlaceName()));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, PLACE_ZOOM));
        });
    }

    @NonNull
    private String readPlaceName() {
        Bundle args = getArguments();
        if (args == null) {
            return getString(R.string.travelpath_place_detail_title);
        }
        String name = args.getString(ARG_PLACE_NAME, "").trim();
        return name.isEmpty() ? getString(R.string.travelpath_place_detail_title) : name;
    }

    @NonNull
    private LatLng readPlaceLatLng() {
        Bundle args = getArguments();
        if (args == null || !args.containsKey(ARG_PLACE_LATITUDE) || !args.containsKey(ARG_PLACE_LONGITUDE)) {
            return DEFAULT_MONTPELLIER;
        }
        return new LatLng(args.getDouble(ARG_PLACE_LATITUDE), args.getDouble(ARG_PLACE_LONGITUDE));
    }
}