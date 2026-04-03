package com.example.beautyparlourapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Map;

public class ServicesActivity extends AppCompatActivity {

    private LinearLayout servicesContainer;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        servicesContainer = findViewById(R.id.services_container);
        progressBar = findViewById(R.id.pb_services);

        fetchServices();
        attachFooter();
    }

    // ── Fetch services directly from Firestore ──────────────────────────────
    private void fetchServices() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseManager.getInstance().fetchServices(new FirebaseManager.ServicesCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> services) {
                progressBar.setVisibility(View.GONE);
                displayServices(services);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ServicesActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayServices(List<Map<String, Object>> services) {
        servicesContainer.removeAllViews();

        // Add hint TextView
        TextView hint = new TextView(this);
        hint.setText("✦ Long press a service for quick details & booking");
        hint.setTextSize(12f);
        hint.setAlpha(0.65f);
        hint.setPadding(0, 0, 0, dp(16));
        servicesContainer.addView(hint);

        // Add service cards
        for (Map<String, Object> service : services) {
            TextView card = createServiceCard(service);
            servicesContainer.addView(card);
        }
    }

    private TextView createServiceCard(Map<String, Object> service) {
        String name = (String) service.get("name");
        int price = service.get("price") != null ? (int) service.get("price") : 0;
        String description = (String) service.get("description");
        String duration = (String) service.get("duration");

        TextView card = new TextView(this);
        card.setText(name + " - ₹" + price + "\n" + description);
        card.setTextSize(15f);
        card.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        card.setPadding(dp(20), dp(16), dp(20), dp(16));
        card.setClickable(true);
        card.setFocusable(true);
        card.setLongClickable(true);
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(10), 0, 0);
        card.setLayoutParams(params);

        // Long press listener
        card.setOnLongClickListener(v -> {
            String detail = name + "  —  ₹" + price + " · " + duration;
            Snackbar.make(v, detail, Snackbar.LENGTH_LONG)
                    .setAction("Book Now", btn -> {
                        Intent intent = new Intent(ServicesActivity.this, BookingActivity.class);
                        intent.putExtra("selected_service", name);
                        startActivity(intent);
                    })
                    .setActionTextColor(getResources().getColor(R.color.dark_pink, getTheme()))
                    .show();
            return true;
        });

        return card;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void attachFooter() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.footer_container, new FooterFragment())
                .commit();
    }
}
