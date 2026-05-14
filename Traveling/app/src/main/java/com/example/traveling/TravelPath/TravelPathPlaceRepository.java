package com.example.traveling.TravelPath;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldPath;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Locale;

public class TravelPathPlaceRepository implements TravelPathPlaceDataSource {

    private static final String TAG = "TravelPathPlaceRepo";
    private static final String COLLECTION_PLACES = "travelpath_places_catalog";
    private static final String FALLBACK_COLLECTION_PLACES = "travelpath_places";
    private static final String LEGACY_COLLECTION_PLACES = "places";
    private static final int RESULTS_LIMIT = 10;
    private static final int WHERE_IN_LIMIT = 10;

    private final FirebaseFirestore firestore;

    public interface LoadCallback {
        void onSuccess(@NonNull List<TravelPathPlace> places);

        void onError(@NonNull Exception exception);
    }

    public TravelPathPlaceRepository() {
        this(FirebaseFirestore.getInstance());
    }

    TravelPathPlaceRepository(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void loadPlaces(@NonNull LoadCallback callback) {
        loadAllPlaces(new LoadCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> places) {
                callback.onSuccess(trimToLimit(places));
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void searchPlacesByName(@NonNull String query, @NonNull LoadCallback callback) {
        String normalizedQuery = normalizeText(query);
        if (normalizedQuery.isEmpty()) {
            loadPlaces(callback);
            return;
        }

        loadAllPlaces(new LoadCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> allPlaces) {
                List<TravelPathPlace> places = new ArrayList<>();
                for (TravelPathPlace place : allPlaces) {
                    if (places.size() >= RESULTS_LIMIT) {
                        break;
                    }
                    String normalizedName = normalizeText(place.getName());
                    if (normalizedName.contains(normalizedQuery)) {
                        places.add(place);
                    }
                }
                callback.onSuccess(places);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void loadRandomPlaces(@Nullable String themeFilter, @NonNull LoadCallback callback) {
        loadAllPlaces(new LoadCallback() {
            @Override
            public void onSuccess(@NonNull List<TravelPathPlace> allPlaces) {
                List<TravelPathPlace> filtered = new ArrayList<>();
                String normalizedTheme = normalizeText(themeFilter == null ? "" : themeFilter);

                if (normalizedTheme.isEmpty()) {
                    filtered.addAll(allPlaces);
                } else {
                    for (TravelPathPlace place : allPlaces) {
                        String placeTheme = normalizeText(place.getTheme());
                        if (placeTheme.equals(normalizedTheme)) {
                            filtered.add(place);
                        }
                    }
                }

                callback.onSuccess(pickRandom(filtered));
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void loadAllPlaces(@NonNull LoadCallback callback) {
        loadAllPlacesInternal(callback);
    }

    private void loadAllPlacesInternal(@NonNull LoadCallback callback) {
        List<String> collections = Arrays.asList(
                COLLECTION_PLACES,
                FALLBACK_COLLECTION_PLACES,
                LEGACY_COLLECTION_PLACES
        );
        loadFromCollectionsSequentially(collections, 0, null, callback);
    }

    private void loadFromCollectionsSequentially(
            @NonNull List<String> collections,
            int index,
            @Nullable Exception lastError,
            @NonNull LoadCallback callback
    ) {
        if (index >= collections.size()) {
            Log.w(TAG, "Aucun lieu trouve dans Firestore; fallback active. Derniere erreur: "
                    + (lastError == null ? "<aucune>" : lastError.getMessage()));
            // Aucun lieu trouve dans Firestore (ou erreur) : bascule sur les donnees de secours.
            callback.onSuccess(buildFallbackPlaces());
            return;
        }

        String collectionName = collections.get(index);
        Log.d(TAG, "Chargement de la collection: " + collectionName);
        firestore.collection(collectionName)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<TravelPathPlace> places = new ArrayList<>();
                    snapshot.getDocuments().forEach(document -> {
                        TravelPathPlace place = TravelPathPlace.fromDocument(document);
                        place.setId(document.getId());
                        place.setSourceCollection(collectionName);
                        places.add(place);
                    });

                    if (!places.isEmpty()) {
                        Log.d(TAG, "Collection " + collectionName + " -> " + places.size() + " lieux charges.");
                        callback.onSuccess(places);
                        return;
                    }

                    Log.w(TAG, "Collection " + collectionName + " vide.");
                    loadFromCollectionsSequentially(collections, index + 1, lastError, callback);
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "Echec chargement collection " + collectionName + ": " + error.getMessage(), error);
                    loadFromCollectionsSequentially(collections, index + 1, error, callback);
                });
    }

    @NonNull
    private List<TravelPathPlace> buildFallbackPlaces() {
        List<TravelPathPlace> fallback = new ArrayList<>();
        fallback.add(new TravelPathPlace("Place de la Comedie", "Monument", null, 43.6086, 3.8793, 0.0, 4.6));
        fallback.add(new TravelPathPlace("Promenade du Peyrou", "Loisir", null, 43.6119, 3.8684, 5.0, 4.4));
        fallback.add(new TravelPathPlace("Musee Fabre", "Culture", null, 43.6116, 3.8814, 12.0, 4.5));
        fallback.add(new TravelPathPlace("Jardin des Plantes", "Loisir", null, 43.6160, 3.8702, 0.0, 4.3));
        fallback.add(new TravelPathPlace("Lez Market", "Shopping", null, 43.6035, 3.8981, 15.0, 4.2));
        fallback.add(new TravelPathPlace("Halles Castellane", "Restaurant", null, 43.6112, 3.8777, 22.0, 4.4));
        fallback.add(new TravelPathPlace("Opera Comedie", "Evenements", null, 43.6088, 3.8796, 18.0, 4.1));
        fallback.add(new TravelPathPlace("Arc de Triomphe", "Monument", null, 43.6113, 3.8700, 0.0, 4.2));
        fallback.add(new TravelPathPlace("MO.CO.", "Culture", null, 43.6068, 3.8749, 10.0, 4.0));
        fallback.add(new TravelPathPlace("Antigone", "Shopping", null, 43.6071, 3.8903, 25.0, 4.3));
        for (TravelPathPlace place : fallback) {
            place.setSourceCollection("fallback");
        }
        return fallback;
    }

    @NonNull
    private List<TravelPathPlace> pickRandom(@NonNull List<TravelPathPlace> source) {
        if (source.isEmpty()) {
            return source;
        }

        List<TravelPathPlace> shuffled = new ArrayList<>(source);
        Collections.shuffle(shuffled);
        int count = Math.min(RESULTS_LIMIT, shuffled.size());
        return new ArrayList<>(shuffled.subList(0, count));
    }

    @NonNull
    private List<TravelPathPlace> trimToLimit(@NonNull List<TravelPathPlace> source) {
        int count = Math.min(RESULTS_LIMIT, source.size());
        return new ArrayList<>(source.subList(0, count));
    }

    @NonNull
    private String normalizeText(@NonNull String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{M}+", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }

    public void loadPlacesByReferences(@NonNull List<String> references, @NonNull LoadCallback callback) {
        if (references.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        Map<String, List<String>> byCollection = new HashMap<>();
        for (String ref : references) {
            String[] parts = ref.split("/", 2);
            if (parts.length != 2) {
                continue;
            }
            byCollection.computeIfAbsent(parts[0], key -> new ArrayList<>()).add(parts[1]);
        }

        if (byCollection.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        Map<String, TravelPathPlace> byReference = new HashMap<>();
        AtomicInteger pending = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        for (Map.Entry<String, List<String>> entry : byCollection.entrySet()) {
            String collection = entry.getKey();
            List<String> ids = entry.getValue();
            for (int start = 0; start < ids.size(); start += WHERE_IN_LIMIT) {
                List<String> batch = ids.subList(start, Math.min(start + WHERE_IN_LIMIT, ids.size()));
                pending.incrementAndGet();
                firestore.collection(collection)
                        .whereIn(FieldPath.documentId(), batch)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            snapshot.getDocuments().forEach(document -> {
                                TravelPathPlace place = TravelPathPlace.fromDocument(document);
                                place.setId(document.getId());
                                place.setSourceCollection(collection);
                                String key = collection + "/" + document.getId();
                                byReference.put(key, place);
                            });
                            if (pending.decrementAndGet() == 0) {
                                callback.onSuccess(orderPlacesByReferences(references, byReference));
                            }
                        })
                        .addOnFailureListener(error -> {
                            failures.incrementAndGet();
                            if (pending.decrementAndGet() == 0) {
                                if (byReference.isEmpty()) {
                                    callback.onError(error);
                                } else {
                                    callback.onSuccess(orderPlacesByReferences(references, byReference));
                                }
                            }
                        });
            }
        }
    }

    @NonNull
    private List<TravelPathPlace> orderPlacesByReferences(
            @NonNull List<String> references,
            @NonNull Map<String, TravelPathPlace> byReference
    ) {
        List<TravelPathPlace> ordered = new ArrayList<>();
        for (String ref : references) {
            TravelPathPlace place = byReference.get(ref);
            if (place != null) {
                ordered.add(place);
            }
        }
        return ordered;
    }
}
