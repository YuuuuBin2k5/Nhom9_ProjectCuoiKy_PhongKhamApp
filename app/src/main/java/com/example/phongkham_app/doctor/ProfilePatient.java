package com.example.phongkham_app.doctor;

import com.example.phongkham_app.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ProfilePatient extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_activity_profile_patient);

        ImageView ivBack = findViewById(R.id.ivBack);
        TextView tvPatientName = findViewById(R.id.tvPatientName);
        TextView tvPatientEmail = findViewById(R.id.tvPatientEmail);

        // *** SỬA LỖI LOGIC: Nhận dữ liệu từ Intent ***
        Intent intent = getIntent();
        String patientName = intent.getStringExtra("PATIENT_NAME");

        // Điền dữ liệu nhận được
        tvPatientName.setText(patientName);
        // Tạo email giả từ tên
        if (patientName != null) {
            String email = patientName.replaceAll("\\s+", ".").toLowerCase() + "@email.com";
            tvPatientEmail.setText(email);
        }

        // Xử lý sự kiện click
        ivBack.setOnClickListener(v -> onBackPressed());

        MaterialButton btnMedicalRecord = findViewById(R.id.btnMedicalRecord);
        MaterialButton btnHistory = findViewById(R.id.btnHistory);
        MaterialButton btnPrescription = findViewById(R.id.btnPrescription);

        // *** SỬA LỖI LOGIC: Nút này sẽ mở MedicalRecordActivity ***
        btnMedicalRecord.setOnClickListener(v -> {
            Intent medicalRecordIntent = new Intent(ProfilePatient.this, MedicalRecordActivity.class);
            // Bạn có thể truyền thêm ID bệnh nhân ở đây
            startActivity(medicalRecordIntent);
        });

        btnPrescription.setOnClickListener(v -> {
            Intent prescriptionIntent = new Intent(ProfilePatient.this, PrescriptionActivity.class);
            startActivity(prescriptionIntent);
        });

        btnHistory.setOnClickListener(v -> Toast.makeText(this, "Mở lịch sử khám", Toast.LENGTH_SHORT).show());
    }
}
