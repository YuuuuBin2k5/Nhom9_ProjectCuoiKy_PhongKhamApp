package com.example.phongkham_app.ui.patient;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.local.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

public class AppointmentDetailActivity extends AppCompatActivity {

    private TextView tvDocName, tvService, tvTime, tvStatus, tvNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_appointment_detail);

        initViews();
        setupToolbar();

        int appointmentId = getIntent().getIntExtra("APPOINTMENT_ID", -1);
        if (appointmentId != -1) {
            loadAppointmentDetails(appointmentId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin cuộc hẹn", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvDocName = findViewById(R.id.tv_detail_doctor_name);
        tvService = findViewById(R.id.tv_detail_service);
        tvTime = findViewById(R.id.tv_detail_datetime);
        tvStatus = findViewById(R.id.tv_detail_status);
        tvNotes = findViewById(R.id.tv_detail_notes);
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

    private void loadAppointmentDetails(int appointmentId) {
        DatabaseHelper db = new DatabaseHelper(this);
        Cursor cursor = db.getAppointmentById(appointmentId);

        if (cursor != null && cursor.moveToFirst()) {
            int docNameIdx = cursor.getColumnIndex("doctor_name");
            int serviceNameIdx = cursor.getColumnIndex("service_name");
            int timeIdx = cursor.getColumnIndex("appointment_datetime");
            int statusIdx = cursor.getColumnIndex("status");
            int notesIdx = cursor.getColumnIndex("notes");

            String docName = docNameIdx != -1 ? cursor.getString(docNameIdx) : "N/A";
            String serviceName = serviceNameIdx != -1 ? cursor.getString(serviceNameIdx) : "N/A";
            String time = timeIdx != -1 ? cursor.getString(timeIdx) : "N/A";
            String status = statusIdx != -1 ? cursor.getString(statusIdx) : "N/A";
            String notes = notesIdx != -1 ? cursor.getString(notesIdx) : "";

            tvDocName.setText("Bs. " + docName);
            tvService.setText(serviceName);
            tvTime.setText(time);
            tvStatus.setText(status);
            
            if (notes != null && !notes.trim().isEmpty()) {
                tvNotes.setText(notes);
            } else {
                tvNotes.setText("Không có ghi chú.");
            }

            cursor.close();
        } else {
            Toast.makeText(this, "Lỗi khi tải chi tiết", Toast.LENGTH_SHORT).show();
        }
    }
}
