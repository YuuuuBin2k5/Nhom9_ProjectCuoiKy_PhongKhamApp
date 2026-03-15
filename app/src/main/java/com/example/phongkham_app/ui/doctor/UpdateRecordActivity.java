package com.example.phongkham_app.ui.doctor;

import com.example.phongkham_app.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class UpdateRecordActivity extends AppCompatActivity {

    private String prescriptionContent = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK && data != null) {
            prescriptionContent = data.getStringExtra("prescriptions_text");
            Toast.makeText(this, "Đã đính kèm đơn thuốc vào bệnh án!", Toast.LENGTH_SHORT).show();
        }
    }

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

        com.google.android.material.textfield.TextInputEditText edtSymptoms = findViewById(R.id.edtSymptoms);
        com.google.android.material.textfield.TextInputEditText edtDiagnosis = findViewById(R.id.edtDiagnosis);
        com.google.android.material.textfield.TextInputEditText edtAdvice = findViewById(R.id.edtAdvice);

        com.google.android.material.button.MaterialButton btnAddMedicine = findViewById(R.id.btnAddMedicine);
        com.google.android.material.button.MaterialButton btnFinishAndUpdate = findViewById(R.id.btnFinishAndUpdate);


        btnAddMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateRecordActivity.this, PrescriptionActivity.class);
            startActivityForResult(intent, 123);
        });

        btnFinishAndUpdate.setOnClickListener(v -> {
            String symptoms = edtSymptoms.getText().toString().trim();
            String diagnosis = edtDiagnosis.getText().toString().trim();
            String advice = edtAdvice.getText().toString().trim();

            if (diagnosis.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập chẩn đoán bệnh", Toast.LENGTH_SHORT).show();
                return;
            }

            String treatmentPlan = "Triệu chứng: " + symptoms + "\nLời dặn: " + advice;

            // Lấy ID từ Intent (Nếu null gán 1 cho demo)
            int appointmentId = getIntent().getIntExtra("appointmentId", 1);
            int customerId = getIntent().getIntExtra("customerId", 1);
            int doctorId = getIntent().getIntExtra("doctorId", 1);

            com.example.phongkham_app.data.local.DatabaseHelper db = new com.example.phongkham_app.data.local.DatabaseHelper(this);
            db.addMedicalRecord(appointmentId, customerId, doctorId, diagnosis, treatmentPlan, prescriptionContent);

            Toast.makeText(this, "Đã cập nhật bệnh án thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });

    }
}
