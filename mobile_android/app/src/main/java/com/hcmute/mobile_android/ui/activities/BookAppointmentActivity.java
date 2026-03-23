package com.hcmute.mobile_android.ui.activities;

import com.hcmute.mobile_android.util.TokenManager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CreateAppointmentRequest;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.ServiceItem;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookAppointmentActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "categoryName";

    // UI
    private Spinner spinnerService, spinnerDoctor;
    private ProgressBar pbServices, pbDoctors;
    private TextView tvSelectedCategory, tvDatetime;
    private MaterialButton btnBook;

    // Data
    private ApiService api;
    private String selectedCategory;
    private List<ServiceItem> serviceList = new ArrayList<>();
    private List<DoctorItem> doctorList = new ArrayList<>();
    private ServiceItem selectedService;
    private DoctorItem selectedDoctor;
    private String selectedDatetime; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        selectedCategory = getIntent().getStringExtra(EXTRA_CATEGORY);

        tvSelectedCategory = findViewById(R.id.tv_selected_category);
        spinnerService = findViewById(R.id.spinner_service);
        spinnerDoctor = findViewById(R.id.spinner_doctor);
        pbServices = findViewById(R.id.pb_services);
        pbDoctors = findViewById(R.id.pb_doctors);
        tvDatetime = findViewById(R.id.tv_datetime);
        btnBook = findViewById(R.id.btn_book);

        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            tvSelectedCategory.setText("Danh mục: " + selectedCategory);
        }

        api = RetrofitClient.getApiService(this);

        tvDatetime.setOnClickListener(v -> pickDate());
        findViewById(R.id.ll_datetime_picker).setOnClickListener(v -> pickDate());

        btnBook.setOnClickListener(v -> submitBooking());

        loadServices();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadServices() {
        pbServices.setVisibility(View.VISIBLE);
        spinnerService.setEnabled(false);

        api.getServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                pbServices.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<ServiceItem> all = response.body();
                    serviceList = new ArrayList<>();
                    for (ServiceItem s : all) {
                        if (selectedCategory == null || selectedCategory.isEmpty()
                                || selectedCategory.equals(s.getCategoryName())) {
                            serviceList.add(s);
                        }
                    }
                    populateServiceSpinner();
                } else {
                    Toast.makeText(BookAppointmentActivity.this,
                            "Không tải được danh sách dịch vụ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                pbServices.setVisibility(View.GONE);
                Toast.makeText(BookAppointmentActivity.this,
                        "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateServiceSpinner() {
        if (serviceList.isEmpty()) {
            Toast.makeText(this, "Không có dịch vụ nào trong danh mục này", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> names = new ArrayList<>();
        for (ServiceItem s : serviceList) {
            names.add(s.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerService.setAdapter(adapter);
        spinnerService.setEnabled(true);

        spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedService = serviceList.get(position);
                loadDoctors(selectedService.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        selectedService = serviceList.get(0);
        loadDoctors(selectedService.getId());
    }

    private void loadDoctors(Long serviceId) {
        pbDoctors.setVisibility(View.VISIBLE);
        spinnerDoctor.setEnabled(false);

        api.getDoctorsByService(serviceId).enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                pbDoctors.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    doctorList = new ArrayList<>(response.body());
                    Collections.sort(doctorList,
                            (a, b) -> Integer.compare(a.getAppointmentCount(), b.getAppointmentCount()));
                    populateDoctorSpinner();
                } else {
                    loadAllDoctors();
                }
            }

            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                pbDoctors.setVisibility(View.GONE);
                loadAllDoctors();
            }
        });
    }

    private void loadAllDoctors() {
        pbDoctors.setVisibility(View.VISIBLE);
        api.getDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                pbDoctors.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    doctorList = new ArrayList<>(response.body());
                    Collections.sort(doctorList,
                            (a, b) -> Integer.compare(a.getAppointmentCount(), b.getAppointmentCount()));
                    populateDoctorSpinner();
                } else {
                    Toast.makeText(BookAppointmentActivity.this,
                            "Không tải được danh sách bác sĩ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                pbDoctors.setVisibility(View.GONE);
                Toast.makeText(BookAppointmentActivity.this,
                        "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateDoctorSpinner() {
        if (doctorList.isEmpty()) {
            Toast.makeText(this, "Không có bác sĩ phù hợp", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> names = new ArrayList<>();
        for (DoctorItem d : doctorList) {
            String label = "BS. " + d.getFullName();
            if (d.getSpecialization() != null && !d.getSpecialization().isEmpty()) {
                label += "  •  " + d.getSpecialization();
            }
            names.add(label);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctor.setAdapter(adapter);
        spinnerDoctor.setEnabled(true);

        selectedDoctor = doctorList.get(0);

        spinnerDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDoctor = doctorList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void pickDate() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                // Định dạng có dấu cách để khớp với DB
                                selectedDatetime = String.format(Locale.getDefault(),
                                        "%04d-%02d-%02d %02d:%02d:00",
                                        year, month + 1, dayOfMonth, hourOfDay, minute);
                                String display = String.format(Locale.getDefault(),
                                        "%02d/%02d/%04d  %02d:%02d",
                                        dayOfMonth, month + 1, year, hourOfDay, minute);
                                tvDatetime.setText(display);
                                tvDatetime.setTextColor(0xFF1A1A1A);
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true);
                    timePicker.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
        datePicker.show();
    }

    private void submitBooking() {
        if (selectedService == null) {
            Toast.makeText(this, "Vui lòng chọn dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDoctor == null) {
            Toast.makeText(this, "Vui lòng chọn bác sĩ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDatetime == null) {
            Toast.makeText(this, "Vui lòng chọn ngày và giờ khám", Toast.LENGTH_SHORT).show();
            return;
        }

        btnBook.setEnabled(false);
        btnBook.setText("Đang đặt lịch...");

        Long patientId = new TokenManager(this).getPatientId();
        
        CreateAppointmentRequest req = new CreateAppointmentRequest(
                selectedService.getId(),
                selectedDoctor.getId(),
                patientId,
                selectedDatetime
        );

        api.createAppointment(req).enqueue(new Callback<UpcomingAppointment>() {
            @Override
            public void onResponse(Call<UpcomingAppointment> call, Response<UpcomingAppointment> response) {
                btnBook.setEnabled(true);
                btnBook.setText("Đặt lịch khám");
                if (response.isSuccessful()) {
                    Toast.makeText(BookAppointmentActivity.this,
                            "✅ Đặt lịch thành công!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(BookAppointmentActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(BookAppointmentActivity.this,
                            "Đặt lịch thất bại (lỗi " + response.code() + "). Vui lòng thử lại.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UpcomingAppointment> call, Throwable t) {
                btnBook.setEnabled(true);
                btnBook.setText("Đặt lịch khám");
                Toast.makeText(BookAppointmentActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
