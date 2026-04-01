package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.RetrofitClient;
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán");
        }
        
        btnConfirmPayment.setOnClickListener(v -> confirmPayment());
    }
    
    private void displayAmount() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvAmount.setText(formatter.format(amount));
    }
    
    private void confirmPayment() {
        String paymentMethod = getSelectedPaymentMethod();
        String note = etNote.getText().toString().trim();
        
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage("Bạn có chắc muốn thanh toán " + tvAmount.getText() + " bằng " + getPaymentMethodName(paymentMethod) + "?")
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
        if (selectedId == R.id.radioVnpay) return "VNPAY";
        
        return "CASH";
    }
    
    private String getPaymentMethodName(String method) {
        switch (method) {
            case "CASH": return "Tiền mặt";
            case "BANK_TRANSFER": return "Chuyển khoản";
            case "CREDIT_CARD": return "Thẻ tín dụng";
            case "MOMO": return "MoMo";
            case "ZALOPAY": return "ZaloPay";
            case "VNPAY": return "VNPay";
            default: return method;
        }
    }
    
    private void processPayment(String paymentMethod, String note) {
        progressBar.setVisibility(View.VISIBLE);
        btnConfirmPayment.setEnabled(false);
        
        PaymentRequest request = new PaymentRequest(
            paymentMethod,
            BigDecimal.valueOf(amount),
            note
        );
        
        ApiService apiService = RetrofitClient.getApiService(this);
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
            .setMessage("Hóa đơn đã được ghi nhận thanh toán. Bạn có thể đánh giá dịch vụ sau tại mục lịch hẹn hoặc thông báo.")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
