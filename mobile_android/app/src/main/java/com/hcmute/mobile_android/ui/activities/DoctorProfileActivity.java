package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorProfileResponse;
import com.hcmute.mobile_android.network.models.MessageResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorProfileActivity extends AppCompatActivity {
    private TextView tvName, tvEmail, tvRoom, tvLicense;
    private EditText etSpecialization, etExperienceYears, etBiography;
    private MaterialButton btnSave;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);
        apiService = RetrofitClient.getApiService(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvName = findViewById(R.id.tvDoctorName);
        tvEmail = findViewById(R.id.tvDoctorEmail);
        tvRoom = findViewById(R.id.tvDoctorRoom);
        tvLicense = findViewById(R.id.tvDoctorLicense);
        etSpecialization = findViewById(R.id.etSpecialization);
        etExperienceYears = findViewById(R.id.etExperienceYears);
        etBiography = findViewById(R.id.etBiography);
        btnSave = findViewById(R.id.btnSaveDoctorProfile);

        btnSave.setOnClickListener(v -> saveProfile());
        loadProfile();
    }

    private void loadProfile() {
        apiService.getDoctorProfile().enqueue(new Callback<DoctorProfileResponse>() {
            @Override
            public void onResponse(Call<DoctorProfileResponse> call, Response<DoctorProfileResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(DoctorProfileActivity.this, "Không tải được hồ sơ bác sĩ", Toast.LENGTH_SHORT).show();
                    return;
                }
                DoctorProfileResponse p = response.body();
                tvName.setText(p.getDisplayName());
                tvEmail.setText(p.getEmail() != null ? p.getEmail() : "");
                tvRoom.setText(p.getRoomName() != null ? p.getRoomName() : "Chưa phân phòng");
                tvLicense.setText(p.getLicenseNumber() != null ? p.getLicenseNumber() : "Chưa có");
                etSpecialization.setText(p.getSpecialization() != null ? p.getSpecialization() : "");
                etExperienceYears.setText(p.getExperienceYears() != null ? String.valueOf(p.getExperienceYears()) : "");
                etBiography.setText(p.getBiography() != null ? p.getBiography() : "");
            }

            @Override
            public void onFailure(Call<DoctorProfileResponse> call, Throwable t) {
                Toast.makeText(DoctorProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        btnSave.setEnabled(false);
        Map<String, Object> body = new HashMap<>();
        body.put("specialization", etSpecialization.getText().toString().trim());
        body.put("biography", etBiography.getText().toString().trim());
        try {
            int years = Integer.parseInt(etExperienceYears.getText().toString().trim());
            body.put("experienceYears", Math.max(0, years));
        } catch (Exception ignored) {}

        apiService.updateDoctorProfile(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(DoctorProfileActivity.this, "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DoctorProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(DoctorProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
