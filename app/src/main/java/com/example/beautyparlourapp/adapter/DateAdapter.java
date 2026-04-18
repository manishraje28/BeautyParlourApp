package com.example.beautyparlourapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beautyparlourapp.R;
import com.example.beautyparlourapp.models.DateItem;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<DateItem> dateList;
    private int selectedPosition = -1; // -1 means none selected initially
    private OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(DateItem dateItem);
    }

    public DateAdapter(List<DateItem> dateList, OnDateSelectedListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateItem dateItem = dateList.get(position);
        holder.tvDayName.setText(dateItem.getDayName());
        holder.tvDateNumber.setText(dateItem.getDayNumber());

        if (selectedPosition == position) {
            holder.viewIndicator.setVisibility(View.VISIBLE);
            holder.tvDateNumber.setTextColor(Color.parseColor("#7A5E35"));
            holder.tvDayName.setTextColor(Color.parseColor("#7A5E35"));
            holder.tvDateNumber.setTextSize(20);
        } else {
            holder.viewIndicator.setVisibility(View.INVISIBLE);
            holder.tvDateNumber.setTextColor(Color.parseColor("#333333"));
            holder.tvDayName.setTextColor(Color.parseColor("#888888"));
            holder.tvDateNumber.setTextSize(18);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != position) {
                int oldPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onDateSelected(dateItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public void selectInitial() {
        if (!dateList.isEmpty() && selectedPosition == -1) {
            selectedPosition = 0;
            notifyItemChanged(0);
            if (listener != null) {
                listener.onDateSelected(dateList.get(0));
            }
        }
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName;
        TextView tvDateNumber;
        View viewIndicator;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvDateNumber = itemView.findViewById(R.id.tv_date_number);
            viewIndicator = itemView.findViewById(R.id.view_indicator);
        }
    }
}