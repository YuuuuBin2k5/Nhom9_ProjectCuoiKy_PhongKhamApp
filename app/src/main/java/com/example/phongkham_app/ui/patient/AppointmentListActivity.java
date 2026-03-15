package com.example.phongkham_app.ui.patient;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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

public class AppointmentListActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private DatabaseHelper dbHelper;
    private LinearLayout llEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_appointment_list);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadAppointments();
    }

    private void initViews() {
        rvAppointments = findViewById(R.id.rvAppointments);
        llEmptyState = findViewById(R.id.llEmptyState);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lịch hẹn của tôi");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new AppointmentAdapter();
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setAdapter(adapter);
    }

    private void loadAppointments() {
        android.content.SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = pref.getInt("userId", 1);

        List<Appointment> appointments = new ArrayList<>();
        Cursor cursor = dbHelper.getAppointmentsByCustomer(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                String doctorName = cursor.getString(cursor.getColumnIndexOrThrow("doctor_name"));
                String serviceName = cursor.getString(cursor.getColumnIndexOrThrow("service_name"));
                String datetime = cursor.getString(cursor.getColumnIndexOrThrow("appointment_datetime"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                // Tách datetime nếu cần, ở đây model Appointment dùng date và time riêng
                // Giả sử datetime lưu định dạng "dd/MM/yyyy HH:mm AM/PM"
                String date = "";
                String time = "";
                if (datetime != null && datetime.contains(" ")) {
                    int lastSpace = datetime.lastIndexOf(" ");
                    int firstSpace = datetime.indexOf(" ");
                    date = datetime.substring(0, firstSpace);
                    time = datetime.substring(firstSpace + 1);
                }

                // Dùng patientName để lưu tên dịch vụ cho tiện hiển thị trong adapter
                Appointment app = new Appointment(id, serviceName, doctorName, time, date, status);
                appointments.add(app);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (appointments.isEmpty()) {
            rvAppointments.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvAppointments.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            adapter.setAppointments(appointments);
        }
    }
}
