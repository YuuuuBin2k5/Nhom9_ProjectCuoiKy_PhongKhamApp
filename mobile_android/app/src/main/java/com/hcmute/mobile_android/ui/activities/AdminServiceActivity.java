package com.hcmute.mobile_android.ui.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.AdminServiceAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.ServiceCategory;
import com.hcmute.mobile_android.network.models.ServiceItem;
import com.hcmute.mobile_android.network.models.CreateServiceRequest;
import com.hcmute.mobile_android.network.models.MessageResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminServiceActivity extends AppCompatActivity {

    private RecyclerView rvServices;
    private AdminServiceAdapter adapter;
    private List<ServiceItem> serviceList = new ArrayList<>();
    private List<ServiceCategory> categoryList = new ArrayList<>();
    private ApiService apiService;
    private Spinner spinnerCategories;
    private MaterialButton btnAddCategory;
    private ExtendedFloatingActionButton fabAddService;
    private int selectedCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_service);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        loadCategories();
        loadServices();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        spinnerCategories = findViewById(R.id.spinnerCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        fabAddService = findViewById(R.id.fabAddService);
        
        rvServices = findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AdminServiceAdapter(serviceList);
        rvServices.setAdapter(adapter);

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        fabAddService.setOnClickListener(v -> showAddServiceDialog());
        
        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < categoryList.size()) {
                    selectedCategoryId = categoryList.get(position).getId();
                    loadServicesByCategory(selectedCategoryId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadCategories() {
        // TODO: Implement API call to get categories
        // For now, create dummy categories
        categoryList.clear();
        categoryList.add(new ServiceCategory(1, "Khám chữa bệnh", "Các dịch vụ khám và điều trị cơ bản"));
        categoryList.add(new ServiceCategory(2, "Chẩn đoán hình ảnh", "Chụp X-Quang và các dịch vụ chẩn đoán"));
        categoryList.add(new ServiceCategory(3, "Thẩm mỹ răng", "Các dịch vụ làm đẹp răng miệng"));
        categoryList.add(new ServiceCategory(4, "Tiểu phẫu", "Các dịch vụ phẫu thuật nhỏ"));
        
        List<String> categoryNames = new ArrayList<>();
        for (ServiceCategory category : categoryList) {
            categoryNames.add(category.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(adapter);
        
        if (!categoryList.isEmpty()) {
            selectedCategoryId = categoryList.get(0).getId();
        }
    }

    private void loadServices() {
        apiService.getServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serviceList.clear();
                    serviceList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                Toast.makeText(AdminServiceActivity.this, "Lỗi tải danh sách dịch vụ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadServicesByCategory(int categoryId) {
        // Filter services by category
        List<ServiceItem> filteredServices = new ArrayList<>();
        for (ServiceItem service : serviceList) {
            // TODO: Add category filtering when API supports it
            filteredServices.add(service);
        }
        adapter.updateServices(filteredServices);
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
                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // TODO: Implement API call to create category
            Toast.makeText(this, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAddServiceDialog() {
        if (selectedCategoryId == -1) {
            Toast.makeText(this, "Vui lòng chọn danh mục trước", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null);
        
        EditText etName = view.findViewById(R.id.etServiceName);
        EditText etDesc = view.findViewById(R.id.etServiceDesc);
        EditText etPrice = view.findViewById(R.id.etServicePrice);
        EditText etDuration = view.findViewById(R.id.etServiceDuration);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String durStr = etDuration.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || durStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int duration = Integer.parseInt(durStr);
                
                // TODO: Implement API call to create service
                Toast.makeText(this, "Thêm dịch vụ thành công", Toast.LENGTH_SHORT).show();
                loadServices();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giá và thời lượng phải là số", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}