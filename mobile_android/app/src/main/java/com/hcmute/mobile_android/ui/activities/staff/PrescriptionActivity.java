package com.hcmute.mobile_android.ui.activities.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PrescriptionResponse;
import com.hcmute.mobile_android.network.models.request.PrescriptionRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionActivity extends AppCompatActivity {

    public static final String EXTRA_APPOINTMENT_ID = "EXTRA_APPOINTMENT_ID";

    private Long appointmentId;
    private ApiService apiService;

    private MaterialToolbar toolbar;
    private TextInputEditText etDiagnosis, etSymptoms, etAdvice;
    private LinearLayout llMedicinesContainer;
    private MaterialButton btnAddMedicine, btnSavePrescription;

    private List<View> medicineViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        appointmentId = getIntent().getLongExtra(EXTRA_APPOINTMENT_ID, -1);
        if (appointmentId == -1) {
            Toast.makeText(this, "Không tìm thấy lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getApiService(this);

        initViews();
        loadExistingPrescription();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etDiagnosis = findViewById(R.id.etDiagnosis);
        etSymptoms = findViewById(R.id.etSymptoms);
        etAdvice = findViewById(R.id.etAdvice);

        llMedicinesContainer = findViewById(R.id.llMedicinesContainer);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        btnSavePrescription = findViewById(R.id.btnSavePrescription);

        btnAddMedicine.setOnClickListener(v -> addMedicineRow(null));
        btnSavePrescription.setOnClickListener(v -> savePrescription());
        
        // Add one empty row by default
        addMedicineRow(null);
    }

    private void addMedicineRow(PrescriptionResponse.DetailResponse detail) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_prescription_detail, llMedicinesContainer, false);
        
        TextInputEditText etMedicineName = view.findViewById(R.id.etMedicineName);
        TextInputEditText etDosage = view.findViewById(R.id.etDosage);
        TextInputEditText etUnit = view.findViewById(R.id.etUnit);
        TextInputEditText etFrequency = view.findViewById(R.id.etFrequency);
        TextInputEditText etDuration = view.findViewById(R.id.etDuration);
        MaterialButton btnRemove = view.findViewById(R.id.btnRemove);

        if (detail != null) {
            etMedicineName.setText(detail.getMedicineName());
            etDosage.setText(detail.getDosage());
            etUnit.setText(detail.getUnit());
            etFrequency.setText(detail.getFrequency());
            etDuration.setText(detail.getDuration());
        }

        btnRemove.setOnClickListener(v -> {
            llMedicinesContainer.removeView(view);
            medicineViews.remove(view);
        });

        llMedicinesContainer.addView(view);
        medicineViews.add(view);
    }

    private void loadExistingPrescription() {
        apiService.getPrescriptionByAppointment(appointmentId).enqueue(new Callback<PrescriptionResponse>() {
            @Override
            public void onResponse(Call<PrescriptionResponse> call, Response<PrescriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PrescriptionResponse prescription = response.body();
                    
                    // Clear default empty row
                    llMedicinesContainer.removeAllViews();
                    medicineViews.clear();
                    
                    if (prescription.getDetails() != null) {
                        for (PrescriptionResponse.DetailResponse detail : prescription.getDetails()) {
                            addMedicineRow(detail);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PrescriptionResponse> call, Throwable t) {
                // If not found, it's fine, it means there's no prescription yet.
            }
        });
    }

    private void savePrescription() {
        btnSavePrescription.setEnabled(false);
        btnSavePrescription.setText("Đang lưu...");

        String diagnosis = etDiagnosis.getText().toString().trim();
        String symptoms = etSymptoms.getText().toString().trim();
        String advice = etAdvice.getText().toString().trim();

        if (diagnosis.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập chẩn đoán", Toast.LENGTH_SHORT).show();
            btnSavePrescription.setEnabled(true);
            btnSavePrescription.setText("Lưu Đơn Thuốc");
            return;
        }

        List<PrescriptionRequest.DetailRequest> medicineDetails = new ArrayList<>();
        for (View view : medicineViews) {
            TextInputEditText etMedicineName = view.findViewById(R.id.etMedicineName);
            TextInputEditText etDosage = view.findViewById(R.id.etDosage);
            TextInputEditText etUnit = view.findViewById(R.id.etUnit);
            TextInputEditText etFrequency = view.findViewById(R.id.etFrequency);
            TextInputEditText etDuration = view.findViewById(R.id.etDuration);

            String medicineName = etMedicineName.getText().toString().trim();
            if (!medicineName.isEmpty()) {
                medicineDetails.add(new PrescriptionRequest.DetailRequest(
                        medicineName,
                        etDosage.getText().toString().trim(),
                        etFrequency.getText().toString().trim(),
                        etDuration.getText().toString().trim(),
                        etUnit.getText().toString().trim()
                ));
            }
        }

        PrescriptionRequest request = new PrescriptionRequest(
                appointmentId, diagnosis, symptoms, advice, medicineDetails
        );

        apiService.createPrescription(request).enqueue(new Callback<PrescriptionResponse>() {
            @Override
            public void onResponse(Call<PrescriptionResponse> call, Response<PrescriptionResponse> response) {
                btnSavePrescription.setEnabled(true);
                btnSavePrescription.setText("Lưu Đơn Thuốc");
                
                if (response.isSuccessful()) {
                    Toast.makeText(PrescriptionActivity.this, "Lưu đơn thuốc thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PrescriptionActivity.this, "Lỗi khi lưu đơn thuốc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PrescriptionResponse> call, Throwable t) {
                btnSavePrescription.setEnabled(true);
                btnSavePrescription.setText("Lưu Đơn Thuốc");
                Toast.makeText(PrescriptionActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
