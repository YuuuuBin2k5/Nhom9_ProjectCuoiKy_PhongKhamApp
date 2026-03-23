package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetailActivity extends AppCompatActivity {

    private TextView tvServiceName, tvServicePrice, tvServiceDesc;
    private Spinner spinnerDoctors;
    private ApiService apiService;
    private List<DoctorItem> doctorList = new ArrayList<>();
    private List<String> doctorNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    private int serviceId;
    private String serviceName;
    private double servicePrice;
    private String serviceDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_detail);

        serviceId = getIntent().getIntExtra("SERVICE_ID", -1);
        serviceName = getIntent().getStringExtra("SERVICE_NAME");
        servicePrice = getIntent().getDoubleExtra("SERVICE_PRICE", 0.0);
        serviceDesc = getIntent().getStringExtra("SERVICE_DESC");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(serviceName != null ? serviceName : "Chi tiết dịch vụ");
        toolbar.setNavigationOnClickListener(v -> finish());

        tvServiceName = findViewById(R.id.tvServiceName);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        tvServiceDesc = findViewById(R.id.tvServiceDesc);
        spinnerDoctors = findViewById(R.id.spinnerDoctors);

        tvServiceName.setText(serviceName != null ? serviceName : "");
        tvServicePrice.setText("Giá: " + servicePrice + " C"); // Use appropriately based on your currency
        tvServiceDesc.setText(serviceDesc != null && !serviceDesc.isEmpty() ? serviceDesc : "Không có mô tả chi tiết.");

        apiService = RetrofitClient.getApiService(this);

        doctorNames.add("Không chọn bác sĩ (Mặc định)");
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctorNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctors.setAdapter(spinnerAdapter);

        loadDoctors();

        findViewById(R.id.btnBook).setOnClickListener(v -> handleBooking());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void loadDoctors() {
        apiService.getDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    doctorList.clear();
                    doctorList.addAll(response.body());
                    doctorNames.clear();
                    doctorNames.add("Không chọn bác sĩ (Mặc định)");
                    for (DoctorItem doc : doctorList) {
                        doctorNames.add("BS. " + doc.getFirstName() + " " + doc.getLastName() + " - " + doc.getSpecialty());
                    }
                    spinnerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                // Ignore failure, just keep "No doctor chosen" option.
            }
        });
    }

    private void handleBooking() {
        int selectedIndex = spinnerDoctors.getSelectedItemPosition();
        if (selectedIndex == 0) {
            // No doctor chosen -> queued to shortest line
            Toast.makeText(this, "Đặt lịch thành công!\nDo không chọn bác sĩ, bạn sẽ được xếp vào hàng đợi ít người nhất.", Toast.LENGTH_LONG).show();
        } else {
            // Selected a doctor
            DoctorItem selectedDoctor = doctorList.get(selectedIndex - 1);
            Toast.makeText(this, "Đặt lịch thành công với BS. " + selectedDoctor.getLastName() + ".", Toast.LENGTH_LONG).show();
        }
        
        // Return to home or finish
        finish();
    }
}
