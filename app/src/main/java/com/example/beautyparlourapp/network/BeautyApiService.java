package com.example.beautyparlourapp.network;

import com.example.beautyparlourapp.model.ServiceResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BeautyApiService {

    // GET https://dummyjson.com/products/category/beauty?limit=10
    @GET("products/category/beauty")
    Call<ServiceResponse> getServices(@Query("limit") int limit);
}
