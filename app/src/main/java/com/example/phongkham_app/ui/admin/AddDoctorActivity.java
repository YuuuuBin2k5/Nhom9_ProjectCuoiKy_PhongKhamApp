package com.example.phongkham_app.ui.admin;

import com.example.phongkham_app.R;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddDoctorActivity extends AppCompatActivity {

    private EditText etDoctorName, etSpecialty, etPhone, etEmail, etUsername, etPassword;
    private Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_add_doctor);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etDoctorName = findViewById(R.id.etDoctorName);
//        etSpecialty = findViewById(R.id.etSpecialty);
//        etPhone = findViewById(R.id.etPhone);
//        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String doctorName = etDoctorName.getText().toString().trim();
            String specialty = etSpecialty.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (doctorName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ và tên", Toast.LENGTH_SHORT).show();
                return;
            }

            if (specialty.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập chuyên khoa", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Lưu thông tin bác sĩ và tài khoản vào database
            Toast.makeText(this, "Đã thêm bác sĩ: " + doctorName + "\nTài khoản: " + username, Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
