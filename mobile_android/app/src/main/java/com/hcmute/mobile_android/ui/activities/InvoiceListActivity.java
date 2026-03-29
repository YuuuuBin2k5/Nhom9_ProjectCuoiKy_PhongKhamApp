package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.InvoiceAdapter;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.Invoice;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private InvoiceAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<Invoice> allInvoices = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_list);
        
        setupViews();
        setupRecyclerView();
        setupFilters();
        loadInvoices();
    }
    
    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hóa đơn của tôi");
        }
    }
    
    private void setupRecyclerView() {
        adapter = new InvoiceAdapter(this, new ArrayList<>(), invoice -> {
            Intent intent = new Intent(this, InvoiceDetailActivity.class);
            intent.putExtra("invoiceId", invoice.getId());
            startActivity(intent);
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupFilters() {
        findViewById(R.id.chipAll).setOnClickListener(v -> filterInvoices("ALL"));
        findViewById(R.id.chipUnpaid).setOnClickListener(v -> filterInvoices("UNPAID"));
        findViewById(R.id.chipPaid).setOnClickListener(v -> filterInvoices("PAID"));
    }
    
    private void loadInvoices() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        
        ApiService apiService = RetrofitClient.getApiService(this);
        Call<List<Invoice>> call = apiService.getMyInvoices();
        
        call.enqueue(new Callback<List<Invoice>>() {
            @Override
            public void onResponse(Call<List<Invoice>> call, Response<List<Invoice>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    allInvoices = response.body();
                    
                    if (allInvoices.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.updateData(allInvoices);
                    }
                } else {
                    Toast.makeText(InvoiceListActivity.this, 
                        "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<Invoice>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(InvoiceListActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void filterInvoices(String filter) {
        List<Invoice> filtered = new ArrayList<>();
        
        for (Invoice invoice : allInvoices) {
            if (filter.equals("ALL")) {
                filtered.add(invoice);
            } else if (filter.equals("UNPAID") && 
                       invoice.getPaymentStatus().equals("UNPAID")) {
                filtered.add(invoice);
            } else if (filter.equals("PAID") && 
                       invoice.getPaymentStatus().equals("PAID")) {
                filtered.add(invoice);
            }
        }
        
        adapter.updateData(filtered);
        
        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadInvoices();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
