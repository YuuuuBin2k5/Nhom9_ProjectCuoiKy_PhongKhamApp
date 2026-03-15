package com.example.phongkham_app.ui.common;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRGenerateActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generate);

        ivQRCode = findViewById(R.id.ivQRCode);
        tvInfo = findViewById(R.id.tvInfo);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        long userId = pref.getLong("USER_ID", -1);
        String userRole = pref.getString("USER_ROLE", "USER");
        String userName = "Bệnh nhân"; // Vì LoginActivity chỉ lưu ID và Role. Có thể nạp từ ViewModel sau.

        String qrType = getIntent().getStringExtra("QR_TYPE");

        if ("CLINIC".equals(qrType)) {
            tvInfo.setText("MÃ QUÉT TẠI PHÒNG KHÁM");
            generateQRCode("CLINIC_CHECKIN");
        } else if (userId != -1) {
            String qrData = "PATIENT_ID:" + userId;
            tvInfo.setText(userName + " (" + userRole + ")");
            generateQRCode(qrData);
        }
    }

    private void generateQRCode(String text) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 800, 800);
            ivQRCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
