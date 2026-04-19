package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.MainActivity;
import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;
import com.google.firebase.auth.FirebaseAuth;

public class TravelShareProfileFragment extends Fragment {

    public TravelShareProfileFragment() {
        super(R.layout.fragment_travelshare_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TravelShareSessionRepository sessionRepository = TravelShareDataProvider.sessionRepository();

        TextView nameView = view.findViewById(R.id.travelshare_profile_name_value);
        Button logoutButton = view.findViewById(R.id.travelshare_profile_logout_button);

        nameView.setText(sessionRepository.getDisplayName());

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            sessionRepository.logout();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).refreshSessionButton();
            }

            Toast.makeText(requireContext(), R.string.travelshare_auth_logout_success, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}

