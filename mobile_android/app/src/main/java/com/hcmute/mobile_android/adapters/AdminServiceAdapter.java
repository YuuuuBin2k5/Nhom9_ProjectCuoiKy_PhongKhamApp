package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.ServiceItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminServiceAdapter extends RecyclerView.Adapter<AdminServiceAdapter.ViewHolder> {

    private List<ServiceItem> serviceList;

    public AdminServiceAdapter(List<ServiceItem> serviceList) {
        this.serviceList = serviceList;
    }

    public void updateServices(List<ServiceItem> newServices) {
        this.serviceList.clear();
        this.serviceList.addAll(newServices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceItem service = serviceList.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDescription, tvPrice, tvDuration, tvCategory;
        private androidx.appcompat.widget.SwitchCompat switchActive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            switchActive = itemView.findViewById(R.id.switchActive);
        }

        public void bind(ServiceItem service) {
            tvName.setText(service.getName());
            tvDescription.setText(service.getDescription());
            
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvPrice.setText(formatter.format(service.getPrice()));
            
            tvDuration.setText(service.getDurationMinutes() + " phút");
            
            // Display category if available
            if (service.getCategoryName() != null && !service.getCategoryName().isEmpty()) {
                tvCategory.setText(service.getCategoryName());
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }
            
            // Set switch state (assuming service is active by default)
            switchActive.setChecked(true);
            
            // Handle switch toggle
            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // TODO: Implement API call to update service status
            });
        }
    }
}