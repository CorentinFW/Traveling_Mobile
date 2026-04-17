package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelSharePostRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TravelShareAddPostFragment extends Fragment {

    public TravelShareAddPostFragment() {
        super(R.layout.fragment_travelshare_add_post);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TravelSharePostRepository postRepository = TravelShareDataProvider.postRepository();
        TravelShareSessionRepository sessionRepository = TravelShareDataProvider.sessionRepository();

        EditText locationInput = view.findViewById(R.id.travelshare_add_location_input);
        EditText periodInput = view.findViewById(R.id.travelshare_add_period_input);
        EditText descriptionInput = view.findViewById(R.id.travelshare_add_description_input);
        EditText howToInput = view.findViewById(R.id.travelshare_add_how_to_input);
        Button publishButton = view.findViewById(R.id.travelshare_add_publish_button);

        if (!sessionRepository.isAuthenticated()) {
            publishButton.setEnabled(false);
            publishButton.setText(R.string.travelshare_add_need_login_cta);
            Toast.makeText(requireContext(), R.string.travelshare_add_need_login, Toast.LENGTH_SHORT).show();
            return;
        }

        publishButton.setOnClickListener(v -> {
            postRepository.createPost(
                    sessionRepository.getDisplayName(),
                    locationInput.getText().toString(),
                    descriptionInput.getText().toString(),
                    periodInput.getText().toString(),
                    howToInput.getText().toString()
            );

            locationInput.setText("");
            periodInput.setText("");
            descriptionInput.setText("");
            howToInput.setText("");

            Toast.makeText(requireContext(), R.string.travelshare_add_publish_success, Toast.LENGTH_SHORT).show();

            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.travelshare_bottom_nav);
            bottomNavigationView.setSelectedItemId(R.id.travelshare_nav_home);
        });
    }
}
