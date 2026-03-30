package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.ServiceItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminServiceAdapter extends RecyclerView.Adapter<AdminServiceAdapter.ViewHolder> {

    public interface OnServiceActionListener {
        void onEditService(ServiceItem service);
        void onDeleteService(ServiceItem service);
    }

    private List<ServiceItem> serviceList;
    private OnServiceActionListener listener;

    public AdminServiceAdapter(List<ServiceItem> serviceList, OnServiceActionListener listener) {
        this.serviceList = new java.util.ArrayList<>(serviceList);
        this.listener = listener;
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
        holder.bind(service, listener);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDescription, tvPrice, tvDuration, tvCategory;
        private android.widget.ImageView ivService, ivMenu;
        private androidx.appcompat.widget.SwitchCompat switchActive;
        private ApiService apiService;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            apiService = RetrofitClient.getApiService(itemView.getContext());
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            ivService = itemView.findViewById(R.id.ivService);
            ivMenu = itemView.findViewById(R.id.ivMenu);
            switchActive = itemView.findViewById(R.id.switchActive);
        }

        public void bind(ServiceItem service, OnServiceActionListener listener) {
            tvName.setText(service.getName());
            if (tvDescription != null) tvDescription.setText(service.getDescription());
            
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String priceFormatted = formatter.format(service.getPrice());
            tvPrice.setText(priceFormatted.replace("₫", "đ"));
            
            tvDuration.setText(service.getDurationMinutes() + " phút");
            
            if (tvCategory != null) {
                if (service.getCategoryName() != null && !service.getCategoryName().isEmpty()) {
                    tvCategory.setText(service.getCategoryName());
                    tvCategory.setVisibility(View.VISIBLE);
                } else {
                    tvCategory.setVisibility(View.GONE);
                }
            }

            if (ivService != null) {
                if (service.getImageUrls() != null && !service.getImageUrls().isEmpty()) {
                    com.bumptech.glide.Glide.with(itemView.getContext())
                            .load(service.getImageUrls().get(0))
                            .placeholder(R.drawable.background)
                            .error(R.drawable.background)
                            .into(ivService);
                } else {
                    ivService.setImageResource(R.drawable.background);
                }
            }
            
            // Set switch state silently
            switchActive.setOnCheckedChangeListener(null);
            switchActive.setChecked(service.isActive());
            
            setupSwitchListener(service);

            // Setup menu button
            if (ivMenu != null) {
                ivMenu.setOnClickListener(v -> showContextMenu(v, service, listener));
            }

            itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), 
                        com.hcmute.mobile_android.ui.activities.AdminServiceDetailActivity.class);
                intent.putExtra("id", service.getId());
                intent.putExtra("name", service.getName());
                intent.putExtra("price", service.getPrice());
                intent.putExtra("description", service.getDescription());
                intent.putExtra("duration", service.getDurationMinutes() != null ? service.getDurationMinutes() : 0);
                intent.putExtra("category", service.getCategoryName());
                intent.putExtra("active", switchActive.isChecked());
                
                if (service.getImageUrls() != null) {
                    intent.putStringArrayListExtra("imageUrls", new java.util.ArrayList<>(service.getImageUrls()));
                }
                
                v.getContext().startActivity(intent);
            });
        }

        private void showContextMenu(View anchor, ServiceItem service, OnServiceActionListener listener) {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(itemView.getContext(), anchor);
            popup.getMenuInflater().inflate(R.menu.menu_admin_service, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) listener.onEditService(service);
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) listener.onDeleteService(service);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }

        private void setupSwitchListener(ServiceItem service) {
            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                apiService.updateServiceStatus(service.getId(), isChecked).enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                        if (!response.isSuccessful()) {
                            revertSwitch(service, !isChecked);
                            Toast.makeText(itemView.getContext(), "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        revertSwitch(service, !isChecked);
                        Toast.makeText(itemView.getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        private void revertSwitch(ServiceItem service, boolean targetState) {
            switchActive.setOnCheckedChangeListener(null);
            switchActive.setChecked(targetState);
            setupSwitchListener(service);
        }
    }
}