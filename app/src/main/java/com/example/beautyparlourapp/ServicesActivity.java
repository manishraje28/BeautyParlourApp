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
        hint.setPadding(dp(20), 0, 0, dp(16));
        servicesContainer.addView(hint);

        // Add service cards
        for (Map<String, Object> service : services) {
            View card = createServiceCard(service);
            servicesContainer.addView(card);
        }
    }

    private View createServiceCard(Map<String, Object> service) {
        String name = (String) service.get("name");
        int price = service.get("price") != null ? (int) service.get("price") : 0;
        String description = (String) service.get("description");
        String duration = (String) service.get("duration");
        String category = (String) service.get("category");

        // Set to default if empty
        if (category == null || category.isEmpty()) category = "General";

        // Inflate the new premium service card
        View cardView = getLayoutInflater().inflate(R.layout.item_service_card, servicesContainer, false);

        android.widget.ImageView ivIcon = cardView.findViewById(R.id.iv_service_icon);
        TextView tvTitle = cardView.findViewById(R.id.tv_title);
        TextView tvPrice = cardView.findViewById(R.id.tv_price);
        TextView tvDesc = cardView.findViewById(R.id.tv_desc);
        TextView tvChip = cardView.findViewById(R.id.tv_chip);

        tvTitle.setText(name);
        tvPrice.setText("₹" + price);
        tvDesc.setText(description);
        tvChip.setText(category);

        // Intelligently map categories to our new vector icons!
        switch (category.toLowerCase()) {
            case "skincare":
            case "facial":
                ivIcon.setImageResource(R.drawable.ic_category_skincare);
                break;
            case "makeup":
            case "bridal makeup":
                ivIcon.setImageResource(R.drawable.ic_category_makeup);
                break;
            case "spa":
            case "massage":
                ivIcon.setImageResource(R.drawable.ic_category_spa);
                break;
            case "hair":
            default:
                ivIcon.setImageResource(R.drawable.ic_category_hair);
                break;
        }

        // Long press listener
        cardView.setOnLongClickListener(v -> {
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

        return cardView;
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
