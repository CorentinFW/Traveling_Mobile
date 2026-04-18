package com.example.traveling.TravelPath;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public final class TravelPathMapLocationHelper {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1101;
    private static final float USER_AREA_ZOOM = 12f;
    private static final LatLng DEFAULT_FALLBACK_LOCATION = new LatLng(48.8566, 2.3522);

    private TravelPathMapLocationHelper() {
    }

    @SuppressLint("MissingPermission")
    public static void centerMapOnUserOrFallback(@NonNull Fragment fragment, @NonNull GoogleMap map) {
        Context context = fragment.getContext();
        if (context == null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_FALLBACK_LOCATION, USER_AREA_ZOOM));
            return;
        }

        if (!hasLocationPermission(context)) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_FALLBACK_LOCATION, USER_AREA_ZOOM));
            fragment.requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException ignored) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_FALLBACK_LOCATION, USER_AREA_ZOOM));
            return;
        }

        Location userLocation = getBestLastKnownLocation(context);
        if (userLocation == null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_FALLBACK_LOCATION, USER_AREA_ZOOM));
            return;
        }

        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, USER_AREA_ZOOM));
    }

    public static boolean isLocationPermissionGranted(@NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasLocationPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private static Location getBestLastKnownLocation(@NonNull Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return null;
        }

        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location == null) {
                    continue;
                }
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                }
            } catch (SecurityException ignored) {
                return null;
            }
        }
        return bestLocation;
    }
}

