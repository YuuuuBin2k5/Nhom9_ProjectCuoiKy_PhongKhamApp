package com.example.phongkham_app.ui.auth;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView tvLogin = findViewById(R.id.tv_login);
        tvLogin.setOnClickListener(v -> finish());
    }
}
