package com.example.phongkham_app.ui.admin;

import com.example.phongkham_app.R;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddServiceActivity extends AppCompatActivity {

    private EditText etServiceName, etPrice, etDescription;
    private Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_add_service);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String serviceName = etServiceName.getText().toString().trim();
            String price = etPrice.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (serviceName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên dịch vụ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (price.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập giá", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Lưu dịch vụ vào database
            Toast.makeText(this, "Đã thêm dịch vụ: " + serviceName, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
