package com.hcmute.mobile_android.ui.activities.staff;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CheckInScanRequest;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QRScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    private DecoratedBarcodeView barcodeView;
    private TextView tvScanStatus, tvScanResult;
    private ImageView ivStatusIcon;
    private MaterialButton btnRetry;
    
    private ApiService apiService;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_scanner);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        checkCameraPermission();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        barcodeView = findViewById(R.id.barcodeView);
        tvScanStatus = findViewById(R.id.tvScanStatus);
        tvScanResult = findViewById(R.id.tvScanResult);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        btnRetry = findViewById(R.id.btnRetry);
        
        btnRetry.setOnClickListener(v -> restartScanning());
        
        // Configure barcode scanner
        barcodeView.setStatusText("Đưa mã QR vào khung để quét");
        barcodeView.decodeContinuous(barcodeCallback);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST);
        } else {
            startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                showError("Cần quyền camera để quét mã QR");
                finish();
            }
        }
    }

    private void startScanning() {
        isScanning = true;
        barcodeView.resume();
        updateScanStatus("Đang quét...", R.drawable.ic_camera, false);
    }

    private void restartScanning() {
        isScanning = true;
        barcodeView.resume();
        updateScanStatus("Đang quét...", R.drawable.ic_camera, false);
        tvScanResult.setText("");
        btnRetry.setVisibility(MaterialButton.GONE);
    }

    private final BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (!isScanning) return;
            
            isScanning = false;
            barcodeView.pause();
            
            String qrContent = result.getText();
            processQRCode(qrContent);
        }
    };

    private void processQRCode(String qrContent) {
        updateScanStatus("Đang xử lý...", R.drawable.ic_processing, false);
        
        CheckInScanRequest request = new CheckInScanRequest(qrContent);
        
        apiService.scanCheckIn(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleScanSuccess(response.body().getMessage());
                } else {
                    handleScanError("Mã QR không hợp lệ hoặc đã hết hạn");
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                handleScanError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void handleScanSuccess(String message) {
        updateScanStatus("Thành công!", R.drawable.ic_check_circle, true);
        tvScanResult.setText(message);
        
        // Auto finish after 2 seconds
        tvScanResult.postDelayed(() -> {
            setResult(RESULT_OK);
            finish();
        }, 2000);
    }

    private void handleScanError(String error) {
        updateScanStatus("Lỗi quét", R.drawable.ic_error_circle, true);
        tvScanResult.setText(error);
        btnRetry.setVisibility(MaterialButton.VISIBLE);
    }

    private void updateScanStatus(String status, int iconRes, boolean showRetry) {
        tvScanStatus.setText(status);
        ivStatusIcon.setImageResource(iconRes);
        btnRetry.setVisibility(showRetry ? MaterialButton.VISIBLE : MaterialButton.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isScanning) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        barcodeView.pause();
    }
}