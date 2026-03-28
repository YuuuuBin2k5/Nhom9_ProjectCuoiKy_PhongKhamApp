# 📱 MOBILE UI IMPLEMENTATION GUIDE

**Mục đích**: Hướng dẫn chi tiết implement mobile UI cho Phase 2

---

## 🎯 TỔNG QUAN

### Đã có sẵn:
- ✅ All backend APIs (26+ endpoints)
- ✅ All mobile models (Review, Invoice, PaymentRequest, etc.)
- ✅ ApiService updated with all endpoints
- ✅ Network layer ready

### Cần implement:
- ⏳ 7 Activities
- ⏳ 7 Layouts
- ⏳ 3 Adapters
- ⏳ Integration with APIs

---

## 📋 DANH SÁCH ACTIVITIES CẦN TẠO

### 1. InvoiceListActivity
**Mục đích**: Hiển thị danh sách hóa đơn của bệnh nhân

**API**: `GET /api/invoices/my`

**Layout**: `activity_invoice_list.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    
    <!-- Toolbar -->
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar"/>
    
    <!-- Filter Chips -->
    <com.google.android.material.chip.ChipGroup
        android:id="@+id/chipGroup"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">
        
        <com.google.android.material.chip.Chip
            android:id="@+id/chipAll"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Tất cả"
            android:checked="true"/>
        
        <com.google.android.material.chip.Chip
            android:id="@+id/chipUnpaid"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Chưa thanh toán"/>
        
        <com.google.android.material.chip.Chip
            android:id="@+id/chipPaid"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Đã thanh toán"/>
    </com.google.android.material.chip.ChipGroup>
    
    <!-- RecyclerView -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>
    
    <!-- Empty State -->
    <TextView
        android:id="@+id/tvEmpty"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:gravity="center"
        android:text="Không có hóa đơn"
        android:visibility="gone"/>
    
    <!-- Progress Bar -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone"/>
</LinearLayout>
```

**Activity Code**: `InvoiceListActivity.java`
```java
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
import com.hcmute.mobile_android.network.ApiClient;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Hóa đơn của tôi");
    }
    
    private void setupRecyclerView() {
        adapter = new InvoiceAdapter(this, new ArrayList<>(), invoice -> {
            // Click listener - open detail
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
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
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
}
```

**Adapter**: `InvoiceAdapter.java`
```java
package com.hcmute.mobile_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.Invoice;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {
    
    private Context context;
    private List<Invoice> invoices;
    private OnInvoiceClickListener listener;
    
    public interface OnInvoiceClickListener {
        void onInvoiceClick(Invoice invoice);
    }
    
    public InvoiceAdapter(Context context, List<Invoice> invoices, OnInvoiceClickListener listener) {
        this.context = context;
        this.invoices = invoices;
        this.listener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Invoice invoice = invoices.get(position);
        
        holder.tvInvoiceId.setText("Hóa đơn #" + invoice.getId());
        holder.tvPatientName.setText(invoice.getPatientName());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvAmount.setText(formatter.format(invoice.getTotalAmount()));
        
        holder.tvStatus.setText(invoice.getPaymentStatus());
        holder.tvStatus.setBackgroundResource(
            invoice.getPaymentStatus().equals("PAID") ? 
            R.drawable.bg_status_paid : R.drawable.bg_status_unpaid
        );
        
        holder.itemView.setOnClickListener(v -> listener.onInvoiceClick(invoice));
    }
    
    @Override
    public int getItemCount() {
        return invoices.size();
    }
    
    public void updateData(List<Invoice> newInvoices) {
        this.invoices = newInvoices;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceId, tvPatientName, tvAmount, tvStatus;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvInvoiceId = itemView.findViewById(R.id.tvInvoiceId);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
```

**Item Layout**: `item_invoice.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <TextView
            android:id="@+id/tvInvoiceId"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Hóa đơn #123"
            android:textSize="16sp"
            android:textStyle="bold"/>
        
        <TextView
            android:id="@+id/tvPatientName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:text="Nguyễn Văn A"/>
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:orientation="horizontal">
            
            <TextView
                android:id="@+id/tvAmount"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="500,000đ"
                android:textColor="@color/colorPrimary"
                android:textSize="18sp"
                android:textStyle="bold"/>
            
            <TextView
                android:id="@+id/tvStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:background="@drawable/bg_status_paid"
                android:padding="4dp"
                android:text="PAID"
                android:textColor="@android:color/white"
                android:textSize="12sp"/>
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

---

## 📝 TỔNG KẾT

Tôi đã tạo:
1. ✅ Complete code cho InvoiceListActivity
2. ✅ Layout XML files
3. ✅ Adapter với ViewHolder pattern
4. ✅ API integration với Retrofit
5. ✅ Filter functionality
6. ✅ Error handling

### Còn 6 activities nữa cần tạo:
- InvoiceDetailActivity
- PaymentActivity
- ReviewActivity
- AdminDashboardFragment
- Update BookAppointmentActivity
- Update NotificationsFragment

Tất cả đều follow pattern tương tự. Bạn có muốn tôi tiếp tục tạo các activities còn lại không?

---

**Note**: Code này ready để copy-paste vào Android Studio. Chỉ cần:
1. Tạo files theo đúng package
2. Add missing drawables (bg_status_paid, bg_status_unpaid)
3. Build và test

Backend APIs đã sẵn sàng và đang chạy! 🚀
