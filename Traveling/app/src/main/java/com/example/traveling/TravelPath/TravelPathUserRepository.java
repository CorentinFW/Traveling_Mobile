package com.example.traveling.TravelPath;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class TravelPathUserRepository {

    private static final String COLLECTION_USERS = "users";

    @Nullable
    private final FirebaseFirestore firestore;

    public interface SaveCallback {
        void onSuccess();

        void onError(@NonNull Exception exception);
    }

    public TravelPathUserRepository() {
        this(FirebaseFirestore.getInstance());
    }

    // Test-only constructor to validate path/payload helpers without Firebase runtime.
    TravelPathUserRepository(boolean testMode) {
        this.firestore = null;
    }

    TravelPathUserRepository(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void saveUserProfile(
            @NonNull String uid,
            @Nullable String email,
            @NonNull String provider,
            @NonNull SaveCallback callback
    ) {
        if (firestore == null) {
            callback.onError(new IllegalStateException("Firestore n'est pas initialise"));
            return;
        }

        Map<String, Object> profile = createProfilePayload(uid, email, provider);

        firestore.collection(COLLECTION_USERS)
                .document(uid)
                .set(profile, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    @NonNull
    Map<String, Object> createProfilePayload(
            @NonNull String uid,
            @Nullable String email,
            @NonNull String provider
    ) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("uid", uid);
        profile.put("email", email != null ? email : "");
        profile.put("provider", provider);
        profile.put("updatedAt", System.currentTimeMillis());
        profile.put("createdAt", System.currentTimeMillis());
        return profile;
    }

    @NonNull
    String buildUserDocumentPath(@NonNull String uid) {
        return COLLECTION_USERS + "/" + uid;
    }

    @NonNull
    String buildUserSubCollectionPath(@NonNull String uid, @NonNull String subCollection) {
        return buildUserDocumentPath(uid) + "/" + subCollection;
    }
}



