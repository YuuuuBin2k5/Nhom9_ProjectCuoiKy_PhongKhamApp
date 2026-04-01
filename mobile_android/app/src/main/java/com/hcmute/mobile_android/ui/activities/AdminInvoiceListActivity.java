package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.Invoice;
import com.hcmute.mobile_android.util.ToastUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminInvoiceListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private AutoCompleteTextView spinnerYear, spinnerMonth, spinnerDay;

    private AdminInvoiceAdapter adapter;
    private List<Invoice> allInvoices = new ArrayList<>();
    private ApiService apiService;

    private String selectedYear, selectedMonth = "Tất cả", selectedDay = "Tất cả";
    private int currentStatusFilter = 0; // 0 = All, 1 = Unpaid, 2 = Paid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_invoice_list);

        apiService = RetrofitClient.getApiService(this);

        initViews();
        setupFilters();
        setupRecyclerView();
        setupTabs();
        
        loadInvoices();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tabLayout = findViewById(R.id.tabLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        emptyState = findViewById(R.id.emptyState);
        
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerDay = findViewById(R.id.spinnerDay);

        swipeRefresh.setOnRefreshListener(this::loadInvoices);
    }

    private void setupFilters() {
        // Years: 2024 - 2030
        List<String> years = new ArrayList<>();
        for (int y = 2024; y <= 2030; y++) years.add(String.valueOf(y));
        selectedYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        
        // Handle incoming filters from Intent
        if (getIntent().hasExtra("extra_year")) {
            selectedYear = getIntent().getStringExtra("extra_year");
        }
        if (getIntent().hasExtra("extra_month")) {
            selectedMonth = getIntent().getStringExtra("extra_month");
        }
        if (getIntent().hasExtra("extra_day")) {
            selectedDay = getIntent().getStringExtra("extra_day");
        }
        
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
            loadInvoices();
        });

        spinnerMonth.setOnItemClickListener((parent, view, position, id) -> {
            selectedMonth = months.get(position);
            if (selectedMonth.equals("Tất cả")) {
                selectedDay = "Tất cả";
                spinnerDay.setText("Tất cả", false);
            }
            loadInvoices();
        });

        spinnerDay.setOnItemClickListener((parent, view, position, id) -> {
            selectedDay = days.get(position);
            if (!selectedDay.equals("Tất cả") && selectedMonth.equals("Tất cả")) {
                selectedMonth = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
                spinnerMonth.setText(selectedMonth, false);
            }
            loadInvoices();
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminInvoiceAdapter(new ArrayList<>(), this::onInvoiceClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentStatusFilter = tab.getPosition();
                applyLocalFilters();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadInvoices() {
        swipeRefresh.setRefreshing(true);
        
        Integer year = Integer.parseInt(selectedYear);
        Integer month = selectedMonth.equals("Tất cả") ? null : Integer.parseInt(selectedMonth);
        Integer day = selectedDay.equals("Tất cả") ? null : Integer.parseInt(selectedDay);

        apiService.getAdminInvoices(year, month, day).enqueue(new Callback<List<Invoice>>() {
            @Override
            public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    allInvoices = response.body();
                    applyLocalFilters();
                } else {
                    ToastUtils.showCenteredToast(AdminInvoiceListActivity.this, "Không thể tải danh sách hóa đơn");
                    showEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<Invoice>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                ToastUtils.showCenteredToast(AdminInvoiceListActivity.this, "Lỗi kết nối: " + t.getMessage());
                showEmpty(true);
            }
        });
    }

    private void applyLocalFilters() {
        List<Invoice> filtered = new ArrayList<>();
        
        for (Invoice invoice : allInvoices) {
            String status = invoice.getPaymentStatus() != null ? invoice.getPaymentStatus() : "";
            boolean isPaid = "PAID".equalsIgnoreCase(status);
            boolean isPending = "UNPAID".equalsIgnoreCase(status) || "PARTIAL".equalsIgnoreCase(status);
            
            if (currentStatusFilter == 0) { // Tất cả
                filtered.add(invoice);
            } else if (currentStatusFilter == 1 && isPending) { // Chưa thanh toán
                filtered.add(invoice);
            } else if (currentStatusFilter == 2 && isPaid) { // Đã thanh toán
                filtered.add(invoice);
            }
        }
        
        adapter.updateData(filtered);
        showEmpty(filtered.isEmpty());
    }

    private void showEmpty(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void onInvoiceClicked(Invoice invoice) {
        Intent intent = new Intent(this, InvoiceDetailActivity.class);
        intent.putExtra("invoiceId", invoice.getId());
        startActivity(intent);
    }

    private static class AdminInvoiceAdapter extends RecyclerView.Adapter<AdminInvoiceAdapter.ViewHolder> {
        
        interface OnItemClickListener {
            void onItemClick(Invoice invoice);
        }
        
        private List<Invoice> invoices;
        private final OnItemClickListener listener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        public AdminInvoiceAdapter(List<Invoice> invoices, OnItemClickListener listener) {
            this.invoices = invoices;
            this.listener = listener;
        }

        public void updateData(List<Invoice> newInvoices) {
            this.invoices = newInvoices;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Reusing item_invoice layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invoice, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Invoice invoice = invoices.get(position);
            
            holder.tvInvoiceId.setText("Hóa đơn #" + invoice.getId());
            
            // For Admin, show patient name
            String pName = invoice.getPatientName() != null ? invoice.getPatientName() : "Khách vãng lai";
            holder.tvPatientName.setText(pName);
            holder.tvPatientName.setVisibility(View.VISIBLE);
            
            if (invoice.getCreatedAt() != null) {
                try {
                    // Assuming ISO date format from backend
                    String dateStr = invoice.getCreatedAt();
                    if (dateStr.contains("T")) {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        java.util.Date date = inputFormat.parse(dateStr);
                        if (date != null) holder.tvDate.setText(dateFormat.format(date));
                    } else {
                        holder.tvDate.setText(dateStr);
                    }
                } catch (Exception e) {
                    holder.tvDate.setText(invoice.getCreatedAt());
                }
            } else {
                holder.tvDate.setText("Chưa xác định");
            }
            
            double amount = invoice.getTotalAmount() != null ? invoice.getTotalAmount().doubleValue() : 0;
            holder.tvAmount.setText(currencyFormat.format(amount) + " đ");
            
            String status = invoice.getPaymentStatus() != null ? invoice.getPaymentStatus() : "";
            if ("PAID".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Đã thanh toán");
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else {
                holder.tvStatus.setText("Chờ thanh toán");
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            }
            
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(invoice);
            });
        }

        @Override
        public int getItemCount() {
            return invoices.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvInvoiceId, tvDate, tvStatus, tvAmount, tvPatientName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvInvoiceId = itemView.findViewById(R.id.tvInvoiceId);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                // Note: item_invoice might not have tvPatientName by default, 
                // but we can reuse a field or update the layout.
                // Looking at item_invoice.xml might be needed.
                tvPatientName = itemView.findViewById(R.id.tvPatientName);
                if (tvPatientName == null) {
                    // Fallback using tvInvoiceId or similar if not found
                    tvPatientName = tvInvoiceId; 
                }
            }
        }
    }
}
