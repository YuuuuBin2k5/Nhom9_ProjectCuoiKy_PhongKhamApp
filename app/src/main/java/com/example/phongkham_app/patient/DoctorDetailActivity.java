package com.example.phongkham_app.patient;

import com.example.phongkham_app.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;

public class DoctorDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DOCTOR_ID = "doctor_id";
    public static final String EXTRA_DOCTOR_NAME = "doctor_name";
    public static final String EXTRA_DOCTOR_SPECIALTY = "doctor_specialty";
    public static final String EXTRA_DOCTOR_RATING = "doctor_rating";
    public static final String EXTRA_DOCTOR_REVIEW_COUNT = "doctor_review_count";

    private TextView tvDoctorName, tvSpecialty, tvRating, tvReviewCount;
    private TextView tvExperience, tvPatientCount, tvReviewStat, tvAbout;
    private Button btnBookAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_doctor_detail);

        initViews();
        setupToolbar();
        loadDoctorData();
        setupClickListeners();
    }

    private void initViews() {
        tvDoctorName      = findViewById(R.id.tvDoctorName);
        tvSpecialty       = findViewById(R.id.tvSpecialty);
        tvRating          = findViewById(R.id.tvRating);
        tvReviewCount     = findViewById(R.id.tvReviewCount);
        tvExperience      = findViewById(R.id.tvExperience);
        tvPatientCount    = findViewById(R.id.tvPatientCount);
        tvReviewStat      = findViewById(R.id.tvReviewStat);
        tvAbout           = findViewById(R.id.tvAbout);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadDoctorData() {
        Intent intent = getIntent();
        String doctorId       = intent.getStringExtra(EXTRA_DOCTOR_ID);
        String doctorName     = intent.getStringExtra(EXTRA_DOCTOR_NAME);
        String doctorSpecialty = intent.getStringExtra(EXTRA_DOCTOR_SPECIALTY);
        double rating         = intent.getDoubleExtra(EXTRA_DOCTOR_RATING, 4.5);
        int reviewCount       = intent.getIntExtra(EXTRA_DOCTOR_REVIEW_COUNT, 0);

        // Set basic info
        tvDoctorName.setText(doctorName != null ? doctorName : "Bác sĩ");
        tvSpecialty.setText(doctorSpecialty != null ? doctorSpecialty : "Chuyên khoa");
        tvRating.setText(String.valueOf(rating));
        tvReviewCount.setText(String.format("(%d đánh giá)", reviewCount));

        // Set mock statistics based on specialty (in real app, fetch from API/DB)
        loadMockDoctorStats(doctorId, doctorSpecialty);
    }

    private void loadMockDoctorStats(String doctorId, String specialty) {
        // Mock data - thay bằng API call thực tế
        int experience = 8;
        int patientCount = 350;
        int reviewStat = 124;
        String about = "Bác sĩ tốt nghiệp loại Giỏi tại Đại học Y Hà Nội, có nhiều năm kinh nghiệm trong lĩnh vực "
                + (specialty != null ? specialty.toLowerCase() : "chuyên khoa")
                + ". Được đào tạo chuyên sâu tại các bệnh viện hàng đầu trong và ngoài nước. "
                + "Bác sĩ luôn tận tâm, chu đáo trong việc chăm sóc và điều trị bệnh nhân.";

        if (doctorId != null) {
            switch (doctorId) {
                case "D001":
                    experience = 12; patientCount = 520; reviewStat = 245;
                    break;
                case "D002":
                    experience = 15; patientCount = 680; reviewStat = 312;
                    break;
                case "D003":
                    experience = 7; patientCount = 290; reviewStat = 98;
                    break;
            }
        }

        tvExperience.setText(experience + "+");
        tvPatientCount.setText(patientCount + "+");
        tvReviewStat.setText(String.valueOf(reviewStat));
        tvAbout.setText(about);
    }

    private void setupClickListeners() {
        btnBookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDetailActivity.this, BookingActivity.class);
            // Pass doctor info to booking screen
            intent.putExtra("doctor_id", getIntent().getStringExtra(EXTRA_DOCTOR_ID));
            intent.putExtra("doctor_name", getIntent().getStringExtra(EXTRA_DOCTOR_NAME));
            startActivity(intent);
        });
    }
}
