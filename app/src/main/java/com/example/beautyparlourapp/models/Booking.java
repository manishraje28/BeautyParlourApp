package com.example.beautyparlourapp.models;

public class Booking {
    private String id;
    private String serviceName;
    private String datetime;
    private String status; // "Confirmed", "Pending", "Completed", "Cancelled"
    private String userId; // To know who booked it
    private String userName; // To display on admin side
    private double price; // To display on admin side

    public Booking() {
    }

    public Booking(String id, String serviceName, String datetime, String status) {
        this.id = id;
        this.serviceName = serviceName;
        this.datetime = datetime;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
