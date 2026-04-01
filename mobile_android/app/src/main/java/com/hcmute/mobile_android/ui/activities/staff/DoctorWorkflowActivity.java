package com.hcmute.mobile_android.ui.activities.staff;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.hcmute.mobile_android.network.models.AddMultipleTeethServiceRequest;
import com.hcmute.mobile_android.network.models.MultipleTeethServiceResponse;
import com.hcmute.mobile_android.network.models.PatientInfo;
import com.hcmute.mobile_android.network.models.TreatmentPlan;
import com.hcmute.mobile_android.network.models.TreatmentTemplate;
import com.hcmute.mobile_android.ui.activities.PatientQRScannerActivity;
import com.hcmute.mobile_android.ui.activities.PaymentActivity;
import com.hcmute.mobile_android.ui.activities.staff.PrescriptionActivity;
import com.hcmute.mobile_android.ui.fragments.BottomSheetMedicalHistory;
import com.hcmute.mobile_android.ui.fragments.FragmentCrownService;
import com.hcmute.mobile_android.ui.fragments.FragmentGeneralDental;
import com.hcmute.mobile_android.ui.fragments.FragmentSurgeryChecklist;
import com.hcmute.mobile_android.ui.fragments.FragmentOrthodontics;
import com.hcmute.mobile_android.ui.fragments.FragmentXray;
import com.hcmute.mobile_android.ui.fragments.FragmentBasicService;
import com.hcmute.mobile_android.network.models.CreateTreatmentPlanRequest;
import com.google.android.material.button.MaterialButtonToggleGroup;
import androidx.fragment.app.Fragment;
import com.hcmute.mobile_android.util.TokenManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorWorkflowActivity extends AppCompatActivity implements 
        TreatmentTemplateAdapter.OnTemplateSelectedListener,
        TreatmentStepAdapter.OnStepActionListener {
    
    public static final String EXTRA_INITIAL_QR = "EXTRA_INITIAL_QR";
    private static final int REQUEST_PATIENT_DETAIL = 1001;
    private static final int REQUEST_PRESCRIPTION = 1002;

    // Views
    private EditText etQrInput;
    private TextView tvPatientHeader, tvDoctorGreeting, tvTotalEstimate;
    private ImageButton btnScanQr;
    private MaterialButton btnActivatePlan, btnSelectTemplate, btnAddService, btnPrescribe, btnPrintPlan, btnLookup, btnCompleteTreatment;

    private MaterialCardView cardLookup, cardTreatmentPlan, cardOdontogram;
    private com.google.android.material.textfield.TextInputEditText etDoctorConclusion;
    private View btnViewHistory;
    private View layoutExamination;
    private View fragmentContainerView;
    private RecyclerView rvTemplates, rvTreatmentSteps, rvPriceBreakdown;
    private com.google.android.material.button.MaterialButtonToggleGroup toggleFormType;
    private RecyclerView rvResultImages;
    
    // Adapters
    private TreatmentTemplateAdapter templateAdapter;
    private TreatmentStepAdapter stepAdapter;
    private PriceBreakdownAdapter priceBreakdownAdapter;
    private ImagePreviewAdapter resultImageAdapter;
    
    // Data
    private List<TreatmentTemplate> templateList = new ArrayList<>();
    private List<TreatmentPlan.Step> treatmentSteps = new ArrayList<>();
    private PatientInfo currentPatient;
    private Long currentTreatmentPlanId;
    // Current room context – passed from QueueManagementActivity to detect X-Ray room
    private String currentRoomName = "";
    private com.hcmute.mobile_android.network.models.ServiceItem pendingServiceToAdd;
    
    private ActivityResultLauncher<Intent> qrScannerLauncher;
    
    private ApiService apiService;

    // Image upload
    private ActivityResultLauncher<String> imagePickerLauncher;
    private List<String> currentStepImageUrls = new ArrayList<>();

    private boolean isSaving = false;
    private boolean isCompletingTreatment = false; // Flag to prevent onPause auto-save during completion
    private android.app.ProgressDialog completionProgressDialog = null; // Track dialog for cleanup on save failure

    @Override
    protected void onPause() {
        super.onPause();
        // Auto-save when leaving the form, but NOT if we're in the middle of completing treatment
        // (completing treatment already saves before locking the plan)
        if (currentTreatmentPlanId != null && currentPatient != null && !isCompletingTreatment) {
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
        } else if (fragment instanceof FragmentCrownService) {
            finalNotes = ((FragmentCrownService) fragment).getFormDataNotes();
        } else if (fragment instanceof FragmentOrthodontics) {
            finalNotes = ((FragmentOrthodontics) fragment).getFormDataNotes();
        } else if (fragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
            finalNotes = ((com.hcmute.mobile_android.ui.fragments.FragmentXray) fragment).getFormDataNotes();
        }

        if (currentStep != null && !finalNotes.trim().isEmpty()) {
            currentStep.setDoctorConclusion(finalNotes);
        }

        // Synchronize images from fragment or activity
        List<String> imageUrlsToSave = new java.util.ArrayList<>();
        if (fragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
            imageUrlsToSave.addAll(((com.hcmute.mobile_android.ui.fragments.FragmentXray) fragment).getImageUrls());
        } else {
            imageUrlsToSave.addAll(currentStepImageUrls);
        }

        if (currentStep != null) {
            currentStep.setImageUrls(new java.util.ArrayList<>(imageUrlsToSave));
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
            
            // Include image URLs in the update request
            if (s.getImageUrls() != null) {
                item.setImageUrls(new java.util.ArrayList<>(s.getImageUrls()));
            }
            
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
                
                if (response.isSuccessful()) {
                    // Refresh adapter to show "View Photos" button if images were uploaded
                    if (stepAdapter != null) {
                        stepAdapter.notifyDataSetChanged();
                    }
                    
                    if (!silent) {
                        Toast.makeText(DoctorWorkflowActivity.this, "Đã tự động lưu hồ sơ bệnh án!", Toast.LENGTH_SHORT).show();
                        if (onDone != null) onDone.run();
                    } else {
                        // Silent mode - still call onDone to notify caller of result
                        if (onDone != null) onDone.run();
                    }
                } else {
                    if (!silent) {
                        Toast.makeText(DoctorWorkflowActivity.this, "Lỗi lưu phác đồ: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    // Reset completion state if we're in the middle of completing
                    if (isCompletingTreatment) {
                        isCompletingTreatment = false;
                        if (completionProgressDialog != null && completionProgressDialog.isShowing()) {
                            completionProgressDialog.dismiss();
                            completionProgressDialog = null;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isSaving = false;
                if (!silent) {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                } else if (onDone != null) {
                    // Show error even in silent mode if there's a callback waiting
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối khi lưu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    // Reset completion state if we're in the middle of completing
                    if (isCompletingTreatment) {
                        isCompletingTreatment = false;
                        if (completionProgressDialog != null && completionProgressDialog.isShowing()) {
                            completionProgressDialog.dismiss();
                            completionProgressDialog = null;
                        }
                    }
                }
                // Don't call onDone on failure
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
        
        // Read room context to detect X-Ray room and prevent transfer loops
        String roomNameExtra = getIntent().getStringExtra("EXTRA_ROOM_NAME");
        if (roomNameExtra != null) {
            currentRoomName = roomNameExtra;
            android.util.Log.d("DoctorWorkflow", "Current room: " + currentRoomName);
        }

        // Handle initial QR from Intent
        String initialQr = getIntent().getStringExtra(EXTRA_INITIAL_QR);
        if (initialQr != null && !initialQr.isEmpty()) {
            etQrInput.setText(initialQr);
            lookupPatient();
        }
        
        // Check if coming back from PatientDetailActivity
        PatientInfo patientFromDetail = (PatientInfo) getIntent().getSerializableExtra("patient_info");
        boolean fromDetail = getIntent().getBooleanExtra("from_detail", false);
        if (fromDetail && patientFromDetail != null) {
            currentPatient = patientFromDetail;
            displayPatientInfo(currentPatient);
            loadLastPlanForPatient(currentPatient.getId());
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_PATIENT_DETAIL && resultCode == RESULT_OK) {
            // Patient detail screen closed, refresh patient info to ensure queueId is still valid
            if (currentPatient != null && currentPatient.getId() != null) {
                // Refresh patient info from server
                String qrCode = "patient:" + currentPatient.getId();
                apiService.lookupPatientByQR(qrCode).enqueue(new Callback<PatientInfo>() {
                    @Override
                    public void onResponse(Call<PatientInfo> call, Response<PatientInfo> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            currentPatient = response.body();
                            displayPatientInfo(currentPatient);
                            
                            // Reload treatment plan if exists
                            if (currentPatient.getHasTreatmentPlan() != null && currentPatient.getHasTreatmentPlan()) {
                                loadExistingTreatmentPlan(currentPatient.getTreatmentPlanId());
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<PatientInfo> call, Throwable t) {
                        // If refresh fails, keep current patient data
                        android.util.Log.w("DoctorWorkflow", "Failed to refresh patient info: " + t.getMessage());
                    }
                });
            }
        } else if (requestCode == REQUEST_PRESCRIPTION && resultCode == RESULT_OK) {
            // Reload steps to reflect updated actualPrice from kê đơn
            if (currentTreatmentPlanId != null) {
                loadTreatmentPlanForRoom(currentTreatmentPlanId);
            }
        }
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
        
        // Skip Patient Button
        MaterialButton btnSkipPatient = findViewById(R.id.btnSkipPatient);
        btnSkipPatient.setOnClickListener(v -> handleSkipPatient());
        
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
        fragmentContainerView = findViewById(R.id.fragmentContainerForm);
        cardOdontogram = findViewById(R.id.cardOdontogram);
        toggleFormType = findViewById(R.id.toggleFormType);
        rvResultImages = findViewById(R.id.rvResultImages);
        
        // Disable các tab chuyên biệt ban đầu – chỉ mở khóa khi có dịch vụ tương ứng 'Bắt đầu'
        setSpecialTabsEnabled(false);
        
        toggleFormType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnFormGeneral) {
                    // Tab Tổng quát: Phác đồ điều trị
                    if (fragmentContainerView != null) fragmentContainerView.setVisibility(View.GONE);
                    // Sơ đồ răng CHỈ hiện ra nếu đang trong quá trình Thêm dịch vụ
                    if (cardOdontogram != null) cardOdontogram.setVisibility(pendingServiceToAdd != null ? View.VISIBLE : View.GONE);
                } else if (checkedId == R.id.btnFormCrown) {
                    // Tab Bọc sứ: hiện fragment form và odontogram
                    if (fragmentContainerView != null) fragmentContainerView.setVisibility(View.VISIBLE);
                    // ALWAYS show odontogram for Crown tab
                    if (cardOdontogram != null) cardOdontogram.setVisibility(View.VISIBLE);
                    
                    Fragment fragment = new FragmentCrownService();
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerForm, fragment)
                        .commit();
                    
                    final Fragment finalFragment = fragment;
                    fragmentContainerView.postDelayed(() -> {
                        autoPopulateFragmentFromCache(finalFragment, "CROWN");
                    }, 100);
                } else {
                    // Tab Tiểu phẫu, Niềng, X-quang: hiện fragment form
                    if (fragmentContainerView != null) fragmentContainerView.setVisibility(View.VISIBLE);
                    // Sơ đồ răng CHỈ hiện ra nếu đang trong quá trình Thêm dịch vụ
                    if (cardOdontogram != null) cardOdontogram.setVisibility(pendingServiceToAdd != null ? View.VISIBLE : View.GONE);
                    
                    Fragment fragment = null;
                    String templateKey = null;
                    
                    if (checkedId == R.id.btnFormSurgery) {
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
                        fragmentContainerView.postDelayed(() -> {
                            autoPopulateFragmentFromCache(finalFragment, finalTemplateKey);
                        }, 100);
                    }
                }
            }
        });
        
        // Treatment Plan
        cardTreatmentPlan = findViewById(R.id.cardTreatmentPlan);
        rvTemplates = findViewById(R.id.rvTemplates);
        rvTreatmentSteps = findViewById(R.id.rvTreatmentSteps);
        rvPriceBreakdown = findViewById(R.id.rvPriceBreakdown);

        // Odontogram View (Integrated Tooth Service Selection)
        com.hcmute.mobile_android.ui.views.OdontogramView odontogramView = findViewById(R.id.odontogramView);
        if (odontogramView != null) {
            odontogramView.setOnToothServiceListener(new com.hcmute.mobile_android.ui.views.OdontogramView.OnToothServiceListener() {
                @Override
                public void onToothSelected(int toothNumber, String serviceName) {
                    // Called when service is selected for a tooth
                    // Update odontogram display
                    odontogramView.addServiceToTooth(toothNumber, serviceName);
                }
                
                @Override
                public void onToothClicked(int toothNumber) {
                    // Check if we're in Crown tab
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
                    if (currentFragment instanceof FragmentCrownService) {
                        // Crown tab: allow tooth selection
                        odontogramView.toggleSelection(toothNumber);
                        ((FragmentCrownService) currentFragment).onToothClicked(toothNumber);
                    } else {
                        // Original behavior: show dialog to select service for this tooth
                        showToothServiceSelectionDialog(toothNumber, odontogramView);
                    }
                }
            });
        }
        
        // Buttons
        btnActivatePlan = findViewById(R.id.btnActivatePlan);
        btnSelectTemplate = findViewById(R.id.btnSelectTemplate);
        btnAddService = findViewById(R.id.btnAddService);
        btnPrescribe = findViewById(R.id.btnPrescribe);
        btnPrintPlan = findViewById(R.id.btnPrintPlan);
        btnCompleteTreatment = findViewById(R.id.btnCompleteTreatment);
        
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
        
        // Patient Info Button (i icon)
        ImageButton btnPatientInfo = findViewById(R.id.btnPatientInfo);
        btnPatientInfo.setOnClickListener(v -> {
            if (currentPatient != null) {
                Intent intent = new Intent(this, com.hcmute.mobile_android.ui.activities.PatientDetailActivity.class);
                intent.putExtra(com.hcmute.mobile_android.ui.activities.PatientDetailActivity.EXTRA_PATIENT_INFO, 
                    currentPatient);
                intent.putExtra(com.hcmute.mobile_android.ui.activities.PatientDetailActivity.EXTRA_FROM_WORKFLOW, 
                    true);
                startActivityForResult(intent, REQUEST_PATIENT_DETAIL);
            }
        });
        
        btnLookup.setOnClickListener(v -> lookupPatient());
        btnActivatePlan.setOnClickListener(v -> activatePlan());
        btnCompleteTreatment.setOnClickListener(v -> {
            if (currentPatient == null || currentTreatmentPlanId == null) {
                Toast.makeText(this, "Vui lòng chọn bệnh nhân và tạo phác đồ điều trị", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Kết thúc Điều trị")
                .setMessage("Tất cả các bước đã hoàn tất.\nBạn có chắc muốn kết thúc phác đồ và lập hóa đơn cho bệnh nhân?\n\n" +
                           "Tổng chi phí dự kiến: " + tvTotalEstimate.getText())
                .setPositiveButton("Xác nhận & Lập Hóa đơn", (dialog, which) -> completeAndGenerateInvoice())
                .setNegativeButton("Hủy", null)
                .show();
        });
        btnSelectTemplate.setOnClickListener(v -> {
            rvTemplates.setVisibility(rvTemplates.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        btnAddService.setOnClickListener(v -> showAddServiceDialog(null));
        btnPrescribe.setOnClickListener(v -> {
            if (currentPatient != null
                    && currentPatient.getAppointmentId() != null
                    && currentPatient.getAppointmentId() != -1
                    && currentTreatmentPlanId != null) {
                Intent intent = new Intent(this, PrescriptionActivity.class);
                intent.putExtra(PrescriptionActivity.EXTRA_APPOINTMENT_ID, currentPatient.getAppointmentId());
                intent.putExtra(PrescriptionActivity.EXTRA_TREATMENT_PLAN_ID, currentTreatmentPlanId);
                startActivityForResult(intent, REQUEST_PRESCRIPTION);
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

        // Result images adapter (Horizontal)
        if (rvResultImages != null) {
            rvResultImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            resultImageAdapter = new ImagePreviewAdapter(currentStepImageUrls, position -> {
                if (position >= 0 && position < currentStepImageUrls.size()) {
                    String imageUrl = currentStepImageUrls.get(position);
                    onImageDeleted(imageUrl);
                }
            });
            rvResultImages.setAdapter(resultImageAdapter);
        }
    }

    public void launchImagePicker() {
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
                java.io.ByteArrayOutputStream byteBuffer = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                byte[] bytes = byteBuffer.toByteArray();
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
        if (resultImageAdapter != null) {
            resultImageAdapter.notifyDataSetChanged();
            
            android.view.View layout = findViewById(R.id.layout_result_images);
            if (layout != null) {
                // Determine if we should show the activity's preview
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
                boolean isXrayFragment = currentFragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray;
                
                // Hide activity preview if XOR (X-ray fragment active OR list empty)
                if (isXrayFragment || currentStepImageUrls.isEmpty()) {
                    layout.setVisibility(android.view.View.GONE);
                } else {
                    layout.setVisibility(android.view.View.VISIBLE);
                }
            }
        }
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
                if (s.getImageUrls() != null) {
                    currentStepImageUrls.addAll(s.getImageUrls());
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
                    
                    // Check if this is first time examining this patient
                    if (isFirstVisit(currentPatient.getId())) {
                        // Show patient detail screen first
                        Intent intent = new Intent(DoctorWorkflowActivity.this, 
                            com.hcmute.mobile_android.ui.activities.PatientDetailActivity.class);
                        intent.putExtra(com.hcmute.mobile_android.ui.activities.PatientDetailActivity.EXTRA_PATIENT_INFO, 
                            currentPatient);
                        intent.putExtra(com.hcmute.mobile_android.ui.activities.PatientDetailActivity.EXTRA_FROM_WORKFLOW, 
                            false);
                        startActivityForResult(intent, REQUEST_PATIENT_DETAIL);
                    } else {
                        // Go directly to examination
                        displayPatientInfo(currentPatient);
                        loadLastPlanForPatient(currentPatient.getId());
                    }
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
    
    private boolean isFirstVisit(Long patientId) {
        SharedPreferences prefs = getSharedPreferences("doctor_visits", MODE_PRIVATE);
        SharedPreferences authPrefs = getSharedPreferences("auth", MODE_PRIVATE);
        Long doctorId = authPrefs.getLong("user_id", 0L);
        
        String key = "doctor_" + doctorId + "_patient_" + patientId + "_visited";
        return !prefs.getBoolean(key, false);
    }

    private void displayPatientInfo(PatientInfo patient) {
        String header = "Khám Bệnh nhân " + patient.getFullName();
        if (patient.getBookedService() != null && !patient.getBookedService().isEmpty()) {
            header += "\n(Đặt lịch: " + patient.getBookedService() + ")";
        }
        tvPatientHeader.setText(header);
        
        // Show patient info button
        ImageButton btnPatientInfo = findViewById(R.id.btnPatientInfo);
        btnPatientInfo.setVisibility(View.VISIBLE);
        
        // Show/hide skip button based on queue status
        MaterialButton btnSkipPatient = findViewById(R.id.btnSkipPatient);
        if (patient.getQueueId() != null && patient.getQueueId() > 0) {
            btnSkipPatient.setVisibility(View.VISIBLE);
        } else {
            btnSkipPatient.setVisibility(View.GONE);
        }
        
        // Hide lookup, show examination area
        cardLookup.setVisibility(View.GONE);
        layoutExamination.setVisibility(View.VISIBLE);
        
        // Reset: disable các tab chuyên biệt, về tab Tổng quát
        setSpecialTabsEnabled(false);
        toggleFormType.check(R.id.btnFormGeneral);
        
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
                    btnPatientInfo.setVisibility(View.GONE);
                    findViewById(R.id.btnSkipPatient).setVisibility(View.GONE);
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
            
            if (("COMPLETED".equals(step.getStatus()) || "IN_PROGRESS".equals(step.getStatus())) 
                    && step.getUiTemplateType() != null) {
                // Enable tab tương ứng để bác sĩ có thể xem lại dữ liệu
                enableTabForStep(step);
            }
            
            if ("COMPLETED".equals(step.getStatus()) && step.getUiTemplateType() != null) {
                completedCount++;
                
                // Lấy dữ liệu
                String conclusion = step.getDoctorConclusion();
                List<String> imageUrls = new ArrayList<>();
                if (step.getImageUrls() != null) {
                    imageUrls.addAll(step.getImageUrls());
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
            
        } else if (fragment instanceof FragmentCrownService) {
            FragmentCrownService crownFragment = (FragmentCrownService) fragment;
            if (finalCachedData.doctorConclusion != null && !finalCachedData.doctorConclusion.trim().isEmpty()) {
                crownFragment.setData(finalCachedData.doctorConclusion);
                android.util.Log.d("DoctorWorkflow", "✓ Set data for FragmentCrownService");
            }
            crownFragment.setReadOnlyMode(true);
            android.util.Log.d("DoctorWorkflow", "✓ Set READ-ONLY mode for FragmentCrownService");
            
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

    @Override
    public void onStepTransfer(TreatmentPlan.Step step) {
        if (currentPatient == null || currentPatient.getQueueId() == null || currentPatient.getQueueId() <= 0) {
            Toast.makeText(this, "Không tìm thấy thông tin hàng đợi bệnh nhân", Toast.LENGTH_SHORT).show();
            return;
        }

        String template = step.getUiTemplateType() != null ? step.getUiTemplateType().toUpperCase() : "";
        String svcName = step.getServiceName() != null ? step.getServiceName().toLowerCase() : "";
        
        boolean isXrayService = template.contains("XRAY") || template.contains("X-RAY") || 
                               svcName.contains("phim") || svcName.contains("xquang") || 
                               svcName.contains("x quang") || svcName.contains("x-quang");
        boolean isSurgeryService = template.contains("SURGERY") || svcName.contains("tiểu phẫu") || 
                                  svcName.contains("phẫu thuật") || svcName.contains("nhổ");

        if (isXrayService) {
            android.util.Log.d("DoctorWorkflow", "Manual transfer to X-Ray room");
            saveTreatmentPlanInternal(true, () -> {
                transferPatientToXRay(currentPatient.getQueueId(), step.getServiceName());
            });
        } else if (isSurgeryService) {
            android.util.Log.d("DoctorWorkflow", "Manual transfer to Surgery room");
            saveTreatmentPlanInternal(true, () -> {
                transferPatientToSurgeryRoom(currentPatient.getQueueId(), step.getServiceName());
            });
        } else {
            Toast.makeText(this, "Dịch vụ này không yêu cầu chuyển phòng đặc biệt", Toast.LENGTH_SHORT).show();
        }
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
                saveTreatmentPlanInternal(true, () -> {
                    // Refresh from server to get the actual ID
                    loadTreatmentPlanForRoom(currentTreatmentPlanId);
                });
                Toast.makeText(this, "Đã lưu và bắt đầu thực hiện: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
                return;
            } else {
                // Start the step via API
                apiService.startTreatmentStep(step.getId()).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
                @Override
                public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                    if (response.isSuccessful()) {
                        step.setStatus("IN_PROGRESS");
                        stepAdapter.notifyDataSetChanged();
                        
                        // Switch to appropriate tab after starting
                        switchToTabForStep(step);
                        
                        Toast.makeText(DoctorWorkflowActivity.this, "Đã bắt đầu thực hiện: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                    // Silently fail or log, as this is an optimization
                }
                });
                return; // Exit early after starting the step
            }
        }



        Toast.makeText(this, "Nhập kết luận cho: " + step.getServiceName(), Toast.LENGTH_SHORT).show();
        
        // CRITICAL FIX: Only switch tab if current fragment doesn't match step's template type
        // This prevents destroying user's input when they click "Bắt đầu"
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
        boolean needsSwitch = true;
        
        String template = step.getUiTemplateType() != null ? step.getUiTemplateType().toUpperCase() : "";
        String svcName = step.getServiceName() != null ? step.getServiceName().toLowerCase() : "";
        
        // Check if current fragment matches the step's template or service name keywords
        if ((template.contains("SURGERY") || svcName.contains("tiểu phẫu") || svcName.contains("phẫu thuật") || svcName.contains("nhổ")) 
            && currentFragment instanceof FragmentSurgeryChecklist) {
            needsSwitch = false;
        } else if ((template.contains("ORTHO") || svcName.contains("niềng")) && currentFragment instanceof FragmentOrthodontics) {
            needsSwitch = false;
        } else if ((template.contains("XRAY") || template.contains("X-RAY") || template.contains("X_RAY") || svcName.contains("phim") || svcName.contains("xquang")) 
                   && currentFragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
            needsSwitch = false;
        } else if (template.contains("CROWN") && currentFragment instanceof FragmentCrownService) {
            // Crown selection form is only for ADDING. For treatment execution, we use Basic Service.
            // So if we are here (Execution), we usually NEED a switch to Basic Service.
            needsSwitch = true;
        } else if (currentFragment instanceof FragmentBasicService) {
            // Default check for Basic Service
            boolean isSpecial = template.contains("SURGERY") || template.contains("ORTHO") || 
                              template.contains("XRAY") || template.contains("X-RAY") || template.contains("X_RAY") ||
                              svcName.contains("phim") || svcName.contains("xquang") || 
                              svcName.contains("tiểu phẫu") || svcName.contains("phẫu thuật") || svcName.contains("nhổ");
            
            if (!isSpecial) {
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
                    if (step.getImageUrls() != null && !step.getImageUrls().isEmpty()) {
                        ((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).setImageUrls(step.getImageUrls());
                    }
                    // Ensure editable mode when editing (status is already IN_PROGRESS)
                    ((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).setReadOnlyMode(false);
                } else if (currentFragment instanceof FragmentSurgeryChecklist) {
                    if (step.getDoctorConclusion() != null) {
                        ((FragmentSurgeryChecklist) currentFragment).setData(step.getDoctorConclusion());
                    }
                    ((FragmentSurgeryChecklist) currentFragment).setReadOnlyMode(false);
                } else if (currentFragment instanceof FragmentCrownService) {
                    if (step.getDoctorConclusion() != null) {
                        ((FragmentCrownService) currentFragment).setData(step.getDoctorConclusion());
                    }
                    ((FragmentCrownService) currentFragment).setReadOnlyMode(false);
                } else if (currentFragment instanceof FragmentOrthodontics) {
                    if (step.getDoctorConclusion() != null) {
                        ((FragmentOrthodontics) currentFragment).setData(step.getDoctorConclusion());
                    }
                    ((FragmentOrthodontics) currentFragment).setReadOnlyMode(false);
                } else if (currentFragment instanceof FragmentBasicService) {
                    // Populate initial data like Service Name and Tooth Position
                    List<Integer> teeth = new ArrayList<>();
                    if (step.getToothNumber() != null && !step.getToothNumber().trim().isEmpty()) {
                        try {
                            String[] parts = step.getToothNumber().split(",");
                            for (String p : parts) {
                                teeth.add(Integer.parseInt(p.trim()));
                            }
                        } catch (Exception e) {}
                    }
                    
                    ((FragmentBasicService) currentFragment).loadStepData(
                        step.getId(), 
                        step.getServiceName(), 
                        null, 
                        null, 
                        teeth
                    );

                    if (step.getDoctorConclusion() != null) {
                        ((FragmentBasicService) currentFragment).setData(step.getDoctorConclusion());
                    }
                    ((FragmentBasicService) currentFragment).setReadOnlyMode(false);
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

    }
    
    /**
     * Show tooth service selection dialog
     * Displays 4 tooth-specific services for user to choose from
     */
    private void showToothServiceSelectionDialog(int toothNumber, com.hcmute.mobile_android.ui.views.OdontogramView odontogramView) {
        // CRITICAL FIX: Ensure treatment plan exists before adding tooth service
        if (currentTreatmentPlanId == null) {
            Toast.makeText(this, "Đang tạo phác đồ điều trị...", Toast.LENGTH_SHORT).show();
            createBlankPlanAndSave(true, () -> {
                // After plan is created, show dialog
                showToothServiceSelectionDialogInternal(toothNumber, odontogramView);
            });
            return;
        }
        
        showToothServiceSelectionDialogInternal(toothNumber, odontogramView);
    }
    
    private void showToothServiceSelectionDialogInternal(int toothNumber, com.hcmute.mobile_android.ui.views.OdontogramView odontogramView) {
        // Get next sequence order
        int nextSequence = treatmentSteps.size() + 1;
        
        // Show ToothServiceDialog
        com.hcmute.mobile_android.ui.dialogs.ToothServiceDialog dialog = 
            com.hcmute.mobile_android.ui.dialogs.ToothServiceDialog.newInstance(
                currentTreatmentPlanId,
                String.valueOf(toothNumber),
                nextSequence
            );
        
        dialog.setOnServiceSelectedListener(new com.hcmute.mobile_android.ui.dialogs.ToothServiceDialog.OnServiceSelectedListener() {
            @Override
            public void onServiceSelected(com.hcmute.mobile_android.network.models.ToothServiceResponse response) {
                // Add to treatment steps
                TreatmentPlan.Step step = new TreatmentPlan.Step();
                step.setId(response.getStepId());
                step.setServiceName(response.getServiceName());
                step.setToothNumber(response.getToothNumber());
                step.setActualPrice(response.getPrice() != null ? response.getPrice().doubleValue() : 0.0);
                step.setStatus("PENDING");
                step.setUiTemplateType("GENERAL");
                
                treatmentSteps.add(step);
                stepAdapter.notifyDataSetChanged();
                
                // Update odontogram to show service color
                odontogramView.addServiceToTooth(toothNumber, response.getServiceName());
                
                // Update total cost
                updateTotalEstimate();
                
                Toast.makeText(DoctorWorkflowActivity.this,
                    "Đã thêm: " + response.getServiceName() + " cho răng " + toothNumber,
                    Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String message) {
                Toast.makeText(DoctorWorkflowActivity.this,
                    "Lỗi: " + message,
                    Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show(getSupportFragmentManager(), "ToothServiceDialog");
    }
    
    // ====================================================================
    //  HELPER: Enable/Disable các tab chuyên biệt
    // ====================================================================
    
    /**
     * Enable hoặc disable các tab chuyên biệt (Tiểu phẫu, Niềng, X-quang)
     * Tab Bọc sứ luôn được enable để bác sĩ có thể thêm dịch vụ bất cứ lúc nào
     * Các tab khác chỉ được mở khóa khi bác sĩ nhấn "Bắt đầu" trên dịch vụ tương ứng
     */
    private void setSpecialTabsEnabled(boolean enabled) {
        com.google.android.material.button.MaterialButton btnSurgery = findViewById(R.id.btnFormSurgery);
        com.google.android.material.button.MaterialButton btnCrown = findViewById(R.id.btnFormCrown);
        com.google.android.material.button.MaterialButton btnOrtho = findViewById(R.id.btnFormOrtho);
        com.google.android.material.button.MaterialButton btnXray = findViewById(R.id.btnFormXray);
        if (btnSurgery != null) { btnSurgery.setEnabled(enabled); btnSurgery.setAlpha(enabled ? 1.0f : 0.5f); }
        // Crown tab is always enabled
        if (btnCrown != null)   { btnCrown.setEnabled(true);   btnCrown.setAlpha(1.0f); }
        if (btnOrtho != null)   { btnOrtho.setEnabled(enabled);   btnOrtho.setAlpha(enabled ? 1.0f : 0.5f); }
        if (btnXray != null)    { btnXray.setEnabled(enabled);    btnXray.setAlpha(enabled ? 1.0f : 0.5f); }
    }
    
    /**
     * Enable đúng tab dựa theo uiTemplateType của dịch vụ đang "Bắt đầu"
     */
    private void enableTabForStep(TreatmentPlan.Step step) {
        if (step == null) return;
        String template = step.getUiTemplateType() != null ? step.getUiTemplateType().toUpperCase() : "";
        String svcName = step.getServiceName() != null ? step.getServiceName().toLowerCase() : "";
        
        com.google.android.material.button.MaterialButton btn = null;
        if (template.contains("SURGERY") || svcName.contains("tiểu phẫu") || svcName.contains("phẫu thuật") || svcName.contains("nhổ")) {
            btn = findViewById(R.id.btnFormSurgery);
        } else if (template.contains("CROWN") || svcName.contains("bọc")) {
            btn = findViewById(R.id.btnFormCrown);
        } else if (template.contains("ORTHO") || svcName.contains("niềng")) {
            btn = findViewById(R.id.btnFormOrtho);
        } else if (template.contains("XRAY") || template.contains("X-RAY") || template.contains("X_RAY") || svcName.contains("phim") || svcName.contains("xquang")) {
            btn = findViewById(R.id.btnFormXray);
        }
        
        if (btn != null) {
            btn.setEnabled(true);
            btn.setAlpha(1.0f);
        }
    }
    
    /**
     * Switch to the appropriate tab based on step's UI template type
     * and load the fragment with existing data
     */
    private void switchToTabForStep(TreatmentPlan.Step step) {
        // QUAN TRỌNG: Enable đúng tab TRƯỚC khi check(), tránh check() bị bỏ qua vì tab disabled
        enableTabForStep(step);
        
        // Determine which fragment to load based on template type
        Fragment targetFragment = null;
        if (toggleFormType != null) {
            String template = step.getUiTemplateType() != null ? step.getUiTemplateType().toUpperCase() : "";
            String svcName = step.getServiceName() != null ? step.getServiceName().toLowerCase() : "";

            if (template.contains("SURGERY") || svcName.contains("tiểu phẫu") || svcName.contains("phẫu thuật") || svcName.contains("nhổ")) {
                toggleFormType.check(R.id.btnFormSurgery);
                targetFragment = new FragmentSurgeryChecklist();
            } else if (template.contains("ORTHO") || svcName.contains("niềng")) {
                toggleFormType.check(R.id.btnFormOrtho);
                targetFragment = new FragmentOrthodontics();
            } else if (template.contains("XRAY") || template.contains("X-RAY") || template.contains("X_RAY") || svcName.contains("phim") || svcName.contains("xquang")) {
                toggleFormType.check(R.id.btnFormXray);
                targetFragment = new com.hcmute.mobile_android.ui.fragments.FragmentXray();
            } else {
                toggleFormType.check(R.id.btnFormGeneral);
                // [FIX] Sử dụng Form Basic Service mới nhất thay cho General Dental cũ
                targetFragment = new FragmentBasicService();
            }
        } else {
            // Default to Basic Service
            if (toggleFormType != null) {
                toggleFormType.clearChecked();
            }
            targetFragment = new FragmentBasicService();
        }
        
        // ĐẢM BẢO HIỂN THỊ CÁC CONTAINER FORM LÊN MÀN HÌNH TRƯỚC KHI THÊM
        android.view.View containerFragment = findViewById(R.id.fragmentContainerForm);
        if (containerFragment != null) {
            containerFragment.setVisibility(android.view.View.VISIBLE);
        }
        
        android.view.View cardOdontogram = findViewById(R.id.cardOdontogram);
        if (cardOdontogram != null) {
            // Hiện sơ đồ răng cho General/Crown/Basic Service (null template), tắt cho các dịch vụ khác
            String typeForOdon = step.getUiTemplateType() != null ? step.getUiTemplateType().toUpperCase() : "";
            if (typeForOdon.contains("GENERAL") || typeForOdon.contains("CROWN") || typeForOdon.isEmpty() || typeForOdon.contains("BASIC")) {
                cardOdontogram.setVisibility(android.view.View.VISIBLE);
            } else {
                cardOdontogram.setVisibility(android.view.View.GONE);
            }
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
                } else if (finalFragment instanceof FragmentBasicService) {
                    // Populate initial data like Service Name and Tooth Position
                    List<Integer> teeth = new ArrayList<>();
                    if (step.getToothNumber() != null) {
                        try {
                            teeth.add(Integer.parseInt(step.getToothNumber()));
                        } catch (Exception e) {}
                    }
                    
                    ((FragmentBasicService) finalFragment).loadStepData(
                        step.getId(), 
                        step.getServiceName(), 
                        null, 
                        null, 
                        teeth
                    );

                    if (existingConclusion != null && !existingConclusion.trim().isEmpty()) {
                        ((FragmentBasicService) finalFragment).setData(existingConclusion);
                    }
                    // Set read-only mode ONLY if step is still COMPLETED (not being edited)
                    if (shouldBeReadOnly) {
                        ((FragmentBasicService) finalFragment).setReadOnlyMode(true);
                        // Show edit button
                        android.view.View btnEdit = finalFragment.getView().findViewById(R.id.btnEditMode);
                        if (btnEdit != null) {
                            btnEdit.setVisibility(View.VISIBLE);
                            btnEdit.setOnClickListener(v -> onStepEdit(step));
                        }
                    } else {
                        // Ensure editable mode when editing
                        ((FragmentBasicService) finalFragment).setReadOnlyMode(false);
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
                } else if (finalFragment instanceof FragmentCrownService) {
                    if (existingConclusion != null && !existingConclusion.trim().isEmpty()) {
                        ((FragmentCrownService) finalFragment).setData(existingConclusion);
                    }
                    // Set read-only mode ONLY if step is still COMPLETED (not being edited)
                    if (shouldBeReadOnly) {
                        ((FragmentCrownService) finalFragment).setReadOnlyMode(true);
                        // Show edit button
                        android.view.View btnEdit = finalFragment.getView().findViewById(R.id.btnEditMode);
                        if (btnEdit != null) {
                            btnEdit.setVisibility(View.VISIBLE);
                            btnEdit.setOnClickListener(v -> onStepEdit(step));
                        }
                    } else {
                        // Ensure editable mode when editing
                        ((FragmentCrownService) finalFragment).setReadOnlyMode(false);
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
                } else if (finalFragment instanceof FragmentBasicService) {
                    // Extract Diagnosis and Notes from Conclusion
                    String diagnosis = "";
                    String notes = step.getDoctorConclusion();
                    if (notes != null && notes.contains("Chẩn đoán: ") && notes.contains("Ghi chú thực hiện: ")) {
                        int diagStart = notes.indexOf("Chẩn đoán: ") + "Chẩn đoán: ".length();
                        int noteStart = notes.indexOf("Ghi chú thực hiện: ");
                        diagnosis = notes.substring(diagStart, noteStart).trim();
                        notes = notes.substring(noteStart + "Ghi chú thực hiện: ".length()).trim();
                    } else if (notes == null) {
                        notes = "";
                    }
                    
                    // Parse teeth
                    List<Integer> teeth = new ArrayList<>();
                    if (step.getToothNumber() != null && !step.getToothNumber().trim().isEmpty()) {
                        try {
                            String[] parts = step.getToothNumber().split(",");
                            for (String p : parts) {
                                teeth.add(Integer.parseInt(p.trim()));
                            }
                        } catch (Exception e) {}
                    }
                    
                    ((FragmentBasicService) finalFragment).loadStepData(step.getId(), step.getServiceName(), diagnosis, notes, teeth);
                    
                    if (shouldBeReadOnly) {
                        ((FragmentBasicService) finalFragment).setReadOnlyMode(true);
                        // Show edit button
                        android.view.View btnEdit = finalFragment.getView().findViewById(R.id.btnEditMode);
                        if (btnEdit != null) {
                            btnEdit.setVisibility(View.VISIBLE);
                            btnEdit.setOnClickListener(v -> onStepEdit(step));
                        }
                    } else {
                        ((FragmentBasicService) finalFragment).setReadOnlyMode(false);
                    }
                } else if (finalFragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
                    android.util.Log.d("DoctorWorkflow", "Loading data for FragmentXray");
                    android.util.Log.d("DoctorWorkflow", "  - Conclusion: " + (existingConclusion != null ? existingConclusion.substring(0, Math.min(100, existingConclusion.length())) : "null"));
                    android.util.Log.d("DoctorWorkflow", "  - Images: " + (step.getImageUrls() != null ? step.getImageUrls().size() : 0));
                    
                    // Always call setData, even if conclusion is null/empty
                    if (existingConclusion != null && !existingConclusion.trim().isEmpty()) {
                        ((com.hcmute.mobile_android.ui.fragments.FragmentXray) finalFragment).setData(existingConclusion);
                        android.util.Log.d("DoctorWorkflow", "✓ Called setData for FragmentXray");
                    } else {
                        android.util.Log.w("DoctorWorkflow", "⚠️ No conclusion to load for FragmentXray");
                    }
                    
                    // Load images for X-ray fragment
                    if (step.getImageUrls() != null && !step.getImageUrls().isEmpty()) {
                        android.util.Log.d("DoctorWorkflow", "Loading " + step.getImageUrls().size() + " images for FragmentXray");
                        final List<String> imageUrls = step.getImageUrls();
                        
                        // Use single post - FragmentXray now handles adapter initialization
                        finalFragment.getView().post(() -> {
                            ((com.hcmute.mobile_android.ui.fragments.FragmentXray) finalFragment).setImageUrls(imageUrls);
                            android.util.Log.d("DoctorWorkflow", "✓ Called setImageUrls for FragmentXray");
                        });
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
                if (step.getId() != null && step.getImageUrls() != null && !step.getImageUrls().isEmpty()) {
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
                            
                        // [FIX] Nếu chưa có currentStepId (vd: ấn bắt đầu một bước mới thêm),
                        // hãy lấy bước IN_PROGRESS duy nhất này làm currentStep
                        if (currentStepId == null) {
                            currentStepId = firstInProgressStep.getId();
                            android.util.Log.d("DoctorWorkflow", "✓ Auto-selected new progress step as current: " + currentStepId);
                        }
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
                    
                    // [FIX] Điều hướng Fragment luôn cho currentStep thay vì mặc định
                    if (currentStep != null) {
                        switchToTabForStep(currentStep);
                    } else if (toggleFormType != null) {
                        // AUTO-SELECT: Tự động chọn tab đầu tiên để fragment được tạo
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
                            addServiceAsStep(selectedSvc);
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
    
    private void addServiceAsStep(com.hcmute.mobile_android.network.models.ServiceItem svc) {
        if (currentTreatmentPlanId == null) {
            Toast.makeText(this, "Vui lòng tạo phác đồ điều trị trước", Toast.LENGTH_SHORT).show();
            return;
        }

        String template = svc.getUiTemplateType() != null ? svc.getUiTemplateType().toUpperCase() : "";
        Long svcId = svc.getId();
        String svcName = svc.getName() != null ? svc.getName().toLowerCase() : "";
        
        // Tự động chuyển hướng sang tab Bọc sứ nếu chọn dịch vụ Bọc sứ (bất chấp template nếu id=9)
        if (template.contains("CROWN") || (svcId != null && svcId == 9L) || svcName.contains("bọc")) {
            Toast.makeText(this, "Đang mở thẻ điều trị Bọc Sứ...", Toast.LENGTH_SHORT).show();
            // Đảm bảo Form Container hiển thị trước khi switch Tab!
            android.view.View formContainer = findViewById(R.id.fragmentContainerForm);
            if (formContainer != null) formContainer.setVisibility(android.view.View.VISIBLE);
            
            // Thay đổi UI Toggle sang nút Bọc sứ
            if (toggleFormType != null) {
                findViewById(R.id.btnFormCrown).setEnabled(true);
                toggleFormType.check(R.id.btnFormCrown);
            }
            return;
        }
        
        // TẤT CẢ CÁC DỊCH VỤ CÒN LẠI (BASIC/GENERAL/SURGERY/XRAY...) ĐỀU TẠO STEP TRỰC TIẾP VÀO PHÁC ĐỒ (Bỏ quy trình Pending riêng biệt)
        
        // TẤT CẢ CÁC DỊCH VỤ CÒN LẠI: Tạo Step trực tiếp vào phác đồ (Bỏ quy trình Pending phức tạp)
        TreatmentPlan.Step newStep = new TreatmentPlan.Step();
        newStep.setServiceId(svc.getId());
        newStep.setServiceName(svc.getName());
        newStep.setEstimatedPrice(svc.getPrice());
        newStep.setActualPrice(svc.getPrice());
        newStep.setStatus("PENDING");
        newStep.setUiTemplateType(svc.getUiTemplateType());
        newStep.setStepOrder(treatmentSteps.size() + 1);
        
        // Theo yêu cầu của khách: Tạo ngay vào phác đồ mà ko cần qua bước Chọn sơ đồ răng
        treatmentSteps.add(newStep);
        stepAdapter.notifyDataSetChanged();
        updateTotalEstimate();
        
        // Lưu server
        saveTreatmentPlanInternal(true, () -> {
            loadTreatmentPlanForRoom(currentTreatmentPlanId);
        });
        
        Toast.makeText(this, "Đã thêm dịch vụ: " + svc.getName(), Toast.LENGTH_SHORT).show();
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
    
    /**
     * Transfer patient to Surgery room
     */
    private void transferPatientToSurgeryRoom(Long queueId, String serviceName) {
        // Call API to transfer patient to surgery room
        apiService.transferToSurgery(queueId, new java.util.HashMap<>()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    new androidx.appcompat.app.AlertDialog.Builder(DoctorWorkflowActivity.this)
                        .setTitle("Chuyển phòng Tiểu phẫu")
                        .setMessage("Bệnh nhân đã được chuyển sang phòng Tiểu phẫu để thực hiện dịch vụ: " + serviceName + 
                                  "\n\nHệ thống sẽ tự động đưa bệnh nhân trở lại sau khi hoàn thành.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Close this activity - patient is now in surgery room
                            finish();
                        })
                        .setCancelable(false)
                        .show();
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi chuyển phòng Tiểu phẫu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối khi chuyển phòng", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Add crown service to multiple teeth
     * Called from FragmentCrownService when user clicks "Thêm dịch vụ bọc răng sứ"
     */
    public void addCrownServiceToMultipleTeeth(List<Integer> teeth, java.math.BigDecimal customPrice, String notes) {
        if (currentTreatmentPlanId == null) {
            Toast.makeText(this, "Vui lòng tạo phác đồ điều trị trước", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (teeth == null || teeth.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một răng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress dialog
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Đang thêm dịch vụ bọc răng sứ...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Convert teeth to string list
        List<String> toothNumbers = new ArrayList<>();
        for (Integer tooth : teeth) {
            toothNumbers.add(String.valueOf(tooth));
        }
        
        // Create request
        com.hcmute.mobile_android.network.models.AddMultipleTeethServiceRequest request = 
            new com.hcmute.mobile_android.network.models.AddMultipleTeethServiceRequest();
        request.setServiceId(9L); // ID for "Bọc răng sứ" service
        request.setToothNumbers(toothNumbers);
        request.setCustomPrice(customPrice);
        request.setNotes(notes);
        request.setStartingSequenceOrder(treatmentSteps.size() + 1);
        
        // Call API
        apiService.addServiceToMultipleTeeth(currentTreatmentPlanId, request)
            .enqueue(new Callback<com.hcmute.mobile_android.network.models.MultipleTeethServiceResponse>() {
                @Override
                public void onResponse(Call<com.hcmute.mobile_android.network.models.MultipleTeethServiceResponse> call, 
                                     Response<com.hcmute.mobile_android.network.models.MultipleTeethServiceResponse> response) {
                    progressDialog.dismiss();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(DoctorWorkflowActivity.this, 
                            "Đã thêm " + response.body().getCreatedSteps().size() + " bước điều trị", 
                            Toast.LENGTH_SHORT).show();
                        
                        // Clear selection in fragment
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
                        if (fragment instanceof FragmentCrownService) {
                            ((FragmentCrownService) fragment).setSelectedTeeth(new ArrayList<>());
                        }
                        
                        // Clear odontogram selection
                        com.hcmute.mobile_android.ui.views.OdontogramView odv = findViewById(R.id.odontogramView);
                        if (odv != null) {
                            odv.clearSelection();
                        }
                        
                        // Reload treatment plan to show new steps
                        loadTreatmentPlanForRoom(currentTreatmentPlanId);
                    } else {
                        Toast.makeText(DoctorWorkflowActivity.this, "Có lỗi xảy ra khi thêm dịch vụ", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.hcmute.mobile_android.network.models.MultipleTeethServiceResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onStepSave(TreatmentPlan.Step step) {
        android.util.Log.d("DoctorWorkflow", "onStepSave called for step: " + step.getServiceName() + " (ID: " + step.getId() + ")");
        
        // SE_14: Extract data and save
        StepData data = getStepDataFromFragment();
        Map<String, Object> body = new HashMap<>();
        body.put("doctorConclusion", data.notes);
        body.put("imageUrls", data.imageUrls);
        
        apiService.saveTreatmentResult(step.getId(), body).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DoctorWorkflowActivity.this, "Đã lưu kết quả (SE_14)", Toast.LENGTH_SHORT).show();
                    loadTreatmentPlanForRoom(currentTreatmentPlanId);
                    editingStep = null;
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi khi lưu kết quả", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class StepData {
        String notes;
        List<String> imageUrls;
        StepData(String notes, List<String> imageUrls) {
            this.notes = notes;
            this.imageUrls = imageUrls;
        }
    }

    private StepData getStepDataFromFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
        String currentData = "";
        List<String> currentImages = new ArrayList<>();
        
        android.util.Log.d("DoctorWorkflow", "=== getStepDataFromFragment ===");
        android.util.Log.d("DoctorWorkflow", "Fragment type: " + (currentFragment != null ? currentFragment.getClass().getSimpleName() : "null"));
        
        if (currentFragment instanceof FragmentGeneralDental) {
            currentData = ((FragmentGeneralDental) currentFragment).getFormDataNotes();
        } else if (currentFragment instanceof FragmentSurgeryChecklist) {
            currentData = ((FragmentSurgeryChecklist) currentFragment).getFormDataNotes();
        } else if (currentFragment instanceof FragmentCrownService) {
            currentData = ((FragmentCrownService) currentFragment).getFormDataNotes();
        } else if (currentFragment instanceof FragmentOrthodontics) {
            currentData = ((FragmentOrthodontics) currentFragment).getFormDataNotes();
        } else if (currentFragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
            currentData = ((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).getFormDataNotes();
            currentImages = ((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).getImageUrls();
            android.util.Log.d("DoctorWorkflow", "FragmentXray.getImageUrls() returned: " + 
                (currentImages != null ? currentImages.size() + " images" : "null"));
            if (currentImages != null) {
                for (int i = 0; i < currentImages.size(); i++) {
                    android.util.Log.d("DoctorWorkflow", "  FragmentXray Image[" + i + "]: " + currentImages.get(i));
                }
            }
        } else if (currentFragment instanceof FragmentBasicService) {
            currentData = ((FragmentBasicService) currentFragment).getFormDataNotes();
        }
        
        android.util.Log.d("DoctorWorkflow", "currentImages from fragment: " + 
            (currentImages != null ? currentImages.size() : "null"));
        android.util.Log.d("DoctorWorkflow", "currentStepImageUrls: " + 
            (currentStepImageUrls != null ? currentStepImageUrls.size() : "null"));
        
        if (currentImages.isEmpty() && currentStepImageUrls != null) {
            currentImages = currentStepImageUrls;
            android.util.Log.d("DoctorWorkflow", "Using currentStepImageUrls as fallback: " + currentImages.size() + " images");
        }
        
        android.util.Log.d("DoctorWorkflow", "Final imageUrls count: " + currentImages.size());
        
        return new StepData(currentData, currentImages);
    }
    
    @Override
    public void onStepComplete(TreatmentPlan.Step step) {
        android.util.Log.d("DoctorWorkflow", "=== onStepComplete (Aligned) ===");
        
        // 1. Extract data & Validate
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
        StepData data = getStepDataFromFragment();
        
        // Perform fragment-specific validation
        if (currentFragment instanceof com.hcmute.mobile_android.ui.fragments.FragmentXray) {
            if (!((com.hcmute.mobile_android.ui.fragments.FragmentXray) currentFragment).validateForm()) return;
        } else if (currentFragment instanceof FragmentSurgeryChecklist) {
            if (!((FragmentSurgeryChecklist) currentFragment).validateForm()) return;
        } else if (currentFragment instanceof FragmentCrownService) {
            if (!((FragmentCrownService) currentFragment).validateForm()) return;
        } else if (currentFragment instanceof FragmentOrthodontics) {
            if (!((FragmentOrthodontics) currentFragment).validateForm()) return;
        } else if (currentFragment instanceof FragmentBasicService) {
            List<Integer> teeth = ((FragmentBasicService) currentFragment).getSelectedTeeth();
            if (teeth != null && !teeth.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < teeth.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(teeth.get(i));
                }
                step.setToothNumber(sb.toString());
            }
        }
        
        step.setDoctorConclusion(data.notes);
        
        // CRITICAL FIX: Sync imageUrls to step before saving
        // This ensures images are preserved even if fragment is destroyed during room transfer
        if (data.imageUrls != null && !data.imageUrls.isEmpty()) {
            step.setImageUrls(new java.util.ArrayList<>(data.imageUrls));
            android.util.Log.d("DoctorWorkflow", "✓ Synced " + data.imageUrls.size() + " image(s) to step before save");
        } else {
            android.util.Log.w("DoctorWorkflow", "⚠ No images to sync! data.imageUrls is " + 
                (data.imageUrls == null ? "null" : "empty"));
        }
        
        this.currentStep = step;

        if (step.getId() == null) {
            Toast.makeText(this, "Lỗi: Bước chưa được lưu. Thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("doctorConclusion", data.notes);
        body.put("imageUrls", data.imageUrls);
        
        android.util.Log.d("DoctorWorkflow", "=== saveTreatmentResult Request ===");
        android.util.Log.d("DoctorWorkflow", "Step ID: " + step.getId());
        android.util.Log.d("DoctorWorkflow", "Conclusion: " + data.notes);
        android.util.Log.d("DoctorWorkflow", "ImageUrls count: " + (data.imageUrls != null ? data.imageUrls.size() : 0));
        if (data.imageUrls != null) {
            for (int i = 0; i < data.imageUrls.size(); i++) {
                android.util.Log.d("DoctorWorkflow", "  Image[" + i + "]: " + data.imageUrls.get(i));
            }
        }

        // SE_14: Ghi nhận kết quả điều trị
        apiService.saveTreatmentResult(step.getId(), body).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                android.util.Log.d("DoctorWorkflow", "=== saveTreatmentResult Response ===");
                android.util.Log.d("DoctorWorkflow", "Success: " + response.isSuccessful());
                android.util.Log.d("DoctorWorkflow", "Code: " + response.code());
                
                if (response.isSuccessful()) {
                    if (editingPreviouslyCompletedStep) {
                        Toast.makeText(DoctorWorkflowActivity.this, "Đã cập nhật kết quả", Toast.LENGTH_SHORT).show();
                        loadTreatmentPlanForRoom(currentTreatmentPlanId);
                        editingPreviouslyCompletedStep = false;
                        currentStep = null;
                    } else {
                        // SE_15: Chuyển bước
                        performMoveToNextStep(step, data.notes);
                    }
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi khi lưu kết quả (SE_14)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối (SE_14)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performMoveToNextStep(TreatmentPlan.Step step, String conclusion) {
        if (currentTreatmentPlanId == null) return;
        
        android.util.Log.d("DoctorWorkflow", "SE_15: Advancing to next step...");
        apiService.moveToNextStep(currentTreatmentPlanId, step.getId()).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String nextRoom = response.body().getNextRoomName();
                    
                    // Update UI
                    step.setStatus("COMPLETED");
                    step.setDoctorConclusion(conclusion);
                    stepAdapter.notifyDataSetChanged();
                    currentStep = null;

                    if (nextRoom != null) {
                        // CRITICAL FIX: Set flag to prevent onPause auto-save when finishing
                        // The step result (including images) was already saved by saveTreatmentResult (SE_14)
                        isCompletingTreatment = true;
                        
                        new androidx.appcompat.app.AlertDialog.Builder(DoctorWorkflowActivity.this)
                            .setTitle("Chuyển phòng")
                            .setMessage("Hồ sơ đã được lưu. Vui lòng hướng dẫn bệnh nhân di chuyển đến " + nextRoom + " để tiếp tục điều trị.")
                            .setPositiveButton("OK", (dialog, id) -> finish())
                            .setCancelable(false)
                            .show();
                    } else {
                        Toast.makeText(DoctorWorkflowActivity.this, "Đã hoàn tất bước điều trị", Toast.LENGTH_SHORT).show();
                        loadTreatmentPlanForRoom(currentTreatmentPlanId);
                    }
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this, "Lỗi chuyển bước (SE_15)", Toast.LENGTH_SHORT).show();
                    loadTreatmentPlanForRoom(currentTreatmentPlanId);
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối (SE_15)", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onToothSelected(int toothNumber) {
        // This method is called from FragmentGeneralDental when a tooth is clicked
        // Show tooth service selection dialog
        showToothServiceSelectionDialog(toothNumber, findViewById(R.id.odontogramView));
    }
    
    /**
     * Update odontogram selection from fragment
     * Called from FragmentCrownService when teeth selection changes
     */
    public void updateOdontogramSelection(Set<Integer> selectedTeeth) {
        com.hcmute.mobile_android.ui.views.OdontogramView odv = findViewById(R.id.odontogramView);
        if (odv != null) {
            odv.clearSelection();
            for (Integer toothNumber : selectedTeeth) {
                odv.toggleSelection(toothNumber);
            }
        }
    }
    
    /**
     * Handle crown service addition by calling bulk API
     * Crown service creates multiple steps (one per tooth) with custom pricing
     * Called from FragmentCrownService when user clicks "Add Service" button
     */


    @Override
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
    public void onViewImages(TreatmentPlan.Step step) {
        if (step.getImageUrls() != null && !step.getImageUrls().isEmpty()) {
            Intent intent = new Intent(this, com.hcmute.mobile_android.ui.activities.ImageViewerActivity.class);
            intent.putStringArrayListExtra("images", new java.util.ArrayList<>(step.getImageUrls()));
            intent.putExtra("position", 0);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Không có ảnh đính kèm", Toast.LENGTH_SHORT).show();
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
        boolean allCompleted = true;
        
        for (TreatmentPlan.Step step : treatmentSteps) {
            String status = step.getStatus() != null ? step.getStatus().toUpperCase() : "PENDING";
            if (!status.equals("CANCELLED") && !status.equals("SKIPPED")) {
                // Ưu tiên actualPrice đã nhập từ phần kê đơn theo từng dịch vụ
                Double price = (step.getActualPrice() != null && step.getActualPrice() > 0)
                        ? step.getActualPrice()
                        : step.getEstimatedPrice();
                if (price != null) {
                    total += price;
                    countedSteps.add(step);
                }
            }
            if (!status.equals("COMPLETED") && !status.equals("SKIPPED") && !status.equals("CANCELLED")) {
                allCompleted = false;
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
        
        // Update logic for Complete Treatment Button
        if (btnCompleteTreatment != null) {
            if (!treatmentSteps.isEmpty()) {
                btnCompleteTreatment.setVisibility(View.VISIBLE);
                if (allCompleted) {
                    btnCompleteTreatment.setEnabled(true);
                    btnCompleteTreatment.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                } else {
                    btnCompleteTreatment.setEnabled(false);
                    btnCompleteTreatment.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BDBDBD")));
                }
            } else {
                btnCompleteTreatment.setVisibility(View.GONE);
            }
        }
    }
    
    private void completeAndGenerateInvoice() {
        // CRITICAL FIX: Save treatment plan BEFORE completing to prevent image loss
        // Issue: If we complete first, the plan becomes locked and any pending images cannot be saved
        // Solution: Force save first, then complete only after save succeeds
        
        // Set flag to prevent onPause from triggering duplicate save
        isCompletingTreatment = true;
        
        // Show loading
        completionProgressDialog = new android.app.ProgressDialog(this);
        completionProgressDialog.setMessage("Đang lưu hồ sơ...");
        completionProgressDialog.setCancelable(false);
        completionProgressDialog.show();
        
        // Step 1: Save treatment plan with any pending changes (including images)
        saveTreatmentPlanInternal(true, () -> {
            // Step 2: After save succeeds, proceed with completing and generating invoice
            completionProgressDialog.setMessage("Đang tạo hóa đơn...");
            
            apiService.completeAndGenerateInvoice(currentTreatmentPlanId).enqueue(new Callback<com.hcmute.mobile_android.network.models.Invoice>() {
                @Override
                public void onResponse(Call<com.hcmute.mobile_android.network.models.Invoice> call, Response<com.hcmute.mobile_android.network.models.Invoice> response) {
                    if (completionProgressDialog != null && completionProgressDialog.isShowing()) {
                        completionProgressDialog.dismiss();
                    }
                    completionProgressDialog = null;
                    
                    if (response.isSuccessful() && response.body() != null) {
                        com.hcmute.mobile_android.network.models.Invoice invoice = response.body();
                        
                        // Show success message
                        Toast.makeText(DoctorWorkflowActivity.this, 
                            "Đã lập hóa đơn và kết thúc hồ sơ khám thành công!", Toast.LENGTH_LONG).show();
                        
                        // Close this activity (onPause will not trigger auto-save due to flag)
                        finish();
                    } else {
                        // Reset flag on error so user can try again
                        isCompletingTreatment = false;
                        
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
                    if (completionProgressDialog != null && completionProgressDialog.isShowing()) {
                        completionProgressDialog.dismiss();
                    }
                    completionProgressDialog = null;
                    isCompletingTreatment = false; // Reset flag on failure
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        // Note: If save fails, the error handlers will dismiss completionProgressDialog and reset flags
    }

    /**
     * Handle skip patient button click
     * Moves current patient back to queue and calls next patient
     */
    private void handleSkipPatient() {
        if (currentPatient == null || currentPatient.getQueueId() == null || currentPatient.getQueueId() <= 0) {
            Toast.makeText(this, "Không có bệnh nhân trong hàng đợi để lùi", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String patientName = (currentPatient.getLastName() + " " + currentPatient.getFirstName()).trim();
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Lùi 1 người")
            .setMessage("Bệnh nhân " + patientName + " sẽ quay lại hàng đợi với độ ưu tiên cao.\n\nNgười tiếp theo sẽ được gọi vào phòng.\n\nXác nhận?")
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
                progressDialog.setMessage("Đang xử lý...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                
                apiService.skipPatient(currentPatient.getQueueId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful()) {
                            Toast.makeText(DoctorWorkflowActivity.this, 
                                "Đã lùi " + patientName + " và gọi người tiếp theo", 
                                Toast.LENGTH_SHORT).show();
                            
                            // Close current activity and return to home
                            finish();
                        } else {
                            Toast.makeText(DoctorWorkflowActivity.this, 
                                "Lỗi: " + response.code(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(DoctorWorkflowActivity.this, 
                            "Lỗi kết nối: " + t.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }



}
      