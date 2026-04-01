package com.hcmute.mobile_android.ui.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.AdminCategoryAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CreateCategoryRequest;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.ServiceCategory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoryActivity extends BaseAdminActivity implements AdminCategoryAdapter.OnCategoryActionListener {

    private RecyclerView rvCategories;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private View emptyStateView;
    private AdminCategoryAdapter adapter;
    private List<ServiceCategory> categoryList = new ArrayList<>();
    private ApiService apiService;
    private ServiceCategory editingCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_category);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        loadCategories();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        emptyStateView = findViewById(R.id.emptyStateView);
        rvCategories = findViewById(R.id.rvCategories);
        
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCategoryAdapter(categoryList, this);
        rvCategories.setAdapter(adapter);

        setupSearch(toolbar, adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddCategoryDialog());

        swipeRefresh.setOnRefreshListener(this::loadCategories);
    }

    private void loadCategories() {
        showLoading(true);
        apiService.getServiceCategories().enqueue(new Callback<List<ServiceCategory>>() {
            @Override
            public void onResponse(Call<List<ServiceCategory>> call, Response<List<ServiceCategory>> response) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    updateEmptyState(categoryList.isEmpty(), "Chưa có danh mục", "Nhấn nút + để thêm mới");
                } else {
                    showError("Lỗi tải danh mục: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ServiceCategory>> call, Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                showError("Lỗi kết nối: " + t.getMessage());
                updateEmptyState(categoryList.isEmpty(), "Lỗi kết nối", "Vui lòng kiểm tra mạng và thử lại", AdminCategoryActivity.this::loadCategories);
            }
        });
    }

    @Override
    public void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvCategories.setVisibility(show ? View.GONE : View.VISIBLE);
    }



    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        
        EditText etName = view.findViewById(R.id.etCategoryName);
        EditText etDesc = view.findViewById(R.id.etCategoryDesc);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            
            if (name.isEmpty()) {
                showError("Vui lòng nhập tên danh mục");
                return;
            }
            
            if (name.length() < 3 || name.length() > 50) {
                showError("Tên danh mục phải từ 3-50 ký tự");
                return;
            }
            
            createCategory(name, desc, dialog);
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void createCategory(String name, String desc, AlertDialog dialog) {
        showLoading(true, "Đang thêm danh mục...");
        apiService.createServiceCategory(new CreateCategoryRequest(name, desc)).enqueue(new Callback<ServiceCategory>() {
            @Override
            public void onResponse(Call<ServiceCategory> call, Response<ServiceCategory> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Thêm danh mục thành công");
                    loadCategories();
                    dialog.dismiss();
                } else {
                    showError("Lỗi khi thêm danh mục: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ServiceCategory> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    public void onEditCategory(ServiceCategory category) {
        editingCategory = category;
        showEditCategoryDialog(category);
    }

    @Override
    public void onDeleteCategory(ServiceCategory category) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa danh mục \"" + category.getName() + "\"?\n\nLưu ý: Các dịch vụ thuộc danh mục này sẽ không bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditCategoryDialog(ServiceCategory category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        
        EditText etName = view.findViewById(R.id.etCategoryName);
        EditText etDesc = view.findViewById(R.id.etCategoryDesc);

        // Pre-fill existing data
        etName.setText(category.getName());
        etDesc.setText(category.getDescription());

        builder.setView(view);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            
            if (name.isEmpty()) {
                showError("Vui lòng nhập tên danh mục");
                return;
            }
            
            if (name.length() < 3 || name.length() > 50) {
                showError("Tên danh mục phải từ 3-50 ký tự");
                return;
            }
            
            updateCategory(category.getId(), name, desc, dialog);
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            editingCategory = null;
            dialog.dismiss();
        });
        
        dialog.setOnDismissListener(d -> editingCategory = null);
        dialog.show();
    }

    private void updateCategory(int categoryId, String name, String desc, AlertDialog dialog) {
        showLoading(true, "Đang cập nhật danh mục...");
        apiService.updateCategory((long) categoryId, new CreateCategoryRequest(name, desc)).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Cập nhật danh mục thành công");
                    editingCategory = null;
                    loadCategories();
                    dialog.dismiss();
                } else {
                    showError("Lỗi khi cập nhật danh mục: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void deleteCategory(ServiceCategory category) {
        showLoading(true, "Đang xóa danh mục...");
        apiService.deleteCategory((long) category.getId()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Xóa danh mục thành công");
                    loadCategories();
                } else {
                    showErrorDialog("Lỗi khi xóa", parseErrorBody(response));
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
