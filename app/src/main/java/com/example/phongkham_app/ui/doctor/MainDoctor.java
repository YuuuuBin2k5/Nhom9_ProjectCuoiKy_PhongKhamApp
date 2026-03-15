package com.example.phongkham_app.ui.doctor;

import com.example.phongkham_app.R;
import com.example.phongkham_app.ui.auth.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.ui.doctor.adapter.UpcomingAppointmentAdapter;
import com.example.phongkham_app.ui.doctor.adapter.WaitingPatientAdapter;
import com.example.phongkham_app.ui.doctor.viewmodel.DoctorMainViewModel;
import com.example.phongkham_app.data.model.Appointment;
import com.example.phongkham_app.data.model.WaitingPatient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class MainDoctor extends AppCompatActivity {

    private DoctorMainViewModel viewModel;
    private UpcomingAppointmentAdapter upcomingAdapter;
    private WaitingPatientAdapter waitingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_activity_main_doctor);

        viewModel = new ViewModelProvider(this).get(DoctorMainViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupRecyclerViews();
        setupNavigation();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerViews() {
        // Upcoming Appointments
        RecyclerView rvUpcoming = findViewById(R.id.rvUpcomingSchedule);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        upcomingAdapter = new UpcomingAppointmentAdapter(new ArrayList<>());
        rvUpcoming.setAdapter(upcomingAdapter);
        
        upcomingAdapter.setOnItemClickListener(position -> {
            Intent intent = new Intent(MainDoctor.this, ProfilePatient.class);
            intent.putExtra("PATIENT_NAME", viewModel.getUpcomingAppointments().getValue().get(position).getPatientName());
            startActivity(intent);
        });

        // Waiting Patients
        RecyclerView rvWaiting = findViewById(R.id.rvWaitingPatients);
        rvWaiting.setLayoutManager(new LinearLayoutManager(this));
        waitingAdapter = new WaitingPatientAdapter(new ArrayList<>());
        rvWaiting.setAdapter(waitingAdapter);

        waitingAdapter.setOnItemClickListener(position -> {
            Intent intent = new Intent(MainDoctor.this, ProfilePatient.class);
            intent.putExtra("PATIENT_NAME", viewModel.getWaitingPatients().getValue().get(position).getName());
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getUpcomingAppointments().observe(this, appointments -> {
            if (appointments != null) {
                upcomingAdapter.updateData(appointments);
            }
        });

        viewModel.getWaitingPatients().observe(this, patients -> {
            if (patients != null) {
                waitingAdapter.updateData(patients);
            }
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.btnQrScan).setOnClickListener(v -> 
            startActivity(new Intent(this, com.example.phongkham_app.ui.common.QRScanActivity.class)));

        findViewById(R.id.btnNotification).setOnClickListener(v -> 
            Toast.makeText(this, "Chức năng thông báo sắp ra mắt!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.tvViewAll).setOnClickListener(v -> 
            Toast.makeText(this, "Xem tất cả lịch hẹn.", Toast.LENGTH_SHORT).show());
        
        View ivLogout = findViewById(R.id.ivDoctorLogout);
        if (ivLogout != null) {
            ivLogout.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.navigation_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) return true;
            if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, SettingsDoctorActivity.class));
                return true;
            }
            
            Toast.makeText(this, "Mở màn hình " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}
