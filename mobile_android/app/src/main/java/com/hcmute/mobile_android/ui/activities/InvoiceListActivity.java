package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private LinearLayout emptyState;

    private InvoiceAdapter adapter;
    private List<Invoice> allInvoices = new ArrayList<>();
    private ApiService apiService;

    // Filter state — mặc định tab "Chờ thanh toán" (UC_08)
    private int currentFilter = 1; // 0 = All, 1 = Unpaid, 2 = Paid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_list);

        apiService = RetrofitClient.getApiService(this);

        initViews();
        setupRecyclerView();
        setupTabs();
        tabLayout.post(() -> {
            TabLayout.Tab t = tabLayout.getTabAt(1);
            if (t != null) t.select();
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
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

        swipeRefresh.setOnRefreshListener(this::loadInvoices);
    }

    private void setupRecyclerView() {
        adapter = new InvoiceAdapter(new ArrayList<>(), this::onInvoiceClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = tab.getPosition();
                applyFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadInvoices() {
        swipeRefresh.setRefreshing(true);
        apiService.getMyInvoices().enqueue(new Callback<List<Invoice>>() {
            @Override
            public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    allInvoices = response.body();
                    
                    // Sort by created date descending (latest first)
                    allInvoices.sort((i1, i2) -> {
                        if (i1.getCreatedAt() == null || i2.getCreatedAt() == null) return 0;
                        return i2.getCreatedAt().compareTo(i1.getCreatedAt());
                    });
                    
                    applyFilter();
                } else {
                    ToastUtils.showCenteredToast(InvoiceListActivity.this, "Không thể tải danh sách hóa đơn");
                    showEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<Invoice>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                ToastUtils.showCenteredToast(InvoiceListActivity.this, "Lỗi kết nối");
                showEmpty(true);
            }
        });
    }

    private void applyFilter() {
        List<Invoice> filtered = new ArrayList<>();
        
        for (Invoice invoice : allInvoices) {
            String st = invoice.getPaymentStatus() != null ? invoice.getPaymentStatus() : "";
            boolean isPaid = "PAID".equalsIgnoreCase(st);
            boolean isPending = "UNPAID".equalsIgnoreCase(st) || "PARTIAL".equalsIgnoreCase(st);
            
            if (currentFilter == 0) { // Tất cả
                filtered.add(invoice);
            } else if (currentFilter == 1 && isPending) { // Chưa thanh toán (PENDING / UNPAID / PARTIAL)
                filtered.add(invoice);
            } else if (currentFilter == 2 && isPaid) { // Đã thanh toán
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

    private static class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {
        
        interface OnItemClickListener {
            void onItemClick(Invoice invoice);
        }
        
        private List<Invoice> invoices;
        private final OnItemClickListener listener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        public InvoiceAdapter(List<Invoice> invoices, OnItemClickListener listener) {
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
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invoice, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Invoice invoice = invoices.get(position);
            
            holder.tvInvoiceId.setText("Hóa đơn #" + invoice.getId());
            
            if (invoice.getCreatedAt() != null) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    java.util.Date date = inputFormat.parse(invoice.getCreatedAt());
                    if (date != null) holder.tvDate.setText(dateFormat.format(date));
                } catch (Exception e) {
                    // Fallback substring if parse fails
                    String dateStr = invoice.getCreatedAt();
                    if (dateStr.length() > 10) {
                        dateStr = dateStr.substring(0, 10);
                    }
                    holder.tvDate.setText(dateStr);
                }
            } else {
                holder.tvDate.setText("Chưa xác định");
            }
            
            double amount = invoice.getTotalAmount() != null ? invoice.getTotalAmount().doubleValue() : 0;
            holder.tvAmount.setText(currencyFormat.format(amount) + " VNĐ");
            
            String st = invoice.getPaymentStatus() != null ? invoice.getPaymentStatus() : "";
            boolean isPaid = "PAID".equalsIgnoreCase(st);
            if (isPaid) {
                holder.tvStatus.setText("Đã thanh toán");
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                // holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_grey);
            } else {
                holder.tvStatus.setText("Chờ thanh toán");
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"));
                // holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_grey);
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
            TextView tvInvoiceId, tvDate, tvStatus, tvAmount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvInvoiceId = itemView.findViewById(R.id.tvInvoiceId);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvAmount = itemView.findViewById(R.id.tvAmount);
            }
        }
    }
}
