package com.example.beautyparlourapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Button nextButton = findViewById(R.id.btn_next);
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }
}
