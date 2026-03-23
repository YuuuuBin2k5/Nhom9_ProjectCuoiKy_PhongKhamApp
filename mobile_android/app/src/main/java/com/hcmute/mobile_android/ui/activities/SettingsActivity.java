package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
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

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btnPersonalInfo).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.btnNotificationList).setOnClickListener(v ->
                Toast.makeText(this, "Mở tab thông báo ở màn hình chính", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        loadProfileCard();
    }

    private void loadProfileCard() {
        TextView tvProfileName = findViewById(R.id.tvProfileName);
        ApiService api = RetrofitClient.getApiService(this);
        api.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientMeResponse p = response.body();
                    String fullName = ((p.getFirstName() != null ? p.getFirstName() : "") + " " +
                            (p.getLastName() != null ? p.getLastName() : "")).trim();
                    tvProfileName.setText(fullName.isEmpty() ? "Bạn" : fullName);
                }
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) { }
        });
    }

    private void logout() {
        new TokenManager(this).clearToken();
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
