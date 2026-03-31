package com.hcmute.mobile_android.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.network.models.UpdatePatientRequest;
import com.hcmute.mobile_android.util.ToastUtils;
import com.hcmute.mobile_android.util.TokenManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress, etDob, etEmail, etQrCode;
    private EditText etAllergies, etConditions;
    private TextView tvUserNameDisplay, tvUserEmailDisplay;
    private AutoCompleteTextView actvGender;
    private AutoCompleteTextView actvBloodType;
    private ArrayAdapter<String> bloodTypeAdapter;
    private ShapeableImageView ivProfile;
    private MaterialButton btnSave;
    private String currentAvatarUrl = "";
    private androidx.activity.result.ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile_premium);

        // Header Icons
        View btnBack = findViewById(R.id.ivBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Bind views
        etQrCode = findViewById(R.id.etQrCode);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etDob = findViewById(R.id.etDob);
        actvGender = findViewById(R.id.actvGender);
        ivProfile = findViewById(R.id.ivUserProfile);
        
        tvUserNameDisplay = findViewById(R.id.tvUserNameDisplay);
        tvUserEmailDisplay = findViewById(R.id.tvUserEmailDisplay);

        actvBloodType = findViewById(R.id.actvBloodType);
        etAllergies = findViewById(R.id.etAllergies);
        etConditions = findViewById(R.id.etConditions);

        List<String> bloodOpts = new ArrayList<>(Arrays.asList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Không rõ"));
        bloodTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodOpts);
        actvBloodType.setAdapter(bloodTypeAdapter);
        actvBloodType.setThreshold(1);
        actvBloodType.setOnClickListener(v -> actvBloodType.showDropDown());

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

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveProfile());
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

    private static String readApiErrorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                org.json.JSONObject obj = new org.json.JSONObject(body);
                if (obj.has("message")) {
                    return obj.getString("message");
                }
            }
        } catch (Exception ignored) {}
        return fallback;
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
                    applyPatientToUi(response.body());
                } else {
                    ToastUtils.showCenteredToast(ProfileActivity.this,
                            readApiErrorMessage(response, "Không tải được hồ sơ (" + response.code() + ")"));
                }
            }
            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                ToastUtils.showCenteredToast(ProfileActivity.this,
                        "Không tải được hồ sơ. Kiểm tra kết nối mạng.");
            }
        });
    }

    /** Cập nhật toàn bộ form + header từ dữ liệu server (dùng sau GET và sau PUT thành công). */
    private void applyPatientToUi(PatientMeResponse p) {
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

        tvUserNameDisplay.setText(fullName.isEmpty() ? "Bệnh nhân" : fullName);
        tvUserEmailDisplay.setText(p.getEmail() != null ? p.getEmail() : "Chưa có email");

        applyBloodTypeToField(p.getBloodType());
        etAllergies.setText(p.getAllergies() != null ? p.getAllergies() : "");
        etConditions.setText(p.getUnderlyingConditions() != null ? p.getUnderlyingConditions() : "");

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

    private void applyBloodTypeToField(String blood) {
        if (blood == null || blood.trim().isEmpty() || "N/A".equalsIgnoreCase(blood.trim())) {
            actvBloodType.setText("", false);
            return;
        }
        String b = blood.trim();
        boolean inList = false;
        for (int i = 0; i < bloodTypeAdapter.getCount(); i++) {
            if (b.equalsIgnoreCase(bloodTypeAdapter.getItem(i))) {
                inList = true;
                break;
            }
        }
        if (!inList) {
            bloodTypeAdapter.add(b);
            bloodTypeAdapter.notifyDataSetChanged();
        }
        actvBloodType.setText(b, false);
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
                etConditions.getText().toString().trim(),
                actvBloodType.getText().toString().trim()
        );

        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        ApiService api = RetrofitClient.getApiService(this);
        api.updatePatientMe(req).enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                btnSave.setEnabled(true);
                btnSave.setText("Lưu thay đổi hồ sơ");
                if (response.isSuccessful()) {
                    PatientMeResponse body = response.body();
                    if (body != null) {
                        applyPatientToUi(body);
                    } else {
                        loadProfile();
                    }
                    ToastUtils.showCenteredToastLong(ProfileActivity.this,
                            "Đã cập nhật hồ sơ thành công.");
                    setResult(RESULT_OK);
                } else {
                    String msg = readApiErrorMessage(response, "Cập nhật thất bại (mã " + response.code() + ")");
                    ToastUtils.showCenteredToastLong(ProfileActivity.this, msg);
                }
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText("Lưu thay đổi hồ sơ");
                ToastUtils.showCenteredToastLong(ProfileActivity.this,
                        "Lỗi kết nối mạng: " + (t.getMessage() != null ? t.getMessage() : ""));
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
