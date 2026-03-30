package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.MessageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDoctorAdapter extends RecyclerView.Adapter<AdminDoctorAdapter.ViewHolder> {

    public interface OnDoctorActionListener {
        void onEditDoctor(DoctorItem doctor);
        void onDeleteDoctor(DoctorItem doctor);
    }

    private List<DoctorItem> doctorList;
    private ApiService apiService;
    private OnDoctorActionListener listener;

    public AdminDoctorAdapter(List<DoctorItem> doctorList, OnDoctorActionListener listener) {
        this.doctorList = doctorList;
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
        private TextView tvName, tvSpecialty, tvRoom;
        private ImageView ivAvatar, ivMenu;
        private MaterialButton btnActive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            ivAvatar = itemView.findViewById(R.id.ivDoctorAvatar);
            ivMenu = itemView.findViewById(R.id.ivMenu);
            btnActive = itemView.findViewById(R.id.btnActive);
            
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
            updateActiveButton(doctor.isActive());

            btnActive.setOnClickListener(v -> toggleDoctorStatus(doctor));

            // Setup menu button
            if (ivMenu != null) {
                ivMenu.setOnClickListener(v -> showContextMenu(v, doctor, listener));
            }
        }

        private void showContextMenu(View anchor, DoctorItem doctor, OnDoctorActionListener listener) {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(itemView.getContext(), anchor);
            popup.getMenuInflater().inflate(R.menu.menu_admin_doctor, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) listener.onEditDoctor(doctor);
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) listener.onDeleteDoctor(doctor);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }

        private void updateActiveButton(boolean isActive) {
            if (isActive) {
                btnActive.setText("Hoạt động");
                btnActive.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                btnActive.setText("Chặn");
                btnActive.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
            }
        }

        private void toggleDoctorStatus(DoctorItem doctor) {
            boolean newStatus = !doctor.isActive();
            btnActive.setEnabled(false);
            
            apiService.updateDoctorStatus(doctor.getId(), newStatus).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    btnActive.setEnabled(true);
                    if (response.isSuccessful()) {
                        doctor.setActive(newStatus);
                        updateActiveButton(newStatus);
                        Toast.makeText(itemView.getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(itemView.getContext(), "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    btnActive.setEnabled(true);
                    Toast.makeText(itemView.getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}