package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;

public class AppointmentDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long id = getIntent().getLongExtra("appointmentId", -1);
        String datetime = getIntent().getStringExtra("datetime");
        String serviceName = getIntent().getStringExtra("serviceName");
        String doctorName = getIntent().getStringExtra("doctorName");
        String status = getIntent().getStringExtra("status");

        ((TextView) findViewById(R.id.tvApptId)).setText("#" + id);
        
        // Format datetime
        String formattedDt = formatDateTime(datetime);
        ((TextView) findViewById(R.id.tvApptDatetime)).setText(formattedDt);
        
        ((TextView) findViewById(R.id.tvApptService)).setText(serviceName != null ? serviceName : "");
        ((TextView) findViewById(R.id.tvApptDoctor)).setText(doctorName != null ? "BS. " + doctorName : "");
        
        TextView tvStatus = findViewById(R.id.tvApptStatus);
        tvStatus.setText(getStatusDisplay(status));
        setStatusStyle(tvStatus, status);
    }

    private String formatDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "";
        try {
            String dt = dateTime.replace(" ", "T");
            // Remove fractional seconds if present
            if (dt.contains(".")) {
                dt = dt.substring(0, dt.indexOf("."));
            }
            // If missing seconds (e.g., 2026-03-25T08:39), add :00
            if (dt.length() == 16) {
                dt += ":00";
            }
            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(dt);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
            return ldt.format(formatter);
        } catch (Exception e) {
            return dateTime;
        }
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "Đã đặt";
        switch (status.toUpperCase()) {
            case "CONFIRMED": return "Đã xác nhận";
            case "SCHEDULED": return "Đã đặt lịch";
            case "PENDING": return "Chờ xác nhận";
            case "CANCELLED": return "Đã hủy";
            case "COMPLETED": return "Hoàn thành";
            default: return status;
        }
    }

    private void setStatusStyle(TextView tv, String status) {
        if (status == null) status = "SCHEDULED";
        int color;
        switch (status.toUpperCase()) {
            case "CONFIRMED":
            case "SCHEDULED":
                color = getColor(R.color.success_green);
                break;
            case "PENDING":
                color = getColor(R.color.warning_amber);
                break;
            case "CANCELLED":
                color = getColor(R.color.alert_coral);
                break;
            default:
                color = getColor(R.color.text_secondary);
                break;
        }
        tv.setTextColor(color);
    }
}
