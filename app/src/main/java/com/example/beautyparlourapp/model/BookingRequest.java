package com.example.beautyparlourapp.model;

public class BookingRequest {
    public String userId;
    public String service;
    public String date;
    public String time;

    public BookingRequest(String userId, String service, String date, String time) {
        this.userId = userId;
        this.service = service;
        this.date = date;
        this.time = time;
    }
}
