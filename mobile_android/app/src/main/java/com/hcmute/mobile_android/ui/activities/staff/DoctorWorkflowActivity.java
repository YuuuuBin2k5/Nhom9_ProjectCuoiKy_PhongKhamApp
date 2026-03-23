package com.hcmute.mobile_android.ui.activities.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.hcmute.mobile_android.ui.views.OdontogramView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorWorkflowActivity extends AppCompatActivity implements 
        TreatmentTemplateAdapter.OnTemplateSelectedListener,
        TreatmentStepAdapter.OnStepActionListener {

    // Views
    private EditText etQrInput;
    private MaterialButton btnLookup, btnSavePlan;
    private MaterialCardView cardPatientInfo, cardTemplates, cardTreatmentPlan;
    private LinearLayout layoutPatientInfo;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        etQrInput = findViewById(R.id.etQrInput);
        btnLookup = findViewById(R.id.btnLookup);
        btnSavePlan = findViewById(R.id.btnSavePlan);
        
        cardPatientInfo = findViewById(R.id.cardPatientInfo);
        cardTemplates = findViewById(R.id.cardTemplates);
        cardTreatmentPlan = findViewById(R.id.cardTreatmentPlan);
        
        layoutPatientInfo = findViewById(R.id.layoutPatientInfo);
        rvTemplates = findViewById(R.id.rvTemplates);
        rvTreatmentSteps = findViewById(R.id.rvTreatmentSteps);
        odontogramView = findViewById(R.id.odontogramView);
        
        // Set default QR for testing
        etQrInput.setText("patient:1");
        
        btnLookup.setOnClickListener(v -> lookupPatient());
        btnSavePlan.setOnClickListener(v -> saveTreatmentPlan());
        
        // Setup odontogram listener
        odontogramView.setOnToothSelectedListener(this::onToothSelected);
        
        // Initially hide cards
        cardPatientInfo.setVisibility(View.GONE);
        cardTreatmentPlan.setVisibility(View.GONE);
    }

    private void setupAdapters() {
        // Templates adapter
        rvTemplates.setLayoutManager(new LinearLayoutManager(this));
        templateAdapter = new TreatmentTemplateAdapter(templateList, this);
        rvTemplates.setAdapter(templateAdapter);
        
        // Treatment steps adapter
        rvTreatmentSteps.setLayoutManager(new LinearLayoutManager(this));
        stepAdapter = new TreatmentStepAdapter(treatmentSteps, this);
        rvTreatmentSteps.setAdapter(stepAdapter);
    }

    private void lookupPatient() {
        String qrCode = etQrInput.getText().toString().trim();
        if (qrCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã QR", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLookup.setEnabled(false);
        btnLookup.setText("Đang tra cứu...");

        apiService.lookupPatientByQR(qrCode).enqueue(new Callback<PatientInfo>() {
            @Override
            public void onResponse(Call<PatientInfo> call, Response<PatientInfo> response) {
                btnLookup.setEnabled(true);
                btnLookup.setText("Tra cứu");
                
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
                btnLookup.setText("Tra cứu");
                Toast.makeText(DoctorWorkflowActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPatientInfo(PatientInfo patient) {
        layoutPatientInfo.removeAllViews();
        
        // Create patient info layout programmatically
        View patientView = getLayoutInflater().inflate(R.layout.item_patient_info, layoutPatientInfo, false);
        
        // Set patient data (you'll need to create this layout)
        // For now, just show the card
        cardPatientInfo.setVisibility(View.VISIBLE);
        
        Toast.makeText(this, "Đã tải thông tin: " + patient.getFullName(), Toast.LENGTH_SHORT).show();
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
                    
                    cardTreatmentPlan.setVisibility(View.VISIBLE);
                    
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Đã tạo phác đồ từ mẫu: " + template.getName(), Toast.LENGTH_SHORT).show();
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
        // TODO: Open step edit dialog
        Toast.makeText(this, "Chỉnh sửa bước: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStepComplete(TreatmentPlan.Step step) {
        // TODO: Mark step as completed
        Toast.makeText(this, "Hoàn thành bước: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onToothSelected(int toothNumber) {
        // Handle tooth selection from odontogram
        Toast.makeText(this, "Đã chọn răng số: " + toothNumber, Toast.LENGTH_SHORT).show();
    }

    private void saveTreatmentPlan() {
        if (currentTreatmentPlanId == null) {
            Toast.makeText(this, "Chưa có phác đồ để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement save treatment plan steps
        Toast.makeText(this, "Đã lưu phác đồ điều trị", Toast.LENGTH_SHORT).show();
    }
}