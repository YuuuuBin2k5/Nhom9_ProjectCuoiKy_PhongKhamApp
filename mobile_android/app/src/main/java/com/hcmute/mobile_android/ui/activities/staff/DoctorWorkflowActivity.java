package com.hcmute.mobile_android.ui.activities.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.TreatmentTemplateAdapter;
import com.hcmute.mobile_android.adapters.TreatmentStepAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PatientInfo;
import com.hcmute.mobile_android.network.models.TreatmentPlan;
import com.hcmute.mobile_android.network.models.TreatmentTemplate;
import com.hcmute.mobile_android.ui.activities.PatientQRScannerActivity;
import com.hcmute.mobile_android.ui.fragments.BottomSheetMedicalHistory;
import com.hcmute.mobile_android.ui.fragments.FragmentGeneralDental;
import com.hcmute.mobile_android.ui.fragments.FragmentSurgeryChecklist;
import com.hcmute.mobile_android.ui.fragments.FragmentOrthodontics;
import com.hcmute.mobile_android.network.models.CreateTreatmentPlanRequest;
import com.google.android.material.button.MaterialButtonToggleGroup;
import androidx.fragment.app.Fragment;
import com.hcmute.mobile_android.util.TokenManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorWorkflowActivity extends AppCompatActivity implements 
        TreatmentTemplateAdapter.OnTemplateSelectedListener,
        TreatmentStepAdapter.OnStepActionListener {
    
    public static final String EXTRA_INITIAL_QR = "EXTRA_INITIAL_QR";

    // Views
    private EditText etQrInput;
    private TextView tvPatientHeader, tvDoctorGreeting;
    private ImageButton btnScanQr;
    private MaterialButton btnLookup, btnSavePlan, btnSelectTemplate, btnPrescribe, btnPrintPlan, btnViewHistory, btnTransferXRay;
    private MaterialCardView cardLookup, cardTreatmentPlan;
    private LinearLayout layoutExamination;
    private RecyclerView rvTemplates, rvTreatmentSteps;
    private MaterialButtonToggleGroup toggleFormType;
    
    // Adapters
    private TreatmentTemplateAdapter templateAdapter;
    private TreatmentStepAdapter stepAdapter;
    
    // Data
    private List<TreatmentTemplate> templateList = new ArrayList<>();
    private List<TreatmentPlan.Step> treatmentSteps = new ArrayList<>();
    private PatientInfo currentPatient;
    private Long currentTreatmentPlanId;
    
    private ActivityResultLauncher<Intent> qrScannerLauncher;
    
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_workflow);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        setupAdapters();
        loadTemplates();

        qrScannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String qrData = result.getData().getStringExtra(PatientQRScannerActivity.EXTRA_SCAN_DATA);
                    if (qrData != null && !qrData.isEmpty()) {
                        etQrInput.setText(qrData);
                        lookupPatient(); // Auto lookup after scan
                    }
                }
            }
        );
        
        // Handle initial QR from Intent
        String initialQr = getIntent().getStringExtra(EXTRA_INITIAL_QR);
        if (initialQr != null && !initialQr.isEmpty()) {
            etQrInput.setText(initialQr);
            lookupPatient();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Header
        tvPatientHeader = findViewById(R.id.tv_patient_header);
        tvDoctorGreeting = findViewById(R.id.tv_doctor_greeting);
        TokenManager tm = new TokenManager(this);
        String docName = tm.getUserName() != null ? tm.getUserName() : "Bác sĩ";
        tvDoctorGreeting.setText("Chào " + docName);

        // Lookup
        cardLookup = findViewById(R.id.cardLookup);
        etQrInput = findViewById(R.id.etQrInput);
        btnScanQr = findViewById(R.id.btnScanQr);
        btnLookup = findViewById(R.id.btnLookup);
        
        // Examination Area
        layoutExamination = findViewById(R.id.layoutExamination);
        toggleFormType = findViewById(R.id.toggleFormType);
        
        toggleFormType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                Fragment fragment = null;
                if (checkedId == R.id.btnFormGeneral) {
                    fragment = new FragmentGeneralDental();
                } else if (checkedId == R.id.btnFormSurgery) {
                    fragment = new FragmentSurgeryChecklist();
                } else if (checkedId == R.id.btnFormOrtho) {
                    fragment = new FragmentOrthodontics();
                }
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerForm, fragment)
                        .commit();
                }
            }
        });
        
        // Treatment Plan
        cardTreatmentPlan = findViewById(R.id.cardTreatmentPlan);
        rvTemplates = findViewById(R.id.rvTemplates);
        rvTreatmentSteps = findViewById(R.id.rvTreatmentSteps);

        // Buttons
        btnSavePlan = findViewById(R.id.btnSavePlan);
        btnSelectTemplate = findViewById(R.id.btnSelectTemplate);
        btnPrescribe = findViewById(R.id.btnPrescribe);
        btnPrintPlan = findViewById(R.id.btnPrintPlan);
        btnTransferXRay = findViewById(R.id.btnTransferXRay);
        
        // Listeners
        btnScanQr.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientQRScannerActivity.class);
            intent.putExtra(PatientQRScannerActivity.EXTRA_RETURN_RESULT, true);
            qrScannerLauncher.launch(intent);
        });
        
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnViewHistory.setOnClickListener(v -> {
            if (currentPatient != null) {
                BottomSheetMedicalHistory bottomSheet = BottomSheetMedicalHistory.newInstance(currentPatient.getId());
                bottomSheet.show(getSupportFragmentManager(), "MedicalHistory");
            }
        });
        
        btnLookup.setOnClickListener(v -> lookupPatient());
        btnSavePlan.setOnClickListener(v -> saveTreatmentPlan());
        btnTransferXRay.setOnClickListener(v -> transferPatientToXRay());
        btnSelectTemplate.setOnClickListener(v -> {
            rvTemplates.setVisibility(rvTemplates.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        btnPrescribe.setOnClickListener(v -> Toast.makeText(this, "Tính năng kê đơn thuốc đang phát triển", Toast.LENGTH_SHORT).show());
        btnPrintPlan.setOnClickListener(v -> Toast.makeText(this, "Đang xuất PDF phác đồ...", Toast.LENGTH_SHORT).show());
    }

    private void setupAdapters() {
        // Templates adapter (Horizontal)
        rvTemplates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        templateAdapter = new TreatmentTemplateAdapter(templateList, this);
        rvTemplates.setAdapter(templateAdapter);
        
        // Treatment steps adapter (Vertical)
        rvTreatmentSteps.setLayoutManager(new LinearLayoutManager(this));
        stepAdapter = new TreatmentStepAdapter(treatmentSteps, this);
        rvTreatmentSteps.setAdapter(stepAdapter);
    }

    private void lookupPatient() {
        String qrCode = etQrInput.getText().toString().trim();
        if (qrCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã bệnh nhân", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLookup.setEnabled(false);
        btnLookup.setText("Đang tìm...");

        apiService.lookupPatientByQR(qrCode).enqueue(new Callback<PatientInfo>() {
            @Override
            public void onResponse(Call<PatientInfo> call, Response<PatientInfo> response) {
                btnLookup.setEnabled(true);
                btnLookup.setText("Tìm kiếm");
                
                if (response.isSuccessful() && response.body() != null) {
                    currentPatient = response.body();
                    displayPatientInfo(currentPatient);
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Không tìm thấy bệnh nhân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PatientInfo> call, Throwable t) {
                btnLookup.setEnabled(true);
                btnLookup.setText("Tìm kiếm");
                Toast.makeText(DoctorWorkflowActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPatientInfo(PatientInfo patient) {
        String header = "Khám Bệnh nhân " + patient.getFullName();
        if (patient.getBookedService() != null && !patient.getBookedService().isEmpty()) {
            header += "\n(Đặt lịch: " + patient.getBookedService() + ")";
        }
        tvPatientHeader.setText(header);
        
        // Hide lookup, show examination area
        cardLookup.setVisibility(View.GONE);
        layoutExamination.setVisibility(View.VISIBLE);
        
        // Cảnh báo nếu chưa Check-in (Trạng thái SCHEDULED)
        if ("SCHEDULED".equals(patient.getAppointmentStatus())) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cảnh báo Check-in")
                .setMessage("Bệnh nhân này hiện đang ở trạng thái 'Chờ khám' (Chưa làm thủ tục tại quầy lễ tân).\n\nBạn có muốn tiếp tục khám hay yêu cầu bệnh nhân quay lại làm thủ tục?")
                .setPositiveButton("Tiếp tục khám", null)
                .setNegativeButton("Quay lại quầy", (dialog, which) -> {
                    // Reset view
                    cardLookup.setVisibility(View.VISIBLE);
                    layoutExamination.setVisibility(View.GONE);
                })
                .setCancelable(false)
                .show();
        }

        String toastMsg = "Đã sẵn sàng khám: " + patient.getFullName();
        if (patient.getBookedService() != null && !patient.getBookedService().isEmpty()) {
            toastMsg += " - Dịch vụ: " + patient.getBookedService();
        }
        Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
    }

    private void loadTemplates() {
        apiService.getTreatmentTemplates().enqueue(new Callback<List<TreatmentTemplate>>() {
            @Override
            public void onResponse(Call<List<TreatmentTemplate>> call, Response<List<TreatmentTemplate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    templateList.clear();
                    templateList.addAll(response.body());
                    templateAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<TreatmentTemplate>> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, 
                    "Lỗi tải mẫu phác đồ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTemplateSelected(TreatmentTemplate template) {
        if (currentPatient == null) {
            Toast.makeText(this, "Vui lòng tra cứu bệnh nhân trước", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide the templates list after selecting one
        rvTemplates.setVisibility(View.GONE);

        CreateTreatmentPlanRequest request = new CreateTreatmentPlanRequest(
            template.getId(), 
            currentPatient.getId()
        );

        apiService.createTreatmentPlanFromTemplate(request).enqueue(new Callback<TreatmentPlan>() {
            @Override
            public void onResponse(Call<TreatmentPlan> call, Response<TreatmentPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TreatmentPlan plan = response.body();
                    currentTreatmentPlanId = plan.getId();
                    
                    treatmentSteps.clear();
                    treatmentSteps.addAll(plan.getSteps());
                    stepAdapter.notifyDataSetChanged();
                    
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Đã thêm phác đồ: " + template.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Lỗi tạo phác đồ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TreatmentPlan> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TreatmentPlan.Step currentStep;

    @Override
    public void onStepEdit(TreatmentPlan.Step step) {
        this.currentStep = step;
        
        // If the step is PENDING, call the "start" API to move it to IN_PROGRESS
        if ("PENDING".equals(step.getStatus())) {
            apiService.startTreatmentStep(step.getId()).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
                @Override
                public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                    if (response.isSuccessful()) {
                        step.setStatus("IN_PROGRESS");
                        stepAdapter.notifyDataSetChanged();
                        Toast.makeText(DoctorWorkflowActivity.this, "Đã bắt đầu thực hiện: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                    // Silently fail or log, as this is an optimization
                }
            });
        }

        Toast.makeText(this, "Nhập kết luận cho: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
        
        if (toggleFormType != null && step.getUiTemplateType() != null) {
            String template = step.getUiTemplateType().toUpperCase();
            if (template.contains("SURGERY")) {
                toggleFormType.check(R.id.btnFormSurgery);
            } else if (template.contains("ORTHO")) {
                toggleFormType.check(R.id.btnFormOrtho);
            } else {
                toggleFormType.check(R.id.btnFormGeneral);
            }
        }
    }

    @Override
    public void onStepComplete(TreatmentPlan.Step step) {
        Toast.makeText(this, "Hoàn thành bước: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onToothSelected(int toothNumber) {
        if (currentStep != null) {
            currentStep.setToothNumber(toothNumber);
            stepAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã chọn răng " + toothNumber + " cho bước " + currentStep.getServiceName(), Toast.LENGTH_SHORT).show();
            
            // Also update the UI fragment if it's the General Dental one
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
            if (fragment instanceof FragmentGeneralDental) {
                ((FragmentGeneralDental) fragment).onToothSelected(toothNumber);
            }
        } else {
            Toast.makeText(this, "Vui lòng chọn 'Chỉnh sửa' một bước điều trị trước khi chọn răng", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTreatmentPlan() {
        if (currentTreatmentPlanId == null) {
            Toast.makeText(this, "Không có phác đồ điều trị để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentStep == null) {
            Toast.makeText(this, "Vui lòng bấm \"Chỉnh sửa\" một bước trước khi nhập kết luận", Toast.LENGTH_LONG).show();
            return;
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
        String finalNotes = "";

        if (fragment instanceof FragmentGeneralDental) {
            FragmentGeneralDental generalDetail = (FragmentGeneralDental) fragment;
            String reason = generalDetail.etReason.getText().toString().trim();
            String diagnosis = generalDetail.etDiagnosis.getText().toString().trim();
            finalNotes = "Lý do: " + reason + "\nChẩn đoán: " + diagnosis + "\n" + 
                         "Điều trị: " + generalDetail.getToothTreatments().size() + " răng.";
        } else if (fragment instanceof FragmentSurgeryChecklist) {
            finalNotes = ((FragmentSurgeryChecklist) fragment).getFormDataNotes();
        } else if (fragment instanceof FragmentOrthodontics) {
            finalNotes = ((FragmentOrthodontics) fragment).getFormDataNotes();
        }

        if (finalNotes.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập thông tin thăm khám", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentStep != null) {
            currentStep.setDoctorConclusion(finalNotes);
        }

        // Build Payload cho Backend
        List<com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest.StepItem> apiItems = new java.util.ArrayList<>();
        int order = 0;
        for (TreatmentPlan.Step s : treatmentSteps) {
            com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest.StepItem item = 
                new com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest.StepItem();
            item.setServiceId(s.getServiceId());
            item.setSequenceOrder(order++);
            if (s.getToothNumber() != null) {
                item.setToothNumber(String.valueOf(s.getToothNumber()));
            }
            item.setDoctorConclusion(s.getDoctorConclusion());
            item.setStatus(s.getStatus()); // Bảo lưu trạng thái (COMPLETED, IN_PROGRESS, vv)
            apiItems.add(item);
        }

        com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest request = 
            new com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest(apiItems);

        btnSavePlan.setEnabled(false);
        apiService.updateTreatmentPlanSteps(currentTreatmentPlanId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSavePlan.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(DoctorWorkflowActivity.this, "Đã lưu hồ sơ bệnh án thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi lưu phác đồ: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSavePlan.setEnabled(true);
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void transferPatientToXRay() {
        if (currentPatient == null || currentPatient.getQueueId() == null || currentPatient.getQueueId() == -1) {
            Toast.makeText(this, "Không thể chuyển: Bệnh nhân chưa check-in hoặc thông hàng đợi lỗi", Toast.LENGTH_LONG).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận Chuyển X-Quang")
            .setMessage("Bệnh nhân " + currentPatient.getFullName() + " sẽ được chuyển sang danh sách chờ tại phòng X-Quang. \n\nHệ thống sẽ tự động cập nhật trạng thái trên App của bệnh nhân.")
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                btnTransferXRay.setEnabled(false);
                apiService.transferToXRay(currentPatient.getQueueId(), new HashMap<>()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(DoctorWorkflowActivity.this, "Đã chuyển bệnh nhân sang phòng X-Quang thành công", Toast.LENGTH_SHORT).show();
                            finish(); // Finish current examination after transfer
                        } else {
                            btnTransferXRay.setEnabled(true);
                            Toast.makeText(DoctorWorkflowActivity.this, "Lỗi khi chuyển X-Quang: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        btnTransferXRay.setEnabled(true);
                        Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}
      