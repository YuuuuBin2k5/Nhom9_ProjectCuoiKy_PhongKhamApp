package com.example.phongkham_app.patient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.TimeSlot;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private List<TimeSlot> timeSlots = new ArrayList<>();
    private final OnTimeSlotSelectedListener listener;

    public interface OnTimeSlotSelectedListener {
        void onTimeSlotSelected(int position);
    }

    public TimeSlotAdapter(OnTimeSlotSelectedListener listener) {
        this.listener = listener;
    }

    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        holder.bind(timeSlots.get(position), position);
    }

    @Override
    public int getItemCount() {
        return timeSlots != null ? timeSlots.size() : 0;
    }

    class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTimeSlot;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
        }

        public void bind(final TimeSlot timeSlot, final int position) {
            tvTimeSlot.setText(timeSlot.getTime());

            int defaultBg = R.drawable.bg_time_slot_default;
            int selectedBg = R.drawable.bg_time_slot_selected;

            int whiteColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
            int darkColor = ContextCompat.getColor(itemView.getContext(), R.color.text_primary);
            int grayColor = ContextCompat.getColor(itemView.getContext(), R.color.text_hint);

            if (!timeSlot.isAvailable()) {
                // Unavailable state
                tvTimeSlot.setBackgroundResource(defaultBg);
                tvTimeSlot.setTextColor(grayColor);
                itemView.setAlpha(0.5f);
                itemView.setOnClickListener(null); // blocking click
            } else {
                itemView.setAlpha(1.0f);
                if (timeSlot.isSelected()) {
                    // Selected state
                    tvTimeSlot.setBackgroundResource(selectedBg);
                    tvTimeSlot.setTextColor(whiteColor);
                } else {
                    // Available default state
                    tvTimeSlot.setBackgroundResource(defaultBg);
                    tvTimeSlot.setTextColor(darkColor);
                }
                
                itemView.setOnClickListener(v -> listener.onTimeSlotSelected(position));
            }
        }
    }
}
