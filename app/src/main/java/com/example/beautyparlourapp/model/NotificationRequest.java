package com.example.beautyparlourapp.model;

public class NotificationRequest {
    private String userId;
    private String title;
    private String body;

    public NotificationRequest(String userId, String title, String body) {
        this.userId = userId;
        this.title = title;
        this.body = body;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}