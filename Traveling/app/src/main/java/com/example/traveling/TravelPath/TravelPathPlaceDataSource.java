package com.example.traveling.TravelPath;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface TravelPathPlaceDataSource {

    void loadPlaces(@NonNull TravelPathPlaceRepository.LoadCallback callback);

    void searchPlacesByName(@NonNull String query, @NonNull TravelPathPlaceRepository.LoadCallback callback);

    void loadRandomPlaces(@Nullable String themeFilter, @NonNull TravelPathPlaceRepository.LoadCallback callback);

    void loadAllPlaces(@NonNull TravelPathPlaceRepository.LoadCallback callback);
}
