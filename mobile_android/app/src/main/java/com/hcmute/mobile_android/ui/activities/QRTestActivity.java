package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.ui.activities.staff.QRScannerActivity;
import com.hcmute.mobile_android.util.QRCodeHelper;

public class QRTestActivity extends AppCompatActivity {

    private EditText etQRContent;
    private ImageView ivGeneratedQR;
    private MaterialButton btnGenerateQR, btnScanQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_test);

        initViews();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        etQRContent = findViewById(R.id.etQRContent);
        ivGeneratedQR = findViewById(R.id.ivGeneratedQR);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        btnScanQR = findViewById(R.id.btnScanQR);
        
        btnGenerateQR.setOnClickListener(v -> generateQRCode());
        btnScanQR.setOnClickListener(v -> openQRScanner());
        
        // Set default content
        etQRContent.setText("patient:12345");
    }

    private void generateQRCode() {
        String content = etQRContent.getText().toString().trim();
        
        if (content.isEmpty()) {
            Toast.makeText(this, "Nhập nội dung để tạo QR", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Generate QR code with custom colors
        Bitmap qrBitmap = QRCodeHelper.generateQRCode(
            content, 
            400, 
            400, 
            getColor(R.color.secondary_calm_teal), 
            getColor(R.color.white)
        );
        
        if (qrBitmap != null) {
            ivGeneratedQR.setImageBitmap(qrBitmap);
            Toast.makeText(this, "Đã tạo QR code", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lỗi tạo QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void openQRScanner() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Toast.makeText(this, "QR scan thành công!", Toast.LENGTH_SHORT).show();
        }
    }
}