package com.example.phongkham_app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;

import com.example.phongkham_app.data.local.DatabaseHelper;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Database to create and seed tables
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.getWritableDatabase(); // Trigger onCreate/onUpgrade

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 2000);
    }
}
