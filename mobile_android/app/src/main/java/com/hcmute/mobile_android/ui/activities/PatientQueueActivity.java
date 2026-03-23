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
            
            // Update queue info
            tvQueueNumber.setText(String.valueOf(status.getQueueNumber()));
            tvQueuePosition.setText("Vị trí: " + status.getQueuePosition());
            tvEstimatedTime.setText("Ước tính: " + status.getEstimatedWaitTime() + " phút");
            tvRoomName.setText(status.getRoomName() != null ? status.getRoomName() : "Phòng khám");
            tvStatus.setText(getStatusDisplay(status.getStatus()));
            
            // Set card color based on status
            setCardColor(status.getStatus());
            
        } else {
            showNotCheckedIn();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        // Start auto refresh when activity is visible
        refreshHandler.postDelayed(refreshRunnable, AUTO_REFRESH_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto refresh when activity is not visible
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
}