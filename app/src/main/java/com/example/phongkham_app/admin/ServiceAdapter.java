package com.example.phongkham_app.admin;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Service;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> services;
    private OnServiceToggleListener listener;
    private OnServiceClickListener clickListener;

    public interface OnServiceToggleListener {
        void onToggle(Service service, boolean isEnabled);
    }

    public interface OnServiceClickListener {
        void onClick(Service service);
    }

    public ServiceAdapter(List<Service> services, OnServiceToggleListener listener) {
        this.services = services;
        this.listener = listener;
    }

    public void updateData(List<Service> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    public void setOnServiceClickListener(OnServiceClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvServicePrice;
        SwitchCompat switchService;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            switchService = itemView.findViewById(R.id.switchService);
        }

        public void bind(Service service) {
            tvServiceName.setText(service.getName());
            tvServicePrice.setText(service.getPrice());
            switchService.setChecked(service.isEnabled());

            switchService.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggle(service, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onClick(service);
                }
            });
        }
    }
}
