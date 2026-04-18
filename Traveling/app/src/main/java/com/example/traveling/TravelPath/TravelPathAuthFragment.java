package com.example.traveling.TravelPath;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.traveling.MainActivity;
import com.example.traveling.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;

public class TravelPathAuthFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    private Button anonymousButton;
    private Button signInButton;
    private Button createAccountButton;
    private Button googleButton;
    private EditText emailInput;
    private EditText passwordInput;
    private ProgressBar progressBar;
    private TextView feedbackText;

    private final ActivityResultLauncher<android.content.Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != android.app.Activity.RESULT_OK) {
                    setLoading(false);
                    showError(getString(R.string.travelpath_auth_error_google_cancelled));
                    return;
                }
                handleGoogleSignInResult(result.getData());
            });

    public TravelPathAuthFragment() {
        super(R.layout.fragment_travelpath_auth);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        bindViews(view);
        setupActions();
    }

    private void bindViews(@NonNull View rootView) {
        anonymousButton = rootView.findViewById(R.id.travelpath_auth_anonymous_button);
        signInButton = rootView.findViewById(R.id.travelpath_auth_sign_in_button);
        createAccountButton = rootView.findViewById(R.id.travelpath_auth_create_account_button);
        googleButton = rootView.findViewById(R.id.travelpath_auth_google_button);
        emailInput = rootView.findViewById(R.id.travelpath_auth_email_input);
        passwordInput = rootView.findViewById(R.id.travelpath_auth_password_input);
        progressBar = rootView.findViewById(R.id.travelpath_auth_progress);
        feedbackText = rootView.findViewById(R.id.travelpath_auth_feedback);
    }

    private void setupActions() {
        anonymousButton.setOnClickListener(v -> signInAnonymously());
        signInButton.setOnClickListener(v -> signInWithEmail());
        createAccountButton.setOnClickListener(v -> createAccountWithEmail());
        googleButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInAnonymously() {
        setLoading(true);
        firebaseAuth.signInAnonymously()
                .addOnSuccessListener(result -> {
                    setLoading(false);
                    notifyAuthSuccess();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(resolveAuthError(e));
                });
    }

    private void signInWithEmail() {
        String email = getInputValue(emailInput);
        String password = getInputValue(passwordInput);

        if (!isCredentialsValid(email, password)) {
            return;
        }

        setLoading(true);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    setLoading(false);
                    notifyAuthSuccess();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(resolveAuthError(e));
                });
    }

    private void createAccountWithEmail() {
        String email = getInputValue(emailInput);
        String password = getInputValue(passwordInput);

        if (!isCredentialsValid(email, password)) {
            return;
        }

        setLoading(true);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    setLoading(false);
                    notifyAuthSuccess();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(resolveAuthError(e));
                });
    }

    private void signInWithGoogle() {
        setLoading(true);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            if (!isAdded()) {
                return;
            }
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        });
    }

    private void handleGoogleSignInResult(@Nullable android.content.Intent data) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException.class);

            if (account == null || TextUtils.isEmpty(account.getIdToken())) {
                setLoading(false);
                showError(getString(R.string.travelpath_auth_error_missing_google_token));
                return;
            }

            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener(result -> {
                        setLoading(false);
                        notifyAuthSuccess();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        showError(resolveAuthError(e));
                    });
        } catch (ApiException exception) {
            setLoading(false);
            showError(getString(R.string.travelpath_auth_error_google_failed));
        }
    }

    private boolean isCredentialsValid(@NonNull String email, @NonNull String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.travelpath_auth_error_missing_credentials));
            return false;
        }

        if (password.length() < 6) {
            showError(getString(R.string.travelpath_auth_error_password_short));
            return false;
        }

        return true;
    }

    @NonNull
    private String getInputValue(@NonNull EditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private void setLoading(boolean isLoading) {
        if (!isAdded() || progressBar == null) {
            return;
        }

        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        anonymousButton.setEnabled(!isLoading);
        signInButton.setEnabled(!isLoading);
        createAccountButton.setEnabled(!isLoading);
        googleButton.setEnabled(!isLoading);
        emailInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
    }

    private void notifyAuthSuccess() {
        showInfo(getString(R.string.travelpath_auth_success));

        Fragment parent = getParentFragment();
        if (parent instanceof TravelPathMainFragment) {
            ((TravelPathMainFragment) parent).onAuthenticationSucceeded();
            return;
        }

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).refreshSessionButton();
        }

        FragmentManager fragmentManager = getParentFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
    }

    private void showError(@NonNull String message) {
        if (!isAdded() || feedbackText == null) {
            return;
        }
        feedbackText.setTextColor(0xFFB00020);
        feedbackText.setText(message);
    }

    private void showInfo(@NonNull String message) {
        if (!isAdded() || feedbackText == null) {
            return;
        }
        feedbackText.setTextColor(0xFF1B5E20);
        feedbackText.setText(message);
    }

    @NonNull
    private String resolveAuthError(@NonNull Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            return getString(R.string.travelpath_auth_error_email_already_used);
        }
        if (exception instanceof FirebaseAuthInvalidUserException) {
            return getString(R.string.travelpath_auth_error_user_not_found);
        }
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return getString(R.string.travelpath_auth_error_invalid_credentials);
        }
        if (exception instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) exception).getErrorCode();
            if ("ERROR_WEAK_PASSWORD".equals(code)) {
                return getString(R.string.travelpath_auth_error_password_short);
            }
            if ("ERROR_NETWORK_REQUEST_FAILED".equals(code)) {
                return getString(R.string.travelpath_auth_error_network);
            }
        }
        return getString(R.string.travelpath_auth_error_generic);
    }
}

