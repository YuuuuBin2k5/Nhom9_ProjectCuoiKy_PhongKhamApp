package com.example.phongkham_app.ui.patient;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Service;
import com.example.phongkham_app.data.repository.ServiceRepository;
import com.example.phongkham_app.ui.patient.adapter.ServiceAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class HomeServiceActivity extends AppCompatActivity {

    private ServiceAdapter adapter;
    private List<Service> allServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_service);
        
        MaterialToolbar topBar = findViewById(R.id.topBar);
        if (topBar != null) {
            topBar.setNavigationOnClickListener(v -> finish());
        }

        RecyclerView rvServices = findViewById(R.id.rvServicesAll);
        adapter = new ServiceAdapter((service, pos) -> {});
        if (rvServices != null) rvServices.setAdapter(adapter);

        ServiceRepository repo = ServiceRepository.getInstance(this);
        allServices = repo.getServices();
        adapter.setServices(allServices);

        TextInputEditText searchBox = findViewById(R.id.searchBox);
        if (searchBox != null) {
            searchBox.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().toLowerCase();
                    List<Service> filtered = new ArrayList<>();
                    for (Service svc : allServices) {
                        if (svc.getName().toLowerCase().contains(query)) filtered.add(svc);
                    }
                    adapter.updateData(filtered);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }
}
