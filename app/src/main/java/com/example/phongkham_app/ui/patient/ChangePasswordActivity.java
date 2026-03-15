package com.example.phongkham_app.ui.patient;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.phongkham_app.R;
import com.google.android.material.appbar.MaterialToolbar;

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        findViewById(R.id.btnUpdatePassword).setOnClickListener(v -> {
            Toast.makeText(this, "Mật khẩu đã được thay đổi", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
