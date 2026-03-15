package com.example.phongkham_app.ui.patient;

import com.example.phongkham_app.R;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.phongkham_app.ui.patient.viewmodel.DoctorReviewViewModel;
import com.google.android.material.snackbar.Snackbar;

public class DoctorReviewActivity extends AppCompatActivity {

    private DoctorReviewViewModel viewModel;

    // UI elements
    private TextView tvDoctorName, tvSpecialty;
    private ImageView[] stars;
    private EditText etReview;
    private Button btnSubmitReview;
    private TextView tvSkip;
    private ImageView btnBack;
    private android.view.View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_doctor_review);

        initViews();
        setupViewModel();
        setupClickListeners();
        
        // Load data from Intent
        int doctorId = getIntent().getIntExtra("doctor_id", -1);
        String doctorName = getIntent().getStringExtra("doctor_name");
        String specialty = getIntent().getStringExtra("doctor_specialty");
        
        if (doctorName == null) doctorName = "BS. Trần Hoàng Nam"; // Fallback
        if (specialty == null) specialty = "Nội tổng quát"; // Fallback
        
        viewModel.setDoctorInfo(doctorId, doctorName, specialty);
    }

    private void initViews() {
        rootLayout = findViewById(android.R.id.content);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvSpecialty = findViewById(R.id.tvSpecialty);
        etReview = findViewById(R.id.etReview);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        tvSkip = findViewById(R.id.tvSkip);
        btnBack = findViewById(R.id.btnBack);

        stars = new ImageView[]{
                findViewById(R.id.star1),
                findViewById(R.id.star2),
                findViewById(R.id.star3),
                findViewById(R.id.star4),
                findViewById(R.id.star5)
        };
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DoctorReviewViewModel.class);

        viewModel.getDoctorName().observe(this, name -> tvDoctorName.setText(name));
        viewModel.getSpecialty().observe(this, spec -> tvSpecialty.setText(spec));

        viewModel.getSelectedRating().observe(this, rating -> updateStarUI(rating));

        viewModel.getSubmitSuccess().observe(this, success -> {
            if (success) {
                Snackbar.make(rootLayout, "Cảm ơn bạn đã đánh giá!", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.success, getTheme()))
                        .setTextColor(getResources().getColor(R.color.white, getTheme()))
                        .show();
                btnSubmitReview.postDelayed(this::finish, 1500);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Snackbar.make(rootLayout, error, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.error, getTheme()))
                        .setTextColor(getResources().getColor(R.color.white, getTheme()))
                        .show();
            }
        });
    }

    private void updateStarUI(int rating) {
        int starFilledTint = getResources().getColor(R.color.star_filled, getTheme());
        int starEmptyTint = getResources().getColor(R.color.star_empty, getTheme());

        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars[i].setImageResource(android.R.drawable.star_big_on);
                stars[i].setColorFilter(starFilledTint);
            } else {
                stars[i].setImageResource(android.R.drawable.star_big_off);
                stars[i].setColorFilter(starEmptyTint);
            }
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        tvSkip.setOnClickListener(v -> finish());

        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> viewModel.selectRating(rating));
        }

        btnSubmitReview.setOnClickListener(v -> viewModel.submitReview(etReview.getText().toString()));
    }
}
