package com.example.phongkham_app.ui.admin;

import com.example.phongkham_app.R;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InvoiceDetailActivity extends AppCompatActivity {

    private TextView tvInvoiceCode, tvPatientName, tvInvoiceDate;
    private TextView tvDoctorName, tvServiceName, tvDiagnosis, tvFollowUpDate, tvAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_invoice_detail);

        initViews();
        loadInvoiceData();
    }

    private void initViews() {
        tvInvoiceCode = findViewById(R.id.tvInvoiceCode);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvInvoiceDate = findViewById(R.id.tvInvoiceDate);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvDiagnosis = findViewById(R.id.tvDiagnosis);
        tvFollowUpDate = findViewById(R.id.tvFollowUpDate);
        tvAmount = findViewById(R.id.tvAmount);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadInvoiceData() {
        // Lấy dữ liệu từ Intent
        String code = getIntent().getStringExtra("INVOICE_CODE");
        String patientName = getIntent().getStringExtra("PATIENT_NAME");
        String date = getIntent().getStringExtra("INVOICE_DATE");
        String doctorName = getIntent().getStringExtra("DOCTOR_NAME");
        String serviceName = getIntent().getStringExtra("SERVICE_NAME");
        String diagnosis = getIntent().getStringExtra("DIAGNOSIS");
        String followUpDate = getIntent().getStringExtra("FOLLOW_UP_DATE");
        String amount = getIntent().getStringExtra("AMOUNT");

        // Hiển thị dữ liệu
        tvInvoiceCode.setText(code != null ? code : "HD001");
        tvPatientName.setText(patientName != null ? patientName : "Nguyễn Văn A");
        tvInvoiceDate.setText(date != null ? date : "01/01/2024");
        tvDoctorName.setText(doctorName != null ? doctorName : "BS. Nguyễn Văn A");
        tvServiceName.setText(serviceName != null ? serviceName : "Khám Tổng Quát");
        tvDiagnosis.setText(diagnosis != null ? diagnosis : "Sức khỏe tốt");
        tvFollowUpDate.setText(followUpDate != null ? followUpDate : "Không có");
        tvAmount.setText(amount != null ? amount : "500,000 VNĐ");
    }
}
