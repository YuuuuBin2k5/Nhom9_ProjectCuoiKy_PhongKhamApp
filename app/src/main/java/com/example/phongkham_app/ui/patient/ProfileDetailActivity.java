package com.example.phongkham_app.ui.patient;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.phongkham_app.R;
import com.google.android.material.appbar.MaterialToolbar;

public class ProfileDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            Toast.makeText(this, "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
