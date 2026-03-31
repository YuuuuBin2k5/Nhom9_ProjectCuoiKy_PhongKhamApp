package com.hcmute.mobile_android.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.hcmute.mobile_android.util.ToastUtils;
import android.content.Intent;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;

public class AppointmentDetailActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
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

        MaterialButton btnCancel = findViewById(R.id.btnCancelAppt);
        if (status != null && (status.equalsIgnoreCase("SCHEDULED") || 
            status.equalsIgnoreCase("PENDING") || 
            status.equalsIgnoreCase("CONFIRMED"))) {
            btnCancel.setVisibility(View.VISIBLE);
        }

        btnCancel.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Hủy lịch hẹn")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này không?")
                .setNegativeButton("Quay lại", null)
                .setPositiveButton("Xác nhận hủy", (dialog, which) -> {
                    cancelAppointment(id, btnCancel, tvStatus);
                })
                .show();
        });
    }

    private void cancelAppointment(long id, MaterialButton btnCancel, TextView tvStatus) {
        btnCancel.setEnabled(false);
        btnCancel.setText("Đang xử lý...");

        com.hcmute.mobile_android.network.RetrofitClient.getApiService(this)
            .cancelAppointment(id)
            .enqueue(new retrofit2.Callback<com.hcmute.mobile_android.network.models.UpcomingAppointment>() {
                @Override
                public void onResponse(retrofit2.Call<com.hcmute.mobile_android.network.models.UpcomingAppointment> call, 
                                     retrofit2.Response<com.hcmute.mobile_android.network.models.UpcomingAppointment> response) {
                    btnCancel.setEnabled(true);
                    btnCancel.setText("Hủy lịch hẹn");
                    if (response.isSuccessful()) {
                        ToastUtils.showCenteredToastLong(AppointmentDetailActivity.this, "✅ Đã hủy lịch hẹn thành công");
                        btnCancel.setVisibility(View.GONE);
                        tvStatus.setText("Đã hủy");
                        setStatusStyle(tvStatus, "CANCELLED");
                        
                        // Auto return to home after success
                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(AppointmentDetailActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        }, 1000);
                    } else {
                        String errMsg = "Lỗi: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                org.json.JSONObject obj = new org.json.JSONObject(response.errorBody().string());
                                if (obj.has("message")) errMsg = obj.getString("message");
                            }
                        } catch (Exception e) {}
                        ToastUtils.showCenteredToastLong(AppointmentDetailActivity.this, errMsg);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.hcmute.mobile_android.network.models.UpcomingAppointment> call, Throwable t) {
                    btnCancel.setEnabled(true);
                    btnCancel.setText("Hủy lịch hẹn");
                    ToastUtils.showCenteredToast(AppointmentDetailActivity.this, "Kiểm tra kết nối mạng");
                }
            });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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
