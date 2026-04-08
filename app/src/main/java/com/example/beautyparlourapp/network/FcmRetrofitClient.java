package com.example.beautyparlourapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FcmRetrofitClient {
    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static FcmRetrofitClient instance;
    private final FcmApi fcmApi;

    private FcmRetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        fcmApi = retrofit.create(FcmApi.class);
    }

    public static synchronized FcmRetrofitClient getInstance() {
        if (instance == null) {
            instance = new FcmRetrofitClient();
        }
        return instance;
    }

    public FcmApi getApi() {
        return fcmApi;
    }
}