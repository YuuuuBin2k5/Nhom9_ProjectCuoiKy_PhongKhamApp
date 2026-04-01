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
import com.hcmute.mobile_android.models.ImageSource;
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

public class AdminServiceActivity extends BaseAdminActivity implements AdminServiceAdapter.OnServiceActionListener {

    private RecyclerView rvServices;
    private AdminServiceAdapter adapter;
    private List<ServiceItem> serviceList = new ArrayList<>();
    private List<ServiceCategory> categoryList = new ArrayList<>();
    private ApiService apiService;
    private Spinner spinnerCategories;
    private MaterialButton btnAddCategory, btnDeleteCategory;
    private ExtendedFloatingActionButton fabAddService;
    private int selectedCategoryId = -1;

    private List<ImageSource> selectedImageSources = new ArrayList<>();
    private SelectedImageAdapter selectedImageAdapter;
    private ActivityResultLauncher<String> pickImagesLauncher;
    private ServiceItem editingService = null;

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
                        for (Uri uri : uris) {
                            selectedImageSources.add(new ImageSource(uri));
                        }
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
        btnDeleteCategory = findViewById(R.id.btnDeleteCategory);
        fabAddService = findViewById(R.id.fabAddService);
        
        rvServices = findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AdminServiceAdapter(serviceList, this);
        rvServices.setAdapter(adapter);

        setupSearch(toolbar, adapter);

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        btnDeleteCategory.setOnClickListener(v -> confirmDeleteCategory());
        fabAddService.setOnClickListener(v -> showAddServiceDialog());
        
        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < categoryList.size()) {
                    selectedCategoryId = categoryList.get(position).getId();
                    loadServicesByCategory(selectedCategoryId);
                    btnDeleteCategory.setVisibility(selectedCategoryId == -1 ? View.GONE : View.VISIBLE);
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
                showError("Lỗi tải danh mục: " + t.getMessage());
            }
        });
    }

    private void loadServices() {
        showLoading(true);
        apiService.getAdminServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    serviceList.clear();
                    serviceList.addAll(response.body());
                    adapter.updateServices(serviceList);
                    updateEmptyState(serviceList.isEmpty(), "Chưa có dịch vụ", "Nhấn nút + để thêm mới");
                    // Re-filter with current selection
                    loadServicesByCategory(selectedCategoryId);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi tải danh sách dịch vụ: " + t.getMessage());
                updateEmptyState(serviceList.isEmpty(), "Lỗi kết nối", "Vui lòng kiểm tra mạng và thử lại", AdminServiceActivity.this::loadServices);
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
                showError("Vui lòng nhập tên danh mục");
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
                if (response.isSuccessful() && response.body() != null) {
                    showSuccess("Thêm danh mục thành công");
                    loadCategories(response.body().getId());
                    dialog.dismiss();
                } else {
                    showError("Lỗi khi thêm danh mục: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ServiceCategory> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void showAddServiceDialog() {
        if (selectedCategoryId == -1) {
            showError("Vui lòng chọn hoặc thêm danh mục trước");
            return;
        }

        selectedImageSources.clear();
        
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
        selectedImageAdapter = new SelectedImageAdapter(selectedImageSources, position -> {
            selectedImageSources.remove(position);
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
                showError("Vui lòng nhập đầy đủ thông tin");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int duration = Integer.parseInt(durStr);
                
                showLoading(true, "Đang xử lý...");
                
                if (selectedImageSources.isEmpty()) {
                    saveService(name, desc, price, duration, new ArrayList<>(), dialog);
                } else {
                    uploadImagesAndSave(name, desc, price, duration, 0, new ArrayList<>(), dialog);
                }
            } catch (NumberFormatException e) {
                showError("Giá và thời lượng phải là số");
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void uploadImagesAndSave(String name, String desc, double price, int duration, int index, List<String> imageUrls, AlertDialog dialog) {
        // Get only new URIs (not URLs)
        List<Uri> newUris = new ArrayList<>();
        for (ImageSource source : selectedImageSources) {
            if (!source.isUrl()) {
                newUris.add(source.getUri());
            }
        }
        
        if (index >= newUris.size()) {
            saveService(name, desc, price, duration, imageUrls, dialog);
            return;
        }

        Uri uri = newUris.get(index);
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
                        showError("Lỗi khi tải ảnh " + (index + 1));
                        saveService(name, desc, price, duration, imageUrls, dialog);
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    showError("Lỗi kết nối khi tải ảnh: " + t.getMessage());
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
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Thêm dịch vụ thành công");
                    loadServices();
                    dialog.dismiss();
                } else {
                    showError("Lỗi khi lưu dịch vụ: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối mạng: " + t.getMessage());
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

    @Override
    public void onEditService(ServiceItem service) {
        editingService = service;
        showEditServiceDialog(service);
    }

    @Override
    public void onDeleteService(ServiceItem service) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa dịch vụ \"" + service.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteService(service))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditServiceDialog(ServiceItem service) {
        selectedImageSources.clear();
        
        // Load existing images as URLs
        if (service.getImageUrls() != null && !service.getImageUrls().isEmpty()) {
            for (String url : service.getImageUrls()) {
                selectedImageSources.add(new ImageSource(url));
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null);
        
        EditText etName = view.findViewById(R.id.etServiceName);
        EditText etDesc = view.findViewById(R.id.etServiceDesc);
        EditText etPrice = view.findViewById(R.id.etServicePrice);
        EditText etDuration = view.findViewById(R.id.etServiceDuration);
        RecyclerView rvSelectedImages = view.findViewById(R.id.rvSelectedImages);
        MaterialButton btnPickImage = view.findViewById(R.id.btnPickImage);

        // Pre-fill existing data
        etName.setText(service.getName());
        etDesc.setText(service.getDescription());
        etPrice.setText(String.valueOf(service.getPrice()));
        etDuration.setText(String.valueOf(service.getDurationMinutes()));

        // Setup Selected Images RecyclerView
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedImageAdapter = new SelectedImageAdapter(selectedImageSources, position -> {
            selectedImageSources.remove(position);
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
                showError("Vui lòng nhập đầy đủ thông tin");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int duration = Integer.parseInt(durStr);
                
                showLoading(true, "Đang xử lý...");
                
                // Separate URLs and URIs
                List<String> existingUrls = new ArrayList<>();
                List<Uri> newUris = new ArrayList<>();
                
                for (ImageSource source : selectedImageSources) {
                    if (source.isUrl()) {
                        existingUrls.add(source.getUrl());
                    } else {
                        newUris.add(source.getUri());
                    }
                }
                
                if (newUris.isEmpty()) {
                    // No new images, just update with existing URLs
                    updateService(service.getId(), service.getCategoryId(), name, desc, price, duration, existingUrls, dialog);
                } else {
                    // Upload new images and combine with existing URLs
                    uploadImagesAndUpdate(service.getId(), service.getCategoryId(), name, desc, price, duration, 0, existingUrls, newUris, dialog);
                }
            } catch (NumberFormatException e) {
                showError("Giá và thời lượng phải là số");
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            editingService = null;
            dialog.dismiss();
        });
        
        dialog.setOnDismissListener(d -> editingService = null);
        dialog.show();
    }

    private void uploadImagesAndUpdate(Long serviceId, Integer categoryId, String name, String desc, double price, int duration, int index, List<String> existingUrls, List<Uri> newUris, AlertDialog dialog) {
        if (index >= newUris.size()) {
            updateService(serviceId, categoryId, name, desc, price, duration, existingUrls, dialog);
            return;
        }

        Uri uri = newUris.get(index);
        try {
            File file = createTempFileFromUri(uri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            apiService.uploadFile(body).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        existingUrls.add(response.body().getFileName());
                        uploadImagesAndUpdate(serviceId, categoryId, name, desc, price, duration, index + 1, existingUrls, newUris, dialog);
                    } else {
                        showError("Lỗi khi tải ảnh " + (index + 1));
                        updateService(serviceId, categoryId, name, desc, price, duration, existingUrls, dialog);
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    showError("Lỗi kết nối khi tải ảnh: " + t.getMessage());
                    updateService(serviceId, categoryId, name, desc, price, duration, existingUrls, dialog);
                }
            });
        } catch (Exception e) {
            uploadImagesAndUpdate(serviceId, categoryId, name, desc, price, duration, index + 1, existingUrls, newUris, dialog);
        }
    }

    private void updateService(Long serviceId, Integer categoryId, String name, String desc, double price, int duration, List<String> imageUrls, AlertDialog dialog) {
        int catId = (categoryId != null && categoryId != -1) ? categoryId : selectedCategoryId;
        CreateServiceRequest request = new CreateServiceRequest(catId, name, desc, price, duration, imageUrls);
        apiService.updateService(serviceId, request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Cập nhật dịch vụ thành công");
                    editingService = null;
                    loadServices();
                    dialog.dismiss();
                } else {
                    showError("Lỗi khi cập nhật dịch vụ: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void deleteService(ServiceItem service) {
        showLoading(true, "Đang xóa...");
        apiService.deleteService(service.getId()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Xóa dịch vụ thành công");
                    loadServices();
                } else {
                    showErrorDialog("Lỗi khi xóa", parseErrorBody(response));
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void confirmDeleteCategory() {
        if (selectedCategoryId == -1) return;

        String categoryName = "";
        for (ServiceCategory cat : categoryList) {
            if (cat.getId() == selectedCategoryId) {
                categoryName = cat.getName();
                break;
            }
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa danh mục \"" + categoryName + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategory(selectedCategoryId))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCategory(int categoryId) {
        showLoading(true, "Đang xóa danh mục...");
        apiService.deleteCategory((long) categoryId).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Xóa danh mục thành công");
                    loadCategories(-1);
                    loadServices();
                } else {
                    showErrorDialog("Lỗi khi xóa", parseErrorBody(response));
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }
}