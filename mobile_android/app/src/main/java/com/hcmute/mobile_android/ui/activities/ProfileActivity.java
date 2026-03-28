package com.hcmute.mobile_android.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.network.models.UpdatePatientRequest;
import com.hcmute.mobile_android.util.TokenManager;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress, etDob, etEmail, etQrCode;
    private EditText etBloodType, etAllergies, etUnderlyingConditions;
    private AutoCompleteTextView actvGender;
    private ShapeableImageView ivProfile;
    private String currentAvatarUrl = "";
    private androidx.activity.result.ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind views
        etQrCode = findViewById(R.id.etQrCode);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etDob = findViewById(R.id.etDob);
        actvGender = findViewById(R.id.actvGender);
        ivProfile = findViewById(R.id.ivProfile);
        etBloodType = findViewById(R.id.etBloodType);
        etAllergies = findViewById(R.id.etAllergies);
        etUnderlyingConditions = findViewById(R.id.etUnderlyingConditions);

        // Setup Gallery Launcher
        galleryLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        android.net.Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            ivProfile.setImageURI(imageUri);
                            currentAvatarUrl = imageUri.toString();
                            Toast.makeText(this, "Ảnh đã được chọn!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Setup Gender Dropdown
        String[] genders = new String[]{"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        actvGender.setAdapter(adapter);

        // Setup Date Picker
        etDob.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());
        ivProfile.setOnClickListener(v -> showAvatarEditDialog());

        // Setup Avatar Edit FAB
        View fabEdit = findViewById(R.id.fabAvatarEdit);
        if (fabEdit != null) {
            fabEdit.setOnClickListener(v -> showAvatarEditDialog());
        }

        // Logout button
        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }

        
        loadProfile();
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = year1 + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth);
                    etDob.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showAvatarEditDialog() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void loadProfile() {
        ApiService api = RetrofitClient.getApiService(this);
        api.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientMeResponse p = response.body();
                    String fullName = ((p.getFirstName() != null ? p.getFirstName() : "") + " " +
                            (p.getLastName() != null ? p.getLastName() : "")).trim();
                    etQrCode.setText(p.getQrCodeData() != null ? p.getQrCodeData() : "-");
                    etName.setText(fullName.isEmpty() ? "" : fullName);
                    etEmail.setText(p.getEmail() != null ? p.getEmail() : "-");
                    etPhone.setText(p.getPhone() != null ? p.getPhone() : "");
                    etAddress.setText(p.getAddress() != null ? p.getAddress() : "");
                    etDob.setText(p.getDob() != null ? p.getDob() : "");
                    actvGender.setText(p.getGender() != null ? p.getGender() : "", false);
                    currentAvatarUrl = p.getAvatarUrl() != null ? p.getAvatarUrl() : "";
                    
                    etBloodType.setText(p.getBloodType() != null ? p.getBloodType() : "");
                    etAllergies.setText(p.getAllergies() != null ? p.getAllergies() : "");
                    etUnderlyingConditions.setText(p.getUnderlyingConditions() != null ? p.getUnderlyingConditions() : "");

                    if (!currentAvatarUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this).load(currentAvatarUrl).placeholder(R.drawable.ic_doctor).into(ivProfile);
                    }
                    
                    boolean isMissingInfo = p.getPhone() == null || p.getPhone().isEmpty() ||
                            p.getAddress() == null || p.getAddress().isEmpty();
                    
                    View warningIcon = findViewById(R.id.ivWarningProfile);
                    if (warningIcon != null) {
                        warningIcon.setVisibility(isMissingInfo ? View.VISIBLE : View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {}
        });
    }

    private void saveProfile() {
        String fullName = etName.getText().toString().trim();
        String firstName = "";
        String lastName = "";
        if (!fullName.isEmpty()) {
            String[] parts = fullName.split(" ");
            if (parts.length > 1) {
                lastName = parts[parts.length - 1];
                firstName = fullName.substring(0, fullName.length() - lastName.length()).trim();
            } else {
                firstName = fullName;
            }
        }

        UpdatePatientRequest req = new UpdatePatientRequest(
                firstName,
                lastName,
                etPhone.getText().toString().trim(),
                etAddress.getText().toString().trim(),
                actvGender.getText().toString(),
                etDob.getText().toString().trim(),
                currentAvatarUrl,
                etAllergies.getText().toString().trim(),
                etUnderlyingConditions.getText().toString().trim(),
                etBloodType.getText().toString().trim()
        );

        ApiService api = RetrofitClient.getApiService(this);
        api.updatePatientMe(req).enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    loadProfile();
                } else {
                    String errorMsg = "Lỗi " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (body.contains("message")) {
                                errorMsg += ": " + body;
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        new TokenManager(this).clearToken();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
