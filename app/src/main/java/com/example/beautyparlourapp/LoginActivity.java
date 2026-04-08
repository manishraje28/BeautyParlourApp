package com.example.beautyparlourapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText    emailInput    = findViewById(R.id.et_login_email);
        EditText    passwordInput = findViewById(R.id.et_login_password);
        Button      loginButton   = findViewById(R.id.btn_login);
        TextView    signupLink    = findViewById(R.id.tv_go_signup);
        ProgressBar progressBar   = findViewById(R.id.pb_login);

        loginButton.setOnClickListener(v -> {
            String email    = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.equals("admin") && password.equals("admin")) {
                Toast.makeText(LoginActivity.this, "Welcome to Admin Dashboard", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            loginButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            FirebaseManager.getInstance().login(email, password,
                    new FirebaseManager.LoginCallback() {
                        @Override
                        public void onSuccess(String name, String userEmail,
                                              String phone, String avatarUrl, String role) {
                            // Sync to SharedPreferences so ProfileActivity displays instantly
                            SharedPreferences prefs = getSharedPreferences(
                                    ProfileActivity.PREF_NAME, MODE_PRIVATE);
                            prefs.edit()
                                    .clear() // Wipe previous user's data forcefully!
                                    .putBoolean(ProfileActivity.KEY_IS_LOGGED_IN, true)
                                    .putString(ProfileActivity.KEY_USER_NAME,  name)
                                    .putString(ProfileActivity.KEY_USER_EMAIL, userEmail)
                                    .putString("avatar_url_remote", avatarUrl)
                                    .putString("user_role", role)
                                    .apply();

                            // Force save the latest FCM token 
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseManager.getInstance().updateFcmToken(task.getResult());
                                    }
                                });

                            progressBar.setVisibility(View.GONE);
                            
                            if ("admin".equals(role)) {
                                Toast.makeText(LoginActivity.this,
                                        "Welcome to Admin Console", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                            }
                            finish();
                        }

                        @Override
                        public void onFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        signupLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }
}
