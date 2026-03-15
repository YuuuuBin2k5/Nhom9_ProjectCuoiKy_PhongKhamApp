package com.example.phongkham_app.ui.patient;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.local.DatabaseHelper;
import com.example.phongkham_app.ui.auth.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.phongkham_app.ui.patient.viewmodel.PatientHomeViewModel;
import com.example.phongkham_app.data.model.Doctor;
import com.example.phongkham_app.data.model.Service;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.ui.patient.adapter.DoctorAdapter;
import com.example.phongkham_app.ui.patient.adapter.ServiceAdapter;

public class Home_Activity_Home extends AppCompatActivity {

    private PatientHomeViewModel viewModel;
    private TextView tvUserName;
    private DoctorAdapter doctorAdapter;
    private ServiceAdapter serviceAdapter;
    private int latestAppointmentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_activity_home);
        
        viewModel = new ViewModelProvider(this).get(PatientHomeViewModel.class);
        
        long userId = getSharedPreferences("UserSession", MODE_PRIVATE).getLong("USER_ID", -1);
        viewModel.loadPatientProfile(userId);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        setupNavigation();
        observeViewModel();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        
        RecyclerView rvDoctors = findViewById(R.id.rv_doctors);
        doctorAdapter = new DoctorAdapter(new DoctorAdapter.OnDoctorActionListener() {
            @Override
            public void onViewDetailClick(Doctor doctor) {
                Intent intent = new Intent(Home_Activity_Home.this, DoctorDetailActivity.class);
                intent.putExtra(DoctorDetailActivity.EXTRA_DOCTOR_ID, doctor.getId());
                intent.putExtra(DoctorDetailActivity.EXTRA_DOCTOR_NAME, doctor.getName());
                intent.putExtra(DoctorDetailActivity.EXTRA_DOCTOR_SPECIALTY, doctor.getSpecialty());
                startActivity(intent);
            }
            @Override
            public void onAddReviewClick(Doctor doctor) {}
            @Override
            public void onFavoriteClick(Doctor doctor) {
                 doctor.setFavorite(!doctor.isFavorite());
                 doctorAdapter.notifyItemChanged(doctorAdapter.getItemCount()); // just visual refresh handle
                 // In complete app we'd update DB too
            }
        });
        if (rvDoctors != null) rvDoctors.setAdapter(doctorAdapter);

        RecyclerView rvServices = findViewById(R.id.rv_services);
        serviceAdapter = new ServiceAdapter(new ServiceAdapter.OnServiceSelectedListener() {
            @Override
            public void onServiceSelected(Service service, int position) {
            }
        });
        if (rvServices != null) rvServices.setAdapter(serviceAdapter);
        
        loadLatestAppointment();
    }

    private void loadLatestAppointment() {
        long userId = getSharedPreferences("UserSession", MODE_PRIVATE).getLong("USER_ID", -1);

        LinearLayout layoutDetail = findViewById(R.id.layout_appointment_detail);
        LinearLayout layoutNoAppt = findViewById(R.id.layout_no_appointment);

        if (userId == -1) {
             if (layoutDetail != null) layoutDetail.setVisibility(View.GONE);
             if (layoutNoAppt != null) layoutNoAppt.setVisibility(View.VISIBLE);
             return;
        }

        DatabaseHelper db = new DatabaseHelper(this);
        android.database.Cursor cursor = db.getLatestAppointment((int) userId);
        
        TextView tvDoc = findViewById(R.id.tv_appointment_doctor_name);
        TextView tvServ = findViewById(R.id.tv_appointment_service_name);
        TextView tvTime = findViewById(R.id.tv_appointment_time);

        if (cursor != null && cursor.moveToFirst()) {
            if (layoutDetail != null) layoutDetail.setVisibility(View.VISIBLE);
            if (layoutNoAppt != null) layoutNoAppt.setVisibility(View.GONE);

            int docNameIdx = cursor.getColumnIndex("doctor_name");
            int serviceNameIdx = cursor.getColumnIndex("service_name");
            int timeIdx = cursor.getColumnIndex("appointment_datetime");
            int idIdx = cursor.getColumnIndex("id");

            String docName = docNameIdx != -1 ? cursor.getString(docNameIdx) : "";
            String serviceName = serviceNameIdx != -1 ? cursor.getString(serviceNameIdx) : "";
            String time = timeIdx != -1 ? cursor.getString(timeIdx) : "";
            
            if (idIdx != -1) {
                latestAppointmentId = cursor.getInt(idIdx);
            }

            if (tvDoc != null) tvDoc.setText("Bs. " + docName);
            if (tvServ != null) tvServ.setText(serviceName);
            if (tvTime != null) tvTime.setText(time);
            
            cursor.close();
        } else {
             if (layoutDetail != null) layoutDetail.setVisibility(View.GONE);
             if (layoutNoAppt != null) layoutNoAppt.setVisibility(View.VISIBLE);
        }
    }

    private void observeViewModel() {
        viewModel.getPatientProfile().observe(this, patient -> {
            if (patient != null && tvUserName != null) {
                tvUserName.setText(patient.getName());
            }
        });

        viewModel.getTopDoctors().observe(this, doctors -> {
            if (doctors != null) {
                doctorAdapter.setDoctors(doctors);
            }
        });

        viewModel.getServices().observe(this, services -> {
            if (services != null) {
                serviceAdapter.setServices(services);
            }
        });
    }

    private void setupNavigation() {
        // 1. Đặt lịch khám -> BookingActivity
        findViewById(R.id.btn_book_appointment).setOnClickListener(v -> 
            startActivity(new Intent(this, BookingActivity.class)));

        findViewById(R.id.btn_qr_scan).setOnClickListener(v -> 
            startActivity(new Intent(this, com.example.phongkham_app.ui.common.QRScanActivity.class)));

        findViewById(R.id.btn_qr_code).setOnClickListener(v -> 
            startActivity(new Intent(this, com.example.phongkham_app.ui.common.QRGenerateActivity.class)));


        // 2. Xem chi tiết cuộc hẹn -> AppointmentDetailActivity
        findViewById(R.id.btn_view_appointment_detail).setOnClickListener(v -> {
            if (latestAppointmentId != -1) {
                Intent intent = new Intent(this, AppointmentDetailActivity.class);
                intent.putExtra("APPOINTMENT_ID", latestAppointmentId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không có cuộc hẹn nào", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Xem chi tiết bác sĩ (Dữ liệu được nạp động qua LiveData)


        // 4. Xem tất cả cuộc hẹn -> AllAppointmentsActivity
        findViewById(R.id.all_appointment).setOnClickListener(v -> {
            startActivity(new Intent(this, AllAppointmentsActivity.class));
        });
        
        findViewById(R.id.all_dv).setOnClickListener(v -> 
            startActivity(new Intent(this, HomeServiceActivity.class)));

        findViewById(R.id.all_bs).setOnClickListener(v -> 
            startActivity(new Intent(this, HomeDoctorActivity.class)));

        // 5. Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) return true;
                if (id == R.id.nav_records) {
                    startActivity(new Intent(this, MedicalRecordActivity.class));
                    return true;
                }
                if (id == R.id.nav_setting) {
                    startActivity(new Intent(this, SettingsPatientActivity.class));
                    return true;
                }
                Toast.makeText(this, "Chức năng " + item.getTitle() + " đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // 6. Header Logout
        View btnHeaderLogout = findViewById(R.id.ivHeaderLogout);
        if (btnHeaderLogout != null) {
            btnHeaderLogout.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupDoctorClick(int layoutId, String name, String specialty, String id) {
        // Obsolete, removed static bindings
    }
}
