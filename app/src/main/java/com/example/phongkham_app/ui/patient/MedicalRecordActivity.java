package com.example.phongkham_app.ui.patient;

import com.example.phongkham_app.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.ui.patient.adapter.DoctorAdapter;
import com.example.phongkham_app.data.model.Doctor;
import com.example.phongkham_app.data.model.Patient;
import com.example.phongkham_app.ui.patient.viewmodel.MedicalRecordViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class MedicalRecordActivity extends AppCompatActivity implements DoctorAdapter.OnDoctorActionListener {

    private MedicalRecordViewModel viewModel;
    private DoctorAdapter adapter;

    // View References
    private TextView tvPatientName, tvPatientId, tvBloodType, tvAge, tvPatientStatus;
    private RecyclerView rvDoctors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.patient_activity_medical_record);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
    }

    private void initViews() {
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientId = findViewById(R.id.tvPatientId);
        tvBloodType = findViewById(R.id.tvBloodType);
        tvAge = findViewById(R.id.tvAge);
        tvPatientStatus = findViewById(R.id.tvPatientStatus);
        
        rvDoctors = findViewById(R.id.rvDoctors);
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

    private void setupRecyclerView() {
        adapter = new DoctorAdapter(this);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        rvDoctors.setAdapter(adapter);
        rvDoctors.setNestedScrollingEnabled(false);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MedicalRecordViewModel.class);

        viewModel.getPatient().observe(this, this::updatePatientUI);
        
        viewModel.getDoctors().observe(this, doctors -> {
            if (doctors != null) {
                adapter.setDoctors(doctors);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePatientUI(Patient patient) {
        if (patient != null) {
            tvPatientName.setText(patient.getName());
            tvPatientId.setText(String.format(getString(R.string.label_patient_id), patient.getId()));
            tvBloodType.setText(String.format(getString(R.string.label_blood_type), patient.getBloodType()));
            tvAge.setText(String.format(getString(R.string.label_age), patient.getAge()));
            tvPatientStatus.setText(patient.getStatus());
        }
    }

    private void setupClickListeners() {
        TextView tvViewDetail = findViewById(R.id.tvViewDetail);
        tvViewDetail.setOnClickListener(v -> {
            // Navigate to Medical Detail
            startActivity(new Intent(MedicalRecordActivity.this, MedicalDetailActivity.class));
        });

        TextView tvViewAll = findViewById(R.id.tvViewAll);
        tvViewAll.setOnClickListener(v -> {
            startActivity(new Intent(MedicalRecordActivity.this, HomeDoctorActivity.class));
        });
    }

    @Override
    public void onViewDetailClick(Doctor doctor) {
        Intent intent = new Intent(this, DoctorDetailActivity.class);
        intent.putExtra(DoctorDetailActivity.EXTRA_DOCTOR_ID, doctor.getId());
        intent.putExtra(DoctorDetailActivity.EXTRA_DOCTOR_NAME, doctor.getName());
        intent.putExtra(DoctorDetailActivity.EXTRA_DOCTOR_SPECIALTY, doctor.getSpecialty());
        intent.putExtra(DoctorDetailActivity.EXTRA_DOCTOR_RATING, doctor.getRating());
        intent.putExtra(DoctorDetailActivity.EXTRA_DOCTOR_REVIEW_COUNT, doctor.getReviewCount());
        startActivity(intent);
    }

    @Override
    public void onAddReviewClick(Doctor doctor) {
        Intent intent = new Intent(this, DoctorReviewActivity.class);
        intent.putExtra("doctor_id", doctor.getId());
        intent.putExtra("doctor_name", doctor.getName());
        intent.putExtra("doctor_specialty", doctor.getSpecialty());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Doctor doctor) {
        viewModel.toggleFavorite(doctor.getId());
    }
}
