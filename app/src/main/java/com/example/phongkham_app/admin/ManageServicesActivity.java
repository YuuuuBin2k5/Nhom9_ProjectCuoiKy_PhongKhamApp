package com.example.phongkham_app.admin;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Service;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageServicesActivity extends AppCompatActivity {

    private RecyclerView rvServices;
    private ServiceAdapter adapter;
    private List<Service> serviceList;
    private List<Service> filteredList;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_manage_services);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadServices();
    }

    private void initViews() {
        rvServices = findViewById(R.id.rvServices);
        etSearch = findViewById(R.id.etSearch);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            Intent intent = new Intent(ManageServicesActivity.this, AddServiceActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        serviceList = new ArrayList<>();
        filteredList = new ArrayList<>();
        
        adapter = new ServiceAdapter(filteredList, this::onServiceToggled);
        adapter.setOnServiceClickListener(service -> {
            Intent intent = new Intent(ManageServicesActivity.this, ServiceDetailActivity.class);
            intent.putExtra("SERVICE_NAME", service.getName());
            intent.putExtra("SERVICE_ENABLED", service.isEnabled());
            startActivity(intent);
        });
        
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterServices(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadServices() {
        // Dữ liệu mẫu
        serviceList.add(new Service("Dịch Vụ Khám A", true, "500,000 VNĐ"));
        serviceList.add(new Service("Dịch Vụ Khám B", true, "300,000 VNĐ"));
        serviceList.add(new Service("Dịch Vụ Khám C", true, "750,000 VNĐ"));
        serviceList.add(new Service("Dịch Vụ Khám D", false, "1,000,000 VNĐ"));
        serviceList.add(new Service("Dịch Vụ Khám E", false, "450,000 VNĐ"));
        serviceList.add(new Service("Dịch Vụ Khám F", true, "600,000 VNĐ"));
        serviceList.add(new Service("Dịch Vụ Khám G", false, "850,000 VNĐ"));
        serviceList.add(new Service("Dịch Vụ Khám H", true, "400,000 VNĐ"));

        filteredList.addAll(serviceList);
        adapter.notifyDataSetChanged();
    }

    private void filterServices(String query) {
        filteredList.clear();
        
        if (query.isEmpty()) {
            filteredList.addAll(serviceList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Service service : serviceList) {
                if (service.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(service);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
    }

    private void onServiceToggled(Service service, boolean isEnabled) {
        service.setEnabled(isEnabled);
        String message = service.getName() + (isEnabled ? " đã bật" : " đã tắt");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
