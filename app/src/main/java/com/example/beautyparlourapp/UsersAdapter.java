package com.example.beautyparlourapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beautyparlourapp.model.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> users;
    private Context context;

    public UsersAdapter(List<User> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView name;
        private TextView email;
        private TextView phone;
        private TextView joinedDate;

        public UserViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.user_avatar);
            name = itemView.findViewById(R.id.user_name);
            email = itemView.findViewById(R.id.user_email);
            phone = itemView.findViewById(R.id.user_phone);
            joinedDate = itemView.findViewById(R.id.user_joined_date);
        }

        public void bind(User user) {
            name.setText(user.getName());
            email.setText(user.getEmail());
            phone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
            joinedDate.setText("Joined: " + (user.getJoinedDate() != null ? user.getJoinedDate().substring(0, 10) : "N/A"));

            // Load avatar with Glide
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getAvatarUrl())
                        .circleCrop()
                        .placeholder(R.drawable.bg_profile_placeholder)
                        .into(avatar);
            } else {
                avatar.setBackgroundResource(R.drawable.bg_profile_placeholder);
            }
        }
    }
}
