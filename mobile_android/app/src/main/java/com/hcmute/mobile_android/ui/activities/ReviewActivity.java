package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.ReviewRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {
    
    private TextView tvDoctorName, tvServiceName, tvAppointmentDate, tvRatingText;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;
    private ProgressBar progressBar;
    
    private Long appointmentId;
    private Long doctorId;
    private Long serviceId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        
        appointmentId = getIntent().getLongExtra("appointmentId", 0);
        doctorId = getIntent().getLongExtra("doctorId", 0);
        serviceId = getIntent().getLongExtra("serviceId", 0);
        
        setupViews();
        displayAppointmentInfo();
    }
    
    private void setupViews() {
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvRatingText = findViewById(R.id.tvRatingText);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đánh giá");
        }
        
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            updateRatingText((int) rating);
        });
        
        btnSubmit.setOnClickListener(v -> submitReview());
    }
    
    private void displayAppointmentInfo() {
        String doctorName = getIntent().getStringExtra("doctorName");
        String serviceName = getIntent().getStringExtra("serviceName");
        String appointmentDate = getIntent().getStringExtra("appointmentDate");
        
        tvDoctorName.setText("Bác sĩ: " + doctorName);
        tvServiceName.setText("Dịch vụ: " + serviceName);
        tvAppointmentDate.setText("Ngày: " + appointmentDate);
    }
    
    private void updateRatingText(int rating) {
        String[] ratingTexts = {
            "Rất tệ",
            "Tệ",
            "Trung bình",
            "Tốt",
            "Xuất sắc"
        };
        
        if (rating > 0 && rating <= 5) {
            tvRatingText.setText(ratingTexts[rating - 1]);
        }
    }
    
    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();
        
        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        
        ReviewRequest request = new ReviewRequest(
            appointmentId,
            doctorId,
            serviceId,
            rating,
            comment
        );
        
        ApiService apiService = RetrofitClient.getApiService(this);
        Call<com.hcmute.mobile_android.network.models.Review> call = apiService.createReview(request);
        
        call.enqueue(new Callback<com.hcmute.mobile_android.network.models.Review>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.Review> call, Response<com.hcmute.mobile_android.network.models.Review> response) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                
                if (response.isSuccessful()) {
                    showSuccessDialog();
                } else {
                    Toast.makeText(ReviewActivity.this, 
                        "Lỗi gửi đánh giá", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.Review> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(ReviewActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Thành công")
            .setMessage("Cảm ơn bạn đã đánh giá!")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
