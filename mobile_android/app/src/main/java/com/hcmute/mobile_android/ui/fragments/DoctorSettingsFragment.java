package com.hcmute.mobile_android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorProfileResponse;
import com.hcmute.mobile_android.ui.activities.DoctorProfileActivity;
import com.hcmute.mobile_android.ui.activities.LoginActivity;
import com.hcmute.mobile_android.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorSettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupProfile(view);
        setupMenus(view);
    }

    private void setupProfile(View view) {
        TextView tvDoctorName = view.findViewById(R.id.tvDoctorName);
        TokenManager tm = new TokenManager(requireContext());
        String name = tm.getUserName();
        if (name != null && !name.isEmpty()) {
            tvDoctorName.setText(name);
        }

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), DoctorProfileActivity.class));
        });

        SwitchMaterial switchReady = view.findViewById(R.id.switchReady);
        switchReady.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Sẵn sàng nhận bệnh" : "Đang bận / Vắng mặt";
            Toast.makeText(getContext(), "Trạng thái: " + status, Toast.LENGTH_SHORT).show();
        });

        // Load doctor profile dynamic data
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getDoctorProfile().enqueue(new Callback<DoctorProfileResponse>() {
            @Override
            public void onResponse(Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                DoctorProfileResponse p = response.body();
                tvDoctorName.setText(p.getDisplayName());
                TextView tvDoctorId = view.findViewById(R.id.tvDoctorId);
                if (tvDoctorId != null) {
                    String bio = p.getBiography() != null ? p.getBiography().trim() : "";
                    if (!bio.isEmpty()) {
                        tvDoctorId.setText(bio.length() > 60 ? bio.substring(0, 60) + "..." : bio);
                    } else {
                        tvDoctorId.setText("Mã BS: " + (p.getId() != null ? p.getId() : ""));
                    }
                }
            }

            @Override
            public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {}
        });
    }

    private void setupMenus(View view) {
        // Giữ lại các chức năng cần thiết cho bác sĩ
        setupRow(view, R.id.rowPrescription, R.drawable.ic_medical_services, "Mẫu Đơn thuốc nhanh", "Quản lý mẫu kê đơn", null);
        setupRow(view, R.id.rowSchedule, R.drawable.ic_calendar, "Đăng ký ca làm việc", null, null);
        setupRow(view, R.id.rowLeaveTime, R.drawable.ic_schedule, "Nghỉ phép", null, null);

        setupSwitch(view, R.id.rowPush, R.drawable.ic_notification, "Thông báo Push", true);
        setupSwitch(view, R.id.rowReminder, R.drawable.ic_calendar, "Nhắc lịch hẹn", true);

        setupRow(view, R.id.rowSupport, R.drawable.ic_user, "Hỗ trợ / Admin", null, null);
        setupRow(view, R.id.rowAbout, R.drawable.ic_info, "Về ứng dụng", "Credits: Nhóm 9 - HCMUT, v1.0.0", null);

        // Ẩn các mục chưa cần thiết để UI gọn và chuyên nghiệp hơn
        int[] hiddenRows = new int[]{R.id.rowDiagnosis, R.id.rowVoice, R.id.rowOdontogram, R.id.rowFaceId, R.id.rowDarkMode};
        for (int id : hiddenRows) {
            View row = view.findViewById(id);
            if (row != null) row.setVisibility(View.GONE);
        }

        // Logout
        view.findViewById(R.id.rowLogout).setOnClickListener(v -> doLogout());
    }

    private void setupRow(View parent, int rowId, int iconRes, String title, String subtitle, String badgeText) {
        View row = parent.findViewById(rowId);
        if (row == null) return;
        
        ImageView ivIcon = row.findViewById(R.id.ivIcon);
        TextView tvTitle = row.findViewById(R.id.tvTitle);
        TextView tvSubtitle = row.findViewById(R.id.tvSubtitle);
        TextView tvBadge = row.findViewById(R.id.tvBadge);
        
        ivIcon.setImageResource(iconRes);
        tvTitle.setText(title);
        
        if (subtitle != null) {
            tvSubtitle.setText(subtitle);
            tvSubtitle.setVisibility(View.VISIBLE);
        }
        
        if (badgeText != null) {
            tvBadge.setText(badgeText);
            tvBadge.setVisibility(View.VISIBLE);
        }
        
        row.setOnClickListener(v -> {
            Toast.makeText(getContext(), title, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSwitch(View parent, int rowId, int iconRes, String title, boolean defaultState) {
        setupSwitchWithBadge(parent, rowId, iconRes, title, defaultState, null);
    }
    
    private void setupSwitchWithBadge(View parent, int rowId, int iconRes, String title, boolean defaultState, String badgeText) {
        View row = parent.findViewById(rowId);
        if (row == null) return;
        
        ImageView ivIcon = row.findViewById(R.id.ivIcon);
        TextView tvTitle = row.findViewById(R.id.tvTitle);
        TextView tvBadge = row.findViewById(R.id.tvBadge);
        SwitchMaterial switchItem = row.findViewById(R.id.switchItem);
        
        ivIcon.setImageResource(iconRes);
        tvTitle.setText(title);
        switchItem.setChecked(defaultState);
        
        if (badgeText != null) {
            tvBadge.setText(badgeText);
            tvBadge.setVisibility(View.VISIBLE);
        }
    }

    private void doLogout() {
        new TokenManager(requireContext()).clearToken();
        startActivity(new Intent(requireContext(), LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        requireActivity().finish();
    }
}
