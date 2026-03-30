package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CheckInMyStatusResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientQueueActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private MaterialCardView cardQueueStatus;
    private TextView tvQueueNumber, tvQueuePosition, tvEstimatedTime, tvRoomName, tvStatus;
    private MaterialButton btnRefresh;
    private View layoutNotCheckedIn, layoutQueueInfo;
    
    private ApiService apiService;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final int AUTO_REFRESH_INTERVAL = 30000; // 30 seconds
    
    // Countdown timer
    private Handler countdownHandler;
    private Runnable countdownRunnable;
    private long countdownEndTimeMillis = 0;
    private boolean isCountdownActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_queue);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        setupAutoRefresh();
        loadQueueStatus();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        swipeRefresh = findViewById(R.id.swipeRefresh);
        cardQueueStatus = findViewById(R.id.cardQueueStatus);
        tvQueueNumber = findViewById(R.id.tvQueueNumber);
        tvQueuePosition = findViewById(R.id.tvQueuePosition);
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime);
        tvRoomName = findViewById(R.id.tvRoomName);
        tvStatus = findViewById(R.id.tvStatus);
        btnRefresh = findViewById(R.id.btnRefresh);
        layoutNotCheckedIn = findViewById(R.id.layoutNotCheckedIn);
        layoutQueueInfo = findViewById(R.id.layoutQueueInfo);
        
        swipeRefresh.setOnRefreshListener(this::loadQueueStatus);
        btnRefresh.setOnClickListener(v -> loadQueueStatus());
    }

    private void setupAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadQueueStatus();
                refreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL);
            }
        };
    }

    private void loadQueueStatus() {
        swipeRefresh.setRefreshing(true);
        
        apiService.getMyCheckInStatus().enqueue(new Callback<CheckInMyStatusResponse>() {
            @Override
            public void onResponse(Call<CheckInMyStatusResponse> call, Response<CheckInMyStatusResponse> response) {
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    updateQueueStatus(response.body());
                } else {
                    showNotCheckedIn();
                }
            }

            @Override
            public void onFailure(Call<CheckInMyStatusResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(PatientQueueActivity.this, 
                    "Lỗi tải trạng thái: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showNotCheckedIn();
            }
        });
    }

    private void updateQueueStatus(CheckInMyStatusResponse status) {
        if (status.isCheckedIn()) {
            layoutNotCheckedIn.setVisibility(View.GONE);
            layoutQueueInfo.setVisibility(View.VISIBLE);
            
            // Update basic info
            tvQueueNumber.setText(String.valueOf(status.getQueueNumber()));
            tvRoomName.setText(status.getRoomName() != null ? status.getRoomName() : "Phòng khám");
            
            // Display queue estimate based on type
            String displayType = status.getEstimateDisplayType();
            
            if ("IN_PROGRESS".equals(displayType)) {
                // Currently being served
                stopCountdown(); // Stop countdown if being served
                tvQueuePosition.setText(status.getEstimateTitle() != null ? status.getEstimateTitle() : "Đang khám");
                tvEstimatedTime.setText(status.getEstimateSubtitle() != null ? status.getEstimateSubtitle() : "");
                tvStatus.setText(status.getEstimateMessage() != null ? status.getEstimateMessage() : "");
                setCardColor("IN_PROGRESS");
                
            } else if ("SOFT_COUNTDOWN".equals(displayType)) {
                // Next in line - start or sync countdown
                Integer countdownSeconds = status.getCountdownStartSeconds();
                if (countdownSeconds != null && countdownSeconds > 0) {
                    if (!isCountdownActive) {
                        startCountdown(countdownSeconds);
                    } else {
                        syncCountdownWithServer(countdownSeconds);
                    }
                } else {
                    // Fallback if no countdown data
                    stopCountdown();
                    tvEstimatedTime.setText(status.getEstimateSubtitle() != null ? status.getEstimateSubtitle() : "~5 phút");
                }
                
                tvQueuePosition.setText(status.getEstimateTitle() != null ? status.getEstimateTitle() : "Bạn kế tiếp");
                tvStatus.setText(status.getEstimateMessage() != null ? status.getEstimateMessage() : "Vui lòng ở gần");
                setCardColor("RETURNED_PRIORITY");
                
            } else if ("RANGE".equals(displayType)) {
                // Waiting - range estimate
                stopCountdown(); // Stop countdown for range estimates
                tvQueuePosition.setText(status.getEstimateTitle() != null ? status.getEstimateTitle() : "Vị trí: " + status.getQueuePosition());
                tvEstimatedTime.setText(status.getEstimateSubtitle() != null ? status.getEstimateSubtitle() : status.getEstimatedWaitTime() + " phút");
                String message = status.getEstimateMessage() != null ? status.getEstimateMessage() : "";
                String confidence = status.getEstimateConfidence() != null ? " (Độ tin cậy: " + status.getEstimateConfidence() + ")" : "";
                tvStatus.setText(message + confidence);
                setCardColor(status.getStatus());
                
            } else {
                // Fallback to old display
                stopCountdown();
                tvQueuePosition.setText("Vị trí: " + status.getQueuePosition());
                tvEstimatedTime.setText("Ước tính: " + status.getEstimatedWaitTime() + " phút");
                tvStatus.setText(getStatusDisplay(status.getStatus()));
                setCardColor(status.getStatus());
            }
            
        } else {
            stopCountdown();
            showNotCheckedIn();
        }
    }
    
    private void loadQueueEstimate() {
        // This method is no longer needed - estimate comes from getMyCheckInStatus
    }

    private void showNotCheckedIn() {
        layoutNotCheckedIn.setVisibility(View.VISIBLE);
        layoutQueueInfo.setVisibility(View.GONE);
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "Đang chờ";
        
        switch (status.toUpperCase()) {
            case "WAITING": return "Đang chờ khám";
            case "IN_PROGRESS": return "Đang khám";
            case "PAUSED_FOR_TEST": return "Đi chụp X-Quang";
            case "RETURNED_PRIORITY": return "Ưu tiên (đã chụp)";
            case "COMPLETED": return "Hoàn thành";
            default: return status;
        }
    }

    private void setCardColor(String status) {
        if (status == null) status = "WAITING";
        
        switch (status.toUpperCase()) {
            case "IN_PROGRESS":
                cardQueueStatus.setCardBackgroundColor(getColor(R.color.success_background));
                cardQueueStatus.setStrokeColor(getColor(R.color.success_green));
                cardQueueStatus.setStrokeWidth(4);
                break;
            case "RETURNED_PRIORITY":
                cardQueueStatus.setCardBackgroundColor(getColor(R.color.priority_background));
                cardQueueStatus.setStrokeColor(getColor(R.color.warning_amber));
                cardQueueStatus.setStrokeWidth(4);
                break;
            case "PAUSED_FOR_TEST":
                cardQueueStatus.setCardBackgroundColor(getColor(R.color.ortho_background));
                cardQueueStatus.setStrokeColor(getColor(R.color.primary_trust_blue));
                cardQueueStatus.setStrokeWidth(2);
                break;
            case "COMPLETED":
                cardQueueStatus.setCardBackgroundColor(getColor(R.color.success_background));
                cardQueueStatus.setStrokeColor(getColor(R.color.success_green));
                cardQueueStatus.setStrokeWidth(2);
                break;
            default: // WAITING
                cardQueueStatus.setCardBackgroundColor(getColor(android.R.color.white));
                cardQueueStatus.setStrokeColor(getColor(R.color.border_gray));
                cardQueueStatus.setStrokeWidth(1);
                break;
        }
    }
    
    // Countdown timer methods
    private void startCountdown(int seconds) {
        stopCountdown(); // Stop any existing countdown
        
        countdownEndTimeMillis = System.currentTimeMillis() + (seconds * 1000L);
        isCountdownActive = true;
        
        countdownHandler = new Handler(Looper.getMainLooper());
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdownDisplay();
                if (isCountdownActive) {
                    countdownHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };
        
        countdownHandler.post(countdownRunnable);
    }
    
    private void updateCountdownDisplay() {
        long remainingMillis = countdownEndTimeMillis - System.currentTimeMillis();
        
        if (remainingMillis <= 0) {
            // Countdown finished
            tvEstimatedTime.setText("~0 phút");
            stopCountdown();
            // Trigger refresh to get latest status
            loadQueueStatus();
            return;
        }
        
        int remainingSeconds = (int) (remainingMillis / 1000);
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        
        // Always show ~ to indicate approximate
        tvEstimatedTime.setText(String.format("~%d:%02d", minutes, seconds));
    }
    
    private void stopCountdown() {
        isCountdownActive = false;
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }
    
    private void syncCountdownWithServer(int newSeconds) {
        if (!isCountdownActive) return;
        
        long currentRemaining = (countdownEndTimeMillis - System.currentTimeMillis()) / 1000;
        long diff = Math.abs(currentRemaining - newSeconds);
        
        // Only adjust if difference > 30 seconds (avoid jitter)
        if (diff > 30) {
            countdownEndTimeMillis = System.currentTimeMillis() + (newSeconds * 1000L);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start auto refresh when activity is visible
        refreshHandler.postDelayed(refreshRunnable, AUTO_REFRESH_INTERVAL);
        // Countdown will restart from server data on next refresh
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto refresh when activity is not visible
        refreshHandler.removeCallbacks(refreshRunnable);
        // Pause countdown when not visible
        stopCountdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
        stopCountdown();
    }
}