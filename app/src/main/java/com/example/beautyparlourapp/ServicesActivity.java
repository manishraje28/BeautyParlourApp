package com.example.beautyparlourapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.beautyparlourapp.model.ServiceItem;
import com.example.beautyparlourapp.model.ServiceResponse;
import com.example.beautyparlourapp.network.RetrofitClient;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicesActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvError;
    private LinearLayout servicesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        progressBar      = findViewById(R.id.progress_bar);
        tvError          = findViewById(R.id.tv_error);
        servicesContainer = findViewById(R.id.services_container);

        fetchServices();
        attachFooter();
    }

    // ── API Call ─────────────────────────────────────────────────────────────
    private void fetchServices() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        RetrofitClient.getInstance().getApi()
                .getServices(10)
                .enqueue(new Callback<ServiceResponse>() {
                    @Override
                    public void onResponse(Call<ServiceResponse> call,
                                           Response<ServiceResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            displayServices(response.body().getProducts());
                        } else {
                            showError("Could not load services. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ServiceResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showError("No internet connection. Please check your network.");
                    }
                });
    }

    private void displayServices(List<ServiceItem> services) {
        servicesContainer.removeAllViews();
        for (ServiceItem service : services) {
            addServiceCard(service);
        }
    }

    // ── Build one card per service ────────────────────────────────────────────
    private void addServiceCard(ServiceItem service) {
        TextView card = new TextView(this);

        int p = dp(14);
        card.setPadding(p, p, p, p);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_card));
        card.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        card.setTextSize(14f);
        card.setLineSpacing(dp(2), 1f);

        // dummyjson prices are in USD — convert to approx INR for demo
        String priceInr = "₹" + (int) (service.getPrice() * 83);
        card.setText(service.getTitle() + "  —  " + priceInr + "\n" + service.getDescription());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(10);
        card.setLayoutParams(params);

        card.setClickable(true);
        card.setFocusable(true);
        card.setLongClickable(true);

        // ── Gesture 3: Long Press → Snackbar with Book Now ───────────────────
        card.setOnLongClickListener(v -> {
            String label = service.getTitle() + "  •  " + priceInr;
            Snackbar.make(v, label, Snackbar.LENGTH_LONG)
                    .setAction("Book Now", btn -> {
                        Intent intent = new Intent(ServicesActivity.this, BookingActivity.class);
                        intent.putExtra("selected_service", service.getTitle());
                        startActivity(intent);
                    })
                    .setActionTextColor(getResources().getColor(R.color.dark_pink, getTheme()))
                    .show();
            return true;
        });

        servicesContainer.addView(card);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
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
