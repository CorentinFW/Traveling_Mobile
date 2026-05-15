package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.traveling.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

public class TravelPathMyRoutesFragment extends Fragment {

    private final TravelPathRouteRepository routeRepository = new TravelPathRouteRepository();
    private final TravelPathPlaceRepository placeRepository = new TravelPathPlaceRepository();
    private LinearLayout routesContainer;

    public TravelPathMyRoutesFragment() {
        super(R.layout.fragment_travelpath_my_routes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        routesContainer = view.findViewById(R.id.travelpath_my_routes_injection_container);

        Button newRouteButton = view.findViewById(R.id.travelpath_new_route_button);
        newRouteButton.setOnClickListener(v -> {
            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showPreferencesScreen();
            }
        });

        loadRoutes();
    }

    private void loadRoutes() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showSingleMessage(R.string.travelpath_db_auth_required);
            return;
        }

        showSingleMessage(R.string.travelpath_db_loading_routes);

        routeRepository.loadRoutesForUser(currentUser.getUid(), new TravelPathRouteRepository.LoadCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathRoute> routes) {
                if (!isAdded()) {
                    return;
                }
                renderRoutes(routes);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                if (!isAdded()) {
                    return;
                }
                showSingleMessage(R.string.travelpath_db_load_error);
            }
        });
    }

    private void renderRoutes(@NonNull List<TravelPathRoute> routes) {
        routesContainer.removeAllViews();

        if (routes.isEmpty()) {
            showSingleMessage(R.string.travelpath_db_no_routes);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (TravelPathRoute route : routes) {
            View itemView = inflater.inflate(R.layout.item_travelpath_my_route, routesContainer, false);
            bindRouteItem(itemView, route);
            routesContainer.addView(itemView);
        }
    }

    private void bindRouteItem(@NonNull View itemView, @NonNull TravelPathRoute route) {
        ImageView routeImage = itemView.findViewById(R.id.travelpath_my_route_image);
        TextView routeName = itemView.findViewById(R.id.travelpath_my_route_name);
        TextView routeBudget = itemView.findViewById(R.id.travelpath_my_route_budget);
        TextView routeDays = itemView.findViewById(R.id.travelpath_my_route_days);
        TextView routeEffort = itemView.findViewById(R.id.travelpath_my_route_effort);
        TextView routePlaces = itemView.findViewById(R.id.travelpath_my_route_places);

        routeName.setText(nonEmptyOrDefault(route.getRouteName(), R.string.travelpath_my_route_name_placeholder));

        String budgetLabel = String.format(
                Locale.getDefault(),
                "%d-%d%s",
                route.getBudgetMin(),
                Math.max(route.getBudgetMin(), route.getBudgetMax()),
                getString(R.string.travelpath_budget_currency)
        );
        routeBudget.setText(budgetLabel);

        routeDays.setText(nonEmptyOrDefault(route.getVisitSummary(), R.string.travelpath_my_route_days_placeholder));
        routeEffort.setText(nonEmptyOrDefault(route.getEffort(), R.string.travelpath_my_route_effort_placeholder));
        routePlaces.setText(nonEmptyOrDefault(route.getPlacesSummary(), R.string.travelpath_my_route_places_placeholder));

        bindRouteImage(routeImage, route);

        itemView.setOnClickListener(v -> {
            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showItineraryShareScreen(route);
            }
        });
    }

    private void bindRouteImage(@NonNull ImageView imageView, @NonNull TravelPathRoute route) {
        List<String> references = route.getPlaceReferences();
        if (references == null || references.isEmpty()) {
            return;
        }
        String firstRef = references.get(0);
        if (TextUtils.isEmpty(firstRef)) {
            return;
        }
        imageView.setTag(firstRef);
        placeRepository.loadPlacesByReferences(Collections.singletonList(firstRef), new TravelPathPlaceRepository.LoadCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> places) {
                if (!isAdded()) {
                    return;
                }
                Object tag = imageView.getTag();
                if (!(tag instanceof String) || !firstRef.equals(tag)) {
                    return;
                }
                if (places.isEmpty()) {
                    return;
                }
                loadPlaceImage(imageView, places.get(0).getImageUrl());
            }

            @Override
            public void onError(@NonNull Exception exception) {
                // Pas d'image si la lecture echoue.
            }
        });
    }

    private void loadPlaceImage(@NonNull ImageView imageView, @Nullable String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }
        if (imageUrl.startsWith("gs://")) {
            FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl)
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        if (isAdded()) {
                            Glide.with(this).load(uri).into(imageView);
                        }
                    });
            return;
        }
        Glide.with(this).load(imageUrl).into(imageView);
    }

    @NonNull
    private String nonEmptyOrDefault(@Nullable String value, int defaultResId) {
        if (value == null || value.trim().isEmpty()) {
            return getString(defaultResId);
        }
        return value.trim();
    }

    private void showSingleMessage(int messageResId) {
        routesContainer.removeAllViews();

        TextView message = new TextView(requireContext());
        message.setText(messageResId);
        message.setTextColor(0xFF4B5563);
        message.setTextSize(15f);
        message.setPadding(8, 20, 8, 20);
        routesContainer.addView(message);
    }
}
