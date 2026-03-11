package com.example.phongkham_app.doctor;

import com.example.phongkham_app.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class UpdateRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_activity_update_record);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Lấy các nút
        MaterialButton btnAddMedicine = findViewById(R.id.btnAddMedicine);
        MaterialButton btnFinishAndUpdate = findViewById(R.id.btnFinishAndUpdate);

        // *** SỬA LỖI LOGIC: Mở màn hình PrescriptionActivity ***
        btnAddMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateRecordActivity.this, PrescriptionActivity.class);
            startActivity(intent);
        });

        btnFinishAndUpdate.setOnClickListener(v -> {
            Toast.makeText(this, "Đã cập nhật và gửi thông báo!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
