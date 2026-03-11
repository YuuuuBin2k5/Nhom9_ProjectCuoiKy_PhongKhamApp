package com.example.phongkham_app.patient;

import com.example.phongkham_app.R;
import com.example.phongkham_app.auth.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.phongkham_app.patient.viewmodel.PatientHomeViewModel;
import com.example.phongkham_app.data.model.Patient;
import com.example.phongkham_app.data.model.Doctor;
import com.example.phongkham_app.data.model.Service;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

public class Home_Activity_Home extends AppCompatActivity {

    private PatientHomeViewModel viewModel;
    private TextView tvUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_activity_home);
        
        viewModel = new ViewModelProvider(this).get(PatientHomeViewModel.class);
        
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
    }

    private void observeViewModel() {
        viewModel.getPatientProfile().observe(this, patient -> {
            if (patient != null && tvUserName != null) {
                tvUserName.setText("Xin chào, " + patient.getName());
            }
        });
    }

    private void setupNavigation() {
        // 1. Đặt lịch khám -> BookingActivity
        findViewById(R.id.btn_book_appointment).setOnClickListener(v -> 
            startActivity(new Intent(this, BookingActivity.class)));

        // 2. Xem chi tiết cuộc hẹn -> MedicalDetailActivity
        findViewById(R.id.btn_view_appointment_detail).setOnClickListener(v -> 
            startActivity(new Intent(this, MedicalDetailActivity.class)));

        // 3. Xem chi tiết bác sĩ (Dùng dữ liệu từ ViewModel nếu là danh sách động, 
        // hiện tại layout XML vẫn đang là layout tĩnh 4 cột)
        setupDoctorClick(R.id.ll_doctor_1, "BS. Trần Hoàng Nam", "Nội tổng quát", "DOC_1");
        setupDoctorClick(R.id.ll_doctor_2, "BS. Lê Thị Mai Anh", "Tim mạch", "DOC_2");
        setupDoctorClick(R.id.ll_doctor_3, "BS. Phạm Minh Tuấn", "Thần kinh", "DOC_3");
        setupDoctorClick(R.id.ll_doctor_4, "BS. Nguyễn Thùy Linh", "Da liễu", "DOC_4");

        // 4. Các nút "Tất cả"
        findViewById(R.id.all_appointment).setOnClickListener(v -> 
            Toast.makeText(this, "Xem tất cả cuộc hẹn (Đang phát triển)", Toast.LENGTH_SHORT).show());
        
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
        View layout = findViewById(layoutId);
        if (layout != null) {
            layout.setOnClickListener(v -> {
                Intent intent = new Intent(this, DoctorDetailActivity.class);
                intent.putExtra("DOCTOR_ID", id);
                intent.putExtra("DOCTOR_NAME", name);
                intent.putExtra("DOCTOR_SPECIALTY", specialty);
                startActivity(intent);
            });
        }
    }
}
