package com.hcmute.mobile_android.ui.activities;

import com.hcmute.mobile_android.util.TokenManager;
import com.hcmute.mobile_android.util.ToastUtils;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    private Spinner spinnerService;
    private RecyclerView rvDoctors;
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
    private static final int START_MINUTES = 8 * 60; // 08:00
    private static final int END_MINUTES = 16 * 60 + 40; // 16:40

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        selectedCategory = getIntent().getStringExtra(EXTRA_CATEGORY);

        tvSelectedCategory = findViewById(R.id.tv_selected_category);
        spinnerService = findViewById(R.id.spinner_service);
        rvDoctors = findViewById(R.id.rv_doctors);
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
                    ToastUtils.showCenteredToast(BookAppointmentActivity.this, "Không tải được danh sách dịch vụ");
                }
            }

            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                pbServices.setVisibility(View.GONE);
                ToastUtils.showCenteredToast(BookAppointmentActivity.this, "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void populateServiceSpinner() {
        if (serviceList.isEmpty()) {
            ToastUtils.showCenteredToast(this, "Không có dịch vụ nào trong danh mục này");
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
        rvDoctors.setVisibility(View.GONE);

        api.getDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                pbDoctors.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<DoctorItem> generalDoctors = new ArrayList<>();
                    for (DoctorItem d : response.body()) {
                        if (!d.isSpecialist() || "Khám tổng quát".equalsIgnoreCase(d.getSpecialization()) || "Nha khoa tổng quát".equalsIgnoreCase(d.getSpecialization())) {
                            generalDoctors.add(d);
                        }
                    }
                    doctorList = generalDoctors;
                    Collections.sort(doctorList,
                            (a, b) -> Integer.compare(a.getAppointmentCount(), b.getAppointmentCount()));
                    populateDoctorRecyclerView();
                } else {
                    doctorList = new ArrayList<>();
                    populateDoctorRecyclerView();
                }
            }

            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                pbDoctors.setVisibility(View.GONE);
                doctorList = new ArrayList<>();
                populateDoctorRecyclerView();
                ToastUtils.showCenteredToast(BookAppointmentActivity.this, "Lỗi tải bác sĩ: " + t.getMessage());
            }
        });
    }


    private DoctorMiniCardAdapter doctorAdapter;

    private void populateDoctorRecyclerView() {
        if (doctorList.isEmpty()) {
            ToastUtils.showCenteredToast(this, "Không có bác sĩ phù hợp");
            rvDoctors.setVisibility(View.GONE);
            return;
        }

        rvDoctors.setVisibility(View.VISIBLE);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        doctorAdapter = new DoctorMiniCardAdapter(doctorList, doctor -> {
            selectedDoctor = doctor;
        });
        rvDoctors.setAdapter(doctorAdapter);
        selectedDoctor = null; // Auto-assign is default selected
    }

    private void pickDate() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    showTimePicker(year, month, dayOfMonth);
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
        datePicker.show();
    }

    private void showTimePicker(int year, int month, int dayOfMonth) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (timeView, hourOfDay, minute) -> {
                    int totalMinutes = hourOfDay * 60 + minute;
                    if (totalMinutes < START_MINUTES || totalMinutes > END_MINUTES) {
                        ToastUtils.showCenteredToastLong(this,
                                "Chỉ được đặt lịch trong khung giờ làm việc: 08:00 – 16:40.");
                        clearSelectedDatetime();
                        return;
                    }

                    Calendar selectedCalend = Calendar.getInstance();
                    selectedCalend.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                    selectedCalend.set(Calendar.MILLISECOND, 0);

                    if (selectedCalend.before(now)) {
                        ToastUtils.showCenteredToastLong(this,
                                "Giờ này đã qua so với hiện tại. Vui lòng chọn giờ từ 08:00 đến 16:40, sau thời điểm hiện tại.");
                        clearSelectedDatetime();
                        return;
                    }

                    // Format for DB
                    selectedDatetime = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d %02d:%02d:00",
                            year, month + 1, dayOfMonth, hourOfDay, minute);
                    // Format for display
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
    }

    private void clearSelectedDatetime() {
        selectedDatetime = null;
        if (tvDatetime != null) {
            tvDatetime.setText("Chọn ngày và giờ");
            tvDatetime.setTextColor(0xFF757575);
        }
    }

    private void submitBooking() {
        if (selectedService == null) {
            ToastUtils.showCenteredToast(this, "Vui lòng chọn dịch vụ");
            return;
        }
        // Doctor is optional now (backend will auto-assign if null)
        if (selectedDatetime == null) {
            ToastUtils.showCenteredToast(this, "Vui lòng chọn ngày và giờ khám");
            return;
        }

        // Validate time range (08:00 - 16:40)
        try {
            // Extract time from YYYY-MM-DD HH:mm:ss
            String timePart = selectedDatetime.substring(11, 16); // "HH:mm"
            String[] parts = timePart.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int totalMinutes = hour * 60 + minute;

            if (totalMinutes < START_MINUTES || totalMinutes > END_MINUTES) {
                ToastUtils.showCenteredToastLong(this,
                        "Chỉ được đặt lịch trong khung giờ làm việc: 08:00 – 16:40.");
                clearSelectedDatetime();
                return;
            }
        } catch (Exception e) {
            // Fallback to backend validation if parsing fails locally
        }

        btnBook.setEnabled(false);
        btnBook.setText("Đang đặt lịch...");

        Long patientId = new TokenManager(this).getPatientId();
        
        CreateAppointmentRequest req = new CreateAppointmentRequest(
                selectedService.getId(),
                selectedDoctor != null ? selectedDoctor.getId() : null,
                patientId,
                selectedDatetime
        );

        api.createAppointment(req).enqueue(new Callback<UpcomingAppointment>() {
            @Override
            public void onResponse(Call<UpcomingAppointment> call, Response<UpcomingAppointment> response) {
                btnBook.setEnabled(true);
                btnBook.setText("Đặt lịch khám");
                if (response.isSuccessful()) {
                    ToastUtils.showCenteredToastLong(BookAppointmentActivity.this, "✅ Đặt lịch hẹn thành công!");
                    Intent intent = new Intent(BookAppointmentActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Đặt lịch thất bại (lỗi " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            org.json.JSONObject obj = new org.json.JSONObject(errorStr);
                            if (obj.has("message")) {
                                errorMsg = obj.getString("message");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ToastUtils.showCenteredToast(BookAppointmentActivity.this, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<UpcomingAppointment> call, Throwable t) {
                btnBook.setEnabled(true);
                btnBook.setText("Đặt lịch khám");
                ToastUtils.showCenteredToast(BookAppointmentActivity.this, "Kiểm tra kết nối mạng");
            }
        });
    }
    private static class DoctorMiniCardAdapter extends RecyclerView.Adapter<DoctorMiniCardAdapter.Holder> {
        private final List<DoctorItem> items;
        private final OnDoctorClickListener listener;
        private int selectedPos = 0; // Default to Auto-assign

        interface OnDoctorClickListener { void onDoctorClick(DoctorItem d); }

        DoctorMiniCardAdapter(List<DoctorItem> list, OnDoctorClickListener l) {
            this.items = list;
            this.listener = l;
        }

        @androidx.annotation.NonNull
        @Override
        public Holder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_mini_card, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull Holder holder, int position) {
            if (position == 0) {
                holder.tvName.setText("Hệ thống chọn");
                holder.tvSpecialty.setText("Khuyên dùng");
                holder.ivAvatar.setImageResource(R.drawable.ic_clinic);
                holder.ivSelectedTick.setVisibility(selectedPos == 0 ? View.VISIBLE : View.GONE);
                holder.itemView.setAlpha(selectedPos == 0 ? 1.0f : 0.6f);
                holder.itemView.setOnClickListener(v -> {
                    int old = selectedPos;
                    selectedPos = position;
                    notifyItemChanged(old);
                    notifyItemChanged(selectedPos);
                    listener.onDoctorClick(null);
                });
                return;
            }

            DoctorItem d = items.get(position - 1);
            holder.tvName.setText("BS. " + d.getFullName());
            holder.tvSpecialty.setText(d.getSpecialization() != null && !d.getSpecialization().isEmpty() ? d.getSpecialization() : "Khám tổng quát");
            holder.ivAvatar.setImageResource(R.drawable.ic_doctor);

            holder.ivSelectedTick.setVisibility(selectedPos == position ? View.VISIBLE : View.GONE);
            holder.itemView.setAlpha(selectedPos == position ? 1.0f : 0.6f);

            holder.itemView.setOnClickListener(v -> {
                int old = selectedPos;
                selectedPos = position;
                notifyItemChanged(old);
                notifyItemChanged(selectedPos);
                listener.onDoctorClick(d);
            });
        }

        @Override public int getItemCount() { return items.size() + 1; } // +1 for auto-assign

        class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvSpecialty;
            ImageView ivAvatar, ivSelectedTick;
            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_name);
                tvSpecialty = v.findViewById(R.id.tv_specialty);
                ivAvatar = v.findViewById(R.id.iv_avatar);
                ivSelectedTick = v.findViewById(R.id.iv_selected_tick);
            }
        }
    }
}
