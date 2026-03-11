package com.example.phongkham_app.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;
import com.example.phongkham_app.auth.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class SettingsPatientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_settings);

        setupToolbar();
        setupClickListeners();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        findViewById(R.id.btnPersonalInfo).setOnClickListener(v -> 
            Toast.makeText(this, "Thông tin cá nhân (Đang phát triển)", Toast.LENGTH_SHORT).show());
            
        findViewById(R.id.btnMedicalHistory).setOnClickListener(v -> 
            Toast.makeText(this, "Lịch sử khám bệnh (Đang phát triển)", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> 
            Toast.makeText(this, "Đổi mật khẩu (Đang phát triển)", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
