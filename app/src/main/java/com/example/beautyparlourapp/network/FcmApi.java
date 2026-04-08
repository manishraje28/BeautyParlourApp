package com.example.beautyparlourapp.network;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FcmApi {
    
    // Using Firebase Cloud Messaging API (V1)
    // Replace "beautyparlourapp-cdd35" with your actual Firebase Project ID if different
    @Headers({
            "Content-Type: application/json"
    })
    @POST("v1/projects/beautyparlourapp-cdd35/messages:send")
    Call<JsonObject> sendNotification(
            @Header("Authorization") String bearerToken,
            @Body JsonObject payload
    );
}