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
                                              String phone, String avatarUrl) {
                            // Sync to SharedPreferences so ProfileActivity displays instantly
                            SharedPreferences prefs = getSharedPreferences(
                                    ProfileActivity.PREF_NAME, MODE_PRIVATE);
                            prefs.edit()
                                    .putBoolean(ProfileActivity.KEY_IS_LOGGED_IN, true)
                                    .putString(ProfileActivity.KEY_USER_NAME,  name)
                                    .putString(ProfileActivity.KEY_USER_EMAIL, userEmail)
                                    .putString("avatar_url_remote", avatarUrl)
                                    .apply();

                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this,
                                    "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
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
