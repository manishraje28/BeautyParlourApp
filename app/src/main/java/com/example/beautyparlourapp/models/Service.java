package com.example.beautyparlourapp.models;

public class Service {
    private String id;
    private String title;
    private String category;
    private String price;
    private String duration;
    private String description;
    private String imageUrl;

    public Service() {}

    public Service(String id, String title, String category, String price, String duration, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.price = price;
        this.duration = duration;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
