package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

public class TravelPathWelcomeFragment extends Fragment {

    public TravelPathWelcomeFragment() {
        super(R.layout.fragment_travelpath_welcome);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView introImage = view.findViewById(R.id.travelpath_intro_image);
        loadIntroImage(introImage);

        View startButton = view.findViewById(R.id.travelpath_start_button);
        startButton.setOnClickListener(v -> {
            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showPreferencesScreen();
            }
        });
    }

    private void loadIntroImage(@NonNull ImageView imageView) {
        String storageUrl = "gs://traveling-e01cf.firebasestorage.app/travelpath/Montpellier.jpeg";
        FirebaseStorage.getInstance()
                .getReferenceFromUrl(storageUrl)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    if (isAdded()) {
                        Glide.with(this).load(uri).into(imageView);
                    }
                });
    }
}
