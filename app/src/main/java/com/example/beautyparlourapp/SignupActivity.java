package com.example.beautyparlourapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText  fullNameInput        = findViewById(R.id.et_signup_full_name);
        EditText  phoneInput           = findViewById(R.id.et_signup_phone);
        EditText  emailInput           = findViewById(R.id.et_signup_email);
        EditText  passwordInput        = findViewById(R.id.et_signup_password);
        EditText  confirmPasswordInput = findViewById(R.id.et_signup_confirm_password);
        CheckBox  termsCheckbox        = findViewById(R.id.cb_terms);
        Button    signupButton         = findViewById(R.id.btn_signup);
        TextView  loginLink            = findViewById(R.id.tv_go_login);
        ProgressBar progressBar        = findViewById(R.id.pb_signup);

        signupButton.setOnClickListener(v -> {
            String fullName         = fullNameInput.getText().toString().trim();
            String phone            = phoneInput.getText().toString().trim();
            String email            = emailInput.getText().toString().trim();
            String password         = passwordInput.getText().toString().trim();
            String confirmPassword  = confirmPasswordInput.getText().toString().trim();

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phone)
                    || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                    || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!termsCheckbox.isChecked()) {
                Toast.makeText(this, "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading
            signupButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            FirebaseManager.getInstance().signUp(fullName, phone, email, password,
                    new FirebaseManager.SignUpCallback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignupActivity.this,
                                    "Account created! Welcome!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            signupButton.setEnabled(true);
                            Toast.makeText(SignupActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }
}
