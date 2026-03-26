package com.hcmute.mobile_android.ui.activities;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.AdminServiceAdapter;
import com.hcmute.mobile_android.adapters.SelectedImageAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.ServiceCategory;
import com.hcmute.mobile_android.network.models.ServiceItem;
import com.hcmute.mobile_android.network.models.CreateServiceRequest;
import com.hcmute.mobile_android.network.models.CreateCategoryRequest;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.UploadResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    private List<Uri> selectedImageUris = new ArrayList<>();
    private SelectedImageAdapter selectedImageAdapter;
    private ActivityResultLauncher<String> pickImagesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_service);

        apiService = RetrofitClient.getApiService(this);
        
        initPickImagesLauncher();
        initViews();
        loadCategories(-1);
        loadServices();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initPickImagesLauncher() {
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        selectedImageUris.addAll(uris);
                        if (selectedImageAdapter != null) {
                            selectedImageAdapter.notifyDataSetChanged();
                        }
                    }
                }
        );
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
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

    private void loadCategories(int idToSelect) {
        apiService.getServiceCategories().enqueue(new Callback<List<ServiceCategory>>() {
            @Override
            public void onResponse(Call<List<ServiceCategory>> call, Response<List<ServiceCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    // Add "All" option
                    categoryList.add(new ServiceCategory(-1, "Tất cả", "Tất cả các dịch vụ"));
                    categoryList.addAll(response.body());
                    
                    List<String> categoryNames = new ArrayList<>();
                    for (ServiceCategory category : categoryList) {
                        categoryNames.add(category.getName());
                    }
                    
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(AdminServiceActivity.this, 
                            android.R.layout.simple_spinner_item, categoryNames);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategories.setAdapter(spinnerAdapter);
                    
                    if (idToSelect != -1) {
                        for (int i = 0; i < categoryList.size(); i++) {
                            if (categoryList.get(i).getId() == idToSelect) {
                                spinnerCategories.setSelection(i);
                                selectedCategoryId = idToSelect;
                                break;
                            }
                        }
                    } else if (!categoryList.isEmpty()) {
                        selectedCategoryId = categoryList.get(0).getId();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ServiceCategory>> call, Throwable t) {
                Toast.makeText(AdminServiceActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadServices() {
        apiService.getAdminServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serviceList.clear();
                    serviceList.addAll(response.body());
                    // Re-filter with current selection
                    loadServicesByCategory(selectedCategoryId);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                Toast.makeText(AdminServiceActivity.this, "Lỗi tải danh sách dịch vụ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadServicesByCategory(int categoryId) {
        List<ServiceItem> filteredServices = new ArrayList<>();
        if (categoryId == -1) {
            filteredServices.addAll(serviceList);
        } else {
            for (ServiceItem service : serviceList) {
                if (service.getCategoryId() != null && service.getCategoryId() == categoryId) {
                    filteredServices.add(service);
                }
            }
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
            
            apiService.createServiceCategory(new CreateCategoryRequest(name, desc)).enqueue(new Callback<ServiceCategory>() {
                @Override
                public void onResponse(Call<ServiceCategory> call, Response<ServiceCategory> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(AdminServiceActivity.this, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show();
                        loadCategories(response.body().getId());
                        dialog.dismiss();
                    } else {
                        Toast.makeText(AdminServiceActivity.this, "Lỗi khi thêm danh mục", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ServiceCategory> call, Throwable t) {
                    Toast.makeText(AdminServiceActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                }
            });
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAddServiceDialog() {
        if (selectedCategoryId == -1) {
            Toast.makeText(this, "Vui lòng chọn hoặc thêm danh mục trước", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedImageUris.clear();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null);
        
        EditText etName = view.findViewById(R.id.etServiceName);
        EditText etDesc = view.findViewById(R.id.etServiceDesc);
        EditText etPrice = view.findViewById(R.id.etServicePrice);
        EditText etDuration = view.findViewById(R.id.etServiceDuration);
        RecyclerView rvSelectedImages = view.findViewById(R.id.rvSelectedImages);
        MaterialButton btnPickImage = view.findViewById(R.id.btnPickImage);

        // Setup Selected Images RecyclerView
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedImageAdapter = new SelectedImageAdapter(selectedImageUris, position -> {
            selectedImageUris.remove(position);
            selectedImageAdapter.notifyItemRemoved(position);
        });
        rvSelectedImages.setAdapter(selectedImageAdapter);

        btnPickImage.setOnClickListener(v -> pickImagesLauncher.launch("image/*"));

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
                
                Toast.makeText(this, "Đang xử lý...", Toast.LENGTH_SHORT).show();
                
                if (selectedImageUris.isEmpty()) {
                    saveService(name, desc, price, duration, new ArrayList<>(), dialog);
                } else {
                    uploadImagesAndSave(name, desc, price, duration, 0, new ArrayList<>(), dialog);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giá và thời lượng phải là số", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void uploadImagesAndSave(String name, String desc, double price, int duration, int index, List<String> imageUrls, AlertDialog dialog) {
        if (index >= selectedImageUris.size()) {
            saveService(name, desc, price, duration, imageUrls, dialog);
            return;
        }

        Uri uri = selectedImageUris.get(index);
        try {
            File file = createTempFileFromUri(uri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            apiService.uploadFile(body).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        imageUrls.add(response.body().getFileName());
                        uploadImagesAndSave(name, desc, price, duration, index + 1, imageUrls, dialog);
                    } else {
                        Toast.makeText(AdminServiceActivity.this, "Lỗi khi tải ảnh " + (index + 1), Toast.LENGTH_SHORT).show();
                        saveService(name, desc, price, duration, imageUrls, dialog);
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Toast.makeText(AdminServiceActivity.this, "Lỗi kết nối khi tải ảnh", Toast.LENGTH_SHORT).show();
                    saveService(name, desc, price, duration, imageUrls, dialog);
                }
            });
        } catch (Exception e) {
            uploadImagesAndSave(name, desc, price, duration, index + 1, imageUrls, dialog);
        }
    }

    private void saveService(String name, String desc, double price, int duration, List<String> imageUrls, AlertDialog dialog) {
        CreateServiceRequest request = new CreateServiceRequest(selectedCategoryId, name, desc, price, duration, imageUrls);
        apiService.createService(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminServiceActivity.this, "Thêm dịch vụ thành công", Toast.LENGTH_SHORT).show();
                    loadServices();
                    dialog.dismiss();
                } else {
                    Toast.makeText(AdminServiceActivity.this, "Lỗi khi lưu dịch vụ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(AdminServiceActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        return tempFile;
    }
}