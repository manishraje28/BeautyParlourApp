package com.example.beautyparlourapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beautyparlourapp.FirebaseManager;
import com.example.beautyparlourapp.R;

import java.util.List;
import java.util.Map;

public class AdminServicesAdapter extends RecyclerView.Adapter<AdminServicesAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, Object>> servicesList;

    public AdminServicesAdapter(Context context, List<Map<String, Object>> servicesList) {
        this.context = context;
        this.servicesList = servicesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> service = servicesList.get(position);
        
        String id = (String) service.get("id");
        String name = (String) service.get("name");
        int price = service.get("price") != null ? (int) service.get("price") : 0;
        String duration = (String) service.get("duration");
        String description = (String) service.get("description");
        String category = (String) service.get("category");
        String imageUrl = (String) service.get("imageUrl");

        holder.tvServiceTitle.setText(name);
        holder.tvServicePrice.setText("₹" + price);
        holder.tvServiceDuration.setText(duration);
        holder.tvServiceDesc.setText(description);
        
        if (category != null && !category.isEmpty()) {
            holder.tvCategory.setText(category.toUpperCase());
        } else {
            holder.tvCategory.setText("GENERAL");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .into(holder.ivServiceImage);
        } else {
            holder.ivServiceImage.setImageResource(R.drawable.bg_salon_photo);
        }

        holder.btnDeleteService.setOnClickListener(v -> deleteService(id, position));
        
        // Disable edit for now or implement if requested. The prompt only focuses on Add functionality
        holder.btnEditService.setOnClickListener(v -> 
            Toast.makeText(context, "Edit soon! Remove and Add again for now.", Toast.LENGTH_SHORT).show()
        );
    }

    private void deleteService(String serviceId, int position) {
        FirebaseManager.getInstance().deleteService(serviceId, new FirebaseManager.ServiceActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Service deleted!", Toast.LENGTH_SHORT).show();
                servicesList.remove(position);
                notifyItemRemoved(position);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(context, "Failed to delete: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return servicesList.size();
    }

    public void updateServices(List<Map<String, Object>> newServices) {
        this.servicesList.clear();
        this.servicesList.addAll(newServices);
        notifyDataSetDataSetChanged();
    }
    
    private void notifyDataSetDataSetChanged() {
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivServiceImage, btnEditService, btnDeleteService;
        TextView tvCategory, tvServiceTitle, tvServiceDesc, tvServicePrice, tvServiceDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceImage = itemView.findViewById(R.id.iv_service_image);
            btnEditService = itemView.findViewById(R.id.btn_edit_service);
            btnDeleteService = itemView.findViewById(R.id.btn_delete_service);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvServiceTitle = itemView.findViewById(R.id.tv_service_title);
            tvServiceDesc = itemView.findViewById(R.id.tv_service_desc);
            tvServicePrice = itemView.findViewById(R.id.tv_service_price);
            tvServiceDuration = itemView.findViewById(R.id.tv_service_duration);
        }
    }
}