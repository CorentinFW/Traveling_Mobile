package com.example.traveling.travelshare.domain;

public class TravelSharePost {
    private final String id;
    private final String authorName;
    private final String locationName;
    private final String description;
    private final String period;
    private final String howToGetThere;

    private int likeCount;
    private int commentCount;
    private int reportCount;
    private boolean likedByCurrentUser;

    public TravelSharePost(
            String id,
            String authorName,
            String locationName,
            String description,
            String period,
            String howToGetThere,
            int likeCount,
            int commentCount
    ) {
        this.id = id;
        this.authorName = authorName;
        this.locationName = locationName;
        this.description = description;
        this.period = period;
        this.howToGetThere = howToGetThere;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.reportCount = 0;
        this.likedByCurrentUser = false;
    }

    public String getId() {
        return id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getDescription() {
        return description;
    }

    public String getPeriod() {
        return period;
    }

    public String getHowToGetThere() {
        return howToGetThere;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getReportCount() {
        return reportCount;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void toggleLike() {
        likedByCurrentUser = !likedByCurrentUser;
        likeCount += likedByCurrentUser ? 1 : -1;
        if (likeCount < 0) {
            likeCount = 0;
        }
    }

    public void addComment() {
        commentCount += 1;
    }

    public void report() {
        reportCount += 1;
    }
}
