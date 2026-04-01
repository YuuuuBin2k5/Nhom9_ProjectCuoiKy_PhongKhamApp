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

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.BuildConfig;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.Invoice;
import com.hcmute.mobile_android.util.ToastUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvStatus, tvTotalAmount, tvInvoiceId, tvDate, tvPatientName;
    private ImageView ivStatusIcon;
    private RecyclerView rvItems;
    private LinearLayout llPrescriptionContainer, llPrescriptionInfo;
    private TextView tvPrescriptionHeader, tvDiagnosis, tvAdvice;
    private View prescriptionDivider;
    private View layoutPaymentAction;
    private MaterialButton btnPay, btnDownloadPrescription;

    private ApiService apiService;
    private Long invoiceId;
    private Invoice currentInvoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        apiService = RetrofitClient.getApiService(this);
        invoiceId = getIntent().getLongExtra("invoiceId", -1);

        if (invoiceId == -1) {
            ToastUtils.showCenteredToast(this, "Không tìm thấy hóa đơn");
            finish();
            return;
        }

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInvoiceDetail();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvStatus = findViewById(R.id.tvStatus);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvInvoiceId = findViewById(R.id.tvInvoiceId);
        tvDate = findViewById(R.id.tvDate);
        tvPatientName = findViewById(R.id.tvPatientName);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        rvItems = findViewById(R.id.rvItems);
        llPrescriptionContainer = findViewById(R.id.llPrescriptionContainer);
        llPrescriptionInfo = findViewById(R.id.llPrescriptionInfo);
        tvPrescriptionHeader = findViewById(R.id.tvPrescriptionHeader);
        tvDiagnosis = findViewById(R.id.tvDiagnosis);
        tvAdvice = findViewById(R.id.tvAdvice);
        prescriptionDivider = findViewById(R.id.prescriptionDivider);
        layoutPaymentAction = findViewById(R.id.layoutPaymentAction);
        btnPay = findViewById(R.id.btnPay);
        btnDownloadPrescription = findViewById(R.id.btnDownloadPrescription);

        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnPay.setOnClickListener(v -> {
            if (currentInvoice != null) {
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra("invoiceId", currentInvoice.getId() != null ? currentInvoice.getId().longValue() : 0L);
                intent.putExtra("amount", currentInvoice.getTotalAmount() != null ? currentInvoice.getTotalAmount().doubleValue() : 0.0);
                if (currentInvoice.getTreatmentPlanId() != null) {
                    intent.putExtra("treatmentPlanId", currentInvoice.getTreatmentPlanId().longValue());
                }
                startActivity(intent);
            }
        });
    }

    private void loadInvoiceDetail() {
        apiService.getInvoiceDetail(invoiceId).enqueue(new Callback<Invoice>() {
            @Override
            public void onResponse(Call<Invoice> call, Response<Invoice> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentInvoice = response.body();
                    displayInvoice(currentInvoice);
                } else {
                    ToastUtils.showCenteredToast(InvoiceDetailActivity.this, "Không thể tải chi tiết hóa đơn");
                }
            }

            @Override
            public void onFailure(Call<Invoice> call, Throwable t) {
                ToastUtils.showCenteredToast(InvoiceDetailActivity.this, "Lỗi kết nối");
            }
        });
    }

    private void displayInvoice(Invoice invoice) {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        tvInvoiceId.setText("#" + invoice.getId());
        tvPatientName.setText(invoice.getPatientName() != null ? invoice.getPatientName() : "Khách hàng");
        
        double amount = invoice.getTotalAmount() != null ? invoice.getTotalAmount().doubleValue() : 0;
        tvTotalAmount.setText(currencyFormat.format(amount) + " VNĐ");

        if (invoice.getCreatedAt() != null) {
            try {
                java.util.Date date = inputFormat.parse(invoice.getCreatedAt());
                if (date != null) {
                    tvDate.setText(displayFormat.format(date));
                }
            } catch (Exception e) {
                String d = invoice.getCreatedAt();
                tvDate.setText(d.length() > 10 ? d.substring(0, 10) : d);
            }
        } else {
            tvDate.setText("Chưa xác định");
        }

        boolean isPaid = "PAID".equalsIgnoreCase(invoice.getPaymentStatus());
        if (isPaid) {
            tvStatus.setText("ĐÃ THANH TOÁN");
            tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
            ivStatusIcon.setColorFilter(android.graphics.Color.parseColor("#4CAF50"));
            layoutPaymentAction.setVisibility(View.GONE);
        } else {
            tvStatus.setText("CHỜ THANH TOÁN");
            tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            ivStatusIcon.setImageResource(R.drawable.ic_processing);
            ivStatusIcon.setColorFilter(android.graphics.Color.parseColor("#FF9800"));
            layoutPaymentAction.setVisibility(View.VISIBLE);
        }

        List<Invoice.InvoiceItem> items = invoice.getItems();
        if (items != null && !items.isEmpty()) {
            rvItems.setAdapter(new InvoiceItemAdapter(items));
        } else {
            rvItems.setAdapter(null);
        }

        // Display Prescription Details
        List<Invoice.PrescriptionDetail> prescriptions = invoice.getPrescriptionDetails();
        boolean hasPrescription = (prescriptions != null && !prescriptions.isEmpty()) 
                                || (invoice.getDiagnosis() != null && !invoice.getDiagnosis().isEmpty())
                                || (invoice.getAdvice() != null && !invoice.getAdvice().isEmpty());

        if (hasPrescription) {
            prescriptionDivider.setVisibility(View.VISIBLE);
            tvPrescriptionHeader.setVisibility(View.VISIBLE);
            
            // Diagnosis and Advice
            if ((invoice.getDiagnosis() != null && !invoice.getDiagnosis().isEmpty()) 
                || (invoice.getAdvice() != null && !invoice.getAdvice().isEmpty())) {
                llPrescriptionInfo.setVisibility(View.VISIBLE);
                tvDiagnosis.setText(invoice.getDiagnosis() != null ? invoice.getDiagnosis() : "Chưa có chẩn đoán");
                tvAdvice.setText(invoice.getAdvice() != null ? invoice.getAdvice() : "Không có lời dặn");
                findViewById(R.id.labelDiagnosis).setVisibility(invoice.getDiagnosis() != null ? View.VISIBLE : View.GONE);
                tvDiagnosis.setVisibility(invoice.getDiagnosis() != null ? View.VISIBLE : View.GONE);
                findViewById(R.id.labelAdvice).setVisibility(invoice.getAdvice() != null ? View.VISIBLE : View.GONE);
                tvAdvice.setVisibility(invoice.getAdvice() != null ? View.VISIBLE : View.GONE);
            } else {
                llPrescriptionInfo.setVisibility(View.GONE);
            }

            // Medicines
            if (prescriptions != null && !prescriptions.isEmpty()) {
                llPrescriptionContainer.setVisibility(View.VISIBLE);
                llPrescriptionContainer.removeAllViews();
                for (Invoice.PrescriptionDetail detail : prescriptions) {
                    View medView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, llPrescriptionContainer, false);
                    TextView text1 = medView.findViewById(android.R.id.text1);
                    TextView text2 = medView.findViewById(android.R.id.text2);
                    text1.setText("💊 " + detail.getMedicineName());
                    text1.setTextColor(getResources().getColor(R.color.text_primary));
                    text1.setTextSize(14f);
                    String info = detail.getDosage() + " " + detail.getUnit() + " - " + detail.getFrequency() + " (" + detail.getDuration() + ")";
                    text2.setText(info);
                    text2.setTextColor(getResources().getColor(R.color.text_secondary));
                    text2.setTextSize(12f);
                    llPrescriptionContainer.addView(medView);
                }
            } else {
                llPrescriptionContainer.setVisibility(View.GONE);
            }

            // Download Button
            if (invoice.getPrescriptionId() != null) {
                btnDownloadPrescription.setVisibility(View.VISIBLE);
                btnDownloadPrescription.setOnClickListener(v -> {
                    String url = BuildConfig.API_BASE_URL;
                    if (!url.endsWith("/")) url += "/";
                    url += "api/prescriptions/" + invoice.getPrescriptionId() + "/pdf";
                    Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    startActivity(intent);
                });
            } else {
                btnDownloadPrescription.setVisibility(View.GONE);
            }
        } else {
            prescriptionDivider.setVisibility(View.GONE);
            tvPrescriptionHeader.setVisibility(View.GONE);
            llPrescriptionInfo.setVisibility(View.GONE);
            llPrescriptionContainer.setVisibility(View.GONE);
            btnDownloadPrescription.setVisibility(View.GONE);
        }
    }

    private static class InvoiceItemAdapter extends RecyclerView.Adapter<InvoiceItemAdapter.ViewHolder> {
        private final List<Invoice.InvoiceItem> items;
        private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        public InvoiceItemAdapter(List<Invoice.InvoiceItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invoice_detail, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Invoice.InvoiceItem item = items.get(position);
            
            holder.tvServiceName.setText(item.getServiceName() != null ? item.getServiceName() : "Dịch vụ");
            
            double unitPrice = item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0;
            double totalPrice = item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : unitPrice * (item.getQuantity() != null ? item.getQuantity() : 1);
            
            holder.tvTotalPrice.setText(currencyFormat.format(totalPrice) + " VNĐ");
            
            String qtyStr = (item.getQuantity() != null ? item.getQuantity() : 1) + " x " + currencyFormat.format(unitPrice);
            holder.tvQuantityPrice.setText(qtyStr);

            if (item.getToothNumber() != null && !item.getToothNumber().isEmpty()) {
                holder.tvTooth.setText("Răng " + item.getToothNumber());
                holder.tvTooth.setVisibility(View.VISIBLE);
                holder.tvDot.setVisibility(View.VISIBLE);
            } else {
                holder.tvTooth.setVisibility(View.GONE);
                holder.tvDot.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvServiceName, tvTotalPrice, tvTooth, tvDot, tvQuantityPrice;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvServiceName = itemView.findViewById(R.id.tvServiceName);
                tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
                tvTooth = itemView.findViewById(R.id.tvTooth);
                tvDot = itemView.findViewById(R.id.tvDot);
                tvQuantityPrice = itemView.findViewById(R.id.tvQuantityPrice);
            }
        }
    }
}
