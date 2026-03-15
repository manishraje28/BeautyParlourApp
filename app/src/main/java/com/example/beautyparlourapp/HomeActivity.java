package com.example.beautyparlourapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomeActivity extends AppCompatActivity {

    private GestureDetector doubleTapDetector;
    private String pendingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupPullToRefresh();    // Gesture 1
        setupDoubleTapCards();   // Gesture 2
        attachFooter();
    }

    // ── Gesture 1: Pull-Down Refresh ────────────────────────────────────────
    private void setupPullToRefresh() {
        SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipe_refresh_home);
        swipeRefresh.setColorSchemeResources(R.color.dark_pink);
        swipeRefresh.setOnRefreshListener(() -> {
            Toast.makeText(this, "Latest offers refreshed!", Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
        });
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
        findViewById(cardId).setOnTouchListener((v, event) -> {
            pendingService = serviceName;
            return doubleTapDetector.onTouchEvent(event);
        });
    }

    private void attachFooter() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.footer_container, new FooterFragment())
                .commit();
    }
}
