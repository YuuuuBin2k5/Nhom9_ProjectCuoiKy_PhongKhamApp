package com.example.phongkham_app.ui.common;

import android.content.Intent;
import android.content.SharedPreferences;
import com.example.phongkham_app.ui.patient.WaitingStatusActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScanActivity extends AppCompatActivity {

    private Button btnScan;
    private TextView tvResult;

    private final ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String content = result.getContents();
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            String role = pref.getString("USER_ROLE", "USER");

            if ("CLINIC_CHECKIN".equals(content) && ("USER".equals(role) || "CUSTOMER".equals(role))) {
                // Customer check-in
                
                // MOCK: Thêm vào Hàng Đợi ảo cho demo - Thực tế lễ tân làm việc này
                com.example.phongkham_app.data.local.DatabaseHelper db = new com.example.phongkham_app.data.local.DatabaseHelper(this);
                db.addQueueItem(1, 1, "08:00", "A-12", 20); // Tạo queue test nếu chưa có
                
                Intent intent = new Intent(this, WaitingStatusActivity.class);
                startActivity(intent);
                finish();
            } else if (content.startsWith("PATIENT_ID:") && "DOCTOR".equals(role)) {
                // Doctor scanning patient
                try {
                    int patientId = Integer.parseInt(content.replace("PATIENT_ID:", ""));
                    
                    com.example.phongkham_app.data.local.DatabaseHelper db = new com.example.phongkham_app.data.local.DatabaseHelper(this);
                    android.database.Cursor latestApp = db.getLatestAppointment(patientId);
                    if (latestApp != null && latestApp.moveToFirst()) {
                        int idIndex = latestApp.getColumnIndex("id");
                        if (idIndex >= 0) {
                            int appId = latestApp.getInt(idIndex);
                            db.startTreatment(appId); // Đổi trạng thái 'IN_PROGRESS' và lưu mốc giờ
                        }
                        latestApp.close();
                    }

                    Intent intent = new Intent(this, com.example.phongkham_app.ui.doctor.ProfilePatient.class);
                    intent.putExtra("patientId", patientId);
                    intent.putExtra("PATIENT_NAME", "Bệnh nhân #" + patientId);
                    startActivity(intent);
                    finish();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Mã bệnh nhân không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                tvResult.setText("Kết quả: " + content);
            }
        } else {
            tvResult.setText("Quét bị hủy");
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        btnScan = findViewById(R.id.btnScan);
        tvResult = findViewById(R.id.tvResult);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnScan.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Quét mã QR");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureActivityPortrait.class); // Custom activity for portrait mode
            qrCodeLauncher.launch(options);
        });
    }
}
