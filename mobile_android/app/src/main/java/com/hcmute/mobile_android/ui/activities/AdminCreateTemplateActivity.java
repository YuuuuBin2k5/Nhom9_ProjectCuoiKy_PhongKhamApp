package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.TemplateStepEditAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.AdminTreatmentPlanTemplateRequest;
import com.hcmute.mobile_android.network.models.ServiceItem;
import com.hcmute.mobile_android.network.models.TreatmentTemplate;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCreateTemplateActivity extends AppCompatActivity {

    private TextInputEditText etName, etDescription;
    private MaterialSwitch switchActive;
    private RecyclerView rvSteps;
    private TemplateStepEditAdapter adapter;
    private ApiService apiService;
    private Long templateId;
    private boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_template);

        apiService = RetrofitClient.getApiService(this);
        templateId = getIntent().getLongExtra("template_id", -1);
        if (templateId == -1) templateId = null;

        initViews();
        loadServices();
        if (templateId != null) {
            loadTemplateData();
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> handleBackPress());
        toolbar.setTitle(templateId == null ? "Thêm mẫu mới" : "Chỉnh sửa mẫu");

        etName = findViewById(R.id.etTemplateName);
        etDescription = findViewById(R.id.etDescription);
        switchActive = findViewById(R.id.switchActive);
        rvSteps = findViewById(R.id.rvSteps);
        
        MaterialButton btnAddStep = findViewById(R.id.btnAddStep);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TemplateStepEditAdapter(position -> {
            adapter.getSteps().remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, adapter.getItemCount());
        });
        rvSteps.setAdapter(adapter);

        btnAddStep.setOnClickListener(v -> {
            adapter.addStep();
            hasChanges = true;
        });
        btnSave.setOnClickListener(v -> saveTemplate());

        // Simple change tracking
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { hasChanges = true; }
        };
        etName.addTextChangedListener(watcher);
        etDescription.addTextChangedListener(watcher);
        switchActive.setOnCheckedChangeListener((v, c) -> hasChanges = true);
    }
    
    @Override
    public void onBackPressed() {
        handleBackPress();
    }

    private void handleBackPress() {
        if (hasChanges) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Hủy bỏ thay đổi?")
                    .setMessage("Các thay đổi chưa lưu sẽ bị mất. Bạn có chắc chắn muốn thoát?")
                    .setPositiveButton("Thoát", (dialog, which) -> finish())
                    .setNegativeButton("Tiếp tục chỉnh sửa", null)
                    .show();
        } else {
            finish();
        }
    }

    private void loadServices() {
        apiService.getServices().enqueue(new Callback<List<com.hcmute.mobile_android.network.models.ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setAvailableServices(response.body());
                }
            }
            @Override public void onFailure(Call<List<ServiceItem>> call, Throwable t) {}
        });
    }

    private void loadTemplateData() {
        apiService.getAdminTreatmentTemplate(templateId).enqueue(new Callback<TreatmentTemplate>() {
            @Override
            public void onResponse(Call<TreatmentTemplate> call, Response<TreatmentTemplate> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TreatmentTemplate t = response.body();
                    etName.setText(t.getName());
                    etDescription.setText(t.getDescription());
                    switchActive.setChecked(t.getIsActive() != null && t.getIsActive());
                    
                    List<TemplateStepEditAdapter.StepEditModel> steps = new ArrayList<>();
                    if (t.getSteps() != null) {
                        for (TreatmentTemplate.TemplateStep ts : t.getSteps()) {
                            steps.add(new TemplateStepEditAdapter.StepEditModel(ts.getId(), ts.getServiceName(), ts.getMedicationDetails()));
                        }
                    }
                    adapter.setSteps(steps);
                }
            }
            @Override public void onFailure(Call<TreatmentTemplate> call, Throwable t) {}
        });
    }

    private void saveTemplate() {
        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Tên mẫu không được để trống");
            return;
        }

        AdminTreatmentPlanTemplateRequest request = new AdminTreatmentPlanTemplateRequest();
        request.setName(name);
        request.setDescription(desc);
        request.setActive(switchActive.isChecked());
        
        List<AdminTreatmentPlanTemplateRequest.StepRequest> stepRequests = new ArrayList<>();
        List<TemplateStepEditAdapter.StepEditModel> adapterSteps = adapter.getSteps();
        
        for (int i = 0; i < adapterSteps.size(); i++) {
            TemplateStepEditAdapter.StepEditModel s = adapterSteps.get(i);
            if (s.serviceId == null) {
                Toast.makeText(this, "Vui lòng chọn dịch vụ cho tất cả các bước", Toast.LENGTH_SHORT).show();
                return;
            }
            stepRequests.add(new AdminTreatmentPlanTemplateRequest.StepRequest(s.serviceId, null, i + 1, s.medicationDetails));
        }
        request.setSteps(stepRequests);

        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setEnabled(false);

        Call<TreatmentTemplate> call;
        if (templateId == null) {
            call = apiService.createTreatmentTemplate(request);
        } else {
            call = apiService.updateTreatmentTemplate(templateId, request);
        }

        call.enqueue(new Callback<TreatmentTemplate>() {
            @Override
            public void onResponse(Call<TreatmentTemplate> call, Response<TreatmentTemplate> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                    hasChanges = false;
                    Toast.makeText(AdminCreateTemplateActivity.this, "Đã lưu mẫu thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Lỗi khi lưu mẫu";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    Toast.makeText(AdminCreateTemplateActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TreatmentTemplate> call, Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(AdminCreateTemplateActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
