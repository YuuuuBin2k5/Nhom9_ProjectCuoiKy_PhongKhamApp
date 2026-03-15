package com.example.phongkham_app.ui.patient.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Service;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> services = new ArrayList<>();
    private final OnServiceSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnServiceSelectedListener {
        void onServiceSelected(Service service, int position);
    }

    public ServiceAdapter(OnServiceSelectedListener listener) {
        this.listener = listener;
    }

    public void setServices(List<Service> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    public void updateData(List<Service> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    public Service getSelectedService() {
        if (selectedPosition >= 0 && selectedPosition < services.size()) {
            return services.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.bind(service, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardService;
        private final ImageView ivIcon;
        private final TextView tvName, tvPrice;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardService = itemView.findViewById(R.id.cardService);
            ivIcon      = itemView.findViewById(R.id.ivServiceIcon);
            tvName      = itemView.findViewById(R.id.tvServiceName);
            tvPrice     = itemView.findViewById(R.id.tvServicePrice);
        }

        public void bind(Service service, boolean isSelected) {
            tvName.setText(service.getName());
            tvPrice.setText(service.getPrice());
            ivIcon.setImageResource(service.getIconResId());

            // Apply selected / unselected style
            if (isSelected) {
                cardService.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.primary_surface));
                cardService.setStrokeColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.primary));
                tvName.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.primary));
                ivIcon.setColorFilter(
                        ContextCompat.getColor(itemView.getContext(), R.color.primary));
            } else {
                cardService.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.white));
                cardService.setStrokeColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.divider));
                tvName.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                ivIcon.setColorFilter(
                        ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }

            // Click
            itemView.setOnClickListener(v -> {
                int oldPosition = selectedPosition;
                selectedPosition = getAdapterPosition();
                if (oldPosition >= 0) notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);

                // Bounce animation
                v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                        .start();

                listener.onServiceSelected(service, selectedPosition);
            });
        }
    }
}
