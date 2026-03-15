package com.example.phongkham_app.ui.patient;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.local.DatabaseHelper;
import com.example.phongkham_app.data.model.Appointment;
import com.example.phongkham_app.ui.patient.adapter.AppointmentAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AllAppointmentsActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private TextView tvEmptyState;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_all_appointments);

        db = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadAppointments();
    }

    private void initViews() {
        rvAppointments = findViewById(R.id.rv_all_appointments);
        tvEmptyState = findViewById(R.id.tv_empty_appointments);
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
        adapter = new AppointmentAdapter(this);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setAdapter(adapter);
    }

    private void loadAppointments() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        long userId = pref.getLong("USER_ID", -1);

        if (userId != -1) {
            Cursor cursor = db.getAllAppointmentsByCustomer((int) userId);
            List<Appointment> list = new ArrayList<>();

            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex("id");
                int docNameIdx = cursor.getColumnIndex("doctor_name");
                int serviceNameIdx = cursor.getColumnIndex("service_name");
                int timeIdx = cursor.getColumnIndex("appointment_datetime");
                int statusIdx = cursor.getColumnIndex("status");
                
                do {
                    String id = idIdx != -1 ? String.valueOf(cursor.getInt(idIdx)) : "";
                    String docName = docNameIdx != -1 ? cursor.getString(docNameIdx) : "N/A";
                    String serviceName = serviceNameIdx != -1 ? cursor.getString(serviceNameIdx) : "N/A";
                    String time = timeIdx != -1 ? cursor.getString(timeIdx) : "N/A";
                    String status = statusIdx != -1 ? cursor.getString(statusIdx) : "N/A";
                    
                    // We parse datetime if needed or pass raw. For simpler model matching we overload fields:
                    // patientName -> serviceName based on AppointmentAdapter usage
                    // time -> full datetime string
                    Appointment appointment = new Appointment();
                    appointment.setId(id);
                    appointment.setDoctorName(docName);
                    appointment.setPatientName(serviceName); // Hack for mapping service Name onto patient Name placeholder.
                    appointment.setTime(time);
                    appointment.setDate(""); // Keep empty since datetime combined
                    appointment.setStatus(status);

                    list.add(appointment);
                } while (cursor.moveToNext());
                cursor.close();
            }

            if (list.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                rvAppointments.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvAppointments.setVisibility(View.VISIBLE);
                adapter.setAppointments(list);
            }
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
