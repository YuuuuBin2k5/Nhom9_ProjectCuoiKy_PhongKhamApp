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
import com.hcmute.mobile_android.adapters.ImagePreviewAdapter;
import com.hcmute.mobile_android.adapters.PriceBreakdownAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PatientInfo;
import com.hcmute.mobile_android.network.models.TreatmentPlan;
import com.hcmute.mobile_android.network.models.TreatmentTemplate;
import com.hcmute.mobile_android.ui.activities.PatientQRScannerActivity;
import com.hcmute.mobile_android.ui.activities.PaymentActivity;
import com.hcmute.mobile_android.ui.activities.staff.PrescriptionActivity;
import com.hcmute.mobile_android.ui.fragments.BottomSheetMedicalHistory;
import com.hcmute.mobile_android.ui.fragments.FragmentGeneralDental;
import com.hcmute.mobile_android.ui.fragments.FragmentSurgeryChecklist;
import com.hcmute.mobile_android.ui.fragments.FragmentOrthodontics;
import com.hcmute.mobile_android.ui.fragments.FragmentXray;
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
    private TextView tvPatientHeader, tvDoctorGreeting, tvTotalEstimate;
    private ImageButton btnScanQr;
    private MaterialButton btnActivatePlan, btnSelectTemplate, btnAddService, btnPrescribe, btnPrintPlan, btnCompleteStep, btnCancelStep, btnLookup, btnPayment;
    private MaterialCardView cardLookup, cardTreatmentPlan;
    private com.google.android.material.textfield.TextInputEditText etDoctorConclusion;
    private View btnViewHistory;
    private View layoutExamination;
    private RecyclerView rvTemplates, rvTreatmentSteps, rvPriceBreakdown;
    private com.google.android.material.button.MaterialButtonToggleGroup toggleFormType;
    
    // Adapters
    private TreatmentTemplateAdapter templateAdapter;
    private TreatmentStepAdapter stepAdapter;
    private PriceBreakdownAdapter priceBreakdownAdapter;
    
    // Data
    private List<TreatmentTemplate> templateList = new ArrayList<>();
    private List<TreatmentPlan.Step> treatmentSteps = new ArrayList<>();
    private PatientInfo currentPatient;
    private Long currentTreatmentPlanId;
    
    private ActivityResultLauncher<Intent> qrScannerLauncher;
    
    private ApiService apiService;

    // Image upload
    private ActivityResultLauncher<String> imagePickerLauncher;
    private List<String> currentStepImageUrls = new ArrayList<>();

    private boolean isSaving = false;

    @Override
    protected void onPause() {
        super.onPause();
        // Auto-save when leaving the form
        if (currentTreatmentPlanId != null && currentPatient != null) {
            saveTreatmentPlanInternal(true);
        }
    }

    private void saveTreatmentPlan() {
        saveTreatmentPlanInternal(false);
    }

    private void saveTreatmentPlanInternal(boolean silent) {
        saveTreatmentPlanInternal(silent, null);
    }

    private void saveTreatmentPlanInternal(boolean silent, Runnable onDone) {
        if (isSaving) return;
        
        if (currentTreatmentPlanId == null) {
            // Even in silent mode, we need to create plan if there are steps to save
            if (treatmentSteps.isEmpty()) {
                if (!silent) Toast.makeText(this, "Không có phác đồ điều trị để cập nhật", Toast.LENGTH_SHORT).show();
                return;
            }
            createBlankPlanAndSave(silent, onDone);
            return;
        }
        
        isSaving = true;

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
        String finalNotes = "";
        if (fragment instanceof FragmentGeneralDental) {
            finalNotes = ((FragmentGeneralDental) fragment).getFormDataNotes();
        } else if (fragment instanceof FragmentSurgeryChecklist) {
            finalNotes = ((FragmentSurgeryChecklist) fragment).getFormDataNotes();
        } else if (fragment instanceof FragmentOrthodontics) {
            finalNotes = ((FragmentOrthodontics) fragment).getFormDataNotes();
        } else if (fragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
            finalNotes = ((com.hcmute.mobile_android.ui.fragments.FragmentXray) fragment).getFormDataNotes();
        }

        if (currentStep != null && !finalNotes.trim().isEmpty()) {
            currentStep.setDoctorConclusion(finalNotes);
        }

        List<com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest.StepItem> apiItems = new java.util.ArrayList<>();
        int order = 0;
        
        android.util.Log.d("DoctorWorkflow", "=== saveTreatmentPlanInternal: Preparing request ===");
        android.util.Log.d("DoctorWorkflow", "editingStep: " + (editingStep != null ? editingStep.getServiceName() + " (ID: " + editingStep.getId() + ")" : "null"));
        android.util.Log.d("DoctorWorkflow", "currentStep: " + (currentStep != null ? currentStep.getServiceName() + " (ID: " + currentStep.getId() + ")" : "null"));
        
        for (TreatmentPlan.Step s : treatmentSteps) {
            com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest.StepItem item = 
                new com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest.StepItem();
            item.setId(s.getId());
            item.setServiceId(s.getServiceId());
            item.setSequenceOrder(order++);
            item.setToothNumber(s.getToothNumber());
            item.setDoctorConclusion(s.getDoctorConclusion());
            
            // CRITICAL FIX: Preserve status of steps that aren't being actively worked on
            // Only allow status changes for editingStep or currentStep
            String statusToSend = s.getStatus();
            
            boolean isEditingThisStep = (editingStep != null && s.getId() != null && s.getId().equals(editingStep.getId()));
            boolean isCurrentThisStep = (currentStep != null && s.getId() != null && s.getId().equals(currentStep.getId()));
            
            if (isEditingThisStep) {
                // This is the step being edited - send its status
                statusToSend = s.getStatus();
                android.util.Log.d("DoctorWorkflow", "→ Step " + s.getId() + " (" + s.getServiceName() + "): EDITING - status=" + statusToSend);
            } else if (isCurrentThisStep) {
                // This is the current workflow step - send its status
                statusToSend = s.getStatus();
                android.util.Log.d("DoctorWorkflow", "→ Step " + s.getId() + " (" + s.getServiceName() + "): CURRENT - status=" + statusToSend);
            } else {
                // CRITICAL: For other steps, preserve their status exactly as-is
                // This prevents accidentally changing PENDING steps to IN_PROGRESS
                statusToSend = s.getStatus();
                android.util.Log.d("DoctorWorkflow", "→ Step " + s.getId() + " (" + s.getServiceName() + "): OTHER - status=" + statusToSend + " (preserving)");
            }
            
            item.setStatus(statusToSend);
            apiItems.add(item);
        }
        
        android.util.Log.d("DoctorWorkflow", "=== Sending " + apiItems.size() + " steps to backend ===");

        com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest request = 
            new com.hcmute.mobile_android.network.models.request.UpdatePlanStepsRequest(apiItems);

        apiService.updateTreatmentPlanSteps(currentTreatmentPlanId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isSaving = false;
                
                android.util.Log.d("DoctorWorkflow", "=== Save response received ===");
                android.util.Log.d("DoctorWorkflow", "Response code: " + response.code());
                android.util.Log.d("DoctorWorkflow", "Success: " + response.isSuccessful());
                
                if (!silent) {
                    if (response.isSuccessful()) {
                        Toast.makeText(DoctorWorkflowActivity.this, "Đã tự động lưu hồ sơ bệnh án!", Toast.LENGTH_SHORT).show();
                        if (onDone != null) onDone.run();
                    } else {
                        Toast.makeText(DoctorWorkflowActivity.this, "Lỗi lưu phác đồ: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                } else if (onDone != null) {
                    if (response.isSuccessful()) {
                        onDone.run();
                    } else {
                        Toast.makeText(DoctorWorkflowActivity.this, "Đồng bộ phác đồ thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isSaving = false;
                if (!silent) {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_workflow);

        apiService = RetrofitClient.getApiService(this);
        
        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImageToServer(uri);
                }
            }
        );
        
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
        
        // Auto-open camera scanner if launched from home "Quét QR" shortcut
        if (getIntent().getBooleanExtra("OPEN_SCANNER", false)) {
            // Small delay to let Activity fully render before launching scanner
            etQrInput.postDelayed(() -> {
                Intent intent = new Intent(this, PatientQRScannerActivity.class);
                intent.putExtra(PatientQRScannerActivity.EXTRA_RETURN_RESULT, true);
                qrScannerLauncher.launch(intent);
            }, 300);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Handle back button press with auto-save
     */
    private void handleBackPress() {
        android.util.Log.d("DoctorWorkflow", "handleBackPress called");
        
        // If no patient or no treatment plan, just exit
        if (currentPatient == null || currentTreatmentPlanId == null) {
            android.util.Log.d("DoctorWorkflow", "No patient or plan, exiting without save");
            finish();
            return;
        }
        
        // If currently saving, wait
        if (isSaving) {
            Toast.makeText(this, "Đang lưu dữ liệu, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if there's any unsaved data
        boolean hasUnsavedData = false;
        
        // Check if current step has data
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
        if (currentFragment != null && currentStep != null && "IN_PROGRESS".equals(currentStep.getStatus())) {
            hasUnsavedData = true;
            android.util.Log.d("DoctorWorkflow", "Has IN_PROGRESS step with potential unsaved data");
        }
        
        // Check if there are any steps
        if (!treatmentSteps.isEmpty()) {
            hasUnsavedData = true;
        }
        
        if (hasUnsavedData) {
            android.util.Log.d("DoctorWorkflow", "Auto-saving before exit");
            // Auto-save silently before exiting
            saveTreatmentPlanInternal(true, () -> {
                android.util.Log.d("DoctorWorkflow", "Auto-save completed, exiting");
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã tự động lưu", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            android.util.Log.d("DoctorWorkflow", "No unsaved data, exiting");
            finish();
        }
    }
    
    /**
     * Override back button to use handleBackPress
     */
    @Override
    public void onBackPressed() {
        handleBackPress();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> handleBackPress());
        
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
        tvTotalEstimate = findViewById(R.id.tvTotalEstimate);
        etDoctorConclusion = findViewById(R.id.etDoctorConclusion);
        
        // Examination Area
        layoutExamination = findViewById(R.id.layoutExamination);
        toggleFormType = findViewById(R.id.toggleFormType);
        
        toggleFormType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                Fragment fragment = null;
                String templateKey = null;
                
                if (checkedId == R.id.btnFormGeneral) {
                    fragment = new FragmentGeneralDental();
                    templateKey = "GENERAL";
                } else if (checkedId == R.id.btnFormSurgery) {
                    fragment = new FragmentSurgeryChecklist();
                    templateKey = "SURGERY";
                } else if (checkedId == R.id.btnFormXray) {
                    fragment = new com.hcmute.mobile_android.ui.fragments.FragmentXray();
                    templateKey = "XRAY";
                } else if (checkedId == R.id.btnFormOrtho) {
                    fragment = new FragmentOrthodontics();
                    templateKey = "ORTHO";
                }
                
                if (fragment != null) {
                    final Fragment finalFragment = fragment;
                    final String finalTemplateKey = templateKey;
                    
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerForm, fragment)
                        .commit();
                    
                    // AUTO-LOAD: Kiểm tra xem có dữ liệu COMPLETED trong cache không
                    findViewById(R.id.fragmentContainerForm).postDelayed(() -> {
                        autoPopulateFragmentFromCache(finalFragment, finalTemplateKey);
                    }, 100);
                }
            }
        });
        
        // Treatment Plan
        cardTreatmentPlan = findViewById(R.id.cardTreatmentPlan);
        rvTemplates = findViewById(R.id.rvTemplates);
        rvTreatmentSteps = findViewById(R.id.rvTreatmentSteps);
        rvPriceBreakdown = findViewById(R.id.rvPriceBreakdown);

        // Buttons
        btnActivatePlan = findViewById(R.id.btnActivatePlan);
        btnSelectTemplate = findViewById(R.id.btnSelectTemplate);
        btnAddService = findViewById(R.id.btnAddService);
        btnPrescribe = findViewById(R.id.btnPrescribe);
        btnPrintPlan = findViewById(R.id.btnPrintPlan);
        btnCompleteStep = findViewById(R.id.btnCompleteStep);
        btnCancelStep = findViewById(R.id.btnCancelStep);
        btnPayment = findViewById(R.id.btnPayment);
        
        // CRITICAL FIX: Setup upload image button
        MaterialButton btnUploadImage = findViewById(R.id.btnUploadImage);
        if (btnUploadImage != null) {
            btnUploadImage.setOnClickListener(v -> {
                // Launch image picker
                imagePickerLauncher.launch("image/*");
            });
        }
        
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
        btnActivatePlan.setOnClickListener(v -> activatePlan());
        btnPayment.setOnClickListener(v -> {
            if (currentPatient == null || currentTreatmentPlanId == null) {
                Toast.makeText(this, "Vui lòng chọn bệnh nhân và tạo phác đồ điều trị", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate: Check if all steps are completed
            boolean hasIncompleteSteps = treatmentSteps.stream()
                .anyMatch(s -> !"COMPLETED".equals(s.getStatus()) && !"SKIPPED".equals(s.getStatus()));
            
            if (hasIncompleteSteps) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Chưa thể thanh toán")
                    .setMessage("Vui lòng hoàn thành tất cả các bước điều trị trước khi thanh toán.")
                    .setPositiveButton("OK", null)
                    .show();
                return;
            }
            
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận hoàn tất")
                .setMessage("Bạn có chắc muốn hoàn tất phác đồ điều trị và tạo hóa đơn thanh toán?\n\n" +
                           "Tổng tiền: " + tvTotalEstimate.getText())
                .setPositiveButton("Xác nhận", (dialog, which) -> completeAndGenerateInvoice())
                .setNegativeButton("Hủy", null)
                .show();
        });
        btnCompleteStep.setOnClickListener(v -> {
            if (currentStep != null) {
                onStepComplete(currentStep);
            }
        });
        btnCancelStep.setOnClickListener(v -> {
            if (currentStep != null) {
                onStepCancel(currentStep);
            }
        });
        btnSelectTemplate.setOnClickListener(v -> {
            rvTemplates.setVisibility(rvTemplates.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        btnAddService.setOnClickListener(v -> showAddServiceDialog(null));
        btnPrescribe.setOnClickListener(v -> {
            if (currentPatient != null && currentPatient.getAppointmentId() != null && currentPatient.getAppointmentId() != -1) {
                Intent intent = new Intent(this, PrescriptionActivity.class);
                intent.putExtra(PrescriptionActivity.EXTRA_APPOINTMENT_ID, currentPatient.getAppointmentId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không thể xác định lịch hẹn cho bệnh nhân này", Toast.LENGTH_SHORT).show();
            }
        });
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
        
        // Price breakdown adapter (Vertical)
        rvPriceBreakdown.setLayoutManager(new LinearLayoutManager(this));
        priceBreakdownAdapter = new PriceBreakdownAdapter(treatmentSteps);
        rvPriceBreakdown.setAdapter(priceBreakdownAdapter);
    }

    private void launchImagePicker() {
        imagePickerLauncher.launch("image/*");
    }
    
    public void triggerImageUpload() {
        launchImagePicker();
    }
    
    private void uploadImageToServer(android.net.Uri uri) {
        // Use background thread for file I/O
        new Thread(() -> {
            try {
                java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                inputStream.close();
                
                // Switch back to main thread for network call
                runOnUiThread(() -> {
                    okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                        okhttp3.MediaType.parse("image/*"), bytes);
                    okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData(
                        "file", "xray_" + System.currentTimeMillis() + ".jpg", requestFile);
                    
                    apiService.uploadFile(body).enqueue(new Callback<com.hcmute.mobile_android.network.models.UploadResponse>() {
                        @Override
                        public void onResponse(Call<com.hcmute.mobile_android.network.models.UploadResponse> call, 
                                             Response<com.hcmute.mobile_android.network.models.UploadResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String imageUrl = response.body().getFileDownloadUri();
                                
                                // ONLY notify FragmentXray - single source of truth
                                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
                                if (fragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
                                    ((com.hcmute.mobile_android.ui.fragments.FragmentXray) fragment).onImageUploaded(imageUrl);
                                } else {
                                    // Fallback: add to activity list for non-Xray fragments
                                    currentStepImageUrls.add(imageUrl);
                                    updateImagePreview();
                                }
                                
                                Toast.makeText(DoctorWorkflowActivity.this, "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi tải ảnh: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<com.hcmute.mobile_android.network.models.UploadResponse> call, Throwable t) {
                            Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi đọc file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateImagePreview() {
        // Image preview is handled by individual fragments (e.g., FragmentXray)
        // This method is kept for compatibility but doesn't update UI
    }
    
    public void onImageDeleted(String imageUrl) {
        // Remove from current list
        currentStepImageUrls.remove(imageUrl);
        updateImagePreview();
        
        // TODO: Call API to delete image from server if needed
    }
    
    private void loadStepImages(Long stepId) {
        // Clear current images
        currentStepImageUrls.clear();
        
        // Find the step and get its images
        for (TreatmentPlan.Step s : treatmentSteps) {
            if (s.getId() != null && s.getId().equals(stepId)) {
                if (s.getImages() != null) {
                    for (TreatmentPlan.Step.ImageItem img : s.getImages()) {
                        if (img.getImageUrl() != null) {
                            currentStepImageUrls.add(img.getImageUrl());
                        }
                    }
                }
                break;
            }
        }
        
        // Update the preview
        updateImagePreview();
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
                    loadLastPlanForPatient(currentPatient.getId());
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
        
        // Cảnh báo chỉ khi CHƯA Check-in (queueId == -1 = chưa vào hàng đợi)
        boolean notCheckedIn = (patient.getQueueId() == null || patient.getQueueId() == -1)
                && "SCHEDULED".equals(patient.getAppointmentStatus());
        if (notCheckedIn) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cảnh báo Check-in")
                .setMessage("Bệnh nhân này chưa làm thủ tục check-in tại quầy lễ tân.\n\nBạn có muốn tiếp tục khám hay yêu cầu bệnh nhân quay lại làm thủ tục?")
                .setPositiveButton("Tiếp tục khám", null)
                .setNegativeButton("Quay lại quầy", (dialog, which) -> {
                    cardLookup.setVisibility(View.VISIBLE);
                    layoutExamination.setVisibility(View.GONE);
                })
                .setCancelable(false)
                .show();
        }

        // FIX: Check nếu bệnh nhân đã có TreatmentPlan
        if (patient.getHasTreatmentPlan() != null && patient.getHasTreatmentPlan()) {
            // Đã có phác đồ -> Load phác đồ existing
            currentTreatmentPlanId = patient.getTreatmentPlanId();
            String status = patient.getTreatmentPlanStatus() != null ? patient.getTreatmentPlanStatus() : "UNKNOWN";
            
            Toast.makeText(this, "Bệnh nhân đã có phác đồ điều trị (Trạng thái: " + status + ")", Toast.LENGTH_LONG).show();
            
            // Load treatment plan từ server
            loadExistingTreatmentPlan(patient.getTreatmentPlanId());
        } else {
            // Chưa có phác đồ -> Hiển thị form tạo mới
            String toastMsg = "Đã sẵn sàng khám: " + patient.getFullName();
            if (patient.getBookedService() != null && !patient.getBookedService().isEmpty()) {
                toastMsg += " - Dịch vụ: " + patient.getBookedService();
            }
            toastMsg += "\n(Chưa có phác đồ điều trị - Vui lòng tạo mới)";
            Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            
            // Clear existing data
            treatmentSteps.clear();
            stepAdapter.notifyDataSetChanged();
            currentTreatmentPlanId = null;
        }
    }
    
    private void loadExistingTreatmentPlan(Long planId) {
        if (planId == null || planId <= 0) {
            Toast.makeText(this, "ID phác đồ không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        apiService.getTreatmentPlan(planId).enqueue(new Callback<TreatmentPlan>() {
            @Override
            public void onResponse(Call<TreatmentPlan> call, Response<TreatmentPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TreatmentPlan plan = response.body();
                    currentTreatmentPlanId = plan.getId();
                    
                    treatmentSteps.clear();
                    treatmentSteps.addAll(plan.getSteps());
                    
                    updateUIMode(plan.isDraft());
                    stepAdapter.notifyDataSetChanged();
                    updateTotalEstimate();
                    
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Đã tải phác đồ điều trị (" + treatmentSteps.size() + " bước)", Toast.LENGTH_SHORT).show();
                    
                    // AUTO-LOAD: Tự động load dữ liệu của bước đang IN_PROGRESS
                    autoLoadInProgressStep();
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Lỗi tải phác đồ: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TreatmentPlan> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Pre-load dữ liệu của TẤT CẢ các bước COMPLETED vào cache
     * Được gọi sau khi load phác đồ điều trị
     */
    private void autoLoadInProgressStep() {
        // Clear cache cũ
        completedStepsDataCache.clear();
        
        // DEBUG: Log tất cả steps
        android.util.Log.d("DoctorWorkflow", "=== AUTO-LOAD DEBUG ===");
        android.util.Log.d("DoctorWorkflow", "Total steps: " + treatmentSteps.size());
        
        // Đếm và cache dữ liệu của TẤT CẢ bước COMPLETED
        int completedCount = 0;
        for (TreatmentPlan.Step step : treatmentSteps) {
            // DEBUG: Log mỗi step
            android.util.Log.d("DoctorWorkflow", "Step ID=" + step.getId() + 
                ", Status=" + step.getStatus() + 
                ", UiTemplateType=" + step.getUiTemplateType() +
                ", ServiceName=" + step.getServiceName());
            
            if ("COMPLETED".equals(step.getStatus()) && step.getUiTemplateType() != null) {
                completedCount++;
                
                // Lấy dữ liệu
                String conclusion = step.getDoctorConclusion();
                List<String> imageUrls = new ArrayList<>();
                if (step.getImages() != null) {
                    for (TreatmentPlan.Step.ImageItem img : step.getImages()) {
                        if (img.getImageUrl() != null) {
                            imageUrls.add(img.getImageUrl());
                        }
                    }
                }
                
                // Lưu vào cache theo template type
                String templateKey = step.getUiTemplateType().toUpperCase();
                completedStepsDataCache.put(templateKey, new StepDataCache(
                    step.getUiTemplateType(),
                    conclusion,
                    imageUrls,
                    true
                ));
                
                android.util.Log.d("DoctorWorkflow", "✓ Cached step: " + templateKey + 
                    ", conclusion=" + (conclusion != null ? conclusion.substring(0, Math.min(50, conclusion.length())) : "null") +
                    ", images=" + imageUrls.size());
            }
        }
        
        android.util.Log.d("DoctorWorkflow", "Completed count: " + completedCount);
        android.util.Log.d("DoctorWorkflow", "Cache size: " + completedStepsDataCache.size());
        
        if (completedCount > 0) {
            Toast.makeText(this, 
                "Đã tải " + completedCount + " bước đã hoàn thành. Dữ liệu sẽ tự động hiển thị khi chuyển tab.", 
                Toast.LENGTH_LONG).show();
        } else {
            // DEBUG: Thông báo không có bước completed
            android.util.Log.w("DoctorWorkflow", "⚠️ Không tìm thấy bước COMPLETED nào!");
        }
    }
    
    /**
     * Tự động populate dữ liệu từ cache vào fragment khi chuyển tab
     * Chỉ populate nếu có dữ liệu COMPLETED trong cache
     */
    private void autoPopulateFragmentFromCache(Fragment fragment, String templateKey) {
        android.util.Log.d("DoctorWorkflow", "=== AUTO-POPULATE DEBUG ===");
        android.util.Log.d("DoctorWorkflow", "Fragment: " + (fragment != null ? fragment.getClass().getSimpleName() : "null"));
        android.util.Log.d("DoctorWorkflow", "Template key: " + templateKey);
        android.util.Log.d("DoctorWorkflow", "Cache size: " + completedStepsDataCache.size());
        android.util.Log.d("DoctorWorkflow", "Cache keys: " + completedStepsDataCache.keySet());
        
        if (fragment == null || templateKey == null) {
            android.util.Log.w("DoctorWorkflow", "⚠️ Fragment or templateKey is null, skipping");
            return;
        }
        
        // Tìm dữ liệu trong cache
        StepDataCache cachedData = null;
        
        // Thử tìm exact match
        if (completedStepsDataCache.containsKey(templateKey)) {
            cachedData = completedStepsDataCache.get(templateKey);
            android.util.Log.d("DoctorWorkflow", "✓ Found exact match for: " + templateKey);
        } else {
            // Thử tìm partial match (ví dụ: XRAY, X-RAY, X_RAY)
            for (Map.Entry<String, StepDataCache> entry : completedStepsDataCache.entrySet()) {
                if (entry.getKey().contains(templateKey) || templateKey.contains(entry.getKey())) {
                    cachedData = entry.getValue();
                    android.util.Log.d("DoctorWorkflow", "✓ Found partial match: " + entry.getKey() + " for: " + templateKey);
                    break;
                }
            }
        }
        
        // Nếu không có dữ liệu cached, không làm gì
        if (cachedData == null) {
            android.util.Log.w("DoctorWorkflow", "⚠️ No cached data found for: " + templateKey);
            return;
        }
        
        android.util.Log.d("DoctorWorkflow", "✓ Populating fragment with cached data");
        android.util.Log.d("DoctorWorkflow", "  - Conclusion: " + (cachedData.doctorConclusion != null ? cachedData.doctorConclusion.substring(0, Math.min(50, cachedData.doctorConclusion.length())) : "null"));
        android.util.Log.d("DoctorWorkflow", "  - Images: " + (cachedData.imageUrls != null ? cachedData.imageUrls.size() : 0));
        
        final StepDataCache finalCachedData = cachedData;
        
        // Populate dữ liệu vào fragment với READ-ONLY mode
        if (fragment instanceof FragmentGeneralDental) {
            FragmentGeneralDental generalFragment = (FragmentGeneralDental) fragment;
            if (finalCachedData.doctorConclusion != null && !finalCachedData.doctorConclusion.trim().isEmpty()) {
                generalFragment.setData(finalCachedData.doctorConclusion);
                android.util.Log.d("DoctorWorkflow", "✓ Set data for FragmentGeneralDental");
            }
            generalFragment.setReadOnlyMode(true);
            android.util.Log.d("DoctorWorkflow", "✓ Set READ-ONLY mode for FragmentGeneralDental");
            
        } else if (fragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
            com.hcmute.mobile_android.ui.fragments.FragmentXray xrayFragment = 
                (com.hcmute.mobile_android.ui.fragments.FragmentXray) fragment;
            
            android.util.Log.d("DoctorWorkflow", "Populating FragmentXray from cached data");
            android.util.Log.d("DoctorWorkflow", "  - Cached conclusion: " + (finalCachedData.doctorConclusion != null ? finalCachedData.doctorConclusion.substring(0, Math.min(100, finalCachedData.doctorConclusion.length())) : "null"));
            android.util.Log.d("DoctorWorkflow", "  - Cached images: " + (finalCachedData.imageUrls != null ? finalCachedData.imageUrls.size() : 0));
            
            // Set conclusion
            if (finalCachedData.doctorConclusion != null && !finalCachedData.doctorConclusion.trim().isEmpty()) {
                xrayFragment.setData(finalCachedData.doctorConclusion);
                android.util.Log.d("DoctorWorkflow", "✓ Set data for FragmentXray from cache");
            } else {
                android.util.Log.w("DoctorWorkflow", "⚠️ No cached conclusion for FragmentXray");
            }
            
            // Set images - use single post, FragmentXray handles adapter initialization
            if (finalCachedData.imageUrls != null && !finalCachedData.imageUrls.isEmpty()) {
                android.util.Log.d("DoctorWorkflow", "Loading " + finalCachedData.imageUrls.size() + " cached images");
                fragment.getView().post(() -> {
                    xrayFragment.setImageUrls(finalCachedData.imageUrls);
                    android.util.Log.d("DoctorWorkflow", "✓ Set images for FragmentXray from cache");
                });
            } else {
                android.util.Log.w("DoctorWorkflow", "⚠️ No cached images for FragmentXray");
            }
            
            xrayFragment.setReadOnlyMode(true);
            android.util.Log.d("DoctorWorkflow", "✓ Set READ-ONLY mode for FragmentXray");
            
        } else if (fragment instanceof FragmentSurgeryChecklist) {
            FragmentSurgeryChecklist surgeryFragment = (FragmentSurgeryChecklist) fragment;
            if (finalCachedData.doctorConclusion != null && !finalCachedData.doctorConclusion.trim().isEmpty()) {
                surgeryFragment.setData(finalCachedData.doctorConclusion);
                android.util.Log.d("DoctorWorkflow", "✓ Set data for FragmentSurgeryChecklist");
            }
            surgeryFragment.setReadOnlyMode(true);
            android.util.Log.d("DoctorWorkflow", "✓ Set READ-ONLY mode for FragmentSurgeryChecklist");
            
        } else if (fragment instanceof FragmentOrthodontics) {
            FragmentOrthodontics orthoFragment = (FragmentOrthodontics) fragment;
            if (finalCachedData.doctorConclusion != null && !finalCachedData.doctorConclusion.trim().isEmpty()) {
                orthoFragment.setData(finalCachedData.doctorConclusion);
                android.util.Log.d("DoctorWorkflow", "✓ Set data for FragmentOrthodontics");
            }
            orthoFragment.setReadOnlyMode(true);
            android.util.Log.d("DoctorWorkflow", "✓ Set READ-ONLY mode for FragmentOrthodontics");
        }
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
                    
                    updateUIMode(plan.isDraft());
                    stepAdapter.notifyDataSetChanged();
                    updateTotalEstimate();
                    
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
    private TreatmentPlan.Step editingStep; // Bước đang được chỉnh sửa (có thể khác currentStep)
    private boolean editingPreviouslyCompletedStep = false; // Track if editing a step that was already COMPLETED
    
    // Map lưu dữ liệu của TẤT CẢ các bước COMPLETED để auto-load
    private Map<String, StepDataCache> completedStepsDataCache = new HashMap<>();
    
    /**
     * Class lưu cache dữ liệu của bước đã hoàn thành
     */
    private static class StepDataCache {
        String uiTemplateType;
        String doctorConclusion;
        List<String> imageUrls;
        boolean isCompleted;
        
        StepDataCache(String uiTemplateType, String doctorConclusion, List<String> imageUrls, boolean isCompleted) {
            this.uiTemplateType = uiTemplateType;
            this.doctorConclusion = doctorConclusion;
            this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
            this.isCompleted = isCompleted;
        }
    }

    @Override
    public void onStepEdit(TreatmentPlan.Step step) {
        if (isSaving) {
            Toast.makeText(this, "Hệ thống đang đồng bộ, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // CRITICAL: Track if this step was previously COMPLETED
        // This prevents auto-advancing when re-completing an edited step
        editingPreviouslyCompletedStep = "COMPLETED".equals(step.getStatus());
        android.util.Log.d("DoctorWorkflow", "onStepEdit: editingPreviouslyCompletedStep = " + editingPreviouslyCompletedStep);
        
        // CRITICAL: Lưu bước đang được chỉnh sửa (có thể khác currentStep)
        this.editingStep = step;
        android.util.Log.d("DoctorWorkflow", "onStepEdit: editingStep = " + step.getServiceName() + " (ID: " + step.getId() + ")");
        android.util.Log.d("DoctorWorkflow", "onStepEdit: currentStep = " + (currentStep != null ? currentStep.getServiceName() : "null"));
        
        // CRITICAL FIX: If editing a COMPLETED step, call cancelStep API to revert to IN_PROGRESS
        // This allows backend to accept updates (backend checks for IN_PROGRESS steps)
        if ("COMPLETED".equals(step.getStatus())) {
            if (step.getId() == null) {
                Toast.makeText(this, "Lỗi: Bước chưa được lưu vào hệ thống.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Call backend API to reopen the step
            apiService.cancelTreatmentStep(step.getId()).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
                @Override
                public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                    if (response.isSuccessful()) {
                        step.setStatus("IN_PROGRESS");
                        stepAdapter.notifyDataSetChanged();
                        Toast.makeText(DoctorWorkflowActivity.this, "Đang chỉnh sửa lại: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
                        
                        // Continue with edit flow
                        continueStepEdit(step);
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            Toast.makeText(DoctorWorkflowActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(DoctorWorkflowActivity.this, "Không thể chỉnh sửa bước đã hoàn thành", Toast.LENGTH_SHORT).show();
                        }
                        // Reset flag on error
                        editingPreviouslyCompletedStep = false;
                    }
                }

                @Override
                public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    // Reset flag on error
                    editingPreviouslyCompletedStep = false;
                }
            });
            return; // Wait for API response before continuing
        }
        
        // For PENDING or IN_PROGRESS steps, continue normally
        continueStepEdit(step);
    }
    
    /**
     * Continue the edit flow after status is confirmed as IN_PROGRESS
     */
    private void continueStepEdit(TreatmentPlan.Step step) {
        // If the step is PENDING, call the "start" API to move it to IN_PROGRESS
        if ("PENDING".equals(step.getStatus())) {
            if (step.getId() == null) {
                // New step without ID: Sync with server first
                step.setStatus("IN_PROGRESS");
                stepAdapter.notifyDataSetChanged();
                btnCompleteStep.setVisibility(View.VISIBLE);
                saveTreatmentPlanInternal(true, () -> {
                    // Refresh from server to get the actual ID
                    loadTreatmentPlanForRoom(currentTreatmentPlanId);
                });
                Toast.makeText(this, "Đã lưu và bắt đầu thực hiện: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
                return;
            } else {
                // CRITICAL FIX: Check if this is X-ray service - transfer patient first
                boolean isXrayService = step.getServiceName() != null && 
                        (step.getServiceName().toLowerCase().contains("x-quang") || 
                         step.getServiceName().toLowerCase().contains("xquang") ||
                         step.getServiceName().toLowerCase().contains("x quang") ||
                         step.getServiceName().toLowerCase().contains("panorama"));
                
                if (isXrayService && currentPatient != null && currentPatient.getQueueId() != null && currentPatient.getQueueId() > 0) {
                    // Transfer to X-ray room first, then start the step
                    transferPatientToXRay(currentPatient.getQueueId(), step.getServiceName());
                    return; // Don't continue - patient is transferred
                }
                
                apiService.startTreatmentStep(step.getId()).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
                @Override
                public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                    if (response.isSuccessful()) {
                        step.setStatus("IN_PROGRESS");
                        stepAdapter.notifyDataSetChanged();
                        btnCompleteStep.setVisibility(View.VISIBLE);
                        Toast.makeText(DoctorWorkflowActivity.this, "Đã bắt đầu thực hiện: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                    // Silently fail or log, as this is an optimization
                }
                });
            }
        }

        if ("IN_PROGRESS".equals(step.getStatus())) {
            btnCompleteStep.setVisibility(View.VISIBLE);
        } else {
            btnCompleteStep.setVisibility(View.GONE);
        }

        Toast.makeText(this, "Nhập kết luận cho: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
        
        // CRITICAL FIX: Only switch tab if current fragment doesn't match step's template type
        // This prevents destroying user's input when they click "Bắt đầu"
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
        boolean needsSwitch = true;
        
        if (step.getUiTemplateType() != null) {
            String template = step.getUiTemplateType().toUpperCase();
            
            // Check if current fragment matches the step's template
            if (template.contains("SURGERY") && currentFragment instanceof FragmentSurgeryChecklist) {
                needsSwitch = false;
            } else if (template.contains("ORTHO") && currentFragment instanceof FragmentOrthodontics) {
                needsSwitch = false;
            } else if ((template.contains("XRAY") || template.contains("X-RAY") || template.contains("X_RAY")) 
                       && currentFragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
                needsSwitch = false;
            } else if (currentFragment instanceof FragmentGeneralDental && 
                      !template.contains("SURGERY") && !template.contains("ORTHO") && 
                      !template.contains("XRAY") && !template.contains("X-RAY") && !template.contains("X_RAY")) {
                needsSwitch = false;
            }
        } else {
            // Default is general dental
            if (currentFragment instanceof FragmentGeneralDental) {
                needsSwitch = false;
            }
        }
        
        // Only switch if necessary to preserve user input
        if (needsSwitch) {
            switchToTabForStep(step);
        } else {
            // Fragment already matches, just load existing data if step has data
            if (step.getDoctorConclusion() != null && !step.getDoctorConclusion().trim().isEmpty()) {
                // Load data into current fragment
                if (currentFragment instanceof FragmentGeneralDental) {
                    ((FragmentGeneralDental) currentFragment).setData(step.getDoctorConclusion());
                    // Ensure editable mode when editing (status is already IN_PROGRESS)
                    ((FragmentGeneralDental) currentFragment).setReadOnlyMode(false);
                } else if (currentFragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
                    ((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).setData(step.getDoctorConclusion());
                    // Load images
                    if (step.getImages() != null && !step.getImages().isEmpty()) {
                        List<String> imageUrls = new ArrayList<>();
                        for (TreatmentPlan.Step.ImageItem img : step.getImages()) {
                            if (img.getImageUrl() != null) {
                                imageUrls.add(img.getImageUrl());
                            }
                        }
                        ((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).setImageUrls(imageUrls);
                    }
                    // Ensure editable mode when editing (status is already IN_PROGRESS)
                    ((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).setReadOnlyMode(false);
                } else if (currentFragment instanceof FragmentSurgeryChecklist) {
                    ((FragmentSurgeryChecklist) currentFragment).setData(step.getDoctorConclusion());
                    // Ensure editable mode when editing (status is already IN_PROGRESS)
                    ((FragmentSurgeryChecklist) currentFragment).setReadOnlyMode(false);
                } else if (currentFragment instanceof FragmentOrthodontics) {
                    ((FragmentOrthodontics) currentFragment).setData(step.getDoctorConclusion());
                    // Ensure editable mode when editing (status is already IN_PROGRESS)
                    ((FragmentOrthodontics) currentFragment).setReadOnlyMode(false);
                }
            }
        }
        
        // Clear old images first
        currentStepImageUrls.clear();
        
        // Load existing images for this step if available
        if (step.getId() != null) {
            loadStepImages(step.getId());
        } else {
            // No step ID yet, just update preview to show empty state
            updateImagePreview();
        }
        
        // Show complete and cancel buttons based on step status
        if (step.isInProgress() && step.isEditable()) {
            btnCompleteStep.setVisibility(View.VISIBLE);
            btnCancelStep.setVisibility(View.VISIBLE);
        } else {
            btnCompleteStep.setVisibility(View.GONE);
            btnCancelStep.setVisibility(View.GONE);
        }
    }
    
    /**
     * Switch to the appropriate tab based on step's UI template type
     * and load the fragment with existing data
     */
    private void switchToTabForStep(TreatmentPlan.Step step) {
        // Determine which fragment to load based on template type
        Fragment targetFragment = null;
        if (toggleFormType != null && step.getUiTemplateType() != null) {
            String template = step.getUiTemplateType().toUpperCase();
            if (template.contains("SURGERY")) {
                toggleFormType.check(R.id.btnFormSurgery);
                targetFragment = new FragmentSurgeryChecklist();
            } else if (template.contains("ORTHO")) {
                toggleFormType.check(R.id.btnFormOrtho);
                targetFragment = new FragmentOrthodontics();
            } else if (template.contains("XRAY") || template.contains("X-RAY") || template.contains("X_RAY")) {
                toggleFormType.check(R.id.btnFormXray);
                targetFragment = new com.hcmute.mobile_android.ui.fragments.FragmentXray();
            } else {
                toggleFormType.check(R.id.btnFormGeneral);
                targetFragment = new FragmentGeneralDental();
            }
        } else {
            // Default to general dental
            if (toggleFormType != null) {
                toggleFormType.check(R.id.btnFormGeneral);
            }
            targetFragment = new FragmentGeneralDental();
        }
        
        // Load the fragment and populate with existing data
        if (targetFragment != null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerForm, targetFragment)
                .commitNow(); // Use commitNow() to ensure fragment is loaded immediately
            
            // ALWAYS populate the fragment with existing data (even if empty)
            // Use post() to ensure view is fully created
            String existingConclusion = step.getDoctorConclusion();
            // CRITICAL FIX: Check CURRENT status, not isCompleted() which checks original status
            // When editing, status is already changed to IN_PROGRESS, so should be editable
            boolean shouldBeReadOnly = "COMPLETED".equals(step.getStatus());
            final Fragment finalFragment = targetFragment;
            
            findViewById(R.id.fragmentContainerForm).post(() -> {
                // Load data for all fragment types
                if (finalFragment instanceof FragmentGeneralDental) {
                    // Always call setData, even if conclusion is null/empty
                    if (existingConclusion != null && !existingConclusion.trim().isEmpty()) {
                        ((FragmentGeneralDental) finalFragment).setData(existingConclusion);
                    }
                    // Set read-only mode ONLY if step is still COMPLETED (not being edited)
                    if (shouldBeReadOnly) {
                        ((FragmentGeneralDental) finalFragment).setReadOnlyMode(true);
                        // Show edit button
                        android.view.View btnEdit = finalFragment.getView().findViewById(R.id.btnEditMode);
                        if (btnEdit != null) {
                            btnEdit.setVisibility(View.VISIBLE);
                            btnEdit.setOnClickListener(v -> onStepEdit(step));
                        }
                    } else {
                        // Ensure editable mode when editing
                        ((FragmentGeneralDental) finalFragment).setReadOnlyMode(false);
                    }
                } else if (finalFragment instanceof FragmentSurgeryChecklist) {
                    if (existingConclusion != null && !existingConclusion.trim().isEmpty()) {
                        ((FragmentSurgeryChecklist) finalFragment).setData(existingConclusion);
                    }
                    // Set read-only mode ONLY if step is still COMPLETED (not being edited)
                    if (shouldBeReadOnly) {
                        ((FragmentSurgeryChecklist) finalFragment).setReadOnlyMode(true);
                        // Show edit button
                        android.view.View btnEdit = finalFragment.getView().findViewById(R.id.btnEditMode);
                        if (btnEdit != null) {
                            btnEdit.setVisibility(View.VISIBLE);
                            btnEdit.setOnClickListener(v -> onStepEdit(step));
                        }
                    } else {
                        // Ensure editable mode when editing
                        ((FragmentSurgeryChecklist) finalFragment).setReadOnlyMode(false);
                    }
                } else if (finalFragment instanceof FragmentOrthodontics) {
                    if (existingConclusion != null && !existingConclusion.trim().isEmpty()) {
                        ((FragmentOrthodontics) finalFragment).setData(existingConclusion);
                    }
                    // Set read-only mode ONLY if step is still COMPLETED (not being edited)
                    if (shouldBeReadOnly) {
                        ((FragmentOrthodontics) finalFragment).setReadOnlyMode(true);
                        // Show edit button
                        android.view.View btnEdit = finalFragment.getView().findViewById(R.id.btnEditMode);
                        if (btnEdit != null) {
                            btnEdit.setVisibility(View.VISIBLE);
                            btnEdit.setOnClickListener(v -> onStepEdit(step));
                        }
                    } else {
                        // Ensure editable mode when editing
                        ((FragmentOrthodontics) finalFragment).setReadOnlyMode(false);
                    }
                } else if (finalFragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
                    android.util.Log.d("DoctorWorkflow", "Loading data for FragmentXray");
                    android.util.Log.d("DoctorWorkflow", "  - Conclusion: " + (existingConclusion != null ? existingConclusion.substring(0, Math.min(100, existingConclusion.length())) : "null"));
                    android.util.Log.d("DoctorWorkflow", "  - Images: " + (step.getImages() != null ? step.getImages().size() : 0));
                    
                    // Always call setData, even if conclusion is null/empty
                    if (existingConclusion != null && !existingConclusion.trim().isEmpty()) {
                        ((com.hcmute.mobile_android.ui.fragments.FragmentXray) finalFragment).setData(existingConclusion);
                        android.util.Log.d("DoctorWorkflow", "✓ Called setData for FragmentXray");
                    } else {
                        android.util.Log.w("DoctorWorkflow", "⚠️ No conclusion to load for FragmentXray");
                    }
                    
                    // Load images for X-ray fragment
                    if (step.getImages() != null && !step.getImages().isEmpty()) {
                        List<String> imageUrls = new ArrayList<>();
                        for (TreatmentPlan.Step.ImageItem img : step.getImages()) {
                            if (img.getImageUrl() != null) {
                                imageUrls.add(img.getImageUrl());
                                android.util.Log.d("DoctorWorkflow", "  - Image URL: " + img.getImageUrl());
                            }
                        }
                        
                        if (!imageUrls.isEmpty()) {
                            android.util.Log.d("DoctorWorkflow", "Loading " + imageUrls.size() + " images for FragmentXray");
                            final List<String> finalImageUrls = imageUrls;
                            
                            // Use single post - FragmentXray now handles adapter initialization
                            finalFragment.getView().post(() -> {
                                ((com.hcmute.mobile_android.ui.fragments.FragmentXray) finalFragment).setImageUrls(finalImageUrls);
                                android.util.Log.d("DoctorWorkflow", "✓ Called setImageUrls for FragmentXray");
                            });
                        }
                    } else {
                        android.util.Log.w("DoctorWorkflow", "⚠️ No images to load for FragmentXray");
                    }
                    
                    // Set read-only mode ONLY if step is still COMPLETED (not being edited)
                    if (shouldBeReadOnly) {
                        ((com.hcmute.mobile_android.ui.fragments.FragmentXray) finalFragment).setReadOnlyMode(true);
                        android.util.Log.d("DoctorWorkflow", "✓ Set READ-ONLY mode for FragmentXray");
                        // Show edit button
                        android.view.View btnEdit = finalFragment.getView().findViewById(R.id.btnEditMode);
                        if (btnEdit != null) {
                            btnEdit.setVisibility(View.VISIBLE);
                            btnEdit.setOnClickListener(v -> onStepEdit(step));
                            android.util.Log.d("DoctorWorkflow", "✓ Setup Edit button for FragmentXray");
                        }
                    } else {
                        // Ensure editable mode when editing
                        ((com.hcmute.mobile_android.ui.fragments.FragmentXray) finalFragment).setReadOnlyMode(false);
                        android.util.Log.d("DoctorWorkflow", "✓ Set EDITABLE mode for FragmentXray");
                    }
                }
                
                // Load images if this step has any
                if (step.getId() != null && step.getImages() != null && !step.getImages().isEmpty()) {
                    loadStepImages(step.getId());
                }
            });
        }
    }

    private void updateUIMode(boolean isDraft) {
        stepAdapter.setDraftMode(isDraft);
        if (isDraft) {
            btnActivatePlan.setVisibility(View.VISIBLE);
        } else {
            btnActivatePlan.setVisibility(View.GONE);
        }
        // Note: btnSavePlan removed - auto-save handles everything
    }

    private void activatePlan() {
        if (currentTreatmentPlanId == null) return;
        
        btnActivatePlan.setEnabled(false);
        apiService.activatePlan(currentTreatmentPlanId).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                btnActivatePlan.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(DoctorWorkflowActivity.this, "Đã kích hoạt phác đồ", Toast.LENGTH_SHORT).show();
                    // Tiện tay reload lại từ API for-room để có data mới nhất theo room
                    loadTreatmentPlanForRoom(currentTreatmentPlanId);
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kích hoạt", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                btnActivatePlan.setEnabled(true);
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTreatmentPlanForRoom(Long planId) {
        apiService.getTreatmentPlanForRoom(planId).enqueue(new Callback<TreatmentPlan>() {
            @Override
            public void onResponse(Call<TreatmentPlan> call, Response<TreatmentPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TreatmentPlan plan = response.body();
                    
                    android.util.Log.d("DoctorWorkflow", "=== loadTreatmentPlanForRoom ===");
                    android.util.Log.d("DoctorWorkflow", "Received " + plan.getSteps().size() + " steps from backend:");
                    for (TreatmentPlan.Step step : plan.getSteps()) {
                        android.util.Log.d("DoctorWorkflow", "  - Step ID=" + step.getId() + 
                            ", Service=" + step.getServiceName() + 
                            ", Status=" + step.getStatus());
                    }
                    
                    // CRITICAL FIX: Save currentStep ID before clearing list
                    Long currentStepId = (currentStep != null) ? currentStep.getId() : null;
                    android.util.Log.d("DoctorWorkflow", "loadTreatmentPlanForRoom: currentStepId = " + currentStepId);
                    
                    // PROFESSIONAL FIX: Chỉ reset IN_PROGRESS steps không hợp lệ
                    // Logic: Chỉ được có TỐI ĐA 1 step IN_PROGRESS trong treatment plan
                    // Nếu có nhiều hơn 1 → reset các step không phải currentStep
                    int inProgressCount = 0;
                    TreatmentPlan.Step firstInProgressStep = null;
                    for (TreatmentPlan.Step step : plan.getSteps()) {
                        if ("IN_PROGRESS".equals(step.getStatus())) {
                            inProgressCount++;
                            if (firstInProgressStep == null) {
                                firstInProgressStep = step;
                            }
                        }
                    }
                    
                    android.util.Log.d("DoctorWorkflow", "Found " + inProgressCount + " IN_PROGRESS steps");
                    
                    // Nếu có nhiều hơn 1 IN_PROGRESS step → reset các step không phải currentStep
                    if (inProgressCount > 1) {
                        android.util.Log.w("DoctorWorkflow", "⚠️ Multiple IN_PROGRESS steps detected - resetting invalid ones");
                        for (TreatmentPlan.Step step : plan.getSteps()) {
                            if ("IN_PROGRESS".equals(step.getStatus())) {
                                // Giữ step đang edit, reset các step khác
                                if (currentStepId == null || !currentStepId.equals(step.getId())) {
                                    android.util.Log.w("DoctorWorkflow", "⚠️ RESET: Step " + step.getId() + 
                                        " (" + step.getServiceName() + ") → PENDING");
                                    step.setStatus("PENDING");
                                }
                            }
                        }
                    }
                    // Nếu chỉ có 1 IN_PROGRESS step → giữ nguyên (đây là step hợp lệ đang chờ xử lý)
                    else if (inProgressCount == 1 && firstInProgressStep != null) {
                        android.util.Log.d("DoctorWorkflow", "✓ Single IN_PROGRESS step is valid: " + 
                            firstInProgressStep.getServiceName());
                    }
                    
                    treatmentSteps.clear();
                    treatmentSteps.addAll(plan.getSteps());
                    
                    // CRITICAL FIX: Restore currentStep by ID, not by index
                    if (currentStepId != null) {
                        currentStep = null; // Reset first
                        for (TreatmentPlan.Step s : treatmentSteps) {
                            if (currentStepId.equals(s.getId())) {
                                currentStep = s;
                                android.util.Log.d("DoctorWorkflow", "✓ Restored currentStep by ID: " + s.getServiceName() + " (Status: " + s.getStatus() + ")");
                                break;
                            }
                        }
                        
                        if (currentStep == null) {
                            android.util.Log.w("DoctorWorkflow", "⚠️ Could not restore currentStep by ID " + currentStepId);
                        }
                    } else {
                        android.util.Log.d("DoctorWorkflow", "No currentStep to restore");
                    }

                    updateUIMode(plan.isDraft());
                    stepAdapter.notifyDataSetChanged();
                    updateTotalEstimate();
                    
                    // AUTO-LOAD: Tự động load dữ liệu của TẤT CẢ bước COMPLETED
                    autoLoadInProgressStep();
                    
                    // AUTO-SELECT: Tự động chọn tab đầu tiên để fragment được tạo
                    // CRITICAL FIX: Uncheck all buttons first, then select to ensure fragment creation
                    if (toggleFormType != null) {
                        toggleFormType.post(() -> {
                            // Uncheck all buttons first
                            toggleFormType.clearChecked();
                            
                            // Then select the first tab after a short delay
                            toggleFormType.postDelayed(() -> {
                                toggleFormType.check(R.id.btnFormGeneral);
                                android.util.Log.d("DoctorWorkflow", "✓ Auto-selected btnFormGeneral tab");
                            }, 100);
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<TreatmentPlan> call, Throwable t) {
                // Ignore
            }
        });
    }

    @Override
    public void onStepRemove(TreatmentPlan.Step step) {
        showDeleteStepDialog(step);
    }
    
    private void showDeleteStepDialog(TreatmentPlan.Step step) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa bước \"" + step.getServiceName() + "\" không?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                treatmentSteps.remove(step);
                stepAdapter.notifyDataSetChanged();
                updateTotalEstimate();
                
                // Save changes
                saveTreatmentPlanInternal(true);
                
                Toast.makeText(this, "Đã xóa: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showAddServiceDialog(Integer toothNumber) {
        if (currentPatient == null) {
            Toast.makeText(this, "Vui lòng tra cứu bệnh nhân trước", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getServices().enqueue(new Callback<List<com.hcmute.mobile_android.network.models.ServiceItem>>() {
            @Override
            public void onResponse(Call<List<com.hcmute.mobile_android.network.models.ServiceItem>> call, Response<List<com.hcmute.mobile_android.network.models.ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.hcmute.mobile_android.network.models.ServiceItem> services = response.body();
                    String[] serviceNames = new String[services.size()];
                    for (int i = 0; i < services.size(); i++) {
                        serviceNames[i] = services.get(i).getName() + " (" + String.format("%,.0f", services.get(i).getPrice()) + "đ)";
                    }

                    String title = toothNumber != null ? "Chỉ định dịch vụ cho Răng " + toothNumber : "Chọn dịch vụ phát sinh";

                    new androidx.appcompat.app.AlertDialog.Builder(DoctorWorkflowActivity.this)
                        .setTitle(title)
                        .setItems(serviceNames, (dialog, which) -> {
                            com.hcmute.mobile_android.network.models.ServiceItem selectedSvc = services.get(which);
                            addServiceAsStep(selectedSvc, toothNumber);
                        })
                        .show();
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, "Không tài được danh sách dịch vụ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<com.hcmute.mobile_android.network.models.ServiceItem>> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addServiceAsStep(com.hcmute.mobile_android.network.models.ServiceItem svc, Integer toothNumber) {
        TreatmentPlan.Step newStep = new TreatmentPlan.Step();
        newStep.setServiceId(svc.getId());
        newStep.setServiceName(svc.getName());
        newStep.setDescription(svc.getDescription());
        newStep.setEstimatedPrice(svc.getPrice());
        newStep.setActualPrice(svc.getPrice());
        newStep.setStatus("PENDING");
        newStep.setUiTemplateType(svc.getUiTemplateType());
        newStep.setEditable(true);
        newStep.setToothNumber(String.valueOf(toothNumber));

        treatmentSteps.add(newStep);
        stepAdapter.notifyDataSetChanged();
        updateTotalEstimate();
        
        String msg = toothNumber != null ? "Đã chỉ định " + svc.getName() + " cho Răng " + toothNumber 
                                         : "Đã thêm dịch vụ: " + svc.getName();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        
        // CRITICAL FIX: Auto-transfer to X-ray room when adding X-ray service
        boolean isXrayService = svc.getName() != null && 
                (svc.getName().toLowerCase().contains("x-quang") || 
                 svc.getName().toLowerCase().contains("xquang") ||
                 svc.getName().toLowerCase().contains("x quang") ||
                 svc.getName().toLowerCase().contains("panorama"));
        
        // Auto-save immediately so the step gets an ID from backend
        saveTreatmentPlanInternal(true, () -> {
            // Reload to get the step IDs from backend
            loadTreatmentPlanForRoom(currentTreatmentPlanId);
            
            // If X-ray service, transfer patient to X-ray room
            if (isXrayService && currentPatient != null && currentPatient.getQueueId() != null && currentPatient.getQueueId() > 0) {
                transferPatientToXRay(currentPatient.getQueueId(), svc.getName());
            }
        });
    }
    
    /**
     * Transfer patient to X-ray room
     */
    private void transferPatientToXRay(Long queueId, String serviceName) {
        // Call API to transfer patient to X-ray room
        apiService.transferToXRay(queueId, new java.util.HashMap<>()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    new androidx.appcompat.app.AlertDialog.Builder(DoctorWorkflowActivity.this)
                        .setTitle("Chuyển phòng X-quang")
                        .setMessage("Bệnh nhân đã được chuyển sang phòng X-quang để thực hiện dịch vụ: " + serviceName + 
                                  "\n\nHệ thống sẽ tự động đưa bệnh nhân trở lại sau khi hoàn thành.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Close this activity - patient is now in X-ray room
                            finish();
                        })
                        .setCancelable(false)
                        .show();
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi chuyển phòng X-quang", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối khi chuyển phòng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStepSave(TreatmentPlan.Step step) {
        android.util.Log.d("DoctorWorkflow", "onStepSave called for step: " + step.getServiceName() + " (ID: " + step.getId() + ")");
        android.util.Log.d("DoctorWorkflow", "  - editingStep: " + (editingStep != null ? editingStep.getServiceName() : "null"));
        android.util.Log.d("DoctorWorkflow", "  - currentStep: " + (currentStep != null ? currentStep.getServiceName() : "null"));
        
        // CRITICAL: Lưu currentStep ban đầu (bước đang trong quy trình)
        // Sau khi lưu editingStep, chúng ta sẽ restore lại currentStep
        final Long originalCurrentStepId = (currentStep != null) ? currentStep.getId() : null;
        final Long editingStepId = step.getId();
        
        // Save changes when editing a COMPLETED step
        saveTreatmentPlanInternal(false, () -> {
            Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
            
            // Reload to refresh UI
            loadTreatmentPlanForRoom(currentTreatmentPlanId);
            
            // CRITICAL FIX: Restore currentStep to the ORIGINAL step (not the edited step)
            // This ensures the workflow continues with the correct step
            if (originalCurrentStepId != null && !originalCurrentStepId.equals(editingStepId)) {
                // Editing a different step than currentStep
                // Restore currentStep to the original workflow step
                for (TreatmentPlan.Step s : treatmentSteps) {
                    if (originalCurrentStepId.equals(s.getId())) {
                        currentStep = s;
                        android.util.Log.d("DoctorWorkflow", "✓ Restored currentStep to original workflow step: " + s.getServiceName());
                        break;
                    }
                }
            } else if (editingStepId != null) {
                // Editing the same step as currentStep, or no original currentStep
                // Keep currentStep as the edited step
                for (TreatmentPlan.Step s : treatmentSteps) {
                    if (editingStepId.equals(s.getId())) {
                        currentStep = s;
                        android.util.Log.d("DoctorWorkflow", "✓ Kept currentStep as edited step: " + s.getServiceName());
                        break;
                    }
                }
            }
            
            // Clear editingStep
            editingStep = null;
        });
    }
    
    @Override
    public void onStepComplete(TreatmentPlan.Step step) {
        android.util.Log.d("DoctorWorkflow", "=== onStepComplete ===");
        android.util.Log.d("DoctorWorkflow", "Step: " + step.getServiceName() + " (ID: " + step.getId() + ")");
        android.util.Log.d("DoctorWorkflow", "editingPreviouslyCompletedStep: " + editingPreviouslyCompletedStep);
        
        // CRITICAL FIX: Get data from CURRENT fragment BEFORE switching tabs
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
        String currentData = "";
        List<String> currentImages = new ArrayList<>();
        
        // Extract data from current fragment
        if (currentFragment instanceof FragmentGeneralDental) {
            currentData = ((FragmentGeneralDental) currentFragment).getFormDataNotes();
        } else if (currentFragment instanceof FragmentSurgeryChecklist) {
            currentData = ((FragmentSurgeryChecklist) currentFragment).getFormDataNotes();
        } else if (currentFragment instanceof FragmentOrthodontics) {
            currentData = ((FragmentOrthodontics) currentFragment).getFormDataNotes();
        } else if (currentFragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
            // Validate BEFORE extracting data
            if (!((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).validateForm()) {
                return; // Validation failed, don't proceed
            }
            currentData = ((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).getFormDataNotes();
            currentImages = ((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).getImageUrls();
        }
        
        // Validate other fragment types
        if (currentFragment instanceof FragmentSurgeryChecklist) {
            if (!((FragmentSurgeryChecklist) currentFragment).validateForm()) {
                return;
            }
        } else if (currentFragment instanceof FragmentOrthodontics) {
            if (!((FragmentOrthodontics) currentFragment).validateForm()) {
                return;
            }
        }
        
        // Store data in step BEFORE any operations
        step.setDoctorConclusion(currentData);
        this.currentStep = step;
        
        // Check if step has ID
        if (step.getId() == null) {
            Toast.makeText(this, "Lỗi: Bước chưa được lưu vào hệ thống. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Save the data we just extracted
        final String finalData = currentData;
        final List<String> finalImages = new ArrayList<>(currentImages);
        
        // CRITICAL FIX: If this step was previously COMPLETED (being re-edited),
        // just save the data WITHOUT calling /complete API to avoid auto-advancing
        if (editingPreviouslyCompletedStep) {
            android.util.Log.d("DoctorWorkflow", "✓ Re-completing previously COMPLETED step - saving without auto-advance");
            
            // PROFESSIONAL FIX: Set status to COMPLETED BEFORE saving
            step.setStatus("COMPLETED");
            step.setDoctorConclusion(finalData);
            
            // Save data with COMPLETED status
            saveTreatmentPlanInternal(true, () -> {
                // Update UI
                stepAdapter.notifyDataSetChanged();
                
                // Hide buttons
                btnCompleteStep.setVisibility(View.GONE);
                btnCancelStep.setVisibility(View.GONE);
                currentStep = null;
                
                // Clear flag
                editingPreviouslyCompletedStep = false;
                editingStep = null;
                
                Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                
                // Reload to get fresh data from server
                loadTreatmentPlanForRoom(currentTreatmentPlanId);
            });
        } else {
            android.util.Log.d("DoctorWorkflow", "✓ Completing step for FIRST time - calling /complete API");
            
            // Normal flow: Save then call /complete API (which may auto-advance)
            saveTreatmentPlanInternal(true, () -> {
                // After save, complete the step with the data we extracted
                completeStepWithData(step, finalData, finalImages);
            });
        }
    }
    
    private void completeStepWithData(TreatmentPlan.Step step, String doctorConclusion, List<String> imageUrls) {
        Map<String, Object> body = new HashMap<>();
        body.put("doctorConclusion", doctorConclusion);
        body.put("imageUrls", imageUrls);
        
        apiService.completeTreatmentStep(step.getId(), body).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String msg = response.body().getMessage();
                    String nextRoom = response.body().getNextRoomName();
                    
                    // Update local step status immediately
                    step.setStatus("COMPLETED");
                    step.setDoctorConclusion(doctorConclusion);
                    stepAdapter.notifyDataSetChanged();
                    btnCompleteStep.setVisibility(View.GONE);
                    btnCancelStep.setVisibility(View.GONE);
                    currentStep = null;
                    
                    if (nextRoom != null) {
                        // Reload data first, then show dialog
                        loadTreatmentPlanForRoom(currentTreatmentPlanId);
                        
                        new androidx.appcompat.app.AlertDialog.Builder(DoctorWorkflowActivity.this)
                            .setTitle("Chuyển phòng")
                            .setMessage("Bệnh nhân cần được chuyển sang " + nextRoom + " để tiếp tục.\nHệ thống đã tự động đẩy hồ sơ.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                finish(); // Done for this doctor
                            })
                            .setCancelable(false)
                            .show();
                    } else {
                        Toast.makeText(DoctorWorkflowActivity.this, "Hoàn tất bước khám", Toast.LENGTH_SHORT).show();
                        loadTreatmentPlanForRoom(currentTreatmentPlanId); // reload step states
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(DoctorWorkflowActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {}
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onStepCancel(TreatmentPlan.Step step) {
        if (step.getId() == null) {
            Toast.makeText(this, "Lỗi: Bước chưa được lưu vào hệ thống.", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy")
            .setMessage("Bạn có chắc muốn hủy bước này và đặt lại về trạng thái chờ?")
            .setPositiveButton("Hủy bước", (dialog, which) -> {
                apiService.cancelTreatmentStep(step.getId()).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
                    @Override
                    public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                        if (response.isSuccessful()) {
                            // Update local step status
                            step.setStatus("PENDING");
                            step.setDoctorConclusion(null);
                            stepAdapter.notifyDataSetChanged();
                            btnCompleteStep.setVisibility(View.GONE);
                            btnCancelStep.setVisibility(View.GONE);
                            currentStep = null;
                            
                            Toast.makeText(DoctorWorkflowActivity.this, "Đã hủy bước khám", Toast.LENGTH_SHORT).show();
                            loadTreatmentPlanForRoom(currentTreatmentPlanId);
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi hủy bước", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                        Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Không", null)
            .show();
    }

    @Override
    public void onToothSelected(int toothNumber) {
        if (currentStep != null) {
            currentStep.setToothNumber(String.valueOf(toothNumber));
            stepAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã chọn răng " + toothNumber + " cho bước " + currentStep.getServiceName(), Toast.LENGTH_SHORT).show();
            
            // Also update the UI fragment if it's the General Dental one
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
            if (fragment instanceof FragmentGeneralDental) {
                ((FragmentGeneralDental) fragment).onToothSelected(toothNumber);
            }
        } else {
            // Hiển thị dialog thêm dịch vụ trực tiếp cho răng này nếu chưa chọn step nào
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chỉ định dịch vụ")
                .setMessage("Bạn có muốn chỉ định một dịch vụ/phát sinh mới cho Răng " + toothNumber + " không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    showAddServiceDialog(toothNumber);
                })
                .setNegativeButton("Không", null)
                .show();
        }
    }





    private void loadLastPlanForPatient(Long patientId) {
        apiService.getTreatmentPlansByPatient(patientId).enqueue(new Callback<List<TreatmentPlan>>() {
            @Override
            public void onResponse(Call<List<TreatmentPlan>> call, Response<List<TreatmentPlan>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<TreatmentPlan> plans = response.body();
                    // Get the most recent one
                    TreatmentPlan lastPlan = plans.get(plans.size() - 1);
                    currentTreatmentPlanId = lastPlan.getId();
                    
                    // Call the room-specific loader to get correct editable flags
                    loadTreatmentPlanForRoom(currentTreatmentPlanId);
                }
            }

            @Override
            public void onFailure(Call<List<TreatmentPlan>> call, Throwable t) {
                // Ignore
            }
        });
    }

    private void createBlankPlanAndSave() {
        createBlankPlanAndSave(false, null);
    }
    
    private void createBlankPlanAndSave(boolean silent, Runnable onDone) {
        if (currentPatient == null) return;
        
        Map<String, Long> body = new HashMap<>();
        body.put("patientId", currentPatient.getId());
        
        apiService.createTreatmentPlanFromTemplate(new com.hcmute.mobile_android.network.models.CreateTreatmentPlanRequest(null, currentPatient.getId())).enqueue(new Callback<TreatmentPlan>() {
            @Override
            public void onResponse(Call<TreatmentPlan> call, Response<TreatmentPlan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentTreatmentPlanId = response.body().getId();
                    // Try saving again now that we have an ID
                    saveTreatmentPlanInternal(silent, onDone);
                } else {
                    if (!silent) {
                        Toast.makeText(DoctorWorkflowActivity.this, "Lỗi tạo phác đồ", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<TreatmentPlan> call, Throwable t) {
                if (!silent) {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateTotalEstimate() {
        // Filter steps that should be counted
        List<TreatmentPlan.Step> countedSteps = new ArrayList<>();
        double total = 0;
        
        for (TreatmentPlan.Step step : treatmentSteps) {
            String status = step.getStatus() != null ? step.getStatus().toUpperCase() : "PENDING";
            if (!status.equals("CANCELLED") && !status.equals("SKIPPED")) {
                Double price = step.getEstimatedPrice();
                if (price != null) {
                    total += price;
                    countedSteps.add(step);
                }
            }
        }
        
        // Update total text
        if (tvTotalEstimate != null) {
            tvTotalEstimate.setText(String.format("%,.0f VNĐ", total));
        }
        
        // Update price breakdown list
        if (priceBreakdownAdapter != null) {
            priceBreakdownAdapter.updateSteps(countedSteps);
        }
    }
    
    private void completeAndGenerateInvoice() {
        // Show loading
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Đang tạo hóa đơn...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        apiService.completeAndGenerateInvoice(currentTreatmentPlanId).enqueue(new Callback<com.hcmute.mobile_android.network.models.Invoice>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.Invoice> call, Response<com.hcmute.mobile_android.network.models.Invoice> response) {
                progressDialog.dismiss();
                
                if (response.isSuccessful() && response.body() != null) {
                    com.hcmute.mobile_android.network.models.Invoice invoice = response.body();
                    
                    // Show success message
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Đã tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to payment activity
                    Intent intent = new Intent(DoctorWorkflowActivity.this, PaymentActivity.class);
                    intent.putExtra("invoiceId", invoice.getId());
                    intent.putExtra("amount", invoice.getTotalAmount().doubleValue());
                    intent.putExtra("PATIENT_NAME", currentPatient.getFullName());
                    startActivity(intent);
                    
                    // Close this activity
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(DoctorWorkflowActivity.this, 
                            "Lỗi: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(DoctorWorkflowActivity.this, 
                            "Lỗi tạo hóa đơn: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.Invoice> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(DoctorWorkflowActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
      