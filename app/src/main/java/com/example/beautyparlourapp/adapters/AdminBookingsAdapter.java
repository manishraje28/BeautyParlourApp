package com.example.beautyparlourapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beautyparlourapp.FirebaseManager;
import com.example.beautyparlourapp.R;
import com.example.beautyparlourapp.models.Booking;
import com.example.beautyparlourapp.network.FcmRetrofitClient;
import com.example.beautyparlourapp.network.AccessTokenHelper;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class AdminBookingsAdapter extends RecyclerView.Adapter<AdminBookingsAdapter.ViewHolder> {

    private final Context context;
    private List<Booking> bookingList;

    public AdminBookingsAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    public void setBookingList(List<Booking> list) {
        this.bookingList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        String name = booking.getUserName() != null ? booking.getUserName() : "Guest User";
        holder.tvUserName.setText(name);

        // Reset image state
        holder.ivProfilePic.setVisibility(View.GONE);
        holder.tvInitials.setVisibility(View.VISIBLE);

        // Calculate initials as placeholder
        String initials = "";
        String[] parts = name.split(" ");
        if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].charAt(0);
        if (parts.length > 1 && !parts[1].isEmpty()) initials += parts[1].charAt(0);
        holder.tvInitials.setText(initials.toUpperCase());

        // Assign a tag to prevent recycled view bleeding
        holder.itemView.setTag(booking.getId());

        // Fetch actual user details
        if (booking.getUserId() != null && !booking.getUserId().isEmpty()) {
            FirebaseManager.getInstance().fetchUserFromFirestore(booking.getUserId(), new FirebaseManager.ProfileCallback() {
                @Override
                public void onSuccess(String fetchedName, String email, String phone, String avatarUrl, List<String> styleJourneyUrls, String role) {
                    if (!holder.itemView.getTag().equals(booking.getId())) return;
                    
                    if (fetchedName != null && !fetchedName.isEmpty()) {
                        holder.tvUserName.setText(fetchedName);
                        
                        // Recalculate initials in case name updated
                        String newInitials = "";
                        String[] newParts = fetchedName.split(" ");
                        if (newParts.length > 0 && !newParts[0].isEmpty()) newInitials += newParts[0].charAt(0);
                        if (newParts.length > 1 && !newParts[1].isEmpty()) newInitials += newParts[1].charAt(0);
                        holder.tvInitials.setText(newInitials.toUpperCase());
                    }

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        holder.tvInitials.setVisibility(View.GONE);
                        holder.ivProfilePic.setVisibility(View.VISIBLE);
                        com.bumptech.glide.Glide.with(context)
                                .load(avatarUrl)
                                .circleCrop()
                                .into(holder.ivProfilePic);
                    }
                }

                @Override
                public void onFailure(String error) {
                    // Fallback to existing placeholder
                }
            });
        }

        holder.tvServiceName.setText(booking.getServiceName());
        holder.tvStatusPill.setText(booking.getStatus().toUpperCase());

        // Split datetime roughly (for demo handling)
        String dt = booking.getDatetime();
        String dateOnly = dt;
        String timeOnly = "";
        if (dt != null && dt.contains(" at ")) {
            String[] dtParts = dt.split(" at ");
            dateOnly = dtParts[0];
            timeOnly = dtParts.length > 1 ? dtParts[1] : "";
        }
        holder.tvDateOnly.setText(dateOnly);
        holder.tvTimeOnly.setText(timeOnly);
        
        // Show actual price from booking
        if (booking.getPrice() > 0) {
            holder.tvPrice.setText(String.format("$%.2f", booking.getPrice()));
        } else {
            holder.tvPrice.setText("Price TBD"); // Fallback if no price
        }

        // Hide buttons if not pending
        if ("pending".equalsIgnoreCase(booking.getStatus())) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.tvStatusPill.setTextColor(Color.parseColor("#D81B60"));
            
            // Set dynamic background color for pending pill
            GradientDrawable bg = (GradientDrawable) holder.tvStatusPill.getBackground().mutate();
            bg.setColor(Color.parseColor("#FDE8E9"));
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            
            GradientDrawable bg = (GradientDrawable) holder.tvStatusPill.getBackground().mutate();
            if ("confirmed".equalsIgnoreCase(booking.getStatus())) {
                holder.tvStatusPill.setTextColor(Color.parseColor("#2E7D32"));
                bg.setColor(Color.parseColor("#E8F5E9"));
            } else {
                holder.tvStatusPill.setTextColor(Color.parseColor("#757575"));
                bg.setColor(Color.parseColor("#EEEEEE"));
            }
        }

        holder.btnApprove.setOnClickListener(v -> updateStatus(booking, "confirmed", position));
        holder.btnReject.setOnClickListener(v -> updateStatus(booking, "cancelled", position));
    }

    private void updateStatus(Booking booking, String newStatus, int position) {
        FirebaseManager.getInstance().updateBookingStatus(booking.getId(), newStatus, new FirebaseManager.BookingCallback() {
            @Override
            public void onSuccess() {
                booking.setStatus(newStatus);
                notifyItemChanged(position);
                Toast.makeText(context, "Booking " + newStatus, Toast.LENGTH_SHORT).show();

                if ("confirmed".equals(newStatus) && booking.getUserId() != null) {
                    sendPushNotification(booking.getUserId(), 
                            "Appointment Confirmed!", 
                            "Your " + booking.getServiceName() + " appointment is confirmed!");
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(context, "Failed to update: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPushNotification(String userId, String title, String body) {
        // Fetch the user's FCM token from Firestore first
        FirebaseManager.getInstance().fetchUserFromFirestore(userId, new FirebaseManager.ProfileCallback() {
            @Override
            public void onSuccess(String name, String email, String phone, String avatarUrl, List<String> styleJourneyUrls, String role) {
                // In a real app, you'd need a separate callback or way to retrieve the fcmToken.
                // For simplicity, assuming the backend or our FirebaseManager could return it,
                // or we do a quick direct fetch here:
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists() && documentSnapshot.contains("fcmToken")) {
                                String token = documentSnapshot.getString("fcmToken");
                                sendTokenToFcm(token, title, body);
                            }
                        });
            }

            @Override
            public void onFailure(String error) { }
        });
    }

    private void sendTokenToFcm(String token, String title, String body) {
        new Thread(() -> {
            String accessToken = AccessTokenHelper.getAccessToken(context);
            if (accessToken == null) return;

            JsonObject messageObj = new JsonObject();
            messageObj.addProperty("token", token);
            
            JsonObject notificationObj = new JsonObject();
            notificationObj.addProperty("title", title);
            notificationObj.addProperty("body", body);
            
            messageObj.add("notification", notificationObj);

            JsonObject rootPayload = new JsonObject();
            rootPayload.add("message", messageObj);

            String bearerValue = "Bearer " + accessToken;
            FcmRetrofitClient.getInstance().getApi().sendNotification(bearerValue, rootPayload)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                android.util.Log.d("FCM", "Notification sent securely using V1 API!");
                            } else {
                                android.util.Log.e("FCM", "Failed V1 API: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            android.util.Log.e("FCM", "V1 Network Error", t);
                        }
                    });
        }).start();
    }

    @Override
    public int getItemCount() {
        return bookingList == null ? 0 : bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvUserName, tvServiceName, tvStatusPill;
        TextView tvDateOnly, tvTimeOnly, tvPrice, btnReject;
        Button btnApprove;
        android.widget.ImageView ivProfilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_initials);
            ivProfilePic = itemView.findViewById(R.id.iv_profile_pic);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvStatusPill = itemView.findViewById(R.id.tv_status_pill);
            tvDateOnly = itemView.findViewById(R.id.tv_date_only);
            tvTimeOnly = itemView.findViewById(R.id.tv_time_only);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}
