package com.example.phongkham_app.ui.auth;

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

        com.example.phongkham_app.data.local.DatabaseHelper dbHelper = new com.example.phongkham_app.data.local.DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            // 1. Kiểm tra Database thực
            android.database.Cursor cursor = dbHelper.login(user, pass);
            if (cursor != null && cursor.moveToFirst()) {
                int roleIndex = cursor.getColumnIndex("role");
                int idIndex = cursor.getColumnIndex("id");
                
                String role = roleIndex != -1 ? cursor.getString(roleIndex) : "USER";
                long userId = idIndex != -1 ? cursor.getLong(idIndex) : -1;

                // Ghi nhận session
                getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                    .putLong("USER_ID", userId)
                    .putString("USER_ROLE", role)
                    .apply();

                cursor.close();

                if ("ADMIN".equals(role)) {
                    startActivity(new Intent(LoginActivity.this, com.example.phongkham_app.ui.admin.AdminHomeActivity.class));
                } else if ("DOCTOR".equals(role)) {
                    startActivity(new Intent(LoginActivity.this, com.example.phongkham_app.ui.doctor.MainDoctor.class));
                } else {
                    startActivity(new Intent(LoginActivity.this, com.example.phongkham_app.ui.patient.Home_Activity_Home.class));
                }
                finish();
                return;
            }
            if (cursor != null) cursor.close();

            // 2. Chế độ Fallback cũ (Mã cứng) để demo nhanh
            if (user.equals("admin") && pass.equals("admin")) {
                startActivity(new Intent(LoginActivity.this, com.example.phongkham_app.ui.admin.AdminHomeActivity.class));
                finish();
            } else if (user.equals("doctor") && pass.equals("doctor")) {
                startActivity(new Intent(LoginActivity.this, com.example.phongkham_app.ui.doctor.MainDoctor.class));
                finish();
            } else if (user.equals("patient") && pass.equals("patient")) {
                // Patient home activity
                startActivity(new Intent(LoginActivity.this, com.example.phongkham_app.ui.patient.Home_Activity_Home.class));
                finish();
            } else {
                Toast.makeText(this, "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
