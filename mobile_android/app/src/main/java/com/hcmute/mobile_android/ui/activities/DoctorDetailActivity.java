package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.hcmute.mobile_android.BuildConfig;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.DoctorReviewAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorDetailResponse;
import com.hcmute.mobile_android.network.models.Review;
import com.hcmute.mobile_android.network.models.ReviewRequest;
import android.widget.RatingBar;
import android.widget.EditText;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorDetailActivity extends AppCompatActivity {

    private Long doctorId;
    private String doctorName;
    private String specialization;

    private TextView tvName;
    private TextView tvSpec;
    private TextView tvMeta;
    private TextView tvAbout;
    private TextView tvReviewsEmpty;
    private ProgressBar progress;
    private ShapeableImageView ivAvatar;
    private DoctorReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail);

        doctorName = getIntent().getStringExtra("doctorName");
        specialization = getIntent().getStringExtra("specialization");
        doctorId = getIntent().getLongExtra("doctorId", -1L);

        if (doctorId <= 0) {
            Toast.makeText(this, "Thiếu thông tin bác sĩ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadFromApi();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvName = findViewById(R.id.tvDoctorNameDetail);
        tvSpec = findViewById(R.id.tvSpecializationDetail);
        tvMeta = findViewById(R.id.tvDoctorMeta);
        tvAbout = findViewById(R.id.tvDoctorAbout);
        tvReviewsEmpty = findViewById(R.id.tvReviewsEmpty);
        progress = findViewById(R.id.progressDoctorDetail);
        ivAvatar = findViewById(R.id.ivDoctorAvatar);

        RecyclerView rvReviews = findViewById(R.id.rvDoctorReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new DoctorReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);

        if (doctorName != null) tvName.setText(doctorName);
        if (specialization != null) tvSpec.setText(specialization);

        findViewById(R.id.btnActionChat).setOnClickListener(v -> openChat());
        findViewById(R.id.btnBookWithDoctor).setOnClickListener(v -> {
            Intent intent = new Intent(this, BookAppointmentActivity.class);
            intent.putExtra("doctorId", doctorId);
            intent.putExtra("doctorName", doctorName != null ? doctorName : "");
            startActivity(intent);
        });

        View btnAddReview = findViewById(R.id.btnAddReview);
        if (btnAddReview != null) {
            btnAddReview.setOnClickListener(v -> showAddReviewDialog());
        }
    }

    private void showAddReviewDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_review, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBarReview);
        EditText etComment = dialogView.findViewById(R.id.etReviewComment);
        View btnCancel = dialogView.findViewById(R.id.btnCancelReview);
        View btnSubmit = dialogView.findViewById(R.id.btnSubmitReview);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String comment = etComment.getText().toString().trim();
            if (comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            submitReview(rating, comment);
        });

        dialog.show();
    }

    private void submitReview(int rating, String comment) {
        progress.setVisibility(View.VISIBLE);
        ReviewRequest req = new ReviewRequest(null, doctorId, null, rating, comment);
        RetrofitClient.getApiService(this).createReview(req).enqueue(new Callback<Review>() {
            @Override
            public void onResponse(@NonNull Call<Review> call, @NonNull Response<Review> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DoctorDetailActivity.this, "Đã gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    loadFromApi();
                } else {
                    Toast.makeText(DoctorDetailActivity.this, "Lỗi, bạn đã đánh giá bác sĩ này rồ? Hoặc phiên bản chưa hoàn thiện.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Review> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(DoctorDetailActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("doctorId", doctorId);
        intent.putExtra("doctorName", doctorName != null ? doctorName : tvName.getText().toString());
        startActivity(intent);
    }

    private void loadFromApi() {
        progress.setVisibility(View.VISIBLE);
        ApiService api = RetrofitClient.getApiService(this);
        AtomicInteger remaining = new AtomicInteger(2);

        Runnable done = () -> {
            if (remaining.decrementAndGet() == 0) {
                progress.setVisibility(View.GONE);
            }
        };

        api.getDoctorDetail(doctorId).enqueue(new Callback<DoctorDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<DoctorDetailResponse> call, @NonNull Response<DoctorDetailResponse> response) {
                done.run();
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(DoctorDetailActivity.this, "Không tải được hồ sơ bác sĩ", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                applyDoctor(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<DoctorDetailResponse> call, @NonNull Throwable t) {
                done.run();
                Toast.makeText(DoctorDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        api.getDoctorReviews(doctorId).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(@NonNull Call<List<Review>> call, @NonNull Response<List<Review>> response) {
                done.run();
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> list = response.body();
                    reviewAdapter.setItems(list);
                    boolean empty = list == null || list.isEmpty();
                    tvReviewsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                } else {
                    tvReviewsEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Review>> call, @NonNull Throwable t) {
                done.run();
                tvReviewsEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(DoctorDetailActivity.this, "Không tải được đánh giá", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyDoctor(DoctorDetailResponse d) {
        doctorName = d.getDisplayName();
        specialization = d.getSpecialization() != null ? d.getSpecialization() : "";
        tvName.setText(doctorName);
        tvSpec.setText(specialization.isEmpty() ? "—" : specialization);

        int exp = d.getExperienceYears() != null ? d.getExperienceYears() : 0;
        String bio = d.getBiography() != null ? d.getBiography().trim() : "";
        
        // Dynamically override hardcoded/identical backend descriptions
        if (bio.isEmpty() || bio.contains("hơn 10 năm kinh nghiệm")) {
            String expText = exp > 0 ? ("cùng với " + exp + " năm kinh nghiệm") : "với nhiều năm kinh nghiệm";
            bio = "Bác sĩ " + doctorName + " là một chuyên gia tận tâm trong chuyên khoa " + 
                  (specialization.isEmpty() ? "Nha khoa" : specialization) + ", " + expText + 
                  " điều trị các ca bệnh từ cơ bản đến phức tạp. Bác sĩ luôn đặt nụ cười của bệnh nhân lên hàng đầu.";
        }
        tvAbout.setText(bio);

        String room = d.getRoomName() != null ? d.getRoomName() : "";
        long appts = d.getAppointmentCount();
        StringBuilder meta = new StringBuilder();
        if (exp > 0) {
            meta.append(exp).append(" năm KN");
        }
        if (!room.isEmpty()) {
            if (meta.length() > 0) meta.append(" · ");
            meta.append(room);
        }
        if (appts >= 0) {
            if (meta.length() > 0) meta.append(" · ");
            meta.append(appts).append(" lịch đã đặt");
        }
        tvMeta.setText(meta.length() > 0 ? meta.toString() : "Phòng khám");

        // Gender heuristic for fallback avatars
        String nameL = doctorName.toLowerCase();
        boolean isFemale = nameL.matches(".*\\b(hà|thu|mai|trang|lan|thị|nữ|tuyết|hoa|hoà|my|ngọc|hạnh)\\b.*");
        
        int[] maleAvatars = { R.drawable.doc1, R.drawable.doc3, R.drawable.doc5 };
        int[] femaleAvatars = { R.drawable.doc2, R.drawable.doc4 };
        
        int fallback = R.drawable.ic_doctor;
        if (d.getId() != null) {
            fallback = isFemale 
                ? femaleAvatars[(int)(Math.abs(d.getId()) % femaleAvatars.length)]
                : maleAvatars[(int)(Math.abs(d.getId()) % maleAvatars.length)];
        }

        String avatar = resolveMediaUrl(d.getAvatarUrl());
        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this).load(avatar)
                 .centerCrop()
                 .placeholder(fallback)
                 .error(fallback)
                 .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(fallback);
        }
    }

    /** Ghép base URL nếu server trả đường dẫn tương đối. */
    private String resolveMediaUrl(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        String base = BuildConfig.API_BASE_URL;
        if (!base.endsWith("/")) base = base + "/";
        String p = path.startsWith("/") ? path.substring(1) : path;
        return base + p;
    }
}
