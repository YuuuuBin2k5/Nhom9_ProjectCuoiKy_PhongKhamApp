package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.ScheduleAppointment;
import com.hcmute.mobile_android.ui.adapters.ScheduleSlotAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminScheduleActivity extends BaseAdminActivity {

    private CalendarView calendarView;
    private Spinner spinnerDoctors;
    private TextView tvSelectedDate;
    private RecyclerView recyclerViewSlots;
    private ScheduleSlotAdapter adapter;
    private ApiService apiService;
    private List<DoctorItem> doctorsList = new ArrayList<>();
    private Long selectedDoctorId;
    private String selectedDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_schedule);

        apiService = RetrofitClient.getApiService(this);
        initViews();
        
        Calendar cal = Calendar.getInstance();
        selectedDate = dateFormat.format(cal.getTime());
        tvSelectedDate.setText("Lịch trình ngày: " + displayFormat.format(cal.getTime()));

        loadDoctors();
    }

    private void initViews() {
        setupToolbar();
        calendarView = findViewById(R.id.calendarView);
        spinnerDoctors = findViewById(R.id.spinnerDoctors);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        recyclerViewSlots = findViewById(R.id.recyclerViewSlots);

        adapter = new ScheduleSlotAdapter();
        recyclerViewSlots.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSlots.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDate = dateFormat.format(cal.getTime());
            tvSelectedDate.setText("Lịch trình ngày: " + displayFormat.format(cal.getTime()));
            refreshSchedule();
        });

        spinnerDoctors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDoctorId = doctorsList.get(position).getId();
                refreshSchedule();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void loadDoctors() {
        showLoading(true);
        apiService.getAdminDoctors().enqueue(new Callback<com.hcmute.mobile_android.network.models.PagedResponse<DoctorItem>>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.PagedResponse<DoctorItem>> call, Response<com.hcmute.mobile_android.network.models.PagedResponse<DoctorItem>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getContent() != null) {
                    doctorsList = response.body().getContent();
                    List<String> names = new ArrayList<>();
                    for (DoctorItem d : doctorsList) {
                        names.add(d.getLastName() + " " + d.getFirstName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminScheduleActivity.this, android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDoctors.setAdapter(adapter);
                    
                    if (!doctorsList.isEmpty()) {
                        selectedDoctorId = doctorsList.get(0).getId();
                        refreshSchedule();
                    }
                } else {
                    showError("Lỗi tải danh mục bác sĩ");
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.PagedResponse<DoctorItem>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối");
            }
        });
    }

    private void refreshSchedule() {
        if (selectedDoctorId == null || selectedDate == null) return;

        apiService.getDoctorSchedule(selectedDoctorId, selectedDate).enqueue(new Callback<List<ScheduleAppointment>>() {
            @Override
            public void onResponse(Call<List<ScheduleAppointment>> call, Response<List<ScheduleAppointment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setAppointments(response.body());
                } else {
                    showError("Lỗi tải lịch trình");
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleAppointment>> call, Throwable t) {
                showError("Lỗi kết nối lịch trình");
            }
        });
    }
}
