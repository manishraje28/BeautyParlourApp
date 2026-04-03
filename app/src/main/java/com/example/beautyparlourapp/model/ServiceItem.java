package com.example.beautyparlourapp.model;

import com.google.gson.annotations.SerializedName;

public class ServiceItem {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private int price;

    @SerializedName("duration")
    private String duration;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    public String getId()           { return id; }
    public String getName()         { return name; }
    public int getPrice()           { return price; }
    public String getDuration()     { return duration; }
    public String getDescription()  { return description; }
    public String getCategory()     { return category; }
}
