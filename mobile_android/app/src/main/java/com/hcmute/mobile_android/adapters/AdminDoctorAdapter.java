package com.hcmute.mobile_android.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.ui.activities.DoctorDetailActivity;

import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDoctorAdapter extends RecyclerView.Adapter<AdminDoctorAdapter.ViewHolder> implements Filterable {

    public interface OnDoctorActionListener {
        void onEditDoctor(DoctorItem doctor);
        void onDeleteDoctor(DoctorItem doctor);
    }

    private List<DoctorItem> doctorList;
    private List<DoctorItem> doctorListFull;
    private ApiService apiService;
    private OnDoctorActionListener listener;

    public AdminDoctorAdapter(List<DoctorItem> doctorList, OnDoctorActionListener listener) {
        this.doctorList = doctorList;
        this.doctorListFull = new ArrayList<>(doctorList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorItem doctor = doctorList.get(position);
        holder.bind(doctor, listener);
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvSpecialty, tvRoom, tvStatus;
        private ImageView ivAvatar, ivMenu;
        private MaterialCardView cardStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            ivAvatar = itemView.findViewById(R.id.ivDoctorAvatar);
            ivMenu = itemView.findViewById(R.id.ivMenu);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cardStatus = itemView.findViewById(R.id.cardStatus);
            
            if (apiService == null) {
                apiService = RetrofitClient.getApiService(itemView.getContext());
            }
        }

        public void bind(DoctorItem doctor, OnDoctorActionListener listener) {
            tvName.setText(doctor.getFullName());
            tvSpecialty.setText(doctor.getSpecialization());
            
            // Display room name
            if (tvRoom != null) {
                if (doctor.getRoomName() != null && !doctor.getRoomName().isEmpty()) {
                    tvRoom.setText("Phòng: " + doctor.getRoomName());
                    tvRoom.setVisibility(View.VISIBLE);
                } else {
                    tvRoom.setVisibility(View.GONE);
                }
            }
            
            // Load Avatar
            Glide.with(itemView.getContext())
                .load(doctor.getAvatarUrl())
                .placeholder(R.drawable.ic_doctor)
                .error(R.drawable.ic_doctor)
                .into(ivAvatar);

            // Set Active Status
            updateStatusBadge(doctor.isActive());

            // Click anywhere else to view profile
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), DoctorDetailActivity.class);
                intent.putExtra("doctorId", doctor.getId());
                intent.putExtra("doctorName", doctor.getFullName());
                intent.putExtra("specialization", doctor.getSpecialization());
                itemView.getContext().startActivity(intent);
            });

            // Setup menu button
            if (ivMenu != null) {
                ivMenu.setOnClickListener(v -> showContextMenu(v, doctor, listener));
            }
        }

        private void showContextMenu(View anchor, DoctorItem doctor, OnDoctorActionListener listener) {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(itemView.getContext(), anchor);
            popup.getMenuInflater().inflate(R.menu.menu_admin_doctor, popup.getMenu());
            
            // Set dynamic title for status toggle
            if (popup.getMenu().findItem(R.id.action_status_toggle) != null) {
                popup.getMenu().findItem(R.id.action_status_toggle)
                        .setTitle(doctor.isActive() ? "Chặn hoạt động" : "Kích hoạt lại");
            }

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) listener.onEditDoctor(doctor);
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) listener.onDeleteDoctor(doctor);
                    return true;
                } else if (id == R.id.action_status_toggle) {
                    toggleDoctorStatus(doctor);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }

        private void updateStatusBadge(boolean isActive) {
            if (isActive) {
                tvStatus.setText("Hoạt động");
                tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                cardStatus.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                cardStatus.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
            } else {
                tvStatus.setText("Bị chặn");
                tvStatus.setTextColor(Color.parseColor("#E51C23"));
                cardStatus.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
                cardStatus.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            }
        }

        private void toggleDoctorStatus(DoctorItem doctor) {
            boolean newStatus = !doctor.isActive();
            
            apiService.updateDoctorStatus(doctor.getId(), newStatus).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    if (response.isSuccessful()) {
                        doctor.setActive(newStatus);
                        updateStatusBadge(newStatus);
                        Toast.makeText(itemView.getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(itemView.getContext(), "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    Toast.makeText(itemView.getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void updateList(List<DoctorItem> newList) {
        this.doctorListFull = new ArrayList<>(newList);
        this.doctorList = newList;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<DoctorItem> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(doctorListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (DoctorItem item : doctorListFull) {
                        if (item.getFullName().toLowerCase().contains(filterPattern) || 
                            (item.getSpecialization() != null && item.getSpecialization().toLowerCase().contains(filterPattern))) {
                            filteredList.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                doctorList = (List<DoctorItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}