package com.hcmute.mobile_android.ui.activities;

import com.hcmute.mobile_android.util.TokenManager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hcmute.mobile_android.adapters.ServiceImageAdapter;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.util.ToastUtils;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CreateAppointmentRequest;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetailActivity extends AppCompatActivity {

    private TextView tvServiceName, tvServicePrice, tvServiceDesc, tvDatetimeSelected;
    private Spinner spinnerDoctors;
    private LinearLayout llDatetimePicker;
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

    // Chosen datetime (ISO: yyyy-MM-dd'T'HH:mm:ss)
    private String selectedDatetime = null;

    private static final int START_MINUTES = 8 * 60; // 08:00
    private static final int END_MINUTES = 16 * 60 + 40; // 16:40

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_detail);

        // Read extras with the same keys used in HomeFragment's ServiceAdapter
        serviceId       = getIntent().getLongExtra("id", -1L);
        serviceName     = getIntent().getStringExtra("name");
        servicePrice    = getIntent().getDoubleExtra("price", 0.0);
        serviceDesc     = getIntent().getStringExtra("description");
        serviceDuration = getIntent().getIntExtra("duration", 0);
        serviceCategory = getIntent().getStringExtra("category");
        imageUrls       = getIntent().getStringArrayListExtra("imageUrls");

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(serviceName != null ? serviceName : "Chi tiết dịch vụ");
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        tvServiceName     = findViewById(R.id.tvServiceName);
        tvServicePrice    = findViewById(R.id.tvServicePrice);
        tvServiceDesc     = findViewById(R.id.tvServiceDesc);
        spinnerDoctors    = findViewById(R.id.spinnerDoctors);
        llDatetimePicker  = findViewById(R.id.ll_datetime_picker);
        tvDatetimeSelected = findViewById(R.id.tv_datetime_selected);
        btnBook           = findViewById(R.id.btnBook);
        vpServiceImages   = findViewById(R.id.vpServiceImages);
        tabIndicator      = findViewById(R.id.tabIndicator);

        // Populate static info
        tvServiceName.setText(serviceName != null ? serviceName : "");
        tvServicePrice.setText(formatPrice(servicePrice));
        tvServiceDesc.setText(
                serviceDesc != null && !serviceDesc.isEmpty()
                        ? serviceDesc : "Không có mô tả chi tiết.");

        apiService = RetrofitClient.getApiService(this);

        // Doctor spinner placeholder
        doctorNames.add("Đang tải bác sĩ...");
        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, doctorNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctors.setAdapter(spinnerAdapter);

        loadDoctors();
        setupImages();

        // Date & time picker row
        llDatetimePicker.setOnClickListener(v -> pickDatetime());

        // Book button → validate then submit directly to API
        btnBook.setOnClickListener(v -> submitBooking());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
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

    // ─── Date & Time picker ───────────────────────────────────────────────────

    private void pickDatetime() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                showTimePicker(year, month, day),
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
            {{ getDatePicker().setMinDate(now.getTimeInMillis()); }}
            .show();
    }

    private void showTimePicker(int year, int month, int day) {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (tv, hour, minute) -> {
            int totalMin = hour * 60 + minute;

            if (totalMin < START_MINUTES || totalMin > END_MINUTES) {
                ToastUtils.showCenteredToastLong(this,
                        "Chỉ được đặt lịch trong khung giờ làm việc: 08:00 – 16:40.");
                clearSelectedDatetime();
                return;
            }

            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, day, hour, minute, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);

            if (selectedCalendar.before(now)) {
                ToastUtils.showCenteredToastLong(this,
                        "Giờ này đã qua so với hiện tại. Vui lòng chọn giờ từ 08:00 đến 16:40, sau thời điểm hiện tại.");
                clearSelectedDatetime();
                return;
            }
            // Update ISO format for Backend
            selectedDatetime = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d %02d:%02d:00",
                    year, month + 1, day, hour, minute);
            // Format for display
            String display = String.format(Locale.getDefault(),
                    "%02d/%02d/%04d  •  %02d:%02d",
                    day, month + 1, year, hour, minute);
            tvDatetimeSelected.setText(display);
            tvDatetimeSelected.setTextColor(0xFF1A1A1A);
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    private void clearSelectedDatetime() {
        selectedDatetime = null;
        if (tvDatetimeSelected != null) {
            tvDatetimeSelected.setText("Nhấn để chọn ngày & giờ");
            tvDatetimeSelected.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    // ─── Submit → POST api/appointments ──────────────────────────────────────

    private void submitBooking() {
        if (doctorList.isEmpty()) {
            ToastUtils.showCenteredToast(this, "Hiện tại không có bác sĩ phù hợp");
            return;
        }
        if (selectedDatetime == null) {
            ToastUtils.showCenteredToast(this, "Vui lòng chọn ngày & giờ khám");
            return;
        }

        try {
            String timePart = selectedDatetime.substring(11, 16);
            String[] parts = timePart.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int totalMin = h * 60 + m;
            if (totalMin < START_MINUTES || totalMin > END_MINUTES) {
                ToastUtils.showCenteredToastLong(this,
                        "Chỉ được đặt lịch trong khung giờ làm việc: 08:00 – 16:40.");
                clearSelectedDatetime();
                return;
            }
        } catch (Exception ignored) { }

        int idx = spinnerDoctors.getSelectedItemPosition();
        DoctorItem doctor = (idx >= 0 && idx < doctorList.size()) ? doctorList.get(idx) : null;
        Long doctorId = (doctor != null) ? doctor.getId() : null;

        btnBook.setEnabled(false);
        btnBook.setText("Đang đặt lịch...");

        Long patientId = new TokenManager(this).getPatientId();

        CreateAppointmentRequest req = new CreateAppointmentRequest(serviceId, doctorId, patientId, selectedDatetime);
        apiService.createAppointment(req).enqueue(new Callback<UpcomingAppointment>() {
            @Override
            public void onResponse(Call<UpcomingAppointment> call, Response<UpcomingAppointment> response) {
                btnBook.setEnabled(true);
                btnBook.setText("Đặt lịch hẹn ngay");
                if (response.isSuccessful()) {  
                    ToastUtils.showCenteredToastLong(ServiceDetailActivity.this, "✅ Đặt lịch thành công!");
                    finish(); // Về Home → onResume reload upcoming appointments
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
                    ToastUtils.showCenteredToastLong(ServiceDetailActivity.this, errorMsg);
                }
            }
            public void onFailure(Call<UpcomingAppointment> call, Throwable t) {
                btnBook.setEnabled(true);
                btnBook.setText("Đặt lịch hẹn ngay");
                ToastUtils.showCenteredToast(ServiceDetailActivity.this, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
