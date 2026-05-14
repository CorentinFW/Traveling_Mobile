package com.example.traveling.TravelPath;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TravelPathRouteRepository {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_ROUTES = "routes";

    private final FirebaseFirestore firestore;

    public interface SaveCallback {
        void onSuccess();

        void onError(@NonNull Exception exception);
    }

    public interface LoadCallback {
        void onSuccess(@NonNull List<TravelPathRoute> routes);

        void onError(@NonNull Exception exception);
    }

    public TravelPathRouteRepository() {
        this(FirebaseFirestore.getInstance());
    }

    TravelPathRouteRepository(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void saveRoute(@NonNull String ownerUid, @NonNull TravelPathRoute route, @NonNull SaveCallback callback) {
        route.setOwnerUid(ownerUid);
        route.setCreatedAt(System.currentTimeMillis());

        firestore.collection(COLLECTION_USERS)
                .document(ownerUid)
                .collection(COLLECTION_ROUTES)
                .add(route.toMap())
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void loadRoutesForUser(@NonNull String ownerUid, @NonNull LoadCallback callback) {
        firestore.collection(COLLECTION_USERS)
                .document(ownerUid)
                .collection(COLLECTION_ROUTES)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<TravelPathRoute> routes = new ArrayList<>();
                    snapshot.getDocuments().forEach(document -> routes.add(TravelPathRoute.fromDocument(document)));
                    Collections.sort(routes, Comparator.comparingLong(TravelPathRoute::getCreatedAt).reversed());
                    callback.onSuccess(routes);
                })
                .addOnFailureListener(callback::onError);
    }
}
