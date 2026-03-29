# 📱 MOBILE UI PHASE 2 - COMPLETE ACTIVITIES

## 2️⃣ InvoiceDetailActivity

**Mục đích**: Hiển thị chi tiết hóa đơn và nút thanh toán

**API**: `GET /api/invoices/{id}`

### Layout: `activity_invoice_detail.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        
        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="?attr/colorPrimary"
            android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar"/>
        
        <!-- Invoice Header -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp">
            
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
                    android:textSize="20sp"
                    android:textStyle="bold"/>
                
                <TextView
                    android:id="@+id/tvPatientName"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="Bệnh nhân: Nguyễn Văn A"/>
                
                <TextView
                    android:id="@+id/tvCreatedDate"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="Ngày tạo: 01/01/2024"/>
                
                <TextView
                    android:id="@+id/tvStatus"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:background="@drawable/bg_status_paid"
                    android:padding="8dp"
                    android:text="PAID"
                    android:textColor="@android:color/white"/>
            </LinearLayout>
        </androidx.cardview.widget.CardView>
        
        <!-- Invoice Items -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:layout_marginTop="8dp"
            android:text="Chi tiết dịch vụ"
            android:textSize="16sp"
            android:textStyle="bold"/>
        
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerViewItems"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"
            android:nestedScrollingEnabled="false"/>
        
        <!-- Total Amount -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"
            android:backgroundTint="@color/colorPrimary">
            
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="16dp">
                
                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Tổng cộng"
                    android:textColor="@android:color/white"
                    android:textSize="18sp"
                    android:textStyle="bold"/>
                
                <TextView
                    android:id="@+id/tvTotalAmount"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="500,000đ"
                    android:textColor="@android:color/white"
                    android:textSize="20sp"
                    android:textStyle="bold"/>
            </LinearLayout>
        </androidx.cardview.widget.CardView>
        
        <!-- Payment Button -->
        <Button
            android:id="@+id/btnPay"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"
            android:text="Thanh toán"
            android:textSize="16sp"/>
        
        <ProgressBar
            android:id="@+id/progressBar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:visibility="gone"/>
    </LinearLayout>
</ScrollView>
```

### Activity: `InvoiceDetailActivity.java`
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
import com.hcmute.mobile_android.adapters.InvoiceItemAdapter;
import com.hcmute.mobile_android.network.ApiClient;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chi tiết hóa đơn");
        
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        
        btnPay.setOnClickListener(v -> openPaymentActivity());
    }
    
    private void loadInvoiceDetail() {
        progressBar.setVisibility(View.VISIBLE);
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
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
        
        // Setup items adapter
        InvoiceItemAdapter adapter = new InvoiceItemAdapter(this, invoice.getItems());
        recyclerViewItems.setAdapter(adapter);
        
        // Show/hide payment button
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
        // Reload when returning from payment
        loadInvoiceDetail();
    }
}
```

### Adapter: `InvoiceItemAdapter.java`
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

public class InvoiceItemAdapter extends RecyclerView.Adapter<InvoiceItemAdapter.ViewHolder> {
    
    private Context context;
    private List<Invoice.InvoiceItem> items;
    
    public InvoiceItemAdapter(Context context, List<Invoice.InvoiceItem> items) {
        this.context = context;
        this.items = items;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Invoice.InvoiceItem item = items.get(position);
        
        holder.tvServiceName.setText(item.getServiceName());
        holder.tvQuantity.setText("x" + item.getQuantity());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(item.getPrice()));
        holder.tvSubtotal.setText(formatter.format(item.getSubtotal()));
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvQuantity, tvPrice, tvSubtotal;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }
    }
}
```

### Item Layout: `item_invoice_item.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="12dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        
        <TextView
            android:id="@+id/tvServiceName"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Khám tổng quát"
            android:textSize="16sp"
            android:textStyle="bold"/>
        
        <TextView
            android:id="@+id/tvQuantity"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="x1"
            android:textSize="14sp"/>
    </LinearLayout>
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:orientation="horizontal">
        
        <TextView
            android:id="@+id/tvPrice"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="200,000đ"
            android:textSize="14sp"/>
        
        <TextView
            android:id="@+id/tvSubtotal"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="200,000đ"
            android:textColor="@color/colorPrimary"
            android:textSize="16sp"
            android:textStyle="bold"/>
    </LinearLayout>
    
    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:layout_marginTop="8dp"
        android:background="#E0E0E0"/>
</LinearLayout>
```

---

## 3️⃣ PaymentActivity

**Mục đích**: Chọn phương thức thanh toán và xử lý thanh toán

**API**: `POST /api/invoices/{id}/pay`

### Layout: `activity_payment.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar"/>
    
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">
            
            <!-- Amount Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:backgroundTint="@color/colorPrimary">
                
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:orientation="vertical"
                    android:padding="24dp">
                    
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Số tiền thanh toán"
                        android:textColor="@android:color/white"
                        android:textSize="16sp"/>
                    
                    <TextView
                        android:id="@+id/tvAmount"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="8dp"
                        android:text="500,000đ"
                        android:textColor="@android:color/white"
                        android:textSize="32sp"
                        android:textStyle="bold"/>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
            
            <!-- Payment Method Selection -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:text="Chọn phương thức thanh toán"
                android:textSize="16sp"
                android:textStyle="bold"/>
            
            <RadioGroup
                android:id="@+id/radioGroupPayment"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp">
                
                <RadioButton
                    android:id="@+id/radioCash"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:padding="12dp"
                    android:text="Tiền mặt"
                    android:checked="true"/>
                
                <RadioButton
                    android:id="@+id/radioBankTransfer"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:padding="12dp"
                    android:text="Chuyển khoản ngân hàng"/>
                
                <RadioButton
                    android:id="@+id/radioCreditCard"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:padding="12dp"
                    android:text="Thẻ tín dụng"/>
                
                <RadioButton
                    android:id="@+id/radioMomo"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:padding="12dp"
                    android:text="Ví MoMo"/>
                
                <RadioButton
                    android:id="@+id/radioZaloPay"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:padding="12dp"
                    android:text="ZaloPay"/>
            </RadioGroup>
            
            <!-- Note -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:text="Ghi chú (tùy chọn)"
                android:textSize="16sp"
                android:textStyle="bold"/>
            
            <EditText
                android:id="@+id/etNote"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:hint="Nhập ghi chú..."
                android:minLines="3"
                android:gravity="top"/>
        </LinearLayout>
    </ScrollView>
    
    <!-- Payment Button -->
    <Button
        android:id="@+id/btnConfirmPayment"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:text="Xác nhận thanh toán"
        android:textSize="16sp"/>
    
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone"/>
</LinearLayout>
```

### Activity: `PaymentActivity.java`
```java
package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.PaymentRequest;
import com.hcmute.mobile_android.network.models.PaymentResponse;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {
    
    private TextView tvAmount;
    private RadioGroup radioGroupPayment;
    private EditText etNote;
    private Button btnConfirmPayment;
    private ProgressBar progressBar;
    private Long invoiceId;
    private double amount;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        
        invoiceId = getIntent().getLongExtra("invoiceId", 0);
        amount = getIntent().getDoubleExtra("amount", 0);
        
        setupViews();
        displayAmount();
    }
    
    private void setupViews() {
        tvAmount = findViewById(R.id.tvAmount);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        etNote = findViewById(R.id.etNote);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        progressBar = findViewById(R.id.progressBar);
        
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thanh toán");
        
        btnConfirmPayment.setOnClickListener(v -> confirmPayment());
    }
    
    private void displayAmount() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvAmount.setText(formatter.format(amount));
    }
    
    private void confirmPayment() {
        // Get selected payment method
        String paymentMethod = getSelectedPaymentMethod();
        String note = etNote.getText().toString().trim();
        
        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage("Bạn có chắc muốn thanh toán " + tvAmount.getText() + " bằng " + paymentMethod + "?")
            .setPositiveButton("Xác nhận", (dialog, which) -> processPayment(paymentMethod, note))
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private String getSelectedPaymentMethod() {
        int selectedId = radioGroupPayment.getCheckedRadioButtonId();
        
        if (selectedId == R.id.radioCash) return "CASH";
        if (selectedId == R.id.radioBankTransfer) return "BANK_TRANSFER";
        if (selectedId == R.id.radioCreditCard) return "CREDIT_CARD";
        if (selectedId == R.id.radioMomo) return "MOMO";
        if (selectedId == R.id.radioZaloPay) return "ZALOPAY";
        
        return "CASH";
    }
    
    private void processPayment(String paymentMethod, String note) {
        progressBar.setVisibility(View.VISIBLE);
        btnConfirmPayment.setEnabled(false);
        
        PaymentRequest request = new PaymentRequest(
            paymentMethod,
            BigDecimal.valueOf(amount),
            note
        );
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<PaymentResponse> call = apiService.processPayment(invoiceId, request);
        
        call.enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnConfirmPayment.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    PaymentResponse paymentResponse = response.body();
                    
                    if (paymentResponse.isSuccess()) {
                        showSuccessDialog();
                    } else {
                        Toast.makeText(PaymentActivity.this, 
                            "Thanh toán thất bại: " + paymentResponse.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PaymentActivity.this, 
                        "Lỗi thanh toán", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnConfirmPayment.setEnabled(true);
                Toast.makeText(PaymentActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Thanh toán thành công")
            .setMessage("Hóa đơn đã được thanh toán thành công!")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
}
```

---

## 4️⃣ ReviewActivity

**Mục đích**: Đánh giá dịch vụ và bác sĩ sau khi hoàn thành

**API**: `POST /api/reviews`

### Layout: `activity_review.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar"/>
    
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">
            
            <!-- Appointment Info -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content">
                
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">
                    
                    <TextView
                        android:id="@+id/tvDoctorName"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Bác sĩ: Nguyễn Văn A"
                        android:textSize="16sp"
                        android:textStyle="bold"/>
                    
                    <TextView
                        android:id="@+id/tvServiceName"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="Dịch vụ: Khám tổng quát"/>
                    
                    <TextView
                        android:id="@+id/tvAppointmentDate"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="Ngày: 01/01/2024"/>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
            
            <!-- Rating Section -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:text="Đánh giá của bạn"
                android:textSize="16sp"
                android:textStyle="bold"/>
            
            <RatingBar
                android:id="@+id/ratingBar"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center"
                android:layout_marginTop="16dp"
                android:numStars="5"
                android:rating="5"
                android:stepSize="1"/>
            
            <TextView
                android:id="@+id/tvRatingText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center"
                android:layout_marginTop="8dp"
                android:text="Xuất sắc"
                android:textSize="18sp"
                android:textStyle="bold"/>
            
            <!-- Comment Section -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:text="Nhận xét"
                android:textSize="16sp"
                android:textStyle="bold"/>
            
            <EditText
                android:id="@+id/etComment"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:hint="Chia sẻ trải nghiệm của bạn..."
                android:minLines="5"
                android:gravity="top"
                android:background="@drawable/bg_edit_text"/>
        </LinearLayout>
    </ScrollView>
    
    <!-- Submit Button -->
    <Button
        android:id="@+id/btnSubmit"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:text="Gửi đánh giá"
        android:textSize="16sp"/>
    
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone"/>
</LinearLayout>
```

### Activity: `ReviewActivity.java`
```java
package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.ReviewRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {
    
    private TextView tvDoctorName, tvServiceName, tvAppointmentDate, tvRatingText;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;
    private ProgressBar progressBar;
    
    private Long appointmentId;
    private Long doctorId;
    private Long serviceId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        
        // Get data from intent
        appointmentId = getIntent().getLongExtra("appointmentId", 0);
        doctorId = getIntent().getLongExtra("doctorId", 0);
        serviceId = getIntent().getLongExtra("serviceId", 0);
        
        setupViews();
        displayAppointmentInfo();
    }
    
    private void setupViews() {
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvRatingText = findViewById(R.id.tvRatingText);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Đánh giá");
        
        // Rating bar listener
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            updateRatingText((int) rating);
        });
        
        btnSubmit.setOnClickListener(v -> submitReview());
    }
    
    private void displayAppointmentInfo() {
        String doctorName = getIntent().getStringExtra("doctorName");
        String serviceName = getIntent().getStringExtra("serviceName");
        String appointmentDate = getIntent().getStringExtra("appointmentDate");
        
        tvDoctorName.setText("Bác sĩ: " + doctorName);
        tvServiceName.setText("Dịch vụ: " + serviceName);
        tvAppointmentDate.setText("Ngày: " + appointmentDate);
    }
    
    private void updateRatingText(int rating) {
        String[] ratingTexts = {
            "Rất tệ",
            "Tệ",
            "Trung bình",
            "Tốt",
            "Xuất sắc"
        };
        
        if (rating > 0 && rating <= 5) {
            tvRatingText.setText(ratingTexts[rating - 1]);
        }
    }
    
    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();
        
        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        
        ReviewRequest request = new ReviewRequest(
            appointmentId,
            doctorId,
            serviceId,
            rating,
            comment
        );
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.createReview(request);
        
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                
                if (response.isSuccessful()) {
                    showSuccessDialog();
                } else {
                    Toast.makeText(ReviewActivity.this, 
                        "Lỗi gửi đánh giá", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(ReviewActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Thành công")
            .setMessage("Cảm ơn bạn đã đánh giá!")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
}
```

### Model: `ReviewRequest.java`
```java
package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("appointmentId")
    private Long appointmentId;
    
    @SerializedName("doctorId")
    private Long doctorId;
    
    @SerializedName("serviceId")
    private Long serviceId;
    
    @SerializedName("rating")
    private Integer rating;
    
    @SerializedName("comment")
    private String comment;
    
    public ReviewRequest(Long appointmentId, Long doctorId, Long serviceId, 
                        Integer rating, String comment) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.serviceId = serviceId;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters and setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
```

---

## 📊 PROGRESS UPDATE

### ✅ Completed (4/7 Activities):
1. ✅ InvoiceListActivity - List all invoices with filters
2. ✅ InvoiceDetailActivity - Show invoice details + payment button
3. ✅ PaymentActivity - Payment method selection + process payment
4. ✅ ReviewActivity - Rating + comment submission

### ⏳ Remaining (3/7 Activities):
5. ⏳ AdminDashboardFragment - Revenue charts + statistics
6. ⏳ Update BookAppointmentActivity - Time slot selection
7. ⏳ Update NotificationsFragment - Mark all as read

---

**Next**: Creating AdminDashboardFragment with charts and statistics...
