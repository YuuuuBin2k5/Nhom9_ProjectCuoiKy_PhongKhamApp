package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.Receptionist;
import com.hcmute.mobile_android.ui.adapters.ReceptionistAdapter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReceptionistActivity extends BaseAdminActivity implements ReceptionistAdapter.OnReceptionistClickListener {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private ReceptionistAdapter adapter;
    private ApiService apiService;
    private com.google.android.material.appbar.MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_receptionist);

        apiService = RetrofitClient.getApiService(this);
        initViews();
        loadReceptionists();
    }

    private void initViews() {
        setupToolbar();
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        
        adapter = new ReceptionistAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupSearch(toolbar, adapter);

        swipeRefresh.setOnRefreshListener(this::loadReceptionists);

        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddReceptionistDialog());
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void loadReceptionists() {
        swipeRefresh.setRefreshing(true);
        apiService.getAdminReceptionists().enqueue(new Callback<List<Receptionist>>() {
            @Override
            public void onResponse(Call<List<Receptionist>> call, Response<List<Receptionist>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setList(response.body());
                    updateEmptyState(adapter.getItemCount() == 0, "No Receptionists", "Add your first receptionist to get started", this::loadReceptionists);
                } else {
                    showError("Lỗi tải danh sách lễ tân");
                }
            }

            @Override
            public void onFailure(Call<List<Receptionist>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                showError("Lỗi kết nối");
            }

            private void loadReceptionists() { // For retry button
                AdminReceptionistActivity.this.loadReceptionists();
            }
        });
    }

    private void showAddReceptionistDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_add_receptionist, null);
        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPhone = view.findViewById(R.id.etPhone);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Thêm Lễ tân")
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("firstName", etFirstName.getText().toString());
                    body.put("lastName", etLastName.getText().toString());
                    body.put("email", etEmail.getText().toString());
                    body.put("phone", etPhone.getText().toString());
                    
                    createReceptionist(body);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createReceptionist(Map<String, String> body) {
        showLoading(true, "Đang xử lý...");
        apiService.createReceptionist(body).enqueue(new Callback<Receptionist>() {
            @Override
            public void onResponse(Call<Receptionist> call, Response<Receptionist> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Thêm lễ tân thành công");
                    loadReceptionists();
                } else {
                    showError("Dữ liệu không hợp lệ hoặc Email đã tồn tại");
                }
            }

            @Override
            public void onFailure(Call<Receptionist> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối");
            }
        });
    }

    @Override
    public void onStatusChange(Receptionist receptionist, boolean active) {
        apiService.updateReceptionistStatus(receptionist.getId(), active).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    showSuccess("Cập nhật trạng thái thành công");
                } else {
                    showError("Không thể cập nhật trạng thái");
                    loadReceptionists(); // Revert UI
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showError("Lỗi kết nối");
                loadReceptionists(); // Revert UI
            }
        });
    }

    @Override
    public void onDelete(Receptionist receptionist) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa lễ tân " + receptionist.getFullName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    showLoading(true, "Đang xóa...");
                    apiService.deleteReceptionist(receptionist.getId()).enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                            showLoading(false);
                            if (response.isSuccessful()) {
                                showSuccess("Đã xóa lễ tân");
                                loadReceptionists();
                            } else {
                                showError("Không thể xóa lễ tân");
                            }
                        }

                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {
                            showLoading(false);
                            showError("Lỗi kết nối");
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
