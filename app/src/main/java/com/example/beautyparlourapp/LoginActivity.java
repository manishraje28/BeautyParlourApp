package com.example.beautyparlourapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText emailInput = findViewById(R.id.et_login_email);
        EditText passwordInput = findViewById(R.id.et_login_password);
        Button loginButton = findViewById(R.id.btn_login);
        TextView signupLink = findViewById(R.id.tv_go_signup);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            String defaultName = email.contains("@") ? email.substring(0, email.indexOf("@")) : "Salon Customer";
            SharedPreferences preferences = getSharedPreferences(ProfileActivity.PREF_NAME, MODE_PRIVATE);
            preferences.edit()
                    .putBoolean(ProfileActivity.KEY_IS_LOGGED_IN, true)
                    .putString(ProfileActivity.KEY_USER_NAME, defaultName)
                    .putString(ProfileActivity.KEY_USER_EMAIL, email)
                    .apply();

            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
