package com.example.beautyparlourapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    public static final String PREF_NAME           = "beauty_parlour_prefs";
    public static final String KEY_IS_LOGGED_IN    = "is_logged_in";
    public static final String KEY_USER_NAME       = "user_name";
    public static final String KEY_USER_EMAIL      = "user_email";
    private static final String KEY_AVATAR_URI     = "avatar_uri";
    private static final String KEY_AVATAR_URL_REMOTE = "avatar_url_remote";

    private GestureDetector swipeDetector;
    private ImageView imgAvatar;
    private ActivityResultLauncher<String> imagePickerLauncher;

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

        updateProfileUI();
        setupChangePhotoButton();
        setupAvatarInteraction();  // Gesture 5 – tap = full-screen popup
        setupSwipeNavigation();    // Gesture 4 – swipe left/right
        attachFooter();
    }

    // ── Change Photo (✎ pencil button) ───────────────────────────────────────
    private void setupChangePhotoButton() {
        TextView btnChangePhoto = findViewById(R.id.btn_change_photo);
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
