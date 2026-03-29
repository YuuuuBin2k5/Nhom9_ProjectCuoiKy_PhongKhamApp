package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.ServiceItem;

import java.util.List;

public class PatientServiceAdapter extends RecyclerView.Adapter<PatientServiceAdapter.ViewHolder> {
    private List<ServiceItem> services;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ServiceItem service);
    }

    public PatientServiceAdapter(List<ServiceItem> services, OnItemClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_list_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceItem item = services.get(position);
        holder.tvName.setText(item.getName() != null ? item.getName() : "Không tên");
        
        // Format price
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        String priceFormatted = formatter.format(item.getPrice());
        holder.tvPrice.setText(priceFormatted.replace("₫", "đ"));
        
        // Set duration
        int dur = item.getDurationMinutes() != null ? item.getDurationMinutes() : 0;
        holder.tvDuration.setText(dur + " phút");
        
        // Load image
        if (holder.ivService != null) {
            if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(item.getImageUrls().get(0))
                        .placeholder(R.drawable.background)
                        .error(R.drawable.background)
                        .into(holder.ivService);
            } else {
                holder.ivService.setImageResource(R.drawable.background);
            }
        }
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return services.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvDuration;
        android.widget.ImageView ivService;
        
        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvServiceName);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvDuration = view.findViewById(R.id.tvDuration);
            ivService = view.findViewById(R.id.ivService);
        }
    }
}
