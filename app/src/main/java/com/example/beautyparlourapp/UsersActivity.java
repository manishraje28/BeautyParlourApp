package com.example.beautyparlourapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.beautyparlourapp.model.User;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UsersAdapter adapter;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        rvUsers = findViewById(R.id.users_recycler);
        userList = new ArrayList<>();

        // Setup RecyclerView with LinearLayoutManager
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with correct parameter order (userList, context)
        adapter = new UsersAdapter(userList, this);
        rvUsers.setAdapter(adapter);

        // Fetch users from Firebase
        fetchUsers();

        // Attach footer
        attachFooter();
    }

    private void fetchUsers() {
        FirebaseManager.getInstance().fetchUsers(new FirebaseManager.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                userList.clear();
                userList.addAll(users);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(UsersActivity.this, "Error loading users: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attachFooter() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.footer_container, new FooterFragment())
                .commit();
    }
}
