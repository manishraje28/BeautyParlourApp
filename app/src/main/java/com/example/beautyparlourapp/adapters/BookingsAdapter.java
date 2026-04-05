package com.example.beautyparlourapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beautyparlourapp.R;
import com.example.beautyparlourapp.models.Booking;

import java.util.List;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.ViewHolder> {

    private final Context context;
    private List<Booking> bookingList;

    public BookingsAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    public void setBookingList(List<Booking> bookingList) {
        this.bookingList = bookingList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.tvServiceName.setText(booking.getServiceName());
        holder.tvDatetime.setText(booking.getDatetime());
        holder.tvStatusBadge.setText(booking.getStatus());

        // Update badge color based on status
        GradientDrawable badgeDrawable = (GradientDrawable) holder.tvStatusBadge.getBackground().mutate();
        switch (booking.getStatus().toLowerCase()) {
            case "confirmed":
                badgeDrawable.setColor(Color.parseColor("#2E7D32")); // Green
                break;
            case "pending":
                badgeDrawable.setColor(Color.parseColor("#F57C00")); // Orange/Amber
                break;
            case "completed":
                badgeDrawable.setColor(Color.parseColor("#1565C0")); // Blue
                break;
            case "cancelled":
                badgeDrawable.setColor(Color.parseColor("#C62828")); // Red
                break;
            default:
                badgeDrawable.setColor(Color.parseColor("#757575")); // Gray
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bookingList == null ? 0 : bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvServiceName;
        public TextView tvDatetime;
        public TextView tvStatusBadge;
        public ImageView ivServiceIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvDatetime = itemView.findViewById(R.id.tv_service_datetime);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            ivServiceIcon = itemView.findViewById(R.id.iv_service_icon);
        }
    }
}
