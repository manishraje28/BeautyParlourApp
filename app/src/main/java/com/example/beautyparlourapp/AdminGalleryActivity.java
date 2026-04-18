package com.example.beautyparlourapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.beautyparlourapp.adapter.GalleryAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminGalleryActivity extends AppCompatActivity {

    private RecyclerView rvGallery;
    private GalleryAdapter adapter;
    private List<Map<String, Object>> imageList;
    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::getResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_gallery);

        rvGallery = findViewById(R.id.rv_admin_gallery);
        Button btnUpload = findViewById(R.id.btn_upload_gallery);

        imageList = new ArrayList<>();
        rvGallery.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        
        adapter = new GalleryAdapter(this, imageList, true, this::deleteImage);
        rvGallery.setAdapter(adapter);

        btnUpload.setOnClickListener(v -> openGallery());

        attachFooter();
        loadGallery();
    }

    private void getResult(androidx.activity.result.ActivityResult result) {
         if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
            Uri selectedImageUri = result.getData().getData();
            if (selectedImageUri != null) {
                uploadToCloudinary(selectedImageUri);
            }
        }
    }

    private void loadGallery() {
        FirebaseManager.getInstance().fetchGalleryImages(new FirebaseManager.GalleryCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> images) {
                imageList.clear();
                imageList.addAll(images);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminGalleryActivity.this, "Failed to load: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void uploadToCloudinary(Uri uri) {
        showProgress("Uploading to Cloud...");
        FirebaseManager.getInstance().uploadGalleryImage(this, uri, new FirebaseManager.PhotoUploadCallback() {
            @Override
            public void onSuccess(String photoUrl) {
                saveToFirestore(photoUrl);
            }

            @Override
            public void onFailure(String error) {
                hideProgress();
                Toast.makeText(AdminGalleryActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFirestore(String url) {
        FirebaseManager.getInstance().addGalleryImage(url, new FirebaseManager.ServiceActionCallback() {
            @Override
            public void onSuccess() {
                hideProgress();
                Toast.makeText(AdminGalleryActivity.this, "Image added to gallery!", Toast.LENGTH_SHORT).show();
                loadGallery(); // refresh lists
            }

            @Override
            public void onFailure(String error) {
                hideProgress();
                Toast.makeText(AdminGalleryActivity.this, "Failed to save: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteImage(String id) {
        showProgress("Deleting...");
        FirebaseManager.getInstance().deleteGalleryImage(id, new FirebaseManager.ServiceActionCallback() {
            @Override
            public void onSuccess() {
                hideProgress();
                Toast.makeText(AdminGalleryActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                loadGallery();
            }

            @Override
            public void onFailure(String error) {
                hideProgress();
                Toast.makeText(AdminGalleryActivity.this, "Delete failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void attachFooter() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.footer_container_admin_gallery, new AdminFooterFragment())
                .commit();
    }
}