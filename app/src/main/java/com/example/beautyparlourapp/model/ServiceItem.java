package com.example.beautyparlourapp.model;

import com.google.gson.annotations.SerializedName;

public class ServiceItem {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("category")
    private String category;

    public int getId()          { return id; }
    public String getTitle()    { return title; }
    public String getDescription() { return description; }
    public double getPrice()    { return price; }
    public String getCategory() { return category; }
}
