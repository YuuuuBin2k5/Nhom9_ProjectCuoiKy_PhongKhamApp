package com.example.phongkham_app.patient;

import com.example.phongkham_app.R;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.patient.adapter.MedicineAdapter;
import com.example.phongkham_app.patient.adapter.VisitHistoryAdapter;
import com.example.phongkham_app.patient.viewmodel.MedicalDetailViewModel;
import com.example.phongkham_app.data.model.Patient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class MedicalDetailActivity extends AppCompatActivity {

    private MedicalDetailViewModel viewModel;
    private MedicineAdapter medicineAdapter;
    private VisitHistoryAdapter visitHistoryAdapter;

    // View References
    private TextView tvPatientName, tvStatus, tvPatientId, tvBloodType, tvAge;
    private TextView tvInsuranceId, tvInsuranceExpiry, tvMedicineCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_medical_detail);

        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupViewModel();
    }

    private void initViews() {
        tvPatientName = findViewById(R.id.tvPatientName);
        tvStatus = findViewById(R.id.tvStatus);
        tvPatientId = findViewById(R.id.tvPatientId);
        tvBloodType = findViewById(R.id.tvBloodType);
        tvAge = findViewById(R.id.tvAge);
        
        tvInsuranceId = findViewById(R.id.tvInsuranceId);
        tvInsuranceExpiry = findViewById(R.id.tvInsuranceExpiry);
        tvMedicineCount = findViewById(R.id.tvMedicineCount);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerViews() {
        RecyclerView rvMedicines = findViewById(R.id.rvMedicines);
        medicineAdapter = new MedicineAdapter();
        rvMedicines.setLayoutManager(new LinearLayoutManager(this));
        rvMedicines.setAdapter(medicineAdapter);
        rvMedicines.setNestedScrollingEnabled(false);

        RecyclerView rvVisitHistory = findViewById(R.id.rvVisitHistory);
        visitHistoryAdapter = new VisitHistoryAdapter();
        rvVisitHistory.setLayoutManager(new LinearLayoutManager(this));
        rvVisitHistory.setAdapter(visitHistoryAdapter);
        rvVisitHistory.setNestedScrollingEnabled(false);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MedicalDetailViewModel.class);

        // Update Patient Info
        viewModel.getPatient().observe(this, patient -> {
            if (patient != null) {
                tvPatientName.setText(patient.getName());
                tvStatus.setText(patient.getStatus());
                // Đã sửa: Loại bỏ \n để ID không bị nhảy dòng
                tvPatientId.setText(String.format("ID: %s", patient.getId()));
                tvBloodType.setText(String.format("🩸 Nhóm máu %s", patient.getBloodType()));
                tvAge.setText(String.format(Locale.getDefault(), "🎂 Tuổi %d", patient.getAge()));
                tvInsuranceId.setText(patient.getInsuranceId());
                tvInsuranceExpiry.setText(patient.getInsuranceExpiry());
            }
        });

        // Update Medicines
        viewModel.getMedicines().observe(this, medicines -> {
            if (medicines != null) {
                medicineAdapter.setMedicines(medicines);
                tvMedicineCount.setText(String.format(Locale.getDefault(), "%02d Thuốc", medicines.size()));
            }
        });

        // Update Visit History
        viewModel.getVisitHistory().observe(this, visitHistory -> {
            if (visitHistory != null) {
                visitHistoryAdapter.setVisits(visitHistory);
            }
        });
    }
}
