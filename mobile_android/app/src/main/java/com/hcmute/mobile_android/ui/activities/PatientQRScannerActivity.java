package com.hcmute.mobile_android.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientQRScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    private DecoratedBarcodeView barcodeView;
    private TextView tvScanStatus, tvScanResult;
    private ImageView ivStatusIcon;
    private MaterialButton btnRetry, btnManualInput;
    
    private ApiService apiService;
    private boolean isScanning = true;
    
    public static final String EXTRA_RETURN_RESULT = "EXTRA_RETURN_RESULT";
    public static final String EXTRA_SCAN_DATA = "EXTRA_SCAN_DATA";
    private boolean returnResultOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_qr_scanner);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        checkCameraPermission();

        returnResultOnly = getIntent().getBooleanExtra(EXTRA_RETURN_RESULT, false);

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
        btnManualInput = findViewById(R.id.btnManualInput);
        
        btnRetry.setOnClickListener(v -> restartScanning());
        btnManualInput.setOnClickListener(v -> showManualInputDialog());
        
        // Configure barcode scanner
        if (returnResultOnly) {
            barcodeView.setStatusText("Quét mã bệnh nhân / phác đồ");
            btnManualInput.setVisibility(View.GONE);
        } else {
            barcodeView.setStatusText("Quét mã QR từ lễ tân");
        }
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
            
            if (returnResultOnly) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SCAN_DATA, qrContent);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                processCheckIn(qrContent);
            }
        }
    };

    private void showManualInputDialog() {
        barcodeView.pause();
        isScanning = false;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập mã số");
        
        final EditText input = new EditText(this);
        input.setHint("Nhập mã số từ lễ tân");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);
        
        builder.setPositiveButton("Check-in", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (code.isEmpty()) {
                showError("Vui lòng nhập mã số");
                restartScanning();
            } else {
                processCheckIn(code);
            }
        });
        
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.cancel();
            restartScanning();
        });
        
        builder.show();
    }

    private void processCheckIn(String qrContent) {
        updateScanStatus("Đang xử lý...", R.drawable.ic_processing, false);
        
        CheckInScanRequest request = new CheckInScanRequest(qrContent);
        
        apiService.selfCheckIn(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleCheckInSuccess(response.body().getMessage());
                } else {
                    String errorMsg = "Mã không hợp lệ";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            JSONObject json = new JSONObject(errorBody);
                            errorMsg = json.optString("message", errorMsg);
                        }
                    } catch (Exception e) {
                        // Use default error message
                    }
                    handleCheckInError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                handleCheckInError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void handleCheckInSuccess(String message) {
        updateScanStatus("Thành công!", R.drawable.ic_check_circle, true);
        tvScanResult.setText(message);
        btnRetry.setVisibility(MaterialButton.GONE);
        
        // Auto finish after 2 seconds
        tvScanResult.postDelayed(() -> {
            setResult(RESULT_OK);
            finish();
        }, 2000);
    }

    private void handleCheckInError(String error) {
        updateScanStatus("Lỗi", R.drawable.ic_error_circle, true);
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
