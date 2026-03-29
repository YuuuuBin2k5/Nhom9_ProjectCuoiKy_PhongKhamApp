package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.QrTokenResponse;
import com.hcmute.mobile_android.util.QRCodeHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QRCheckInActivity extends AppCompatActivity {

    private MaterialCardView cardQRCode;
    private ImageView ivQRCode;
    private TextView tvQRToken, tvInstructions, tvTokenExpiry;
    private MaterialButton btnRefreshQR, btnViewQueue;
    private View layoutQRDisplay, layoutLoading;
    
    private ApiService apiService;
    private String currentQRToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_checkin);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        loadQRToken();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
        
        cardQRCode = findViewById(R.id.cardQRCode);
        ivQRCode = findViewById(R.id.ivQRCode);
        tvQRToken = findViewById(R.id.tvQRToken);
        tvInstructions = findViewById(R.id.tvInstructions);
        tvTokenExpiry = findViewById(R.id.tvTokenExpiry);
        btnRefreshQR = findViewById(R.id.btnRefreshQR);
        btnViewQueue = findViewById(R.id.btnViewQueue);
        layoutQRDisplay = findViewById(R.id.layoutQRDisplay);
        layoutLoading = findViewById(R.id.layoutLoading);
        
        btnRefreshQR.setOnClickListener(v -> loadQRToken());
        btnViewQueue.setOnClickListener(v -> openQueueStatus());
    }

    private void loadQRToken() {
        showLoading(true);
        
        apiService.getQrToken().enqueue(new Callback<QrTokenResponse>() {
            @Override
            public void onResponse(Call<QrTokenResponse> call, Response<QrTokenResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    QrTokenResponse qrResponse = response.body();
                    currentQRToken = qrResponse.getToken();
                    displayQRCode(qrResponse);
                } else {
                    showError("Lỗi tải mã QR");
                }
            }

            @Override
            public void onFailure(Call<QrTokenResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayQRCode(QrTokenResponse qrResponse) {
        layoutQRDisplay.setVisibility(View.VISIBLE);
        
        // Generate QR Code bitmap using ZXing
        String qrContent = qrResponse.getToken();
        Bitmap qrBitmap = QRCodeHelper.generateQRCode(qrContent, 400, 400);
        
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
            ivQRCode.setVisibility(View.VISIBLE);
            
            // Show token text as backup
            tvQRToken.setText("Token: " + qrContent);
            tvQRToken.setVisibility(View.VISIBLE);
        } else {
            // Fallback to text display if QR generation fails
            ivQRCode.setVisibility(View.GONE);
            tvQRToken.setText("Mã check-in: " + qrContent);
            tvQRToken.setVisibility(View.VISIBLE);
            showError("Không thể tạo mã QR, hiển thị mã text");
        }
        
        // Set expiry info
        if (qrResponse.getExpiresAt() != null) {
            tvTokenExpiry.setText("Hết hạn: " + formatExpiryTime(qrResponse.getExpiresAt()));
            tvTokenExpiry.setVisibility(View.VISIBLE);
        } else {
            tvTokenExpiry.setVisibility(View.GONE);
        }
        
        // Update instructions
        tvInstructions.setText("Đưa mã QR này cho nhân viên lễ tân để check-in");
    }

    private String formatExpiryTime(String expiryTime) {
        // TODO: Format expiry time properly
        return expiryTime;
    }

    private void showLoading(boolean show) {
        if (show) {
            layoutLoading.setVisibility(View.VISIBLE);
            layoutQRDisplay.setVisibility(View.GONE);
        } else {
            layoutLoading.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openQueueStatus() {
        startActivity(new Intent(this, PatientQueueActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh QR token when returning to activity
        if (currentQRToken != null) {
            loadQRToken();
        }
    }
}