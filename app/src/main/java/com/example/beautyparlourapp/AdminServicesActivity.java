package com.example.beautyparlourapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.beautyparlourapp.R;

public class AdminServicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_services);

        // Initialize bottom navigation for Admin
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_footer_container, new AdminFooterFragment())
                .commit();

        findViewById(R.id.btn_add_new).setOnClickListener(v -> {
            Toast.makeText(this, "Add New Service clicked!", Toast.LENGTH_SHORT).show();
            // Implement Add Service logic here
        });
    }
}
