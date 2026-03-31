package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.AdminTreatmentTemplateAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.TreatmentTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTreatmentTemplateActivity extends AppCompatActivity implements AdminTreatmentTemplateAdapter.OnTemplateClickListener {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTemplates;
    private AdminTreatmentTemplateAdapter adapter;
    private ApiService apiService;
    private View emptyState, progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_treatment_template);

        apiService = RetrofitClient.getApiService(this);
        initViews();
        loadTemplates();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvTemplates = findViewById(R.id.rvTemplates);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAddTemplate);

        rvTemplates.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminTreatmentTemplateAdapter(this);
        rvTemplates.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadTemplates);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCreateTemplateActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTemplates();
    }

    private void loadTemplates() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getAdminTreatmentTemplates().enqueue(new Callback<List<TreatmentTemplate>>() {
            @Override
            public void onResponse(Call<List<TreatmentTemplate>> call, Response<List<TreatmentTemplate>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<TreatmentTemplate> templates = response.body();
                    adapter.setTemplates(templates);
                    emptyState.setVisibility(templates.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(AdminTreatmentTemplateActivity.this, "Lỗi khi tải danh sách mẫu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TreatmentTemplate>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminTreatmentTemplateActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTemplateClick(TreatmentTemplate template) {
        Intent intent = new Intent(this, AdminCreateTemplateActivity.class);
        intent.putExtra("template_id", template.getId());
        startActivity(intent);
    }

    @Override
    public void onMenuClick(View view, TreatmentTemplate template) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Chỉnh sửa");
        popup.getMenu().add("Xóa").getIcon();
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Chỉnh sửa")) {
                onTemplateClick(template);
            } else if (item.getTitle().equals("Xóa")) {
                confirmDelete(template);
            }
            return true;
        });
        popup.show();
    }

    private void confirmDelete(TreatmentTemplate template) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa mẫu liệu trình")
                .setMessage("Bạn có chắc chắn muốn xóa mẫu \"" + template.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTemplate(template))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteTemplate(TreatmentTemplate template) {
        apiService.deleteTreatmentTemplate(template.getId()).enqueue(new Callback<com.hcmute.mobile_android.network.models.MessageResponse>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Response<com.hcmute.mobile_android.network.models.MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminTreatmentTemplateActivity.this, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                    loadTemplates();
                } else {
                    Toast.makeText(AdminTreatmentTemplateActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.MessageResponse> call, Throwable t) {
                Toast.makeText(AdminTreatmentTemplateActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
