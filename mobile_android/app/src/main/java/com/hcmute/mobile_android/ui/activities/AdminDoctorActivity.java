package com.hcmute.mobile_android.ui.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.AdminDoctorAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CreateDoctorRequest;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.MessageResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDoctorActivity extends AppCompatActivity {

    private RecyclerView rvDoctors;
    private AdminDoctorAdapter adapter;
    private List<DoctorItem> doctorList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_doctor);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        loadDoctors();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        rvDoctors = findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        
        adapter = new AdminDoctorAdapter(doctorList);
        rvDoctors.setAdapter(adapter);

        FloatingActionButton fabAddDoctor = findViewById(R.id.fabAddDoctor);
        fabAddDoctor.setOnClickListener(v -> showAddDoctorDialog());
    }

    private void loadDoctors() {
        apiService.getAdminDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    doctorList.clear();
                    doctorList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                Toast.makeText(AdminDoctorActivity.this, "Lỗi tải danh sách bác sĩ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDoctorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null);
        
        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etSpecialty = view.findViewById(R.id.etSpecialty);
        EditText etExperience = view.findViewById(R.id.etExperience);
        EditText etBio = view.findViewById(R.id.etBio);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String specialty = etSpecialty.getText().toString().trim();
            String expStr = etExperience.getText().toString().trim();
            String bio = etBio.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || 
                password.isEmpty() || specialty.isEmpty() || expStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int experience = Integer.parseInt(expStr);
                CreateDoctorRequest request = new CreateDoctorRequest(
                    firstName, lastName, email, password, specialty, experience, bio
                );
                
                createDoctor(request, dialog);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Kinh nghiệm phải là số", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void createDoctor(CreateDoctorRequest request, AlertDialog dialog) {
        apiService.createDoctor(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminDoctorActivity.this, "Thêm bác sĩ thành công", Toast.LENGTH_SHORT).show();
                    loadDoctors();
                    dialog.dismiss();
                } else {
                    Toast.makeText(AdminDoctorActivity.this, "Lỗi khi thêm bác sĩ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(AdminDoctorActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}