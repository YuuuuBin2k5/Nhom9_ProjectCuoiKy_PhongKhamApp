package com.example.phongkham_app.ui.patient;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Doctor;
import com.example.phongkham_app.data.repository.DoctorRepository;
import com.example.phongkham_app.ui.patient.adapter.DoctorAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class HomeDoctorActivity extends AppCompatActivity {

    private DoctorAdapter adapter;
    private List<Doctor> allDoctors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_doctor);
        
        MaterialToolbar topBar = findViewById(R.id.topBar);
        if (topBar != null) {
            topBar.setNavigationOnClickListener(v -> finish());
        }

        RecyclerView rvDoctors = findViewById(R.id.rvDoctorsAll);
        adapter = new DoctorAdapter(new DoctorAdapter.OnDoctorActionListener() {
            @Override
            public void onViewDetailClick(Doctor doctor) {
                Intent intent = new Intent(HomeDoctorActivity.this, DoctorDetailActivity.class);
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
                 adapter.notifyDataSetChanged();
            }
        });
        if (rvDoctors != null) rvDoctors.setAdapter(adapter);

        DoctorRepository repo = DoctorRepository.getInstance(this);
        allDoctors = repo.getDoctors();
        adapter.setDoctors(allDoctors);

        TextInputEditText searchBox = findViewById(R.id.searchBox);
        if (searchBox != null) {
            searchBox.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().toLowerCase();
                    List<Doctor> filtered = new ArrayList<>();
                    for (Doctor doc : allDoctors) {
                        if (doc.getName().toLowerCase().contains(query)) filtered.add(doc);
                    }
                    adapter.updateData(filtered);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }
}
