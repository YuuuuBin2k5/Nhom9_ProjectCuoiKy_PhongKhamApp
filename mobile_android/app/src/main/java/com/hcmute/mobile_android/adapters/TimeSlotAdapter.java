package com.hcmute.mobile_android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.TimeSlot;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    
    private Context context;
    private List<TimeSlot> slots;
    private OnSlotClickListener listener;
    private TimeSlot selectedSlot;
    
    public interface OnSlotClickListener {
        void onSlotClick(TimeSlot slot);
    }
    
    public TimeSlotAdapter(Context context, List<TimeSlot> slots, OnSlotClickListener listener) {
        this.context = context;
        this.slots = slots;
        this.listener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TimeSlot slot = slots.get(position);
        
        holder.tvTime.setText(slot.getTime());
        
        if (!slot.isAvailable()) {
            holder.tvTime.setEnabled(false);
            holder.tvTime.setAlpha(0.5f);
            holder.tvTime.setBackgroundResource(R.drawable.bg_time_slot_available);
        } else if (slot.equals(selectedSlot)) {
            holder.tvTime.setEnabled(true);
            holder.tvTime.setAlpha(1.0f);
            holder.tvTime.setBackgroundResource(R.drawable.bg_time_slot_selected);
            holder.tvTime.setTextColor(Color.WHITE);
        } else {
            holder.tvTime.setEnabled(true);
            holder.tvTime.setAlpha(1.0f);
            holder.tvTime.setBackgroundResource(R.drawable.bg_time_slot_available);
            holder.tvTime.setTextColor(Color.BLACK);
        }
        
        holder.itemView.setOnClickListener(v -> listener.onSlotClick(slot));
    }
    
    @Override
    public int getItemCount() {
        return slots != null ? slots.size() : 0;
    }
    
    public void setSelectedSlot(TimeSlot slot) {
        this.selectedSlot = slot;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
