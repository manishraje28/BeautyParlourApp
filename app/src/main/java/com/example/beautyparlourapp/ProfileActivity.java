package com.example.beautyparlourapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class ProfileActivity extends AppCompatActivity {

    public static final String PREF_NAME = "beauty_parlour_prefs";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        updateProfileUI();
        attachFooter();
    }

    private void updateProfileUI() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false);

        View loggedOutContainer = findViewById(R.id.layout_logged_out);
        View loggedInContainer = findViewById(R.id.layout_logged_in);
        Button loginButton = findViewById(R.id.btn_go_login);
        Button logoutButton = findViewById(R.id.btn_logout);

        if (isLoggedIn) {
            loggedOutContainer.setVisibility(View.GONE);
            loggedInContainer.setVisibility(View.VISIBLE);

            TextView nameText = findViewById(R.id.tv_profile_name);
            TextView emailText = findViewById(R.id.tv_profile_email);

            String name = preferences.getString(KEY_USER_NAME, "Guest User");
            String email = preferences.getString(KEY_USER_EMAIL, "guest@example.com");

            nameText.setText(name);
            emailText.setText(email);

            logoutButton.setOnClickListener(v -> {
                preferences.edit()
                        .putBoolean(KEY_IS_LOGGED_IN, false)
                        .remove(KEY_USER_NAME)
                        .remove(KEY_USER_EMAIL)
                        .apply();
                updateProfileUI();
            });
        } else {
            loggedOutContainer.setVisibility(View.VISIBLE);
            loggedInContainer.setVisibility(View.GONE);

            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
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
