package com.example.phongkham_app.ui.patient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;
import com.example.phongkham_app.ui.auth.LoginActivity;
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
            startActivity(new Intent(this, ProfileDetailActivity.class)));
            
        findViewById(R.id.btnMedicalHistory).setOnClickListener(v -> 
            startActivity(new Intent(this, MedicalRecordActivity.class)));

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> 
            startActivity(new Intent(this, ChangePasswordActivity.class)));

        findViewById(R.id.btnLanguage).setOnClickListener(v -> showLanguageDialog());

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void showLanguageDialog() {
        String[] languages = {"Tiếng Việt", "English"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn ngôn ngữ")
            .setItems(languages, (dialog, which) -> {
                Toast.makeText(this, "Đã chọn: " + languages[which], Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
