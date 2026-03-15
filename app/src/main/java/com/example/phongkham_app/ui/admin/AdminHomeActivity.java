package com.example.phongkham_app.ui.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;
import com.example.phongkham_app.ui.auth.LoginActivity;
import com.example.phongkham_app.data.local.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminHomeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_home);

        dbHelper = new DatabaseHelper(this);

        setupNavigation();
        setupManagementClickListeners();
    }

    private void setupManagementClickListeners() {
        findViewById(R.id.cardManageDoctors).setOnClickListener(v -> startActivity(new Intent(this, ManageDoctorsActivity.class)));
        findViewById(R.id.cardManageShifts).setOnClickListener(v -> startActivity(new Intent(this, ManageShiftsActivity.class)));
        findViewById(R.id.cardManageInvoices).setOnClickListener(v -> startActivity(new Intent(this, ManageInvoicesActivity.class)));
        findViewById(R.id.cardSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsAdminActivity.class)));

        findViewById(R.id.cardClinicQR).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.phongkham_app.ui.common.QRGenerateActivity.class);
            intent.putExtra("QR_TYPE", "CLINIC");
            startActivity(intent);
        });
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_services) {
                Intent intent = new Intent(this, AdminServiceActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_clinic) {
                Intent intent = new Intent(this, AdminRoomActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void logout() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
