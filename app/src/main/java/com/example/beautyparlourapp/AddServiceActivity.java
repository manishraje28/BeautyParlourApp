package com.example.beautyparlourapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

public class AddServiceActivity extends AppCompatActivity {

    private FrameLayout flImageContainer;
    private ImageView ivServicePreview, ivBack;
    private TextInputEditText edName, edPrice, edDuration, edCategory, edDesc;
    private Button btnSave;
    private ProgressBar pbAddService;

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Glide.with(this).load(selectedImageUri).into(ivServicePreview);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        flImageContainer = findViewById(R.id.fl_image_container);
        ivServicePreview = findViewById(R.id.iv_service_preview);
        ivBack = findViewById(R.id.iv_back);
        
        edName = findViewById(R.id.ed_service_name);
        edPrice = findViewById(R.id.ed_service_price);
        edDuration = findViewById(R.id.ed_service_duration);
        edCategory = findViewById(R.id.ed_service_category);
        edDesc = findViewById(R.id.ed_service_desc);
        
        btnSave = findViewById(R.id.btn_save_service);
        pbAddService = findViewById(R.id.pb_add_service);

        ivBack.setOnClickListener(v -> finish());

        flImageContainer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveService());
    }

    private void saveService() {
        String name = edName.getText().toString().trim();
        String priceStr = edPrice.getText().toString().trim();
        String duration = edDuration.getText().toString().trim();
        String category = edCategory.getText().toString().trim();
        String desc = edDesc.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || duration.isEmpty() || category.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        pbAddService.setVisibility(View.VISIBLE);

        if (selectedImageUri != null) {
            FirebaseManager.getInstance().uploadServiceImage(this, selectedImageUri, new FirebaseManager.PhotoUploadCallback() {
                @Override
                public void onSuccess(String url) {
                    saveToFirestore(name, price, duration, desc, category, url);
                }

                @Override
                public void onFailure(String error) {
                    pbAddService.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(AddServiceActivity.this, "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            saveToFirestore(name, price, duration, desc, category, null);
        }
    }

    private void saveToFirestore(String name, int price, String duration, String desc, String category, String imageUrl) {
        FirebaseManager.getInstance().addService(name, price, duration, desc, category, imageUrl, new FirebaseManager.ServiceActionCallback() {
            @Override
            public void onSuccess() {
                pbAddService.setVisibility(View.GONE);
                Toast.makeText(AddServiceActivity.this, "Service added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                pbAddService.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(AddServiceActivity.this, "Failed to save to database: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}