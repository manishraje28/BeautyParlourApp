package com.example.beautyparlourapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.beautyparlourapp.R;

import java.util.List;
import java.util.Map;

public class BookingServiceAdapter extends RecyclerView.Adapter<BookingServiceAdapter.ServiceViewHolder> {

    private List<Map<String, Object>> serviceList;
    private int selectedPosition = -1;
    private OnServiceSelectedListener listener;

    public interface OnServiceSelectedListener {
        void onServiceSelected(Map<String, Object> service);
    }

    public BookingServiceAdapter(List<Map<String, Object>> serviceList, OnServiceSelectedListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Map<String, Object> service = serviceList.get(position);
        
        String name = (String) service.get("name");
        holder.tvServiceName.setText(name != null ? name : "Service");
        
        Object priceObj = service.get("price");
        if (priceObj != null) {
            holder.tvServicePrice.setText("₹" + priceObj.toString());
        } else {
            holder.tvServicePrice.setText("₹0");
        }

        String imageUrl = (String) service.get("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.ivServiceImage);
        } else {
            holder.ivServiceImage.setImageResource(R.drawable.salon_logo);
        }

        if (selectedPosition == position) {
            holder.viewBorder.setBackgroundResource(R.drawable.bg_service_card_selected);
        } else {
            holder.viewBorder.setBackgroundResource(android.R.color.transparent);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != position) {
                int oldPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onServiceSelected(service);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public void updateData(List<Map<String, Object>> newData) {
        this.serviceList = newData;
        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivServiceImage;
        TextView tvServiceName;
        TextView tvServicePrice;
        View viewBorder;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceImage = itemView.findViewById(R.id.iv_service_image);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvServicePrice = itemView.findViewById(R.id.tv_service_price);
            viewBorder = itemView.findViewById(R.id.view_service_border);
        }
    }
}