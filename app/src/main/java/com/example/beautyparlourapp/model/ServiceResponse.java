package com.example.beautyparlourapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServiceResponse {

    @SerializedName("products")
    private List<ServiceItem> products;

    public List<ServiceItem> getProducts() { return products; }
}
