package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.MainActivity;
import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;

public class TravelShareAuthFragment extends Fragment {

    public TravelShareAuthFragment() {
        super(R.layout.fragment_travelshare_auth);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TravelShareSessionRepository sessionRepository = TravelShareDataProvider.sessionRepository();

        EditText usernameInput = view.findViewById(R.id.travelshare_auth_username_input);
        Button loginButton = view.findViewById(R.id.travelshare_auth_login_button);

        loginButton.setOnClickListener(v -> {
            String name = usernameInput.getText().toString().trim();
            if (name.isEmpty()) {
                name = "Voyageur";
            }

            sessionRepository.login(name);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).refreshSessionButton();
            }

            String message = getString(R.string.travelshare_auth_login_success, sessionRepository.getDisplayName());
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}

