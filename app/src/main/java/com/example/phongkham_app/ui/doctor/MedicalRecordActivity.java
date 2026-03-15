package com.example.phongkham_app.ui.doctor;

import com.example.phongkham_app.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MedicalRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_activity_medical_record);

        // Lấy Toolbar từ layout
        Toolbar toolbar = findViewById(R.id.toolbar);

        // *** DÒNG QUAN TRỌNG: Đặt Toolbar làm ActionBar cho Activity ***
        setSupportActionBar(toolbar);

        // Xử lý nút Back trên Toolbar
        // Giờ đây có thể dùng API của ActionBar, sẽ an toàn hơn
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Ẩn tiêu đề mặc định nếu cần
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Xử lý nút bắt đầu khám
        MaterialButton btnStartExamination = findViewById(R.id.btnStartExamination);
        btnStartExamination.setOnClickListener(v -> {
            // Điều hướng đến màn hình Cập nhật bệnh án
            Intent intent = new Intent(MedicalRecordActivity.this, UpdateRecordActivity.class);
            startActivity(intent);
        });
    }
}
