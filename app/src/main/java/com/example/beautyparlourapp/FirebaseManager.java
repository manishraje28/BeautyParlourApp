package com.example.beautyparlourapp;

import android.net.Uri;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Central helper for all Firebase operations:
 *   - Authentication  (sign up / login / logout)
 *   - Firestore       (save & fetch user profile, save bookings)
 *   - Storage         (upload profile photo)
 */
public class FirebaseManager {

    private static FirebaseManager instance;

    private final FirebaseAuth     auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage  storage;

    private FirebaseManager() {
        auth    = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    // ── Convenience getters ───────────────────────────────────────────────────
    public FirebaseUser getCurrentUser()  { return auth.getCurrentUser(); }
    public boolean      isLoggedIn()      { return auth.getCurrentUser() != null; }

    // ── Sign Up ───────────────────────────────────────────────────────────────
    public void signUp(String name, String phone, String email,
                       String password, SignUpCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    saveUserToFirestore(uid, name, phone, email, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void saveUserToFirestore(String uid, String name, String phone,
                                     String email, SignUpCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("name",       name);
        user.put("phone",      phone);
        user.put("email",      email);
        user.put("avatarUrl",  "");
        user.put("joinedDate", new Timestamp(new Date()));

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    public void login(String email, String password, LoginCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    fetchUserFromFirestore(uid, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void fetchUserFromFirestore(String uid, LoginCallback callback) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess(
                                doc.getString("name"),
                                doc.getString("email"),
                                doc.getString("phone"),
                                doc.getString("avatarUrl"));
                    } else {
                        callback.onFailure("User profile not found in database.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    public void logout() {
        auth.signOut();
    }

    // ── Upload Profile Photo → Storage, then save URL → Firestore ────────────
    public void uploadProfilePhoto(Uri uri, PhotoUploadCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) { callback.onFailure("Not logged in."); return; }

        StorageReference ref = storage.getReference()
                .child("avatars/" + user.getUid() + ".jpg");

        ref.putFile(uri)
                .addOnSuccessListener(snap -> ref.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            String url = downloadUri.toString();
                            db.collection("users").document(user.getUid())
                                    .update("avatarUrl", url)
                                    .addOnSuccessListener(v -> callback.onSuccess(url))
                                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                        })
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage())))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Save Booking → Firestore ──────────────────────────────────────────────
    public void saveBooking(String service, String date, String time,
                            BookingCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) { callback.onFailure("Not logged in."); return; }

        Map<String, Object> booking = new HashMap<>();
        booking.put("service",   service);
        booking.put("date",      date);
        booking.put("time",      time);
        booking.put("status",    "confirmed");
        booking.put("createdAt", new Timestamp(new Date()));

        db.collection("bookings")
                .document(user.getUid())
                .collection("appointments")
                .add(booking)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Callbacks ─────────────────────────────────────────────────────────────
    public interface SignUpCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface LoginCallback {
        void onSuccess(String name, String email, String phone, String avatarUrl);
        void onFailure(String error);
    }

    public interface PhotoUploadCallback {
        void onSuccess(String photoUrl);
        void onFailure(String error);
    }

    public interface BookingCallback {
        void onSuccess();
        void onFailure(String error);
    }
}
