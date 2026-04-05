package com.example.beautyparlourapp.models;

public class Booking {
    private String serviceName;
    private String datetime;
    private String status; // "Confirmed", "Pending", "Completed", "Cancelled"

    public Booking() {
    }

    public Booking(String serviceName, String datetime, String status) {
        this.serviceName = serviceName;
        this.datetime = datetime;
        this.status = status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
