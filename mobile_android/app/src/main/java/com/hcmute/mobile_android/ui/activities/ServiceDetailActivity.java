package com.hcmute.mobile_android.ui.activities;

import com.hcmute.mobile_android.util.TokenManager;
import com.hcmute.mobile_android.util.ToastUtils;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CreateAppointmentRequest;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;
import com.hcmute.mobile_android.network.models.ScheduleAppointment;
import com.hcmute.mobile_android.adapters.ServiceImageAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetailActivity extends AppCompatActivity {

    private TextView tvServiceName, tvServicePrice, tvServiceDesc;
    private Spinner spinnerDoctors;
    private RecyclerView rvDates, rvTimeSlots;
    private ProgressBar loadingSlots;
    private MaterialButton btnBook;
    private ViewPager2 vpServiceImages;
    private TabLayout tabIndicator;
    private ApiService apiService;

    private List<DoctorItem> doctorList = new ArrayList<>();
    private List<String> doctorNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    // Service data from Intent (keys match HomeFragment.ServiceAdapter)
    private long serviceId;
    private String serviceName;
    private double servicePrice;
    private String serviceDesc;
    private int serviceDuration;
    private String serviceCategory;
    private List<String> imageUrls;

    // Logic variables
    private List<DateItem> dateList = new ArrayList<>();
    private List<TimeSlot> timeSlotList = new ArrayList<>();
    private DateAdapter dateAdapter;
    private TimeSlotAdapter timeSlotAdapter;
    private DateItem selectedDateItem = null;
    private TimeSlot selectedTimeSlot = null;

    private static final int START_MINUTES = 8 * 60; // 08:00
    private static final int END_MINUTES = 16 * 60 + 40; // 16:40
    private static final int SLOT_INTERVAL = 40; // 40 minutes per slot

    public interface OnDateClickListener { void onDateClick(DateItem item); }
    public interface OnTimeSlotClickListener { void onTimeClick(TimeSlot item); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        // 1. Data from Intent
        serviceId       = getIntent().getLongExtra("id", -1L);
        serviceName     = getIntent().getStringExtra("name");
        servicePrice    = getIntent().getDoubleExtra("price", 0.0);
        serviceDesc     = getIntent().getStringExtra("description");
        imageUrls       = getIntent().getStringArrayListExtra("imageUrls");
        serviceCategory = getIntent().getStringExtra("category");

        // 2. Bind Views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(serviceName != null ? serviceName : "Chi tiết dịch vụ");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvServiceName     = findViewById(R.id.tvServiceName);
        tvServicePrice    = findViewById(R.id.tvServicePrice);
        tvServiceDesc     = findViewById(R.id.tvServiceDesc);
        spinnerDoctors    = findViewById(R.id.spinnerDoctors);
        rvDates           = findViewById(R.id.rv_dates);
        rvTimeSlots       = findViewById(R.id.rv_time_slots);
        loadingSlots      = findViewById(R.id.loading_slots);
        btnBook           = findViewById(R.id.btnBook);
        vpServiceImages   = findViewById(R.id.vpServiceImages);
        tabIndicator      = findViewById(R.id.tabIndicator);

        // 3. Setup UI
        tvServiceName.setText(serviceName);
        tvServicePrice.setText(formatPrice(servicePrice));
        tvServiceDesc.setText(serviceDesc);
        
        setupSelectors();
        setupImages();

        // 4. API & Listeners
        apiService = RetrofitClient.getApiService(this);
        
        doctorNames.add("Đang tải bác sĩ...");
        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, doctorNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctors.setAdapter(spinnerAdapter);
        
        spinnerDoctors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (selectedDateItem != null) loadTimeSlots(selectedDateItem);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        loadDoctors();
        
        btnBook.setOnClickListener(v -> submitBooking());
    }

    // ─── Format ───────────────────────────────────────────────────────────────

    private String formatPrice(double price) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format((long) price) + " đ";
    }

    // ─── Load doctors ─────────────────────────────────────────────────────────

    private void setupImages() {
        if (imageUrls == null || imageUrls.isEmpty()) {
            // If no actual images, show a few placeholders or just one
            imageUrls = new ArrayList<>();
            imageUrls.add("ic_tooth");
        }
        
        ServiceImageAdapter imageAdapter = new ServiceImageAdapter(this, imageUrls);
        vpServiceImages.setAdapter(imageAdapter);
        
        if (imageUrls.size() > 1) {
            new TabLayoutMediator(tabIndicator, vpServiceImages, (tab, position) -> {
                // No text for dots
            }).attach();
            tabIndicator.setVisibility(View.VISIBLE);
        } else {
            tabIndicator.setVisibility(View.GONE);
        }
    }

    private void loadDoctors() {
        apiService.getDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DoctorItem> allDoctors = response.body();
                    List<DoctorItem> validDoctors = new ArrayList<>();
                    
                    for (DoctorItem d : allDoctors) {
                        String spec = d.getSpecialization() != null ? d.getSpecialization().toLowerCase() : "";
                        String cat = serviceCategory != null ? serviceCategory.toLowerCase() : "";
                        String name = serviceName != null ? serviceName.toLowerCase() : "";
                        
                        // Bypass strict backend rules: General dentists can do anything like X-rays.
                        if (spec.contains("tổng quát") || spec.contains("general") || spec.isEmpty()) {
                            validDoctors.add(d);
                            continue;
                        }
                        
                        // Otherwise try soft matching
                        if (name.contains(spec) || spec.contains(name) || cat.contains(spec) || spec.contains(cat)) {
                            validDoctors.add(d);
                        }
                    }
                    
                    // Fail-safe: if filtering left 0 doctors, just show them all.
                    if (validDoctors.isEmpty() && !allDoctors.isEmpty()) {
                        validDoctors.addAll(allDoctors);
                    }
                    
                    if (!validDoctors.isEmpty()) {
                        populateDoctors(validDoctors);
                    } else {
                        showNoDoctors();
                    }
                } else {
                    showNoDoctors();
                }
            }
            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) { showNoDoctors(); }
        });
    }

    private void populateDoctors(List<DoctorItem> doctors) {
        doctorList = new ArrayList<>(doctors);
        // Sort: fewest appointments first (auto-select best doctor)
        Collections.sort(doctorList,
                (a, b) -> Integer.compare(a.getAppointmentCount(), b.getAppointmentCount()));
        doctorNames.clear();
        for (DoctorItem doc : doctorList) {
            String spec = (doc.getSpecialization() != null && !doc.getSpecialization().isEmpty())
                    ? " – " + doc.getSpecialization() : "";
            doctorNames.add("BS. " + doc.getFullName() + spec);
        }
        if (doctorNames.isEmpty()) doctorNames.add("Không có bác sĩ phù hợp");
        spinnerAdapter.notifyDataSetChanged();
        spinnerDoctors.setSelection(0);
    }

    private void showNoDoctors() {
        doctorNames.clear();
        doctorNames.add("Không tải được danh sách bác sĩ");
        spinnerAdapter.notifyDataSetChanged();
    }

    // ─── Inline Selectors ──────────────────────────────────────────────────

    private void setupSelectors() {
        // Simple layout for now as per user request to restore "old" feel
        rvDates.setVisibility(View.GONE);
        rvTimeSlots.setVisibility(View.GONE);
        findViewById(R.id.loading_slots).setVisibility(View.GONE);
        
        // Hide the labels too
        View labelDate = findViewById(R.id.label_select_date);
        if (labelDate != null) labelDate.setVisibility(View.GONE);
        View labelTime = findViewById(R.id.label_select_time);
        if (labelTime != null) labelTime.setVisibility(View.GONE);

        btnBook.setEnabled(true);
        btnBook.setAlpha(1.0f);
    }

    private void loadTimeSlots(DateItem dateItem) {
        selectedTimeSlot = null;
        updateBtnState();
        
        int docIdx = spinnerDoctors.getSelectedItemPosition();
        if (docIdx < 0 || doctorList.isEmpty()) {
            generateLocalTimeSlots(dateItem, new ArrayList<>());
            return;
        }

        // doctorNames[0] is "Tất cả bác sĩ", so doctorList[docIdx - 1]
        Long doctorId = null;
        if (docIdx > 0 && docIdx <= doctorList.size()) {
            doctorId = doctorList.get(docIdx - 1).getId();
        } else if (docIdx == 0 && doctorList.size() > 0) {
            // "All doctors" selected - maybe pick the first one or pass null
            // For now, let's pick the first one if we need a specific ID for the API
            // Or pass null if the API supports it
            doctorId = doctorList.get(0).getId(); 
        }

        if (doctorId == null) {
            generateLocalTimeSlots(dateItem, new ArrayList<>());
            return;
        }

        loadingSlots.setVisibility(View.VISIBLE);
        SimpleDateFormat apiFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = apiFmt.format(dateItem.calendar.getTime());

        loadingSlots.setVisibility(View.VISIBLE);
        apiService.getDoctorSchedule(doctorId, dateStr).enqueue(new Callback<List<ScheduleAppointment>>() {
            @Override
            public void onResponse(Call<List<ScheduleAppointment>> call, Response<List<ScheduleAppointment>> response) {
                loadingSlots.setVisibility(View.GONE);
                List<ScheduleAppointment> booked = response.isSuccessful() && response.body() != null 
                    ? response.body() : new ArrayList<>();
                generateLocalTimeSlots(dateItem, booked);
            }
            @Override
            public void onFailure(Call<List<ScheduleAppointment>> call, Throwable t) {
                loadingSlots.setVisibility(View.GONE);
                generateLocalTimeSlots(dateItem, new ArrayList<>());
            }
        });
    }

    private void generateLocalTimeSlots(DateItem dateItem, List<ScheduleAppointment> booked) {
        timeSlotList.clear();
        Calendar now = Calendar.getInstance();
        boolean isToday = isSameDay(dateItem.calendar, now);

        for (int minutes = START_MINUTES; minutes <= END_MINUTES; minutes += SLOT_INTERVAL) {
            int h = minutes / 60;
            int m = minutes % 60;
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            
            boolean isPassed = isToday && (minutes <= (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE) + 15));
            boolean isBooked = false;
            for (ScheduleAppointment appt : booked) {
                if (appt.getDatetime() != null && appt.getDatetime().contains(timeStr)) {
                    isBooked = true;
                    break;
                }
            }

            timeSlotList.add(new TimeSlot(timeStr, !isPassed && !isBooked));
        }
        timeSlotAdapter.notifyDataSetChanged();
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void updateBtnState() {
        boolean ready = selectedDateItem != null && selectedTimeSlot != null;
        btnBook.setEnabled(ready);
        if (ready) {
            btnBook.setAlpha(1.0f);
        } else {
            btnBook.setAlpha(0.6f);
        }
    }

    // ─── Inner Models & Adapters ──────────────────────────────────────────

    private static class DateItem {
        Calendar calendar;
        String dayOfWeek, date, month;
        boolean isSelected = false;

        DateItem(Calendar calendar, String dayOfWeek, String date, String month) {
            this.calendar = calendar;
            this.dayOfWeek = dayOfWeek;
            this.date = date;
            this.month = month;
        }
    }

    private static class TimeSlot {
        String time;
        boolean isAvailable;
        boolean isSelected = false;

        TimeSlot(String time, boolean isAvailable) {
            this.time = time;
            this.isAvailable = isAvailable;
        }
    }

    private class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
        private final List<DateItem> items;
        private final OnDateClickListener listener;

        DateAdapter(List<DateItem> items, OnDateClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_pill, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DateItem item = items.get(position);
            holder.tvDayOfWeek.setText(item.dayOfWeek);
            holder.tvDate.setText(item.date);
            holder.tvMonth.setText(item.month);

            if (item.isSelected) {
                holder.card.setCardBackgroundColor(ContextCompat.getColor(ServiceDetailActivity.this, R.color.primary_trust_blue));
                holder.card.setStrokeColor(ContextCompat.getColor(ServiceDetailActivity.this, R.color.primary_trust_blue));
                holder.tvDayOfWeek.setTextColor(0xCCFFFFFF);
                holder.tvDate.setTextColor(0xFFFFFFFF);
                holder.tvMonth.setTextColor(0xCCFFFFFF);
            } else {
                holder.card.setCardBackgroundColor(0xFFFFFFFF);
                holder.card.setStrokeColor(0xFFE2E8F0);
                holder.tvDayOfWeek.setTextColor(ContextCompat.getColor(ServiceDetailActivity.this, R.color.text_secondary));
                holder.tvDate.setTextColor(ContextCompat.getColor(ServiceDetailActivity.this, R.color.text_primary));
                holder.tvMonth.setTextColor(ContextCompat.getColor(ServiceDetailActivity.this, R.color.text_secondary));
            }

            holder.itemView.setOnClickListener(v -> {
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

        TimeSlotAdapter(List<TimeSlot> items, OnTimeSlotClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TimeSlot item = items.get(position);
            holder.tvTime.setText(item.time);
            holder.tvTime.setEnabled(item.isAvailable);
            holder.tvTime.setSelected(item.isSelected);

            holder.itemView.setOnClickListener(v -> {
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

    // ─── Submit → POST api/appointments ──────────────────────────────────────

    private void submitBooking() {
        // Redirect to a dedicated booking screen or show a simpler dialog
        Intent intent = new Intent(this, BookAppointmentActivity.class);
        intent.putExtra(BookAppointmentActivity.EXTRA_CATEGORY, serviceCategory);
        startActivity(intent);
        finish();
    }
}
