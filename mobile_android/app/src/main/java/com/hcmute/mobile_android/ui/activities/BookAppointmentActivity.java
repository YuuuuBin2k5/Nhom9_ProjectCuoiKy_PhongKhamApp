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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.bumptech.glide.Glide;

public class BookAppointmentActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "categoryName";

    // UI
    private Spinner spinnerService;
    private RecyclerView rvDoctors, rvDates, rvTimeSlots;
    private ProgressBar pbServices, pbDoctors, loadingSlots;
    private TextView tvSelectedCategory, tvDatetime;
    private MaterialButton btnBook;

    // Data
    private ApiService api;
    private String selectedCategory;
    private List<ServiceItem> serviceList = new ArrayList<>();
    private List<DoctorItem> doctorList = new ArrayList<>();
    private ServiceItem selectedService;
    private DoctorItem selectedDoctor;

    // Selection State
    private List<DateItem> dateList = new ArrayList<>();
    private List<TimeSlot> timeSlotList = new ArrayList<>();
    private DateAdapter dateAdapter;
    private TimeSlotAdapter timeSlotAdapter;
    private DateItem selectedDateItem;
    private TimeSlot selectedTimeSlot;
    private String selectedDatetime; 

    private static final int START_MINUTES = 8 * 60; // 08:00
    private static final int END_MINUTES = 16 * 60 + 40; // 16:40
    private static final int SLOT_INTERVAL = 40;

    public interface OnDateClickListener { void onDateClick(DateItem item); }
    public interface OnTimeSlotClickListener { void onTimeClick(TimeSlot item); }

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
        tvDatetime = findViewById(R.id.tv_datetime); // Hidden but used for compatibility if needed
        rvDates = findViewById(R.id.rv_dates);
        rvTimeSlots = findViewById(R.id.rv_time_slots);
        loadingSlots = findViewById(R.id.loading_slots);
        btnBook = findViewById(R.id.btn_book);

        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            tvSelectedCategory.setText("Danh mục: " + selectedCategory);
        }

        api = RetrofitClient.getApiService(this);

        setupSelectors();

        btnBook.setOnClickListener(v -> submitBooking());

        loadServices();
    }

    private void setupSelectors() {
        // Date Selector
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateAdapter = new DateAdapter(dateList, item -> {
            selectedDateItem = item;
            loadTimeSlots(item);
        });
        rvDates.setAdapter(dateAdapter);
        generateDates();

        // Time Selector
        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 3));
        timeSlotAdapter = new TimeSlotAdapter(timeSlotList, item -> {
            selectedTimeSlot = item;
            updateBtnState();
        });
        rvTimeSlots.setAdapter(timeSlotAdapter);
        
        updateBtnState();
    }

    private void updateBtnState() {
        boolean ready = selectedDateItem != null && selectedTimeSlot != null && selectedService != null;
        btnBook.setEnabled(ready);
        btnBook.setAlpha(ready ? 1.0f : 0.6f);
    }

    private void generateDates() {
        dateList.clear();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFmt = new SimpleDateFormat("EEE", new Locale("vi", "VN"));
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monthFmt = new SimpleDateFormat("'Tháng 'M", new Locale("vi", "VN"));

        for (int i = 0; i < 14; i++) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.DATE, i);
            dateList.add(new DateItem(c, dayFmt.format(c.getTime()), dateFmt.format(c.getTime()), monthFmt.format(c.getTime())));
        }
        
        if (!dateList.isEmpty()) {
            selectedDateItem = dateList.get(0);
            selectedDateItem.isSelected = true;
        }
        dateAdapter.notifyDataSetChanged();
        
        if (selectedDateItem != null) {
            loadTimeSlots(selectedDateItem);
        }
    }

    private void loadTimeSlots(DateItem dateItem) {
        selectedTimeSlot = null;
        updateBtnState();

        loadingSlots.setVisibility(View.VISIBLE);
        
        SimpleDateFormat apiFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = apiFmt.format(dateItem.calendar.getTime());

        // Long doctorId = selectedDoctor != null ? selectedDoctor.getId() : (doctorList.isEmpty() ? null : doctorList.get(0).getId());
        Long doctorId = selectedDoctor != null ? selectedDoctor.getId() : null;

        api.getDoctorSchedule(doctorId, dateStr).enqueue(new Callback<List<com.hcmute.mobile_android.network.models.ScheduleAppointment>>() {
            @Override
            public void onResponse(Call<List<com.hcmute.mobile_android.network.models.ScheduleAppointment>> call, 
                                   Response<List<com.hcmute.mobile_android.network.models.ScheduleAppointment>> response) {
                loadingSlots.setVisibility(View.GONE);
                List<com.hcmute.mobile_android.network.models.ScheduleAppointment> booked = (response.isSuccessful() && response.body() != null) ? response.body() : new ArrayList<>();
                generateLocalTimeSlots(dateItem, booked);
            }
            @Override
            public void onFailure(Call<List<com.hcmute.mobile_android.network.models.ScheduleAppointment>> call, Throwable t) {
                loadingSlots.setVisibility(View.GONE);
                generateLocalTimeSlots(dateItem, new ArrayList<>());
            }
        });
    }

    private void generateLocalTimeSlots(DateItem dateItem, List<com.hcmute.mobile_android.network.models.ScheduleAppointment> booked) {
        timeSlotList.clear();
        Calendar now = Calendar.getInstance();
        boolean isToday = dateItem.calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                dateItem.calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);

        for (int minutes = START_MINUTES; minutes <= END_MINUTES; minutes += SLOT_INTERVAL) {
            int h = minutes / 60;
            int m = minutes % 60;
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            
            boolean isPassed = isToday && (minutes <= (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE) + 15));
            boolean isBooked = false;
            for (com.hcmute.mobile_android.network.models.ScheduleAppointment appt : booked) {
                if (appt.getDatetime() != null && appt.getDatetime().contains(timeStr)) {
                    isBooked = true;
                    break;
                }
            }
            timeSlotList.add(new TimeSlot(timeStr, !isPassed && !isBooked));
        }
        timeSlotAdapter.notifyDataSetChanged();
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
                }
            }
            @Override public void onFailure(Call<List<ServiceItem>> call, Throwable t) { pbServices.setVisibility(View.GONE); }
        });
    }

    private void populateServiceSpinner() {
        if (serviceList.isEmpty()) return;
        List<String> names = new ArrayList<>();
        for (ServiceItem s : serviceList) names.add(s.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerService.setAdapter(adapter);
        spinnerService.setEnabled(true);

        spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedService = serviceList.get(position);
                loadDoctors(selectedService.getId());
                updateBtnState();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
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
                    doctorList = response.body();
                    Collections.sort(doctorList, (a, b) -> Integer.compare(b.getAppointmentCount(), a.getAppointmentCount()));
                    populateDoctorRecyclerView();
                }
            }
            @Override public void onFailure(Call<List<DoctorItem>> call, Throwable t) { pbDoctors.setVisibility(View.GONE); }
        });
    }

    private void populateDoctorRecyclerView() {
        rvDoctors.setVisibility(View.VISIBLE);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DoctorMiniCardAdapter doctorAdapter = new DoctorMiniCardAdapter(doctorList, doctor -> {
            selectedDoctor = doctor;
            if (selectedDateItem != null) loadTimeSlots(selectedDateItem);
        });
        rvDoctors.setAdapter(doctorAdapter);
    }

    private void submitBooking() {
        if (selectedService == null || selectedDateItem == null || selectedTimeSlot == null) {
            ToastUtils.showCenteredToast(this, "Vui lòng chọn đầy đủ dịch vụ, ngày và giờ");
            return;
        }

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDatetime = fmt.format(selectedDateItem.calendar.getTime()) + " " + selectedTimeSlot.time + ":00";

        btnBook.setEnabled(false);
        btnBook.setText("Đang xử lý...");

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
                btnBook.setText("Hoàn tất Đặt Lịch");
                if (response.isSuccessful()) {
                    ToastUtils.showCenteredToastLong(BookAppointmentActivity.this, "✅ Đặt lịch hẹn thành công!");
                    finish();
                } else {
                    ToastUtils.showCenteredToast(BookAppointmentActivity.this, "Lỗi từ server, vui lòng thử lại");
                }
            }
            @Override public void onFailure(Call<UpcomingAppointment> call, Throwable t) {
                btnBook.setEnabled(true);
                btnBook.setText("Hoàn tất Đặt Lịch");
            }
        });
    }

    // --- Inner Models & Adapters ---
    
    private static class DateItem {
        Calendar calendar;
        String dayOfWeek, date, month;
        boolean isSelected = false;
        DateItem(Calendar cal, String d, String dt, String m) {
            this.calendar = cal; this.dayOfWeek = d; this.date = dt; this.month = m;
        }
    }

    private static class TimeSlot {
        String time; boolean isAvailable; boolean isSelected = false;
        TimeSlot(String t, boolean a) { this.time = t; this.isAvailable = a; }
    }

    private class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
        private final List<DateItem> items;
        private final OnDateClickListener listener;
        DateAdapter(List<DateItem> items, OnDateClickListener l) { this.items = items; this.listener = l; }

        @Override public ViewHolder onCreateViewHolder(ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_date_pill, p, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(ViewHolder h, int pos) {
            DateItem item = items.get(pos);
            h.tvDayOfWeek.setText(item.dayOfWeek);
            h.tvDate.setText(item.date);
            h.tvMonth.setText(item.month);
            if (item.isSelected) {
                h.card.setCardBackgroundColor(ContextCompat.getColor(BookAppointmentActivity.this, R.color.primary_trust_blue));
                h.card.setStrokeColor(ContextCompat.getColor(BookAppointmentActivity.this, R.color.primary_trust_blue));
                h.tvDate.setTextColor(0xFFFFFFFF);
            } else {
                h.card.setCardBackgroundColor(0xFFFFFFFF);
                h.card.setStrokeColor(0xFFE2E8F0);
                h.tvDate.setTextColor(0xFF1E293B);
            }
            h.itemView.setOnClickListener(v -> {
                for (DateItem di : items) di.isSelected = false;
                item.isSelected = true;
                notifyDataSetChanged();
                listener.onDateClick(item);
            });
        }
        @Override public int getItemCount() { return items.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDayOfWeek, tvDate, tvMonth;
            com.google.android.material.card.MaterialCardView card;
            ViewHolder(View v) {
                super(v);
                tvDayOfWeek = v.findViewById(R.id.tvDayOfWeek);
                tvDate = v.findViewById(R.id.tvDate);
                tvMonth = v.findViewById(R.id.tvMonth);
                card = v.findViewById(R.id.cardDate);
            }
        }
    }

    private class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
        private final List<TimeSlot> items;
        private final OnTimeSlotClickListener listener;
        TimeSlotAdapter(List<TimeSlot> items, OnTimeSlotClickListener l) { this.items = items; this.listener = l; }

        @Override public ViewHolder onCreateViewHolder(ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_time_slot, p, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(ViewHolder h, int pos) {
            TimeSlot item = items.get(pos);
            h.tvTime.setText(item.time);
            h.tvTime.setEnabled(item.isAvailable);
            h.tvTime.setSelected(item.isSelected);
            h.itemView.setOnClickListener(v -> {
                if (!item.isAvailable) return;
                for (TimeSlot ts : items) ts.isSelected = false;
                item.isSelected = true;
                notifyDataSetChanged();
                listener.onTimeClick(item);
            });
        }
        @Override public int getItemCount() { return items.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTime;
            ViewHolder(View v) { super(v); tvTime = v.findViewById(R.id.tvTime); }
        }
    }

    private static class DoctorMiniCardAdapter extends RecyclerView.Adapter<DoctorMiniCardAdapter.Holder> {
        private final List<DoctorItem> items;
        private final OnDoctorClickListener listener;
        private int selectedPos = 0;

        interface OnDoctorClickListener { void onDoctorClick(DoctorItem d); }
        DoctorMiniCardAdapter(List<DoctorItem> list, OnDoctorClickListener l) { this.items = list; this.listener = l; }

        @Override public Holder onCreateViewHolder(ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_doctor_mini_card, p, false);
            return new Holder(v);
        }
        @Override public void onBindViewHolder(Holder h, int pos) {
            if (pos == 0) {
                h.tvName.setText("Tự chọn"); h.tvSpecialty.setText("Khuyên dùng");
                h.ivAvatar.setImageResource(R.drawable.ic_clinic);
                h.ivSelectedTick.setVisibility(selectedPos == 0 ? View.VISIBLE : View.GONE);
                h.itemView.setAlpha(selectedPos == 0 ? 1.0f : 0.6f);
                h.itemView.setOnClickListener(v -> {
                    selectedPos = 0; notifyDataSetChanged(); listener.onDoctorClick(null);
                });
                return;
            }
            DoctorItem d = items.get(pos - 1);
            h.tvName.setText("BS. " + d.getFullName());
            h.tvSpecialty.setText(d.getSpecialization());

            // Human fallbacks
            int[] males = { R.drawable.doc1, R.drawable.doc3, R.drawable.doc5 };
            int[] females = { R.drawable.doc2, R.drawable.doc4 };
            String n = d.getFullName().toLowerCase();
            boolean f = n.contains("hà") || n.contains("thu") || n.contains("mai") || n.contains("lan");
            int fallback = f ? females[pos % females.length] : males[pos % males.length];

            String avatarUrl = d.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                String finalUrl = avatarUrl;
                if (!avatarUrl.startsWith("http")) {
                    String base = com.hcmute.mobile_android.BuildConfig.API_BASE_URL;
                    if (!base.endsWith("/")) base = base + "/";
                    String p = avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl;
                    finalUrl = base + "uploads/" + p;
                }
                Glide.with(h.ivAvatar.getContext()).load(finalUrl).centerCrop().placeholder(fallback).error(fallback).into(h.ivAvatar);
            } else {
                h.ivAvatar.setImageResource(fallback);
            }

            h.ivSelectedTick.setVisibility(selectedPos == pos ? View.VISIBLE : View.GONE);
            h.itemView.setAlpha(selectedPos == pos ? 1.0f : 0.6f);
            h.itemView.setOnClickListener(v -> {
                selectedPos = pos; notifyDataSetChanged(); listener.onDoctorClick(d);
            });
        }
        @Override public int getItemCount() { return items.size() + 1; }
        class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvSpecialty; ImageView ivAvatar, ivSelectedTick;
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
