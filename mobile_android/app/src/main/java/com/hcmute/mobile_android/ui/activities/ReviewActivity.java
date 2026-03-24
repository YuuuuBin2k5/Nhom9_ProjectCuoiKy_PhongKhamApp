package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.hcmute.mobile_android.R;

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review);

        String doctorName = getIntent().getStringExtra("DOCTOR_NAME");
        String serviceName = getIntent().getStringExtra("SERVICE_NAME");

        if (doctorName == null) doctorName = "BS. Nguyễn Văn A";
        if (serviceName == null) serviceName = "Khám tổng quát";

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvDoctorName = findViewById(R.id.tvDoctorName);
        TextView tvServiceName = findViewById(R.id.tvServiceName);
        tvDoctorName.setText(doctorName);
        tvServiceName.setText(serviceName);

        RatingBar ratingBar = findViewById(R.id.ratingBar);
        TextInputEditText etComment = findViewById(R.id.etComment);

        findViewById(R.id.btnSubmitReview).setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            Toast.makeText(this, "Đã gửi đánh giá " + rating + " sao!", Toast.LENGTH_SHORT).show();
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
