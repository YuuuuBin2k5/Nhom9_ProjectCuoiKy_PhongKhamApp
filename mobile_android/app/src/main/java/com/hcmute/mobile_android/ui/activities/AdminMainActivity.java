package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.ui.activities.staff.QRScannerActivity;
import com.hcmute.mobile_android.util.TokenManager;
import com.google.android.material.button.MaterialButtonToggleGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.NumberFormat;

import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.AdminRevenueReport;
import com.hcmute.mobile_android.network.models.AdminRevenueCategory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMainActivity extends AppCompatActivity {

    private MaterialCardView cardServices, cardRooms, cardDoctors, cardLogout;
    private AutoCompleteTextView spinnerYear, spinnerMonth, spinnerDay;
    private TextView tvTotalRevenue;
    private android.widget.Button btnViewDetailedInvoices;
    private LinearLayout layoutRevenueItems;
    private String selectedYear, selectedMonth = "Tất cả", selectedDay = "Tất cả";
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);
        
        apiService = RetrofitClient.getApiService(this);

        initViews();
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        cardServices = findViewById(R.id.cardServices);
        cardRooms = findViewById(R.id.cardRooms);
        cardDoctors = findViewById(R.id.cardDoctors);
        cardLogout = findViewById(R.id.cardLogout);
        
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerDay = findViewById(R.id.spinnerDay);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        btnViewDetailedInvoices = findViewById(R.id.btnViewDetailedInvoices);
        layoutRevenueItems = findViewById(R.id.layoutRevenueItems);
        
        setupFilterSpinners();
        loadRealRevenueData();
    }

    private void setupFilterSpinners() {
        // Years: 2024 - 2030
        List<String> years = new ArrayList<>();
        for (int y = 2024; y <= 2030; y++) years.add(String.valueOf(y));
        selectedYear = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
        
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setText(selectedYear, false);

        // Months: Tất cả, 1-12
        List<String> months = new ArrayList<>();
        months.add("Tất cả");
        for (int m = 1; m <= 12; m++) months.add(String.valueOf(m));
        
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, months);
        spinnerMonth.setAdapter(monthAdapter);
        spinnerMonth.setText(selectedMonth, false);

        // Days: Tất cả, 1-31
        List<String> days = new ArrayList<>();
        days.add("Tất cả");
        for (int d = 1; d <= 31; d++) days.add(String.valueOf(d));
        
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, days);
        spinnerDay.setAdapter(dayAdapter);
        spinnerDay.setText(selectedDay, false);

        // Listeners
        spinnerYear.setOnItemClickListener((parent, view, position, id) -> {
            selectedYear = years.get(position);
            loadRealRevenueData();
        });

        spinnerMonth.setOnItemClickListener((parent, view, position, id) -> {
            selectedMonth = months.get(position);
            if (selectedMonth.equals("Tất cả")) {
                selectedDay = "Tất cả";
                spinnerDay.setText("Tất cả", false);
            }
            loadRealRevenueData();
        });

        spinnerDay.setOnItemClickListener((parent, view, position, id) -> {
            selectedDay = days.get(position);
            if (!selectedDay.equals("Tất cả") && selectedMonth.equals("Tất cả")) {
                selectedMonth = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1);
                spinnerMonth.setText(selectedMonth, false);
            }
            loadRealRevenueData();
        });
    }

    private void setupClickListeners() {
        cardServices.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminServiceActivity.class));
        });

        cardRooms.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminRoomActivity.class));
        });

        cardDoctors.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDoctorActivity.class));
        });

        cardLogout.setOnClickListener(v -> {
            logout();
        });

        btnViewDetailedInvoices.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminInvoiceListActivity.class);
            intent.putExtra("extra_year", selectedYear);
            intent.putExtra("extra_month", selectedMonth);
            intent.putExtra("extra_day", selectedDay);
            startActivity(intent);
        });
    }

    private void loadRealRevenueData() {
        Integer year = Integer.parseInt(selectedYear);
        Integer month = selectedMonth.equals("Tất cả") ? null : Integer.parseInt(selectedMonth);
        Integer day = selectedDay.equals("Tất cả") ? null : Integer.parseInt(selectedDay);

        apiService.getAdminRevenueReport(year, month, day).enqueue(new Callback<AdminRevenueReport>() {
            @Override
            public void onResponse(Call<AdminRevenueReport> call, Response<AdminRevenueReport> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayRevenueData(response.body());
                } else {
                    Toast.makeText(AdminMainActivity.this, "Lỗi khi lấy dữ liệu doanh thu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminRevenueReport> call, Throwable t) {
                Toast.makeText(AdminMainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRevenueData(AdminRevenueReport report) {
        layoutRevenueItems.removeAllViews();
        NumberFormat vnFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        if (report.getCategories() != null) {
            for (AdminRevenueCategory category : report.getCategories()) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_revenue_category, layoutRevenueItems, false);
                TextView tvName = itemView.findViewById(R.id.tvCategoryName);
                TextView tvAmount = itemView.findViewById(R.id.tvCategoryAmount);
                
                tvName.setText(category.getCategoryName());
                tvAmount.setText(vnFormat.format(category.getTotalAmount()).replace("₫", "đ"));
                
                layoutRevenueItems.addView(itemView);
            }
        }
        
        tvTotalRevenue.setText(vnFormat.format(report.getTotalRevenue()).replace("₫", "đ"));
    }

    private void logout() {
        new TokenManager(this).clearToken();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}