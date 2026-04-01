package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MedicalRecordDetailResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicalRecordDetailActivity extends AppCompatActivity {

    private ProgressBar progress;
    private Long medicalRecordId;
    private Long prescriptionId = 0L;
    private String doctorNameStr = "";
    private String dateStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medical_record_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        progress = findViewById(R.id.progress);
        medicalRecordId = getIntent().getLongExtra("medicalRecordId", -1L);

        findViewById(R.id.btnXemDonThuoc).setOnClickListener(v -> {
            if (prescriptionId != null && prescriptionId != 0L) {
                Intent intent = new Intent(this, PrescriptionDetailActivity.class);
                intent.putExtra("doctorName", doctorNameStr);
                intent.putExtra("date", dateStr);
                intent.putExtra("prescriptionId", prescriptionId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không có đơn thuốc cho bệnh án này", Toast.LENGTH_SHORT).show();
            }
        });

        if (medicalRecordId != -1L) {
            loadMedicalRecordDetail();
        }
    }

    private void loadMedicalRecordDetail() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        
        ApiService api = RetrofitClient.getApiService(this);
        api.getMedicalRecordDetail(medicalRecordId).enqueue(new Callback<MedicalRecordDetailResponse>() {
            @Override
            public void onResponse(Call<MedicalRecordDetailResponse> call, Response<MedicalRecordDetailResponse> response) {
                if (progress != null) progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayData(response.body());
                } else {
                    Toast.makeText(MedicalRecordDetailActivity.this, "Không thể tải chi tiết bệnh án", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MedicalRecordDetailResponse> call, Throwable t) {
                if (progress != null) progress.setVisibility(View.GONE);
                Toast.makeText(MedicalRecordDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayData(MedicalRecordDetailResponse data) {
        doctorNameStr = data.getDoctorName();
        dateStr = formatDate(data.getDate());
        
        if (data.getPrescription() != null && data.getPrescription().getId() != null) {
            prescriptionId = data.getPrescription().getId();
        } else {
            prescriptionId = 0L;
        }

        setText(R.id.tvExamDate, dateStr);
        setText(R.id.tvDoctorName, doctorNameStr);
        String diagnosis = (data.getDiagnosis() != null && !data.getDiagnosis().isEmpty()) ? 
                data.getDiagnosis() : "Khám tổng quát";
        setText(R.id.tvDiagnosis, diagnosis);
        setText(R.id.tvSymptoms, data.getSymptoms());
        
        String vitals = "Huyết áp: " + (data.getBloodPressure() != null && !data.getBloodPressure().isEmpty() ? data.getBloodPressure() : "N/A") + 
                        " | Nhịp tim: " + (data.getHeartRate() != null && data.getHeartRate() != 0 ? data.getHeartRate() + " bpm" : "N/A");
        setText(R.id.tvVitals, vitals);
        
        setText(R.id.tvAdvice, data.getAdvice());

        // 1. Treatment Steps (Tiến trình điều trị)
        LinearLayout container = findViewById(R.id.treatmentDetailsContainer);
        if (container != null) {
            container.removeAllViews();
            
            // Prefer treatmentSteps from TreatmentPlan
            if (data.getTreatmentSteps() != null && !data.getTreatmentSteps().isEmpty()) {
                for (MedicalRecordDetailResponse.TreatmentStepDetail s : data.getTreatmentSteps()) {
                    View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, container, false);
                    TextView t1 = row.findViewById(android.R.id.text1);
                    TextView t2 = row.findViewById(android.R.id.text2);
                    
                    t1.setText(s.getServiceName() + (s.getToothNumber() != null && !s.getToothNumber().isEmpty() ? " (Răng " + s.getToothNumber() + ")" : ""));
                    t1.setTextColor(Color.parseColor("#005B9F"));
                    t1.setTextSize(15);
                    
                    t2.setText(s.getNotes());
                    t2.setTextColor(Color.parseColor("#37474F"));
                    
                    container.addView(row);
                }
            } 
            // Fallback to basic details
            else if (data.getDetails() != null) {
                for (MedicalRecordDetailResponse.Detail d : data.getDetails()) {
                    View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, container, false);
                    TextView t1 = row.findViewById(android.R.id.text1);
                    TextView t2 = row.findViewById(android.R.id.text2);
                    t1.setText(d.getServiceName() + (d.getToothNumber() != null && !d.getToothNumber().isEmpty() ? " (Răng " + d.getToothNumber() + ")" : ""));
                    t2.setText(d.getNote());
                    container.addView(row);
                }
            }
        }

        // 2. Medical Images (Hình ảnh y khoa)
        LinearLayout imagesContainer = findViewById(R.id.medicalImagesContainer);
        if (imagesContainer != null) {
            imagesContainer.removeAllViews();
            java.util.List<String> allImages = new java.util.ArrayList<>();
            
            if (data.getTreatmentSteps() != null) {
                for (MedicalRecordDetailResponse.TreatmentStepDetail s : data.getTreatmentSteps()) {
                    if (s.getImageUrls() != null) {
                        allImages.addAll(s.getImageUrls());
                    }
                }
            }
            
            if (allImages.isEmpty()) {
                TextView tvEmpty = new TextView(this);
                tvEmpty.setText("Không có hình ảnh đính kèm");
                tvEmpty.setTextColor(Color.GRAY);
                imagesContainer.addView(tvEmpty);
            } else {
                for (String url : allImages) {
                    com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(this);
                    int size = (int) (120 * getResources().getDisplayMetrics().density);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    params.setMargins(0, 0, (int)(12 * getResources().getDisplayMetrics().density), 0);
                    card.setLayoutParams(params);
                    card.setRadius(24 * getResources().getDisplayMetrics().density);
                    card.setCardElevation(0);
                    
                    ImageView iv = new ImageView(this);
                    iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    
                    card.addView(iv);
                    imagesContainer.addView(card);
                    
                    com.bumptech.glide.Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.ic_medical_services)
                            .into(iv);
                }
            }
        }
    }

    private void setText(int id, String text) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(text != null ? text : "");
    }

    private static String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return iso.substring(0, 10);
    }
}
