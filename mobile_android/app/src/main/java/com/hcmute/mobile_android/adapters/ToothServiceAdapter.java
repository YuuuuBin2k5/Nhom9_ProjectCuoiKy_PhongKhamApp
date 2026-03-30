package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.ServiceItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying tooth-specific services in ToothServiceDialog
 */
public class ToothServiceAdapter extends RecyclerView.Adapter<ToothServiceAdapter.ViewHolder> {
    
    private List<ServiceItem> services;
    private OnServiceClickListener listener;
    
    public interface OnServiceClickListener {
        void onServiceClick(ServiceItem service);
    }
    
    public ToothServiceAdapter(List<ServiceItem> services, OnServiceClickListener listener) {
        this.services = services;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_tooth_service, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceItem service = services.get(position);
        holder.bind(service, listener);
    }
    
    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvServiceName;
        private TextView tvServicePrice;
        private MaterialButton btnSelectService;
        
        public ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            btnSelectService = itemView.findViewById(R.id.btnSelectService);
        }
        
        public void bind(ServiceItem service, OnServiceClickListener listener) {
            tvServiceName.setText(service.getName());
            
            // Format price
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            String priceText = nf.format((long)service.getPrice()) + " đ";
            tvServicePrice.setText(priceText);
            
            // Set click listener
            btnSelectService.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServiceClick(service);
                }
            });
        }
    }
}
