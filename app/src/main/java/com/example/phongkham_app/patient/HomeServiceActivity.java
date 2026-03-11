package com.example.phongkham_app.patient;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;
import com.google.android.material.appbar.MaterialToolbar;

public class HomeServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_service);
        
        MaterialToolbar topBar = findViewById(R.id.topBar);
        if (topBar != null) {
            topBar.setNavigationOnClickListener(v -> finish());
        }
    }
}
