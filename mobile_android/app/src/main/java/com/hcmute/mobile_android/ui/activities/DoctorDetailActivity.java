package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.hcmute.mobile_android.R;

public class DoctorDetailActivity extends AppCompatActivity {

    private String doctorName;
    private String specialization;
    private Long doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail);

        // Get data from intent
        doctorName = getIntent().getStringExtra("doctorName");
        specialization = getIntent().getStringExtra("specialization");
        doctorId = getIntent().getLongExtra("doctorId", -1L);

        initViews();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvName = findViewById(R.id.tvDoctorNameDetail);
        TextView tvSpec = findViewById(R.id.tvSpecializationDetail);
        
        if (doctorName != null) tvName.setText(doctorName);
        if (specialization != null) tvSpec.setText(specialization);

        findViewById(R.id.btnActionCall).setOnClickListener(v -> 
            Toast.makeText(this, "Đang kết nối cuộc gọi với " + doctorName, Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnActionVideo).setOnClickListener(v -> 
            Toast.makeText(this, "Đang khởi tạo cuộc gọi Video...", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnActionChat).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("doctorName", doctorName);
            intent.putExtra("doctorId", doctorId);
            startActivity(intent);
        });

        findViewById(R.id.btnBookWithDoctor).setOnClickListener(v -> {
            Intent intent = new Intent(this, BookAppointmentActivity.class);
            intent.putExtra("doctorId", doctorId);
            intent.putExtra("doctorName", doctorName);
            startActivity(intent);
        });
    }
}
