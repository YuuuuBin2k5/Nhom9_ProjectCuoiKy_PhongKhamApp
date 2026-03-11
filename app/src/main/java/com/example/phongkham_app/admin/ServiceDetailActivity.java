package com.example.phongkham_app.admin;

import com.example.phongkham_app.R;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class ServiceDetailActivity extends AppCompatActivity {

    private EditText etServiceName, etPrice, etDescription;
    private SwitchCompat switchStatus;
    private LinearLayout layoutButtons;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_service_detail);

        initViews();
        loadServiceData();
        setupListeners();
    }

    private void initViews() {
        etServiceName = findViewById(R.id.etServiceName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        switchStatus = findViewById(R.id.switchStatus);
        layoutButtons = findViewById(R.id.layoutButtons);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadServiceData() {
        // Lấy dữ liệu từ Intent
        String serviceName = getIntent().getStringExtra("SERVICE_NAME");
        boolean isEnabled = getIntent().getBooleanExtra("SERVICE_ENABLED", true);

        // Hiển thị dữ liệu mẫu
        etServiceName.setText(serviceName != null ? serviceName : "Dịch Vụ Khám A");
        etPrice.setText("500000");
        etDescription.setText("Khám tổng quát bao gồm:\n- Đo huyết áp\n- Kiểm tra tim mạch\n- Xét nghiệm máu cơ bản\n- Tư vấn sức khỏe");
        switchStatus.setChecked(isEnabled);
    }

    private void setupListeners() {
        findViewById(R.id.btnEdit).setOnClickListener(v -> toggleEditMode());

        findViewById(R.id.btnCancel).setOnClickListener(v -> {
            toggleEditMode();
            loadServiceData(); // Reload dữ liệu gốc
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> {
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

            // TODO: Lưu dữ liệu vào database
            Toast.makeText(this, "Đã cập nhật dịch vụ", Toast.LENGTH_SHORT).show();
            toggleEditMode();
        });
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;

        etServiceName.setEnabled(isEditMode);
        etPrice.setEnabled(isEditMode);
        etDescription.setEnabled(isEditMode);
        switchStatus.setEnabled(isEditMode);

        layoutButtons.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }
}
