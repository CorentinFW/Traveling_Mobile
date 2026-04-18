package com.example.traveling.TravelPath;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class TravelPathRoute {

    private String id;
    private String ownerUid;
    private String routeName;
    private String activities;
    private int budgetMin;
    private int budgetMax;
    private String visitSummary;
    private String effort;
    private String routeType;
    private String placesSummary;
    private long createdAt;

    public TravelPathRoute() {
        // Required for Firebase/serialization.
    }

    @NonNull
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("ownerUid", ownerUid);
        data.put("routeName", routeName);
        data.put("activities", activities);
        data.put("budgetMin", budgetMin);
        data.put("budgetMax", budgetMax);
        data.put("visitSummary", visitSummary);
        data.put("effort", effort);
        data.put("routeType", routeType);
        data.put("placesSummary", placesSummary);
        data.put("createdAt", createdAt);
        return data;
    }

    @NonNull
    public static TravelPathRoute fromDocument(@NonNull DocumentSnapshot document) {
        TravelPathRoute route = new TravelPathRoute();
        route.setId(document.getId());
        route.setOwnerUid(document.getString("ownerUid"));
        route.setRouteName(document.getString("routeName"));
        route.setActivities(document.getString("activities"));
        route.setBudgetMin(readInt(document, "budgetMin"));
        route.setBudgetMax(readInt(document, "budgetMax"));
        route.setVisitSummary(document.getString("visitSummary"));
        route.setEffort(document.getString("effort"));
        route.setRouteType(document.getString("routeType"));
        route.setPlacesSummary(document.getString("placesSummary"));
        route.setCreatedAt(readLong(document, "createdAt"));
        return route;
    }

    private static int readInt(@NonNull DocumentSnapshot document, @NonNull String key) {
        Number value = document.getDouble(key);
        if (value == null) {
            value = document.getLong(key);
        }
        return value == null ? 0 : value.intValue();
    }

    private static long readLong(@NonNull DocumentSnapshot document, @NonNull String key) {
        Long value = document.getLong(key);
        return value != null ? value : 0L;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(@Nullable String ownerUid) {
        this.ownerUid = ownerUid;
    }

    @Nullable
    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(@Nullable String routeName) {
        this.routeName = routeName;
    }

    @Nullable
    public String getActivities() {
        return activities;
    }

    public void setActivities(@Nullable String activities) {
        this.activities = activities;
    }

    public int getBudgetMin() {
        return budgetMin;
    }

    public void setBudgetMin(int budgetMin) {
        this.budgetMin = Math.max(0, budgetMin);
    }

    public int getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(int budgetMax) {
        this.budgetMax = Math.max(0, budgetMax);
    }

    @Nullable
    public String getVisitSummary() {
        return visitSummary;
    }

    public void setVisitSummary(@Nullable String visitSummary) {
        this.visitSummary = visitSummary;
    }

    @Nullable
    public String getEffort() {
        return effort;
    }

    public void setEffort(@Nullable String effort) {
        this.effort = effort;
    }

    @Nullable
    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(@Nullable String routeType) {
        this.routeType = routeType;
    }

    @Nullable
    public String getPlacesSummary() {
        return placesSummary;
    }

    public void setPlacesSummary(@Nullable String placesSummary) {
        this.placesSummary = placesSummary;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = Math.max(0L, createdAt);
    }
}

