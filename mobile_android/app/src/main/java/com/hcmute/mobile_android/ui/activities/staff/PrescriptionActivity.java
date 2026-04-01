package com.hcmute.mobile_android.ui.activities.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PrescriptionResponse;
import com.hcmute.mobile_android.network.models.TreatmentPlan;
import com.hcmute.mobile_android.network.models.request.PrescriptionRequest;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionActivity extends AppCompatActivity {

    public static final String EXTRA_APPOINTMENT_ID = "EXTRA_APPOINTMENT_ID";
    public static final String EXTRA_TREATMENT_PLAN_ID = "EXTRA_TREATMENT_PLAN_ID";

    private Long appointmentId;
    private Long treatmentPlanId;
    private ApiService apiService;

    private MaterialToolbar toolbar;
    private TextInputEditText etDiagnosis, etSymptoms, etAdvice;

    private Spinner spService;
    private SwitchMaterial swEnablePrescription;
    private TextInputLayout layoutServiceAmount;
    private TextInputEditText etServiceAmount;

    private View layoutMedicinesHeader;
    private LinearLayout llMedicinesContainer;
    private MaterialButton btnAddMedicine, btnSavePrescription;

    private List<View> medicineViews = new ArrayList<>();
    private final Map<Long, List<PrescriptionResponse.PrescriptionDetail>> medicineDetailsByStep = new HashMap<>();
    private List<TreatmentPlan.Step> treatmentSteps = new ArrayList<>();

    private Long selectedStepId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        appointmentId = getIntent().getLongExtra(EXTRA_APPOINTMENT_ID, -1);
        treatmentPlanId = getIntent().getLongExtra(EXTRA_TREATMENT_PLAN_ID, -1);
        if (appointmentId == -1) {
            Toast.makeText(this, "Không tìm thấy lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // treatmentPlanId is now optional - we load by appointmentId instead

        apiService = RetrofitClient.getApiService(this);

        initViews();
        loadTreatmentPlanSteps();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etDiagnosis = findViewById(R.id.etDiagnosis);
        etSymptoms = findViewById(R.id.etSymptoms);
        etAdvice = findViewById(R.id.etAdvice);

        spService = findViewById(R.id.spService);
        swEnablePrescription = findViewById(R.id.swEnablePrescription);
        layoutServiceAmount = findViewById(R.id.layoutServiceAmount);
        etServiceAmount = findViewById(R.id.etServiceAmount);
        layoutMedicinesHeader = findViewById(R.id.layoutMedicinesHeader);

        llMedicinesContainer = findViewById(R.id.llMedicinesContainer);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        btnSavePrescription = findViewById(R.id.btnSavePrescription);

        btnAddMedicine.setOnClickListener(v -> addMedicineRow(null));
        btnSavePrescription.setOnClickListener(v -> savePrescription());

        // Default: hide prescription UI until a step is selected and "Kê đơn" is enabled
        btnSavePrescription.setEnabled(false);
        llMedicinesContainer.setVisibility(View.GONE);
        btnAddMedicine.setVisibility(View.GONE);
        if (layoutMedicinesHeader != null) layoutMedicinesHeader.setVisibility(View.GONE);
        layoutServiceAmount.setVisibility(View.GONE);
        swEnablePrescription.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only toggle UI; selectedStepId is controlled by spinner
            if (isChecked) {
                llMedicinesContainer.setVisibility(View.VISIBLE);
                btnAddMedicine.setVisibility(View.VISIBLE);
                if (layoutMedicinesHeader != null) layoutMedicinesHeader.setVisibility(View.VISIBLE);
                layoutServiceAmount.setVisibility(View.VISIBLE);
                btnSavePrescription.setEnabled(true);
                loadSelectedStepPrescriptionUI();
            } else {
                llMedicinesContainer.setVisibility(View.GONE);
                btnAddMedicine.setVisibility(View.GONE);
                if (layoutMedicinesHeader != null) layoutMedicinesHeader.setVisibility(View.GONE);
                layoutServiceAmount.setVisibility(View.GONE);
                btnSavePrescription.setEnabled(false);
                clearMedicinesUI();
            }
        });
    }

    private void addMedicineRow(PrescriptionResponse.PrescriptionDetail detail) {
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

    private void loadTreatmentPlanSteps() {
        // CRITICAL FIX: Load treatment plan by appointmentId instead of treatmentPlanId
        // This ensures we always get the correct treatment plan for the current appointment
        apiService.getTreatmentPlanByAppointment(appointmentId).enqueue(new Callback<TreatmentPlan>() {
            @Override
            public void onResponse(Call<TreatmentPlan> call, Response<TreatmentPlan> response) {
                // FALLBACK: If no treatment plan exists for this appointment, allow general prescription
                if (!response.isSuccessful()) {
                    if (response.code() == 404) {
                        // No treatment plan found - allow general prescription without steps
                        android.util.Log.w("PrescriptionActivity", "No treatment plan found for appointment " + appointmentId + ". Allowing general prescription.");
                        treatmentPlanId = null;
                        treatmentSteps = new ArrayList<>();
                        setupServiceSpinner();
                        // Disable step selection since there are no steps
                        swEnablePrescription.setEnabled(false);
                        swEnablePrescription.setChecked(false);
                        Toast.makeText(PrescriptionActivity.this, 
                            "Chưa có phác đồ điều trị. Bạn có thể kê đơn chung.", 
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(PrescriptionActivity.this, "Không tải được phác đồ điều trị", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (response.body() == null) {
                    Toast.makeText(PrescriptionActivity.this, "Không tải được phác đồ điều trị", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                TreatmentPlan plan = response.body();
                
                // Validate that treatment plan belongs to current appointment (should always be true now)
                if (plan.getAppointmentId() != null && !plan.getAppointmentId().equals(appointmentId)) {
                    Toast.makeText(PrescriptionActivity.this, 
                        "Phác đồ không thuộc lịch hẹn này. Vui lòng kiểm tra lại.", 
                        Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                
                // Extract treatmentPlanId from response
                treatmentPlanId = plan.getId();
                
                treatmentSteps = plan.getSteps() != null ? plan.getSteps() : new ArrayList<>();
                if (treatmentSteps.isEmpty()) {
                    android.util.Log.w("PrescriptionActivity", "Treatment plan exists but has no steps. Allowing general prescription.");
                    swEnablePrescription.setEnabled(false);
                    swEnablePrescription.setChecked(false);
                    Toast.makeText(PrescriptionActivity.this, "Phác đồ chưa có dịch vụ. Bạn có thể kê đơn chung.", Toast.LENGTH_SHORT).show();
                }

                setupServiceSpinner();
                
                // Then load prescription details from appointment
                loadExistingPrescription();
            }

            @Override
            public void onFailure(Call<TreatmentPlan> call, Throwable t) {
                Toast.makeText(PrescriptionActivity.this, "Lỗi kết nối phác đồ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupServiceSpinner() {
        if (treatmentSteps.isEmpty()) {
            // No steps available - disable spinner
            spService.setEnabled(false);
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, 
                new String[]{"Không có dịch vụ"});
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spService.setAdapter(emptyAdapter);
            return;
        }

        // Build step names for spinner
        List<String> stepNames = new ArrayList<>();
        for (TreatmentPlan.Step step : treatmentSteps) {
            String name = step.getServiceName() != null ? step.getServiceName() : "Dịch vụ #" + step.getId();
            stepNames.add(name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, 
            stepNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spService.setAdapter(adapter);

        spService.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= treatmentSteps.size()) return;
                
                TreatmentPlan.Step step = treatmentSteps.get(position);
                selectedStepId = step.getId();

                // Load saved data state for this step (switch dựa trên dữ liệu có sẵn)
                boolean hasSavedMedicine = medicineDetailsByStep.containsKey(selectedStepId)
                        && medicineDetailsByStep.get(selectedStepId) != null
                        && !medicineDetailsByStep.get(selectedStepId).isEmpty();

                // Sync amount from current step state
                Double base = step.getEstimatedPrice() != null ? step.getEstimatedPrice() : 0.0;
                Double actual = step.getActualPrice() != null ? step.getActualPrice() : base;
                // amount nhập vào là phần "cần cộng thêm" vào giá dịch vụ
                Double extra = actual - base;
                if (extra < 0) extra = 0.0;
                etServiceAmount.setText(String.valueOf(extra.intValue() == extra ? extra.intValue() : extra));

                boolean shouldEnable = hasSavedMedicine;
                if (swEnablePrescription.isChecked() != shouldEnable) {
                    swEnablePrescription.setChecked(shouldEnable);
                } else if (shouldEnable) {
                    loadSelectedStepPrescriptionUI();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Select first by default
        if (!treatmentSteps.isEmpty()) {
            TreatmentPlan.Step firstStep = treatmentSteps.get(0);
            selectedStepId = firstStep.getId();
        }
    }

    private void loadExistingPrescription() {
        apiService.getPrescriptionByAppointment(appointmentId).enqueue(new Callback<PrescriptionResponse>() {
            @Override
            public void onResponse(Call<PrescriptionResponse> call, Response<PrescriptionResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return; // no prescription is fine
                }

                PrescriptionResponse prescription = response.body();
                if (prescription.getDiagnosis() != null && !prescription.getDiagnosis().isEmpty()) {
                    etDiagnosis.setText(prescription.getDiagnosis());
                }
                if (prescription.getSymptoms() != null && !prescription.getSymptoms().isEmpty()) {
                    etSymptoms.setText(prescription.getSymptoms());
                }
                if (prescription.getAdvice() != null && !prescription.getAdvice().isEmpty()) {
                    etAdvice.setText(prescription.getAdvice());
                }
                medicineDetailsByStep.clear();

                if (prescription.getDetails() != null) {
                    for (PrescriptionResponse.PrescriptionDetail detail : prescription.getDetails()) {
                        if (detail.getTreatmentPlanStepId() == null) continue;
                        medicineDetailsByStep
                                .computeIfAbsent(detail.getTreatmentPlanStepId(), k -> new ArrayList<>())
                                .add(detail);
                    }
                }

                if (selectedStepId != null) {
                    boolean hasSavedMedicine = medicineDetailsByStep.containsKey(selectedStepId)
                            && medicineDetailsByStep.get(selectedStepId) != null
                            && !medicineDetailsByStep.get(selectedStepId).isEmpty();

                    if (swEnablePrescription.isChecked() != hasSavedMedicine) {
                        swEnablePrescription.setChecked(hasSavedMedicine);
                    } else if (hasSavedMedicine) {
                        loadSelectedStepPrescriptionUI();
                    }
                }
            }

            @Override
            public void onFailure(Call<PrescriptionResponse> call, Throwable t) {
                // If not found, it's fine, it means there's no prescription yet.
            }
        });
    }

    private void clearMedicinesUI() {
        llMedicinesContainer.removeAllViews();
        medicineViews.clear();
    }

    private void loadSelectedStepPrescriptionUI() {
        if (selectedStepId == null) return;

        clearMedicinesUI();

        List<PrescriptionResponse.PrescriptionDetail> details =
                medicineDetailsByStep.get(selectedStepId);

        if (details == null || details.isEmpty()) {
            // Add one empty row for better UX
            addMedicineRow(null);
            return;
        }

        for (PrescriptionResponse.PrescriptionDetail detail : details) {
            addMedicineRow(detail);
        }
    }

    private void savePrescription() {
        if (selectedStepId == null) {
            Toast.makeText(this, "Chưa chọn dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!swEnablePrescription.isChecked()) {
            Toast.makeText(this, "Chưa bật kê đơn cho dịch vụ này", Toast.LENGTH_SHORT).show();
            return;
        }

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

        String amountStr = etServiceAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            btnSavePrescription.setEnabled(true);
            btnSavePrescription.setText("Lưu Đơn Thuốc");
            return;
        }

        Double amount;
        try {
            String sanitized = amountStr.replaceAll("[^0-9.]", "");
            amount = Double.parseDouble(sanitized);
        } catch (Exception e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
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

        if (medicineDetails.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập ít nhất 1 thuốc", Toast.LENGTH_SHORT).show();
            btnSavePrescription.setEnabled(true);
            btnSavePrescription.setText("Lưu Đơn Thuốc");
            return;
        }

        PrescriptionRequest request = new PrescriptionRequest(
                appointmentId,
                diagnosis,
                symptoms,
                advice,
                selectedStepId,
                amount,
                medicineDetails
        );

        apiService.createPrescription(request).enqueue(new Callback<PrescriptionResponse>() {
            @Override
            public void onResponse(Call<PrescriptionResponse> call, Response<PrescriptionResponse> response) {
                btnSavePrescription.setEnabled(true);
                btnSavePrescription.setText("Lưu Đơn Thuốc");
                
                if (response.isSuccessful()) {
                    Toast.makeText(PrescriptionActivity.this, "Lưu đơn thuốc thành công", Toast.LENGTH_SHORT).show();
                    if (response.body() != null && response.body().getDetails() != null) {
                        // Rebuild local map (có thể bao gồm nhiều step)
                        medicineDetailsByStep.clear();
                        for (PrescriptionResponse.PrescriptionDetail d : response.body().getDetails()) {
                            if (d.getTreatmentPlanStepId() == null) continue;
                            medicineDetailsByStep
                                    .computeIfAbsent(d.getTreatmentPlanStepId(), k -> new ArrayList<>())
                                    .add(d);
                        }
                    }
                    // Sync actualPrice for current step
                    for (TreatmentPlan.Step step : treatmentSteps) {
                        if (step.getId() != null && step.getId().equals(selectedStepId)) {
                            Double base = step.getEstimatedPrice() != null ? step.getEstimatedPrice() : 0.0;
                            step.setActualPrice(base + amount);
                            break;
                        }
                    }

                    setResult(RESULT_OK);
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
