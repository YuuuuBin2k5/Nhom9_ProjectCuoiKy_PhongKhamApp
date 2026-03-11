package com.example.phongkham_app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;

public class LoginActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvSignup = findViewById(R.id.tv_signup);
        
        // Setup eye toggle
        // ImageView ivEye = findViewById(R.id.iv_eye); (Need to add ID in layout... just an example, leaving out toggle logic for simplicity)

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.equals("admin") && pass.equals("admin")) {
                startActivity(new Intent(LoginActivity.this, com.example.phongkham_app.admin.MainActivity.class));
                finish();
            } else if (user.equals("doctor") && pass.equals("doctor")) {
                startActivity(new Intent(LoginActivity.this, com.example.phongkham_app.doctor.MainDoctor.class));
                finish();
            } else if (user.equals("patient") && pass.equals("patient")) {
                // Patient home activity
                startActivity(new Intent(LoginActivity.this, com.example.phongkham_app.patient.Home_Activity_Home.class));
                finish();
            } else {
                Toast.makeText(this, "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
