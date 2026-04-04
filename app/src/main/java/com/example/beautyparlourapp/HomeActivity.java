package com.example.beautyparlourapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private GestureDetector doubleTapDetector;
    private String pendingService;
    private LinearLayout offersContainer;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        offersContainer = findViewById(R.id.offers_container);
        swipeRefresh = findViewById(R.id.swipe_refresh_home);

        setupPullToRefresh();    // Gesture 1
        setupDoubleTapCards();   // Gesture 2
        fetchOffers();
        attachFooter();
    }

    // ── Gesture 1: Pull-Down Refresh ────────────────────────────────────────
    private void setupPullToRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.dark_pink);
        swipeRefresh.setOnRefreshListener(() -> {
            fetchOffers();
            Toast.makeText(this, "Offers refreshed!", Toast.LENGTH_SHORT).show();
        });
    }

    // ── Fetch offers directly from Firestore ────────────────────────────────
    private void fetchOffers() {
        FirebaseManager.getInstance().fetchOffers(new FirebaseManager.OffersCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> offers) {
                swipeRefresh.setRefreshing(false);
                displayOffers(offers);
            }

            @Override
            public void onFailure(String error) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(HomeActivity.this, "Error loading offers: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOffers(List<Map<String, Object>> offers) {
        offersContainer.removeAllViews();
        for (Map<String, Object> offer : offers) {
            String title = (String) offer.get("title");
            String code = (String) offer.get("code");
            String validTill = (String) offer.get("validTill");
            int discount = offer.get("discount") != null ? (int) offer.get("discount") : 0;

            // Inflate our beautiful new offer card layout
            View child = getLayoutInflater().inflate(R.layout.item_offer, offersContainer, false);

            android.widget.TextView tvDiscount = child.findViewById(R.id.tv_offer_discount);
            android.widget.TextView tvTitle = child.findViewById(R.id.tv_offer_title);
            android.widget.TextView tvCode = child.findViewById(R.id.tv_offer_code);
            android.widget.TextView tvValid = child.findViewById(R.id.tv_offer_valid);

            tvDiscount.setText(discount + "% off");
            tvTitle.setText(title);
            tvCode.setText("Code: " + code);
            tvValid.setText("Valid till " + validTill);

            offersContainer.addView(child);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    // ── Gesture 2: Double Tap on service card → jump to BookingActivity ─────
    private void setupDoubleTapCards() {
        doubleTapDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true; // required for GestureDetector to track the event stream
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Intent intent = new Intent(HomeActivity.this, BookingActivity.class);
                intent.putExtra("selected_service", pendingService);
                startActivity(intent);
                return true;
            }
        });

        attachDoubleTap(R.id.card_haircut, "Haircut");
        attachDoubleTap(R.id.card_facial, "Facial");
        attachDoubleTap(R.id.card_bridal, "Bridal Makeup");
    }

    private void attachDoubleTap(int cardId, String serviceName) {
        View card = findViewById(cardId);
        if (card != null) {
            card.setOnTouchListener((v, event) -> {
                pendingService = serviceName;
                return doubleTapDetector.onTouchEvent(event);
            });
        }
    }

    private void attachFooter() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.footer_container, new FooterFragment())
                .commit();
    }
}
