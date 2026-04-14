package com.example.beautyparlourapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beautyparlourapp.adapters.AdminServicesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminServicesActivity extends AppCompatActivity {

    private RecyclerView rvAdminItems;
    private AdminServicesAdapter adapter;
    private List<Map<String, Object>> servicesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_services);

        rvAdminItems = findViewById(R.id.rv_admin_items);
        rvAdminItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminServicesAdapter(this, servicesList);
        rvAdminItems.setAdapter(adapter);

        // Initialize bottom navigation for Admin
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_footer_container, new AdminFooterFragment())
                .commit();

        findViewById(R.id.btn_add_new).setOnClickListener(v -> {
            Intent intent = new Intent(AdminServicesActivity.this, AddServiceActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchServices();
    }

    private void fetchServices() {
        FirebaseManager.getInstance().fetchServices(new FirebaseManager.ServicesCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> services) {
                servicesList.clear();
                servicesList.addAll(services);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminServicesActivity.this, "Failed to load: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
