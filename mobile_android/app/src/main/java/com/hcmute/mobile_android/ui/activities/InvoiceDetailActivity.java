package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.InvoiceItemAdapter;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.Invoice;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceDetailActivity extends AppCompatActivity {
    
    private TextView tvInvoiceId, tvPatientName, tvCreatedDate, tvStatus, tvTotalAmount;
    private RecyclerView recyclerViewItems;
    private Button btnPay;
    private ProgressBar progressBar;
    private Long invoiceId;
    private Invoice invoice;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);
        
        invoiceId = getIntent().getLongExtra("invoiceId", 0);
        
        setupViews();
        loadInvoiceDetail();
    }
    
    private void setupViews() {
        tvInvoiceId = findViewById(R.id.tvInvoiceId);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        btnPay = findViewById(R.id.btnPay);
        progressBar = findViewById(R.id.progressBar);
        
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết hóa đơn");
        }
        
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        
        btnPay.setOnClickListener(v -> openPaymentActivity());
    }
    
    private void loadInvoiceDetail() {
        progressBar.setVisibility(View.VISIBLE);
        
        ApiService apiService = RetrofitClient.getApiService(this);
        Call<Invoice> call = apiService.getInvoiceDetail(invoiceId);
        
        call.enqueue(new Callback<Invoice>() {
            @Override
            public void onResponse(Call<Invoice> call, Response<Invoice> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    invoice = response.body();
                    displayInvoiceDetail();
                } else {
                    Toast.makeText(InvoiceDetailActivity.this, 
                        "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Invoice> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(InvoiceDetailActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayInvoiceDetail() {
        tvInvoiceId.setText("Hóa đơn #" + invoice.getId());
        tvPatientName.setText("Bệnh nhân: " + invoice.getPatientName());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvCreatedDate.setText("Ngày tạo: " + dateFormat.format(invoice.getCreatedAt()));
        
        tvStatus.setText(invoice.getPaymentStatus());
        tvStatus.setBackgroundResource(
            invoice.getPaymentStatus().equals("PAID") ? 
            R.drawable.bg_status_paid : R.drawable.bg_status_unpaid
        );
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText(formatter.format(invoice.getTotalAmount()));
        
        if (invoice.getItems() != null) {
            InvoiceItemAdapter adapter = new InvoiceItemAdapter(this, invoice.getItems());
            recyclerViewItems.setAdapter(adapter);
        }
        
        if (invoice.getPaymentStatus().equals("PAID")) {
            btnPay.setVisibility(View.GONE);
        } else {
            btnPay.setVisibility(View.VISIBLE);
        }
    }
    
    private void openPaymentActivity() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("invoiceId", invoice.getId());
        intent.putExtra("amount", invoice.getTotalAmount().doubleValue());
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadInvoiceDetail();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
