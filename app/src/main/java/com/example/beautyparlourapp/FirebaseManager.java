package com.example.beautyparlourapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import com.example.beautyparlourapp.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Central helper for all Firebase operations:
 *   - Authentication  (sign up / login / logout)
 *   - Firestore       (save & fetch user profile, save bookings, fetch services, fetch offers)
 *   - Storage         (upload profile photo)
 */
public class FirebaseManager {

    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;

    private final FirebaseAuth     auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage  storage;

    private boolean defaultDataSeeded = false;
    private boolean isCloudinaryInitialized = false;

    private FirebaseManager() {
        auth    = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        seedDefaultDataIfNeeded();
    }

    public void initCloudinaryIfNeeded(Context context) {
        if (!isCloudinaryInitialized) {
            try {
                HashMap<String, String> config = new HashMap<>();
                config.put("cloud_name", "dp2uadpyh");
                MediaManager.init(context.getApplicationContext(), config);
                isCloudinaryInitialized = true;
                Log.d(TAG, "Cloudinary initialized successfully");
            } catch (Exception e) {
                isCloudinaryInitialized = true; // Likely already initialized
            }
        }
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    // ── Convenience getters ───────────────────────────────────────────────────
    public FirebaseUser getCurrentUser()  { return auth.getCurrentUser(); }
    public boolean      isLoggedIn()      { return auth.getCurrentUser() != null; }

    // ══════════════════════════════════════════════════════════════════════════
    // SEED DEFAULT DATA (services & offers) if Firestore collections are empty
    // ══════════════════════════════════════════════════════════════════════════

    private void seedDefaultDataIfNeeded() {
        if (defaultDataSeeded) return;
        defaultDataSeeded = true;

        // Seed services if empty
        db.collection("services").limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Log.d(TAG, "Seeding default services...");
                        seedDefaultServices();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking services: " + e.getMessage()));

        // Seed offers if empty
        db.collection("offers").limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Log.d(TAG, "Seeding default offers...");
                        seedDefaultOffers();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking offers: " + e.getMessage()));
    }

    private void seedDefaultServices() {
        String[][] services = {
                {"Haircut", "499", "45 mins", "Classic styling and finish", "Hair"},
                {"Facial", "899", "60 mins", "Deep cleansing and glowing skin treatment", "Skincare"},
                {"Bridal Makeup", "5999", "180 mins", "Complete bridal look by certified artists", "Makeup"},
                {"Hair Spa", "1299", "90 mins", "Nourishing spa for smooth, healthy hair", "Hair"},
                {"Waxing", "699", "30 mins", "Gentle waxing for clean and soft skin", "Hair Removal"}
        };

        for (String[] s : services) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", s[0]);
            data.put("price", Integer.parseInt(s[1]));
            data.put("duration", s[2]);
            data.put("description", s[3]);
            data.put("category", s[4]);
            db.collection("services").add(data)
                    .addOnSuccessListener(ref -> Log.d(TAG, "Service seeded: " + s[0]))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to seed service: " + e.getMessage()));
        }
    }

    private void seedDefaultOffers() {
        String[][] offers = {
                {"20% off Facials", "2026-12-31", "GLOW20", "20"},
                {"15% off Hair Spa", "2026-12-31", "RELAX15", "15"},
                {"Free Haircut with Spa", "2026-12-31", "COMBO50", "50"}
        };

        for (String[] o : offers) {
            Map<String, Object> data = new HashMap<>();
            data.put("title", o[0]);
            data.put("validTill", o[1]);
            data.put("code", o[2]);
            data.put("discount", Integer.parseInt(o[3]));
            db.collection("offers").add(data)
                    .addOnSuccessListener(ref -> Log.d(TAG, "Offer seeded: " + o[0]))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to seed offer: " + e.getMessage()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FETCH SERVICES (direct Firestore)
    // ══════════════════════════════════════════════════════════════════════════

    public void fetchServices(ServicesCallback callback) {
        db.collection("services").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Map<String, Object>> services = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> service = new HashMap<>();
                        service.put("id", doc.getId());
                        service.put("name", doc.getString("name"));
                        Object priceObj = doc.get("price");
                        int price = 0;
                        if (priceObj instanceof Long) price = ((Long) priceObj).intValue();
                        else if (priceObj instanceof Double) price = ((Double) priceObj).intValue();
                        service.put("price", price);
                        service.put("duration", doc.getString("duration"));
                        service.put("description", doc.getString("description"));
                        service.put("category", doc.getString("category"));
                        services.add(service);
                    }
                    callback.onSuccess(services);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FETCH OFFERS (direct Firestore)
    // ══════════════════════════════════════════════════════════════════════════

    public void fetchOffers(OffersCallback callback) {
        db.collection("offers").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Map<String, Object>> offers = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> offer = new HashMap<>();
                        offer.put("id", doc.getId());
                        offer.put("title", doc.getString("title"));
                        offer.put("validTill", doc.getString("validTill"));
                        offer.put("code", doc.getString("code"));
                        Object discountObj = doc.get("discount");
                        int discount = 0;
                        if (discountObj instanceof Long) discount = ((Long) discountObj).intValue();
                        else if (discountObj instanceof Double) discount = ((Double) discountObj).intValue();
                        offer.put("discount", discount);
                        offers.add(offer);
                    }
                    callback.onSuccess(offers);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Fetch all users ───────────────────────────────────────────────────────
    public void fetchUsers(UsersCallback callback) {
        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = new User(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("email"),
                                doc.getString("phone"),
                                doc.getString("avatarUrl"),
                                doc.getString("joinedDate")
                        );
                        users.add(user);
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CREATE BOOKING (direct Firestore — top-level bookings collection)
    // ══════════════════════════════════════════════════════════════════════════

    public void createBooking(String service, String date, String time,
                              BookingCallback callback) {
        String userId;
        if (isLoggedIn()) {
            userId = getCurrentUser().getUid();
        } else {
            userId = UUID.randomUUID().toString();
        }

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId",    userId);
        booking.put("service",   service);
        booking.put("date",      date);
        booking.put("time",      time);
        booking.put("status",    "confirmed");
        booking.put("createdAt", new Timestamp(new Date()));

        db.collection("bookings").add(booking)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "Booking created: " + ref.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface FetchBookingsCallback {
        void onSuccess(List<com.example.beautyparlourapp.models.Booking> bookings);
        void onFailure(String error);
    }

    public void fetchUserBookings(FetchBookingsCallback callback) {
        if (!isLoggedIn()) {
            callback.onFailure("User not logged in");
            return;
        }
        String uid = getCurrentUser().getUid();
        
        db.collection("bookings")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.beautyparlourapp.models.Booking> bookings = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        String service = doc.getString("service");
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        String status = doc.getString("status");
                        
                        // Combine date and time
                        String datetime = (date != null ? date : "") + " at " + (time != null ? time : "");
                        
                        bookings.add(new com.example.beautyparlourapp.models.Booking(
                            service != null ? service : "Unknown Service",
                            datetime,
                            status != null ? status : "Confirmed"
                        ));
                    }
                    callback.onSuccess(bookings);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

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
        user.put("userId",     uid); // Explicitly storing the UID as a field!
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

    public interface ProfileCallback {
        void onSuccess(String name, String email, String phone, String avatarUrl, List<String> styleJourneyUrls);
        void onFailure(String error);
    }

    public void fetchUserFromFirestore(String uid, ProfileCallback callback) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> journeyUrls = new ArrayList<>();
                        Object urlsObj = doc.get("styleJourneyUrls");
                        if (urlsObj instanceof List) {
                            for (Object item : (List<?>) urlsObj) {
                                if (item instanceof String) {
                                    journeyUrls.add((String) item);
                                }
                            }
                        }
                    
                        callback.onSuccess(
                                doc.getString("name"),
                                doc.getString("email"),
                                doc.getString("phone"),
                                doc.getString("avatarUrl"),
                                journeyUrls);
                    } else {
                        callback.onFailure("User profile not found in database.");
                    }
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

    // ── Upload Style Journey Media → Cloudinary, then arrayUnion → Firestore ────────────
    public void uploadStyleJourneyMedia(Context context, Uri uri, boolean isVideo, PhotoUploadCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) { callback.onFailure("Not logged in."); return; }

        initCloudinaryIfNeeded(context);

        String resourceType = isVideo ? "video" : "image";

        MediaManager.get().upload(uri)
                .unsigned("beautypalace")
                .option("resource_type", resourceType) // CRITICAL for videos
                .option("folder", "style_journey")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, java.util.Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        
                        // Cloudinary works! Save the URL precisely to Firestore
                        Map<String, Object> data = new HashMap<>();
                        data.put("styleJourneyUrls", com.google.firebase.firestore.FieldValue.arrayUnion(url));

                        db.collection("users").document(user.getUid())
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener(v -> callback.onSuccess(url))
                                .addOnFailureListener(e -> callback.onFailure("Firestore Save Error: " + e.getMessage()));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onFailure("Cloudinary Error: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                }).dispatch();
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    public void logout() {
        auth.signOut();
    }

    // ── Upload Profile Photo → Cloudinary, then save URL → Firestore ────────────
    public void uploadProfilePhoto(Context context, Uri uri, PhotoUploadCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) { callback.onFailure("Not logged in."); return; }

        initCloudinaryIfNeeded(context);

        MediaManager.get().upload(uri)
                .unsigned("beautypalace")
                .option("folder", "avatars")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, java.util.Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        
                        Map<String, Object> data = new HashMap<>();
                        data.put("avatarUrl", url);

                        db.collection("users").document(user.getUid())
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener(v -> callback.onSuccess(url))
                                .addOnFailureListener(e -> callback.onFailure("Database save error: " + e.getMessage()));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onFailure("Cloudinary Error: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                }).dispatch();
    }

    // ── Delete Style Journey Media from Firestore ────────────
    public void deleteStyleJourneyMedia(String url) {
        FirebaseUser user = getCurrentUser();
        if (user == null) return;
        
        db.collection("users").document(user.getUid())
                .update("styleJourneyUrls", com.google.firebase.firestore.FieldValue.arrayRemove(url))
                .addOnSuccessListener(v -> {
                     // Since it's unsigned cloudinary, we can't delete actual file without Backend-Secret
                     // So we just safely remove it from the User's displayed UI array list!
                });
    }

    // ── Save Booking → Firestore (legacy method kept for compatibility) ──────
    public void saveBooking(String service, String date, String time,
                            BookingCallback callback) {
        createBooking(service, date, time, callback);
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

    public interface ServicesCallback {
        void onSuccess(List<Map<String, Object>> services);
        void onFailure(String error);
    }

    public interface OffersCallback {
        void onSuccess(List<Map<String, Object>> offers);
        void onFailure(String error);
    }

    public interface UsersCallback {
        void onSuccess(List<User> users);
        void onFailure(String error);
    }
}
