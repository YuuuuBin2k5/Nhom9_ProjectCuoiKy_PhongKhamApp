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
import com.hcmute.mobile_android.network.models.CreateTreatmentPlanRequest;
import com.hcmute.mobile_android.ui.activities.PatientQRScannerActivity;
import com.hcmute.mobile_android.ui.views.OdontogramView;
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

    // Views
    private EditText etQrInput, etReason, etDiagnosis;
    private TextView tvPatientHeader, tvDoctorGreeting, tvToothNotes;
    private ImageButton btnScanQr;
    private MaterialButton btnLookup, btnSavePlan, btnSelectTemplate, btnPrescribe, btnPrintPlan;
    private MaterialCardView cardLookup, cardTreatmentPlan;
    private LinearLayout layoutExamination;
    private RecyclerView rvTemplates, rvTreatmentSteps;
    private OdontogramView odontogramView;
    
    // Adapters
    private TreatmentTemplateAdapter templateAdapter;
    private TreatmentStepAdapter stepAdapter;
    
    // Data
    private List<TreatmentTemplate> templateList = new ArrayList<>();
    private List<TreatmentPlan.Step> treatmentSteps = new ArrayList<>();
    private PatientInfo currentPatient;
    private Long currentTreatmentPlanId;
    
    // Store custom notes for teeth
    private Map<Integer, String> toothCustomNotesMap = new HashMap<>();
    
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
        odontogramView = findViewById(R.id.odontogramView);
        tvToothNotes = findViewById(R.id.tvToothNotes);
        etReason = findViewById(R.id.etReason);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        
        // Treatment Plan
        cardTreatmentPlan = findViewById(R.id.cardTreatmentPlan);
        rvTemplates = findViewById(R.id.rvTemplates);
        rvTreatmentSteps = findViewById(R.id.rvTreatmentSteps);
        
        // Setup odontogram listener
        odontogramView.setOnToothSelectedListener(this::onToothSelected);

        // Buttons
        btnSavePlan = findViewById(R.id.btnSavePlan);
        btnSelectTemplate = findViewById(R.id.btnSelectTemplate);
        btnPrescribe = findViewById(R.id.btnPrescribe);
        btnPrintPlan = findViewById(R.id.btnPrintPlan);
        
        // Listeners
        btnScanQr.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientQRScannerActivity.class);
            intent.putExtra(PatientQRScannerActivity.EXTRA_RETURN_RESULT, true);
            qrScannerLauncher.launch(intent);
        });
        btnLookup.setOnClickListener(v -> lookupPatient());
        btnSavePlan.setOnClickListener(v -> saveTreatmentPlan());
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
        tvPatientHeader.setText("Khám Bệnh nhân " + patient.getFullName());
        
        // Hide lookup, show examination area
        cardLookup.setVisibility(View.GONE);
        layoutExamination.setVisibility(View.VISIBLE);
        
        Toast.makeText(this, "Đã sẵn sàng khám: " + patient.getFullName(), Toast.LENGTH_SHORT).show();
        
        // Optionally, pre-fill some fields or load previous notes from backend
        // etReason.setText("...");
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

    @Override
    public void onStepEdit(TreatmentPlan.Step step) {
        Toast.makeText(this, "Chỉnh sửa bước: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStepComplete(TreatmentPlan.Step step) {
        Toast.makeText(this, "Hoàn thành bước: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onToothSelected(int toothNumber) {
        showToothNoteDialog(toothNumber);
    }
    
    private void showToothNoteDialog(int toothNumber) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        
        View view = getLayoutInflater().inflate(R.layout.dialog_tooth_note, null);
        dialog.setContentView(view);
        
        TextView tvTitle = view.findViewById(R.id.tvToothTitle);
        tvTitle.setText("Ghi chú Răng R" + toothNumber);
        
        android.widget.RadioGroup rgStatus = view.findViewById(R.id.rgToothStatus);
        EditText etNote = view.findViewById(R.id.etToothNote);
        
        // Restore previous status
        String existingStatus = odontogramView.getToothStatus(toothNumber);
        if (existingStatus != null) {
            switch (existingStatus) {
                case "caries": rgStatus.check(R.id.rbCaries); break;
                case "filled": rgStatus.check(R.id.rbFilled); break;
                case "requested": rgStatus.check(R.id.rbRequested); break;
                case "rct": rgStatus.check(R.id.rbRct); break;
                default: rgStatus.check(R.id.rbHealthy); break;
            }
        } else {
            rgStatus.check(R.id.rbHealthy);
        }
        
        // Restore previous note
        if (toothCustomNotesMap.containsKey(toothNumber)) {
            etNote.setText(toothCustomNotesMap.get(toothNumber));
        }
        
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            int checkedId = rgStatus.getCheckedRadioButtonId();
            String status = "healthy";
            String statusText = "Bình thường";
            
            if (checkedId == R.id.rbCaries) { status = "caries"; statusText = "Sâu răng"; }
            else if (checkedId == R.id.rbFilled) { status = "filled"; statusText = "Đã trám"; }
            else if (checkedId == R.id.rbRequested) { status = "requested"; statusText = "BN yêu cầu"; }
            else if (checkedId == R.id.rbRct) { status = "rct"; statusText = "Cần chữa tủy"; }
            
            odontogramView.setToothStatus(toothNumber, status);
            
            String customNote = etNote.getText().toString().trim();
            if (status.equals("healthy") && customNote.isEmpty()) {
                toothCustomNotesMap.remove(toothNumber);
            } else {
                String fullNote = statusText;
                if (!customNote.isEmpty()) {
                    fullNote += " - " + customNote;
                }
                toothCustomNotesMap.put(toothNumber, fullNote);
            }
            
            updateToothNotesDisplay();
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void updateToothNotesDisplay() {
        if (toothCustomNotesMap.isEmpty()) {
            tvToothNotes.setText("Chưa có ghi chú nào. Nhấn vào răng trên sơ đồ để thêm ghi chú.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String> entry : toothCustomNotesMap.entrySet()) {
            sb.append("• R").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        tvToothNotes.setText(sb.toString().trim());
    }

    private void saveTreatmentPlan() {
        String reason = etReason.getText().toString().trim();
        String diagnosis = etDiagnosis.getText().toString().trim();
        
        if (reason.isEmpty() || diagnosis.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập lý do và chẩn đoán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ideally, combine toothNotesMap, reason, and diagnosis and post to backend
        // apiService.saveDentalRecord(...)
        
        Toast.makeText(this, "Đã lưu hồ sơ bệnh án thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}