package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.hcmute.mobile_android.BuildConfig;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorProfileResponse;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.UploadResponse;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorProfileActivity extends AppCompatActivity {
    private ApiService apiService;
    private ShapeableImageView ivAvatar;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnChangeAvatar;
    private TextView tvName, tvEmail, tvRoom, tvLicense;
    private EditText etSpecialization, etExperienceYears, etBiography;
    private MaterialButton btnSave;
    private String currentAvatarUrl = "";
    private androidx.activity.result.ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);
        apiService = RetrofitClient.getApiService(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivAvatar = findViewById(R.id.ivDoctorAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        tvName = findViewById(R.id.tvDoctorName);
        tvEmail = findViewById(R.id.tvDoctorEmail);
        tvRoom = findViewById(R.id.tvDoctorRoom);
        tvLicense = findViewById(R.id.tvDoctorLicense);
        etSpecialization = findViewById(R.id.etSpecialization);
        etExperienceYears = findViewById(R.id.etExperienceYears);
        etBiography = findViewById(R.id.etBiography);
        btnSave = findViewById(R.id.btnSaveDoctorProfile);

        // Setup Image Picker
        imagePickerLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        android.net.Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadAvatar(imageUri);
                        }
                    }
                }
        );

        btnChangeAvatar.setOnClickListener(v -> pickImage());
        ivAvatar.setOnClickListener(v -> pickImage());

        btnSave.setOnClickListener(v -> saveProfile());
        loadProfile();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadAvatar(android.net.Uri uri) {
        try {
            // Get file from Uri
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            java.io.File file = new java.io.File(getCacheDir(), "temp_avatar_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Prepare MultipartBody.Part
            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    file, 
                    okhttp3.MediaType.parse(getContentResolver().getType(uri))
            );
            okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            // Show loading
            Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

            apiService.uploadFile(body).enqueue(new Callback<com.hcmute.mobile_android.network.models.UploadResponse>() {
                @Override
                public void onResponse(Call<com.hcmute.mobile_android.network.models.UploadResponse> call, Response<com.hcmute.mobile_android.network.models.UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentAvatarUrl = response.body().getFileDownloadUri();
                        // Load image with Glide for preview
                        com.bumptech.glide.Glide.with(DoctorProfileActivity.this)
                                .load(currentAvatarUrl)
                                .placeholder(R.drawable.ic_doctor)
                                .circleCrop()
                                .into(ivAvatar);
                        Toast.makeText(DoctorProfileActivity.this, "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DoctorProfileActivity.this, "Tải ảnh lên thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.hcmute.mobile_android.network.models.UploadResponse> call, Throwable t) {
                    Toast.makeText(DoctorProfileActivity.this, "Lỗi kết nối khi upload: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xử lý tệp tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
                
                currentAvatarUrl = p.getAvatarUrl() != null ? p.getAvatarUrl() : "";
                if (!currentAvatarUrl.isEmpty()) {
                    String finalUrl = currentAvatarUrl;
                    if (!currentAvatarUrl.startsWith("http")) {
                        String baseUrl = BuildConfig.API_BASE_URL;
                        if (baseUrl.endsWith("/")) {
                            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                        }
                        finalUrl = baseUrl + (currentAvatarUrl.startsWith("/") ? "" : "/") + currentAvatarUrl;
                    }
                    
                    com.bumptech.glide.Glide.with(DoctorProfileActivity.this)
                            .load(finalUrl)
                            .placeholder(R.drawable.ic_doctor)
                            .circleCrop()
                            .into(ivAvatar);
                }
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
        body.put("avatarUrl", currentAvatarUrl);
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
