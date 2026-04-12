package com.example.beautyparlourapp;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.beautyparlourapp.adapters.AdminBookingsAdapter;
import com.example.beautyparlourapp.models.Booking;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tabPending, tabApproved, tabCompleted;
    private RecyclerView rvBookings;
    private AdminBookingsAdapter adapter;
    private List<Booking> allBookings = new ArrayList<>();
    private String currentFilter = "pending";

    // Launcher for POST_NOTIFICATIONS
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Please enable notifications to receive booking requests.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        tabPending = findViewById(R.id.tab_pending);
        tabApproved = findViewById(R.id.tab_approved);
        tabCompleted = findViewById(R.id.tab_completed);
        rvBookings = findViewById(R.id.rv_admin_bookings);

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminBookingsAdapter(this, new ArrayList<>());
        rvBookings.setAdapter(adapter);

        // Click listeners for tabs
        tabPending.setOnClickListener(v -> selectTab("pending"));
        tabApproved.setOnClickListener(v -> selectTab("approved"));
        tabCompleted.setOnClickListener(v -> selectTab("completed"));

        // Initialize bottom navigation for Admin
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_footer_container, new AdminFooterFragment())
                .commit();

        // Subscribe to admin notifications topic
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .subscribeToTopic("admin_notifications")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("FCM", "Subscribed to admin_notifications topic");
                    }
                });

        selectTab("pending");
        fetchAdminBookings();
    }

    private void fetchAdminBookings() {
        Toast.makeText(this, "Loading dashboard...", Toast.LENGTH_SHORT).show();
        FirebaseManager.getInstance().fetchAllBookings(new FirebaseManager.FetchBookingsCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                allBookings.clear();
                allBookings.addAll(bookings);
                filterBookings();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminDashboardActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectTab(String tab) {
        currentFilter = tab;
        
        tabPending.setBackground(null);
        tabApproved.setBackground(null);
        tabCompleted.setBackground(null);

        tabPending.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tabApproved.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tabCompleted.setTextColor(getResources().getColor(android.R.color.darker_gray));

        if (tab.equals("pending")) {
            tabPending.setBackground(getResources().getDrawable(R.drawable.bg_admin_tab_active));
            tabPending.setTextColor(getResources().getColor(R.color.black));
        } else if (tab.equals("approved")) {
            tabApproved.setBackground(getResources().getDrawable(R.drawable.bg_admin_tab_active));
            tabApproved.setTextColor(getResources().getColor(R.color.black));
        } else {
            tabCompleted.setBackground(getResources().getDrawable(R.drawable.bg_admin_tab_active));
            tabCompleted.setTextColor(getResources().getColor(R.color.black));
        }

        filterBookings();
    }

    private void filterBookings() {
        List<Booking> filteredList = new ArrayList<>();
        for (Booking b : allBookings) {
            String status = b.getStatus().toLowerCase();
            if (currentFilter.equals("pending") && status.equals("pending")) {
                filteredList.add(b);
            } else if (currentFilter.equals("approved") && status.equals("confirmed")) {
                filteredList.add(b);
            } else if (currentFilter.equals("completed") && (status.equals("completed") || status.equals("cancelled"))) {
                filteredList.add(b);
            }
        }
        adapter.setBookingList(filteredList);
    }
}
