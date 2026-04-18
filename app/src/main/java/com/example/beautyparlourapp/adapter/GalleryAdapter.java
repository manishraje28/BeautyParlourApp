package com.example.beautyparlourapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.beautyparlourapp.R;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> images;
    private final boolean isAdmin;
    private final OnGalleryClickListener listener;
    private final Random random;

    public interface OnGalleryClickListener {
        void onDeleteClick(String id);
    }

    public GalleryAdapter(Context context, List<Map<String, Object>> images, boolean isAdmin, OnGalleryClickListener listener) {
        this.context = context;
        this.images = images;
        this.isAdmin = isAdmin;
        this.listener = listener;
        this.random = new Random();
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery_image, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        Map<String, Object> imgData = images.get(position);
        String url = (String) imgData.get("imageUrl");
        String id = (String) imgData.get("id");

        // Let the image's natural aspect ratio determine the height
        LayoutParams params = holder.ivImage.getLayoutParams();
        params.height = LayoutParams.WRAP_CONTENT;
        holder.ivImage.setLayoutParams(params);

        Glide.with(context)
             .load(url)
             .apply(new RequestOptions()
                    .placeholder(new ColorDrawable(Color.parseColor("#E0E0E0")))
                    .error(new ColorDrawable(Color.parseColor("#CCCCCC"))))
             .into(holder.ivImage);

        if (isAdmin && listener != null) {
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(id));
        } else {
            holder.ivDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivDelete;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_gallery_image);
            ivDelete = itemView.findViewById(R.id.iv_delete_gallery);
        }
    }
}