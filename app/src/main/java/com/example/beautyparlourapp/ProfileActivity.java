package com.example.beautyparlourapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    public static final String PREF_NAME           = "beauty_parlour_prefs";
    public static final String KEY_IS_LOGGED_IN    = "is_logged_in";
    public static final String KEY_USER_NAME       = "user_name";
    public static final String KEY_USER_EMAIL      = "user_email";
    private static final String KEY_AVATAR_URI     = "avatar_uri";
    private static final String KEY_AVATAR_URL_REMOTE = "avatar_url_remote";
    private static final String KEY_STYLE_JOURNEY_URIS = "style_journey_uris";

    private GestureDetector swipeDetector;
    private ImageView imgAvatar;
    private ActivityResultLauncher<String> imagePickerLauncher;
    
    // Style Journey Additions
    private ActivityResultLauncher<String> journeyMediaPickerLauncher;
    private RecyclerView rvStyleJourney;
    private StyleJourneyAdapter journeyAdapter;
    private List<String> journeyMediaUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgAvatar = findViewById(R.id.img_avatar);

        // Register before onStart — opens gallery when ✎ is tapped
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;

                    // Show locally right away for instant feedback
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                            .edit().putString(KEY_AVATAR_URI, uri.toString()).apply();
                    loadAvatarFromUri(uri);

                    // Upload to Firebase Storage → save URL to Firestore + prefs
                    if (FirebaseManager.getInstance().isLoggedIn()) {
                        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show();
                        FirebaseManager.getInstance().uploadProfilePhoto(uri,
                                new FirebaseManager.PhotoUploadCallback() {
                                    @Override
                                    public void onSuccess(String url) {
                                        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                                                .edit()
                                                .putString(KEY_AVATAR_URL_REMOTE, url)
                                                .apply();
                                        loadAvatarFromUrl(url);
                                        Toast.makeText(ProfileActivity.this,
                                                "Profile photo uploaded!", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(ProfileActivity.this,
                                                "Upload failed. Photo saved locally.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                    }
                });

        // Register launcher for Style Journey media
        journeyMediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(), // Supports selecting multiple items
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            try {
                                getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (SecurityException e) {
                                Log.e("ProfileActivity", "Failed to take persistable permission", e);
                            }
                            journeyMediaUris.add(uri.toString());
                        }
                        saveJourneyUris();
                        journeyAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Added to Style Journey!", Toast.LENGTH_SHORT).show();
                    }
                });

        updateProfileUI();
        setupChangePhotoButton();
        setupAvatarInteraction();  // Gesture 5 – tap = full-screen popup
        setupSwipeNavigation();    // Gesture 4 – swipe left/right
        setupStyleJourneyGrid();
        attachFooter();
    }

    // ── Style Journey Setup ───────────────────────────────────────────────────
    private void setupStyleJourneyGrid() {
        rvStyleJourney = findViewById(R.id.rv_style_journey);
        ImageView btnAddMedia = findViewById(R.id.btn_add_media);

        // Load saved URIs
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedUris = prefs.getString(KEY_STYLE_JOURNEY_URIS, "");
        if (!savedUris.isEmpty()) {
            journeyMediaUris.addAll(Arrays.asList(savedUris.split(",")));
        }
        
        // Setup Grid
        rvStyleJourney.setLayoutManager(new GridLayoutManager(this, 3));
        journeyAdapter = new StyleJourneyAdapter(journeyMediaUris);
        rvStyleJourney.setAdapter(journeyAdapter);
        
        // Setup Add Button
        btnAddMedia.setOnClickListener(v -> {
            journeyMediaPickerLauncher.launch("*/*"); // image and video support
        });
    }

    private void saveJourneyUris() {
        if (journeyMediaUris.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < journeyMediaUris.size(); i++) {
            sb.append(journeyMediaUris.get(i));
            if (i < journeyMediaUris.size() - 1) sb.append(",");
        }
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_STYLE_JOURNEY_URIS, sb.toString()).apply();
    }

    // ── Style Journey RecyclerView Adapter ────────────────────────────────────
    private class StyleJourneyAdapter extends RecyclerView.Adapter<StyleJourneyAdapter.ViewHolder> {
        private final List<String> mediaUris;

        public StyleJourneyAdapter(List<String> mediaUris) {
            this.mediaUris = mediaUris;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_style_journey, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String uriString = mediaUris.get(position);
            Uri uri = Uri.parse(uriString);
            
            // Glide handles local video URIs by grabbing the first frame automatically
            Glide.with(ProfileActivity.this)
                    .load(uri)
                    .centerCrop()
                    .into(holder.imgStyle);
            
            // Optional: Launch full screen slider on click
            holder.itemView.setOnClickListener(v -> showGridMediaFullScreen(position));
        }

        @Override
        public int getItemCount() {
            return mediaUris.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgStyle;
            ViewHolder(View itemView) {
                super(itemView);
                imgStyle = itemView.findViewById(R.id.img_style);
            }
        }
    }

    private void showGridMediaFullScreen(int startPosition) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCanceledOnTouchOutside(true);

        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(0xEE000000); // 93% black background

        androidx.viewpager2.widget.ViewPager2 viewPager = new androidx.viewpager2.widget.ViewPager2(this);
        viewPager.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        viewPager.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                FrameLayout container = new FrameLayout(parent.getContext());
                container.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                return new RecyclerView.ViewHolder(container) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                FrameLayout container = (FrameLayout) holder.itemView;
                container.removeAllViews();
                
                Uri mediaUri = Uri.parse(journeyMediaUris.get(position));
                String mimeType = getContentResolver().getType(mediaUri);
                boolean isVideo = mimeType != null && mimeType.startsWith("video/");
                
                if (isVideo) {
                    android.widget.VideoView videoView = new android.widget.VideoView(container.getContext());
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.CENTER;
                    videoView.setLayoutParams(params);
                    videoView.setVideoURI(mediaUri);
                    
                    android.widget.MediaController mediaController = new android.widget.MediaController(container.getContext());
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);
                    
                    container.addView(videoView);
                    videoView.start();
                } else {
                    ImageView imageView = new ImageView(container.getContext());
                    imageView.setLayoutParams(new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    Glide.with(container.getContext()).load(mediaUri).into(imageView);
                    container.addView(imageView);
                }
            }

            @Override
            public int getItemCount() {
                return journeyMediaUris.size();
            }
        });

        // Set the ViewPager to launch on the tapped photo's position
        viewPager.setCurrentItem(startPosition, false);

        // Close button
        ImageView btnClose = new ImageView(this);
        btnClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnClose.setColorFilter(0xFFFFFFFF); // White icon
        btnClose.setPadding(32, 32, 32, 32);
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(
                120, 120);
        closeParams.gravity = Gravity.TOP | Gravity.END;
        closeParams.setMargins(0, 48, 48, 0);
        btnClose.setLayoutParams(closeParams);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        overlay.addView(viewPager);
        overlay.addView(btnClose);

        dialog.setContentView(overlay);
        dialog.show();
    }

    // ── Change Photo (✎ pencil button) ───────────────────────────────────────
    private void setupChangePhotoButton() {
        ImageView btnChangePhoto = findViewById(R.id.btn_change_photo);
        btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    // ── Avatar loading helpers ────────────────────────────────────────────────
    private void loadAvatarFromUri(Uri uri) {
        Glide.with(this).load(uri).circleCrop().into(imgAvatar);
    }

    private void loadAvatarFromUrl(String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).circleCrop()
                    .placeholder(R.drawable.bg_profile_placeholder)
                    .into(imgAvatar);
        }
    }

    private void loadSavedAvatar() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String remoteUrl = prefs.getString(KEY_AVATAR_URL_REMOTE, null);
        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            loadAvatarFromUrl(remoteUrl);   // cloud photo takes priority
        } else {
            String localUri = prefs.getString(KEY_AVATAR_URI, null);
            if (localUri != null) loadAvatarFromUri(Uri.parse(localUri));
        }
    }

    // ── Refreshes name/email/avatar from Firestore ────────────────────────────
    private void refreshFromFirestore() {
        if (!FirebaseManager.getInstance().isLoggedIn()) return;
        String uid = FirebaseManager.getInstance().getCurrentUser().getUid();

        FirebaseManager.getInstance().fetchUserFromFirestore(uid,
                new FirebaseManager.LoginCallback() {
                    @Override
                    public void onSuccess(String name, String email,
                                         String phone, String avatarUrl) {
                        // Update SharedPreferences with fresh Firestore data
                        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                        prefs.edit()
                                .putString(KEY_USER_NAME,  name)
                                .putString(KEY_USER_EMAIL, email)
                                .putString(KEY_AVATAR_URL_REMOTE, avatarUrl)
                                .apply();

                        ((TextView) findViewById(R.id.tv_profile_name)).setText(name);
                        ((TextView) findViewById(R.id.tv_profile_email)).setText(email);
                        loadAvatarFromUrl(avatarUrl);
                    }

                    @Override
                    public void onFailure(String error) { /* silent — prefs data still shown */ }
                });
    }

    // ── Gesture 5: Tap avatar → Instagram-style full-screen popup ────────────
    private void setupAvatarInteraction() {
        imgAvatar.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        imgAvatar.setClipToOutline(true);
        imgAvatar.setOnClickListener(v -> showAvatarFullScreen());
    }

    private void showAvatarFullScreen() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCanceledOnTouchOutside(true);

        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackgroundColor(0xEE000000);

        ImageView fullImg = new ImageView(this);
        fullImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        fullImg.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Load photo into full-screen view (remote URL preferred, fallback to local)
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String remoteUrl = prefs.getString(KEY_AVATAR_URL_REMOTE, null);
        String localUri  = prefs.getString(KEY_AVATAR_URI, null);
        if (remoteUrl != null && !remoteUrl.isEmpty()) {
            Glide.with(this).load(remoteUrl).into(fullImg);
        } else if (localUri != null) {
            Glide.with(this).load(Uri.parse(localUri)).into(fullImg);
        } else {
            fullImg.setBackgroundResource(R.drawable.bg_profile_placeholder);
        }

        TextView hint = new TextView(this);
        hint.setText("Tap anywhere to close");
        hint.setTextColor(0xAAFFFFFF);
        hint.setTextSize(12f);
        hint.setGravity(Gravity.CENTER);
        int bottomMargin = (int) (48 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        hintParams.bottomMargin = bottomMargin;
        hint.setLayoutParams(hintParams);

        overlay.addView(fullImg);
        overlay.addView(hint);

        fullImg.setScaleX(0.3f);
        fullImg.setScaleY(0.3f);
        fullImg.setAlpha(0f);
        fullImg.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(220).start();

        final float[] scale = {1.0f};
        ScaleGestureDetector dialogPinch = new ScaleGestureDetector(this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        scale[0] *= detector.getScaleFactor();
                        scale[0] = Math.max(0.5f, Math.min(scale[0], 5.0f));
                        fullImg.setScaleX(scale[0]);
                        fullImg.setScaleY(scale[0]);
                        return true;
                    }
                });

        GestureDetector dialogTap = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override public boolean onDown(MotionEvent e) { return true; }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        dialog.dismiss();
                        return true;
                    }
                });

        overlay.setOnTouchListener((v, event) -> {
            dialogPinch.onTouchEvent(event);
            dialogTap.onTouchEvent(event);
            return true;
        });

        dialog.setContentView(overlay);
        dialog.show();
    }

    // ── Gesture 4: Swipe Left → Booking  |  Swipe Right → Home ─────────────
    private void setupSwipeNavigation() {
        swipeDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_MIN_DISTANCE = 100;
            private static final int SWIPE_MIN_VELOCITY = 200;

            @Override public boolean onDown(MotionEvent e) { return true; }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)
                        && Math.abs(diffX) > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_MIN_VELOCITY) {
                    if (diffX < 0) {
                        Toast.makeText(ProfileActivity.this, "Opening Booking...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProfileActivity.this, BookingActivity.class));
                    } else {
                        Toast.makeText(ProfileActivity.this, "Going Home...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        swipeDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    private void updateProfileUI() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Also honour Firebase session if SharedPreferences was cleared
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
                || FirebaseManager.getInstance().isLoggedIn();

        View   loggedOutContainer = findViewById(R.id.layout_logged_out);
        View   loggedInContainer  = findViewById(R.id.layout_logged_in);
        Button loginButton        = findViewById(R.id.btn_go_login);
        Button logoutButton       = findViewById(R.id.btn_logout);

        if (isLoggedIn) {
            loggedOutContainer.setVisibility(View.GONE);
            loggedInContainer.setVisibility(View.VISIBLE);

            ((TextView) findViewById(R.id.tv_profile_name))
                    .setText(prefs.getString(KEY_USER_NAME, "Guest User"));
            ((TextView) findViewById(R.id.tv_profile_email))
                    .setText(prefs.getString(KEY_USER_EMAIL, "guest@example.com"));

            loadSavedAvatar();
            refreshFromFirestore(); // pull latest data from Firestore silently

            logoutButton.setOnClickListener(v -> {
                FirebaseManager.getInstance().logout();
                prefs.edit()
                        .putBoolean(KEY_IS_LOGGED_IN, false)
                        .remove(KEY_USER_NAME)
                        .remove(KEY_USER_EMAIL)
                        .remove(KEY_AVATAR_URL_REMOTE)
                        .apply();
                imgAvatar.setImageURI(null);
                Glide.with(this).clear(imgAvatar);
                updateProfileUI();
            });

//            Button viewUsersButton = findViewById(R.id.btn_view_users);
//            viewUsersButton.setOnClickListener(v ->
//                    startActivity(new Intent(ProfileActivity.this, UsersActivity.class))
//            );
        } else {
            loggedOutContainer.setVisibility(View.VISIBLE);
            loggedInContainer.setVisibility(View.GONE);

            loginButton.setOnClickListener(v ->
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class)));
        }
    }

    private void attachFooter() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.footer_container, new FooterFragment())
                .commit();
    }
}
