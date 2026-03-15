package com.example.phongkham_app.ui.admin;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Doctor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageDoctorsActivity extends AppCompatActivity {

    private RecyclerView rvDoctors;
    private DoctorAdapter adapter;
    private List<Doctor> doctorList;
    private List<Doctor> filteredList;
    private EditText etSearch;
    private com.example.phongkham_app.data.repository.DoctorRepository doctorRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_manage_doctors);

        doctorRepository = com.example.phongkham_app.data.repository.DoctorRepository.getInstance(getApplication());

        initViews();
        setupRecyclerView();
        setupListeners();
        loadDoctors();
    }

    private void initViews() {
        rvDoctors = findViewById(R.id.rvDoctors);
        etSearch = findViewById(R.id.etSearch);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            Intent intent = new Intent(ManageDoctorsActivity.this, AddDoctorActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        doctorList = new ArrayList<>();
        filteredList = new ArrayList<>();
        
        adapter = new DoctorAdapter(filteredList, new DoctorAdapter.OnDoctorActionListener() {
            @Override
            public void onEdit(Doctor doctor, int position) {
                Toast.makeText(ManageDoctorsActivity.this, 
                    "Chỉnh sửa: " + doctor.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Mở activity chỉnh sửa bác sĩ
            }

            @Override
            public void onDelete(Doctor doctor, int position) {
                showDeleteConfirmDialog(doctor, position);
            }
        });
        
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        rvDoctors.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadDoctors() {
        doctorList.clear();
        if (doctorRepository != null) {
            doctorList.addAll(doctorRepository.getDoctors());
        }
        filteredList.clear();
        filteredList.addAll(doctorList);
        adapter.notifyDataSetChanged();
    }

    private void filterDoctors(String query) {
        filteredList.clear();
        
        if (query.isEmpty()) {
            filteredList.addAll(doctorList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Doctor doctor : doctorList) {
                if (doctor.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(doctor);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
    }

    private void showDeleteConfirmDialog(Doctor doctor, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa " + doctor.getName() + "?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                doctorList.remove(doctor);
                filteredList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(this, "Đã xóa " + doctor.getName(), Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}
