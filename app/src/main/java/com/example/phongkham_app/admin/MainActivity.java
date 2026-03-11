package com.example.phongkham_app.admin;

import com.example.phongkham_app.R;
import com.example.phongkham_app.auth.LoginActivity;
import com.example.phongkham_app.admin.viewmodel.AdminHomeViewModel;
import com.example.phongkham_app.data.model.Invoice;
import com.example.phongkham_app.data.model.Doctor;
import com.example.phongkham_app.data.model.Service;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AdminHomeViewModel viewModel;
    private InvoiceAdapter invoiceAdapter;
    private TextView tvTodayRevenue, tvYearRevenue, tvTotalPatients;
    private Spinner spinnerRevenueFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_admin_home);

        viewModel = new ViewModelProvider(this).get(AdminHomeViewModel.class);

        initViews();
        setupRecyclerView();
        setupSpinner();
        setupClickListeners();
        setupBottomNavigation();
        observeViewModel();
    }

    private void initViews() {
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue);
        tvYearRevenue = findViewById(R.id.tvYearRevenue);
        tvTotalPatients = findViewById(R.id.tvTotalPatients);
        spinnerRevenueFilter = findViewById(R.id.spinnerRevenueFilter);
    }

    private void setupRecyclerView() {
        RecyclerView rvRecentInvoices = findViewById(R.id.rvRecentInvoices);
        rvRecentInvoices.setLayoutManager(new LinearLayoutManager(this));
        invoiceAdapter = new InvoiceAdapter(new ArrayList<>(), invoice -> {
            Intent intent = new Intent(this, InvoiceDetailActivity.class);
            startActivity(intent);
        });
        rvRecentInvoices.setAdapter(invoiceAdapter);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this,
            R.array.revenue_filter_options,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerRevenueFilter != null) {
            spinnerRevenueFilter.setAdapter(adapter);
            spinnerRevenueFilter.setSelection(1);
            spinnerRevenueFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Logic lọc doanh thu có thể chuyển vào ViewModel sau này
                    updateRevenueDisplay(position);
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void updateRevenueDisplay(int filterType) {
        // Logic hiển thị giả lập (Nên chuyển vào ViewModel để quản lý State)
        switch (filterType) {
            case 0:
                tvTodayRevenue.setText("5,120,000 VNĐ");
                tvYearRevenue.setText("48");
                tvTotalPatients.setText("48");
                break;
            case 1:
                tvTodayRevenue.setText(viewModel.getTotalRevenue().getValue());
                tvYearRevenue.setText("1,245");
                tvTotalPatients.setText("1,245");
                break;
            case 2:
                tvTodayRevenue.setText("1.85 Tỷ VNĐ");
                tvYearRevenue.setText("15,340");
                tvTotalPatients.setText("15,340");
                break;
        }
    }

    private void observeViewModel() {
        viewModel.getRecentInvoices().observe(this, invoices -> {
            if (invoices != null) {
                invoiceAdapter.updateData(invoices);
            }
        });
        
        // Cập nhật doanh thu từ ViewModel nếu cần thiết
        viewModel.getTotalRevenue().observe(this, revenue -> {
            if (spinnerRevenueFilter != null && spinnerRevenueFilter.getSelectedItemPosition() == 1) {
                tvTodayRevenue.setText(revenue);
            }
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.cardManageDoctors).setOnClickListener(v -> startActivity(new Intent(this, ManageDoctorsActivity.class)));
        findViewById(R.id.cardManageServices).setOnClickListener(v -> startActivity(new Intent(this, ManageServicesActivity.class)));
        findViewById(R.id.cardManageShifts).setOnClickListener(v -> startActivity(new Intent(this, ManageShiftsActivity.class)));
        findViewById(R.id.cardManageInvoices).setOnClickListener(v -> startActivity(new Intent(this, ManageInvoicesActivity.class)));

        View btnAdminLogout = findViewById(R.id.ivAdminLogout);
        if (btnAdminLogout != null) {
            btnAdminLogout.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_setting) {
                startActivity(new Intent(this, SettingsAdminActivity.class));
                return true;
            }
            Toast.makeText(this, "Chức năng " + item.getTitle() + " đang phát triển", Toast.LENGTH_SHORT).show();
            return true;
        });
    }
}