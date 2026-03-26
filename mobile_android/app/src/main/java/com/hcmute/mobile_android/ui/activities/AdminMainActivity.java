package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.ui.activities.staff.QRScannerActivity;
import com.hcmute.mobile_android.ui.activities.staff.QueueManagementActivity;
import com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity;
import com.hcmute.mobile_android.util.TokenManager;

public class AdminMainActivity extends AppCompatActivity {

    private MaterialCardView cardServices, cardRooms, cardDoctors, cardQueue, cardDoctorWorkflow, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);

        initViews();
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        cardServices = findViewById(R.id.cardServices);
        cardRooms = findViewById(R.id.cardRooms);
        cardDoctors = findViewById(R.id.cardDoctors);
        cardQueue = findViewById(R.id.cardQueue);
        cardDoctorWorkflow = findViewById(R.id.cardDoctorWorkflow);
        cardLogout = findViewById(R.id.cardLogout);
    }

    private void setupClickListeners() {
        cardServices.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminServiceActivity.class));
        });

        cardRooms.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminRoomActivity.class));
        });

        cardDoctors.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDoctorActivity.class));
        });

        cardQueue.setOnClickListener(v -> {
            startActivity(new Intent(this, com.hcmute.mobile_android.ui.activities.staff.QueueManagementActivity.class));
        });

        cardDoctorWorkflow.setOnClickListener(v -> {
            startActivity(new Intent(this, com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity.class));
        });

        cardLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void logout() {
        new TokenManager(this).clearToken();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}