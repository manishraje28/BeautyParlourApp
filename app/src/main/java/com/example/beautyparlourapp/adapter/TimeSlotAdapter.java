package com.example.beautyparlourapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beautyparlourapp.R;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeViewHolder> {

    private List<String> timeList;
    private List<String> bookedTimes;
    private int selectedPosition = -1;
    private OnTimeSelectedListener listener;

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }

    public TimeSlotAdapter(List<String> timeList, List<String> bookedTimes, OnTimeSelectedListener listener) {
        this.timeList = timeList;
        this.bookedTimes = bookedTimes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_time, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        String time = timeList.get(position);
        holder.tvTime.setText(time);

        boolean isBooked = bookedTimes != null && bookedTimes.contains(time);

        if (isBooked) {
            holder.tvTime.setBackgroundResource(R.drawable.bg_time_slot_disabled);
            holder.tvTime.setTextColor(Color.parseColor("#BDBDBD"));
            holder.itemView.setOnClickListener(null); // Disabled clicked
            holder.itemView.setClickable(false);
        } else {
            if (selectedPosition == position) {
                holder.tvTime.setBackgroundResource(R.drawable.bg_time_slot_selected);
                holder.tvTime.setTextColor(Color.WHITE);
            } else {
                holder.tvTime.setBackgroundResource(R.drawable.bg_time_slot_available);
                holder.tvTime.setTextColor(Color.WHITE);
            }

            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v -> {
                if (selectedPosition != position) {
                    int oldPosition = selectedPosition;
                    selectedPosition = position;
                    notifyItemChanged(oldPosition);
                    notifyItemChanged(selectedPosition);
                    if (listener != null) {
                        listener.onTimeSelected(time);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    public void updateBookedTimes(List<String> newBookedTimes) {
        this.bookedTimes = newBookedTimes;
        this.selectedPosition = -1; // Reset selection on date change
        notifyDataSetChanged();
    }

    public String getSelectedTime() {
        if (selectedPosition != -1) {
            return timeList.get(selectedPosition);
        }
        return null;
    }

    static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}