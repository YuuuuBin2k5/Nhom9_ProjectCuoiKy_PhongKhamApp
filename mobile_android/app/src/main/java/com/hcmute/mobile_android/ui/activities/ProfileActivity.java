package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btnSave).setOnClickListener(v ->
                Toast.makeText(this, "Đã lưu thông tin cục bộ", Toast.LENGTH_SHORT).show());
        findViewById(R.id.ivProfile).setOnClickListener(v ->
                Toast.makeText(this, "Tính năng đổi ảnh sẽ cập nhật sau", Toast.LENGTH_SHORT).show());
        findViewById(R.id.etEmail).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        loadProfile();
    }

    private void loadProfile() {
        EditText etName = findViewById(R.id.etName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);
        EditText etAddress = findViewById(R.id.etAddress);

        ApiService api = RetrofitClient.getApiService(this);
        api.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientMeResponse p = response.body();
                    String fullName = ((p.getFirstName() != null ? p.getFirstName() : "") + " " +
                            (p.getLastName() != null ? p.getLastName() : "")).trim();
                    etName.setText(fullName.isEmpty() ? "Bạn" : fullName);
                    etEmail.setText(p.getEmail() != null ? p.getEmail() : "-");
                    etPhone.setText("Chưa cập nhật");
                    etAddress.setText("Chưa cập nhật");
                }
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                // Keep fallback UI values.
            }
        });
    }

    private void logout() {
        new TokenManager(this).clearToken();
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
