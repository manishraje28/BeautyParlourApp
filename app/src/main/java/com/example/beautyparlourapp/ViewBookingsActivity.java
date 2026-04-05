package com.example.beautyparlourapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewBookingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvUpcoming, tvPast;
    private RecyclerView rvBookings;
    private com.example.beautyparlourapp.adapters.BookingsAdapter adapter;
    private List<com.example.beautyparlourapp.models.Booking> upcomingBookings = new ArrayList<>();
    private List<com.example.beautyparlourapp.models.Booking> pastBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bookings);

        btnBack = findViewById(R.id.btn_back);
        tvUpcoming = findViewById(R.id.tab_upcoming);
        tvPast = findViewById(R.id.tab_past);
        rvBookings = findViewById(R.id.rv_bookings);

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new com.example.beautyparlourapp.adapters.BookingsAdapter(this, new ArrayList<>());
        rvBookings.setAdapter(adapter);

        // Click listeners
        btnBack.setOnClickListener(v -> finish());

        tvUpcoming.setOnClickListener(v -> {
            tvUpcoming.setTextColor(getResources().getColor(android.R.color.white));
            tvPast.setTextColor(getResources().getColor(android.R.color.darker_gray));
            // Load upcoming
            loadBookings(true);
        });

        tvPast.setOnClickListener(v -> {
            tvPast.setTextColor(getResources().getColor(android.R.color.white));
            tvUpcoming.setTextColor(getResources().getColor(android.R.color.darker_gray));
            // Load past
            loadBookings(false);
        });

        // Initialize bottom navigation
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.footer_container, new FooterFragment())
                    .commit();
        }

        // Initially fetch and load upcoming
        fetchBookings();
    }

    private void fetchBookings() {
        Toast.makeText(this, "Loading bookings...", Toast.LENGTH_SHORT).show();
        FirebaseManager.getInstance().fetchUserBookings(new FirebaseManager.FetchBookingsCallback() {
            @Override
            public void onSuccess(List<com.example.beautyparlourapp.models.Booking> bookings) {
                upcomingBookings.clear();
                pastBookings.clear();

                for (com.example.beautyparlourapp.models.Booking b : bookings) {
                    // Force all to Confirmed for now as requested
                    b.setStatus("Confirmed");
                    // We can do simple logic: if it has 'Pending'/'Confirmed' it's usually Upcoming
                    // Let's just put all in upcoming for now, or split if it's 'Completed'
                    if ("Completed".equalsIgnoreCase(b.getStatus()) || "Cancelled".equalsIgnoreCase(b.getStatus())) {
                        pastBookings.add(b);
                    } else {
                        upcomingBookings.add(b);
                    }
                }
                loadBookings(true); // default to upcoming
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ViewBookingsActivity.this, "Failed to load: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBookings(boolean isUpcoming) {
        if (isUpcoming) {
            adapter.setBookingList(upcomingBookings);
        } else {
            adapter.setBookingList(pastBookings);
        }
    }
}
