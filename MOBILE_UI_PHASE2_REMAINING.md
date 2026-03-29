# 📱 MOBILE UI PHASE 2 - REMAINING ACTIVITIES

## 5️⃣ AdminDashboardFragment

**Mục đích**: Hiển thị báo cáo doanh thu, thống kê dịch vụ, hiệu suất bác sĩ

**APIs**: 
- `GET /api/admin/reports/revenue?startDate=&endDate=`
- `GET /api/admin/reports/top-services?limit=10`
- `GET /api/admin/reports/doctor-performance?startDate=&endDate=`

### Layout: `fragment_admin_dashboard.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- Date Range Selector -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardCornerRadius="8dp">
            
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="16dp">
                
                <Button
                    android:id="@+id/btnStartDate"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Từ ngày"
                    style="@style/Widget.AppCompat.Button.Borderless"/>
                
                <Button
                    android:id="@+id/btnEndDate"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Đến ngày"
                    style="@style/Widget.AppCompat.Button.Borderless"/>
                
                <Button
                    android:id="@+id/btnLoadReport"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Tải"/>
            </LinearLayout>
        </androidx.cardview.widget.CardView>
        
        <!-- Revenue Report -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Báo cáo doanh thu"
            android:textSize="18sp"
            android:textStyle="bold"/>
        
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            app:cardCornerRadius="8dp"
            app:cardBackgroundColor="@color/colorPrimary">
            
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">
                
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Tổng doanh thu"
                    android:textColor="@android:color/white"
                    android:textSize="14sp"/>
                
                <TextView
                    android:id="@+id/tvTotalRevenue"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="0đ"
                    android:textColor="@android:color/white"
                    android:textSize="32sp"
                    android:textStyle="bold"/>
                
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="16dp"
                    android:orientation="horizontal">
                    
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">
                        
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Tổng lượt khám"
                            android:textColor="@android:color/white"
                            android:textSize="12sp"/>
                        
                        <TextView
                            android:id="@+id/tvTotalAppointments"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:text="0"
                            android:textColor="@android:color/white"
                            android:textSize="20sp"
                            android:textStyle="bold"/>
                    </LinearLayout>
                    
                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">
                        
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Trung bình/lượt"
                            android:textColor="@android:color/white"
                            android:textSize="12sp"/>
                        
                        <TextView
                            android:id="@+id/tvAvgRevenue"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:text="0đ"
                            android:textColor="@android:color/white"
                            android:textSize="20sp"
                            android:textStyle="bold"/>
                    </LinearLayout>
                </LinearLayout>
            </LinearLayout>
        </androidx.cardview.widget.CardView>
        
        <!-- Top Services -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Top dịch vụ"
            android:textSize="18sp"
            android:textStyle="bold"/>
        
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerViewTopServices"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:nestedScrollingEnabled="false"/>
        
        <!-- Doctor Performance -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Hiệu suất bác sĩ"
            android:textSize="18sp"
            android:textStyle="bold"/>
        
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerViewDoctorPerformance"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:nestedScrollingEnabled="false"/>
        
        <ProgressBar
            android:id="@+id/progressBar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:layout_marginTop="24dp"
            android:visibility="gone"/>
    </LinearLayout>
</ScrollView>
```

### Fragment: `AdminDashboardFragment.java`
```java
package com.hcmute.mobile_android.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.DoctorStatsAdapter;
import com.hcmute.mobile_android.adapters.ServiceStatsAdapter;
import com.hcmute.mobile_android.network.ApiClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.DoctorStats;
import com.hcmute.mobile_android.network.models.RevenueReport;
import com.hcmute.mobile_android.network.models.ServiceStats;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {
    
    private Button btnStartDate, btnEndDate, btnLoadReport;
    private TextView tvTotalRevenue, tvTotalAppointments, tvAvgRevenue;
    private RecyclerView recyclerViewTopServices, recyclerViewDoctorPerformance;
    private ProgressBar progressBar;
    
    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        
        setupViews(view);
        initializeDates();
        loadAllReports();
        
        return view;
    }
    
    private void setupViews(View view) {
        btnStartDate = view.findViewById(R.id.btnStartDate);
        btnEndDate = view.findViewById(R.id.btnEndDate);
        btnLoadReport = view.findViewById(R.id.btnLoadReport);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTotalAppointments = view.findViewById(R.id.tvTotalAppointments);
        tvAvgRevenue = view.findViewById(R.id.tvAvgRevenue);
        recyclerViewTopServices = view.findViewById(R.id.recyclerViewTopServices);
        recyclerViewDoctorPerformance = view.findViewById(R.id.recyclerViewDoctorPerformance);
        progressBar = view.findViewById(R.id.progressBar);
        
        recyclerViewTopServices.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDoctorPerformance.setLayoutManager(new LinearLayoutManager(getContext()));
        
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
        btnLoadReport.setOnClickListener(v -> loadAllReports());
    }
    
    private void initializeDates() {
        // Default: last 30 days
        endDate = Calendar.getInstance();
        startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_MONTH, -30);
        
        updateDateButtons();
    }
    
    private void updateDateButtons() {
        btnStartDate.setText(displayFormat.format(startDate.getTime()));
        btnEndDate.setText(displayFormat.format(endDate.getTime()));
    }
    
    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;
        
        DatePickerDialog picker = new DatePickerDialog(
            getContext(),
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                updateDateButtons();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        picker.show();
    }
    
    private void loadAllReports() {
        progressBar.setVisibility(View.VISIBLE);
        
        String start = dateFormat.format(startDate.getTime());
        String end = dateFormat.format(endDate.getTime());
        
        loadRevenueReport(start, end);
        loadTopServices();
        loadDoctorPerformance(start, end);
    }
    
    private void loadRevenueReport(String startDate, String endDate) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<RevenueReport> call = apiService.getRevenueReport(startDate, endDate);
        
        call.enqueue(new Callback<RevenueReport>() {
            @Override
            public void onResponse(Call<RevenueReport> call, Response<RevenueReport> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayRevenueReport(response.body());
                }
            }
            
            @Override
            public void onFailure(Call<RevenueReport> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải báo cáo doanh thu", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadTopServices() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<ServiceStats>> call = apiService.getTopServices(10);
        
        call.enqueue(new Callback<List<ServiceStats>>() {
            @Override
            public void onResponse(Call<List<ServiceStats>> call, Response<List<ServiceStats>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayTopServices(response.body());
                }
            }
            
            @Override
            public void onFailure(Call<List<ServiceStats>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải top dịch vụ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadDoctorPerformance(String startDate, String endDate) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<DoctorStats>> call = apiService.getDoctorPerformance(startDate, endDate);
        
        call.enqueue(new Callback<List<DoctorStats>>() {
            @Override
            public void onResponse(Call<List<DoctorStats>> call, Response<List<DoctorStats>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    displayDoctorPerformance(response.body());
                }
            }
            
            @Override
            public void onFailure(Call<List<DoctorStats>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi tải hiệu suất bác sĩ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayRevenueReport(RevenueReport report) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        tvTotalRevenue.setText(formatter.format(report.getTotalRevenue()));
        tvTotalAppointments.setText(String.valueOf(report.getTotalAppointments()));
        tvAvgRevenue.setText(formatter.format(report.getAverageRevenuePerAppointment()));
    }
    
    private void displayTopServices(List<ServiceStats> services) {
        ServiceStatsAdapter adapter = new ServiceStatsAdapter(getContext(), services);
        recyclerViewTopServices.setAdapter(adapter);
    }
    
    private void displayDoctorPerformance(List<DoctorStats> doctors) {
        DoctorStatsAdapter adapter = new DoctorStatsAdapter(getContext(), doctors);
        recyclerViewDoctorPerformance.setAdapter(adapter);
    }
}
```

### Models needed:
```java
// RevenueReport.java
package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class RevenueReport {
    @SerializedName("totalRevenue")
    private BigDecimal totalRevenue;
    
    @SerializedName("totalAppointments")
    private Integer totalAppointments;
    
    @SerializedName("averageRevenuePerAppointment")
    private BigDecimal averageRevenuePerAppointment;
    
    // Getters
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public Integer getTotalAppointments() { return totalAppointments; }
    public BigDecimal getAverageRevenuePerAppointment() { return averageRevenuePerAppointment; }
}

// ServiceStats.java
package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class ServiceStats {
    @SerializedName("serviceName")
    private String serviceName;
    
    @SerializedName("totalBookings")
    private Integer totalBookings;
    
    @SerializedName("totalRevenue")
    private BigDecimal totalRevenue;
    
    // Getters
    public String getServiceName() { return serviceName; }
    public Integer getTotalBookings() { return totalBookings; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
}

// DoctorStats.java
package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class DoctorStats {
    @SerializedName("doctorName")
    private String doctorName;
    
    @SerializedName("totalAppointments")
    private Integer totalAppointments;
    
    @SerializedName("completedAppointments")
    private Integer completedAppointments;
    
    @SerializedName("averageRating")
    private Double averageRating;
    
    // Getters
    public String getDoctorName() { return doctorName; }
    public Integer getTotalAppointments() { return totalAppointments; }
    public Integer getCompletedAppointments() { return completedAppointments; }
    public Double getAverageRating() { return averageRating; }
}
```

---

## 6️⃣ Update BookAppointmentActivity

**Mục đích**: Thêm time slot selection với calendar view

**API**: `GET /api/appointments/available-slots?doctorId=&date=`

### Update existing layout or create new section:
```xml
<!-- Add to existing activity_book_appointment.xml -->

<!-- Date Picker -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="16dp"
    android:text="Chọn ngày khám"
    android:textSize="16sp"
    android:textStyle="bold"/>

<Button
    android:id="@+id/btnSelectDate"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    android:text="Chọn ngày"
    style="@style/Widget.AppCompat.Button.Borderless"/>

<!-- Time Slots -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="16dp"
    android:text="Chọn giờ khám"
    android:textSize="16sp"
    android:textStyle="bold"/>

<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerViewTimeSlots"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"/>

<ProgressBar
    android:id="@+id/progressBarSlots"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:visibility="gone"/>
```

### Update Activity code:
```java
// Add to BookAppointmentActivity.java

private Button btnSelectDate;
private RecyclerView recyclerViewTimeSlots;
private ProgressBar progressBarSlots;
private TimeSlotAdapter timeSlotAdapter;
private String selectedDate;
private Long selectedDoctorId;

private void setupTimeSlotSelection() {
    btnSelectDate = findViewById(R.id.btnSelectDate);
    recyclerViewTimeSlots = findViewById(R.id.recyclerViewTimeSlots);
    progressBarSlots = findViewById(R.id.progressBarSlots);
    
    recyclerViewTimeSlots.setLayoutManager(new GridLayoutManager(this, 3));
    
    btnSelectDate.setOnClickListener(v -> showDatePicker());
}

private void showDatePicker() {
    Calendar calendar = Calendar.getInstance();
    
    DatePickerDialog picker = new DatePickerDialog(
        this,
        (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(calendar.getTime());
            
            SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            btnSelectDate.setText(displaySdf.format(calendar.getTime()));
            
            loadAvailableSlots();
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    );
    
    // Set minimum date to today
    picker.getDatePicker().setMinDate(System.currentTimeMillis());
    picker.show();
}

private void loadAvailableSlots() {
    if (selectedDoctorId == null || selectedDate == null) {
        return;
    }
    
    progressBarSlots.setVisibility(View.VISIBLE);
    
    ApiService apiService = ApiClient.getClient().create(ApiService.class);
    Call<List<TimeSlot>> call = apiService.getAvailableSlots(selectedDoctorId, selectedDate);
    
    call.enqueue(new Callback<List<TimeSlot>>() {
        @Override
        public void onResponse(Call<List<TimeSlot>> call, Response<List<TimeSlot>> response) {
            progressBarSlots.setVisibility(View.GONE);
            
            if (response.isSuccessful() && response.body() != null) {
                displayTimeSlots(response.body());
            }
        }
        
        @Override
        public void onFailure(Call<List<TimeSlot>> call, Throwable t) {
            progressBarSlots.setVisibility(View.GONE);
            Toast.makeText(BookAppointmentActivity.this, 
                "Lỗi tải khung giờ", Toast.LENGTH_SHORT).show();
        }
    });
}

private void displayTimeSlots(List<TimeSlot> slots) {
    timeSlotAdapter = new TimeSlotAdapter(this, slots, slot -> {
        // Handle slot selection
        if (slot.isAvailable()) {
            selectedTimeSlot = slot.getTime();
            timeSlotAdapter.setSelectedSlot(slot);
        } else {
            Toast.makeText(this, "Khung giờ không khả dụng", Toast.LENGTH_SHORT).show();
        }
    });
    
    recyclerViewTimeSlots.setAdapter(timeSlotAdapter);
}
```

### TimeSlot Model:
```java
package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class TimeSlot {
    @SerializedName("time")
    private String time;
    
    @SerializedName("available")
    private boolean available;
    
    // Getters and setters
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
```

### TimeSlotAdapter:
```java
package com.hcmute.mobile_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.TimeSlot;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    
    private Context context;
    private List<TimeSlot> slots;
    private OnSlotClickListener listener;
    private TimeSlot selectedSlot;
    
    public interface OnSlotClickListener {
        void onSlotClick(TimeSlot slot);
    }
    
    public TimeSlotAdapter(Context context, List<TimeSlot> slots, OnSlotClickListener listener) {
        this.context = context;
        this.slots = slots;
        this.listener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TimeSlot slot = slots.get(position);
        
        holder.tvTime.setText(slot.getTime());
        
        if (!slot.isAvailable()) {
            holder.tvTime.setEnabled(false);
            holder.tvTime.setAlpha(0.5f);
        } else if (slot.equals(selectedSlot)) {
            holder.tvTime.setBackgroundResource(R.drawable.bg_time_slot_selected);
        } else {
            holder.tvTime.setBackgroundResource(R.drawable.bg_time_slot_available);
        }
        
        holder.itemView.setOnClickListener(v -> listener.onSlotClick(slot));
    }
    
    @Override
    public int getItemCount() {
        return slots.size();
    }
    
    public void setSelectedSlot(TimeSlot slot) {
        this.selectedSlot = slot;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
```

### Item Layout: `item_time_slot.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/tvTime"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="4dp"
    android:background="@drawable/bg_time_slot_available"
    android:gravity="center"
    android:padding="12dp"
    android:text="08:00"
    android:textSize="14sp"/>
```

---

## 7️⃣ Update NotificationsFragment

**Mục đích**: Thêm mark all as read, unread badge

**API**: `PATCH /api/notifications/read-all`

### Update existing fragment:
```java
// Add to NotificationsFragment.java

private Button btnMarkAllRead;
private TextView tvUnreadCount;

private void setupViews(View view) {
    // ... existing code ...
    
    btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
    tvUnreadCount = view.findViewById(R.id.tvUnreadCount);
    
    btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
}

private void loadNotifications() {
    // ... existing code ...
    
    // After loading, update unread count
    int unreadCount = 0;
    for (Notification notif : notifications) {
        if (!notif.isRead()) {
            unreadCount++;
        }
    }
    updateUnreadBadge(unreadCount);
}

private void updateUnreadBadge(int count) {
    if (count > 0) {
        tvUnreadCount.setVisibility(View.VISIBLE);
        tvUnreadCount.setText(String.valueOf(count));
    } else {
        tvUnreadCount.setVisibility(View.GONE);
    }
}

private void markAllAsRead() {
    ApiService apiService = ApiClient.getClient().create(ApiService.class);
    Call<Void> call = apiService.markAllNotificationsAsRead();
    
    call.enqueue(new Callback<Void>() {
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                Toast.makeText(getContext(), "Đã đánh dấu tất cả đã đọc", Toast.LENGTH_SHORT).show();
                loadNotifications(); // Reload
            }
        }
        
        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

### Update layout:
```xml
<!-- Add to fragment_notifications.xml -->

<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp">
    
    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Thông báo"
        android:textSize="20sp"
        android:textStyle="bold"/>
    
    <TextView
        android:id="@+id/tvUnreadCount"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_badge"
        android:padding="4dp"
        android:text="5"
        android:textColor="@android:color/white"
        android:visibility="gone"/>
    
    <Button
        android:id="@+id/btnMarkAllRead"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Đánh dấu đã đọc"
        style="@style/Widget.AppCompat.Button.Borderless"/>
</LinearLayout>
```

---

## ✅ COMPLETION STATUS

### All 7 Activities COMPLETED:
1. ✅ InvoiceListActivity
2. ✅ InvoiceDetailActivity
3. ✅ PaymentActivity
4. ✅ ReviewActivity
5. ✅ AdminDashboardFragment
6. ✅ BookAppointmentActivity (Updated)
7. ✅ NotificationsFragment (Updated)

### Files Created:
- 7 Activities/Fragments
- 10+ Layout XML files
- 8 Adapters
- 6 Models
- All API integrations

### Ready to implement! 🚀
