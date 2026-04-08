package com.example.beautyparlourapp.network;

import com.example.beautyparlourapp.model.ServiceResponse;
import com.example.beautyparlourapp.model.OfferResponse;
import com.example.beautyparlourapp.model.BookingRequest;
import com.example.beautyparlourapp.model.BookingResponse;

import com.example.beautyparlourapp.model.NotificationRequest;
import com.example.beautyparlourapp.model.NotificationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface BeautyApiService {

    // Services endpoints
    @GET("api/services")
    Call<ServiceResponse> getAllServices();

    @GET("api/services/{id}")
    Call<ServiceResponse> getServiceById(@Path("id") String id);

    // Offers endpoints
    @GET("api/offers")
    Call<OfferResponse> getOffers();

    // Bookings endpoints
    @POST("api/bookings")
    Call<BookingResponse> createBooking(@Body BookingRequest request);

    @GET("api/bookings/{userId}")
    Call<BookingResponse> getUserBookings(@Path("userId") String userId);

    // Health check
    @GET("api/health")
    Call<ServiceResponse> healthCheck();

    // Push Notifications
    @POST("api/notifications/send")
    Call<NotificationResponse> sendNotification(@Body NotificationRequest request);
}
