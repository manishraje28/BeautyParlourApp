package com.example.beautyparlourapp.model;

public class BookingResponse {
    public boolean success;
    public BookingData data;
    public String message;
    public String error;

    public static class BookingData {
        public String id;
        public String userId;
        public String service;
        public String date;
        public String time;
        public String status;
        public String createdAt;

        public String getId() { return id; }
        public String getService() { return service; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getStatus() { return status; }
    }
}
