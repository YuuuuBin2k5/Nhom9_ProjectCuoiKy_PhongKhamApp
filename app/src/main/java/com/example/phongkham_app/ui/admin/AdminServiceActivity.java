package com.example.phongkham_app.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.local.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminServiceActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Spinner spinnerCategories;
    private RecyclerView rvServices;
    private ServiceAdminAdapter adapter;
    private List<CategoryItem> categoryList = new ArrayList<>();
    private int selectedCategoryId = -1;
    private Uri selectedImageUri;
    private ImageView imgPreview;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (imgPreview != null) {
                        imgPreview.setImageURI(selectedImageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_service);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadCategories();
    }

    private void initViews() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        spinnerCategories = findViewById(R.id.spinnerCategories);
        rvServices = findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnAddCategory).setOnClickListener(v -> showAddCategoryDialog());
        findViewById(R.id.fabAddService).setOnClickListener(v -> showAddServiceDialog());

        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = categoryList.get(position).id;
                loadServices(selectedCategoryId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadCategories() {
        categoryList.clear();
        Cursor cursor = dbHelper.getAllCategories();
        List<String> names = new ArrayList<>();
        
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            categoryList.add(new CategoryItem(id, name));
            names.add(name);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(adapter);
    }

    private void loadServices(int categoryId) {
        Cursor cursor = dbHelper.getServicesByCategory(categoryId);
        if (adapter == null) {
            adapter = new ServiceAdminAdapter(cursor, (serviceId, isActive) -> {
                dbHelper.updateServiceStatus(serviceId, isActive);
                Toast.makeText(this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
            });
            rvServices.setAdapter(adapter);
        } else {
            adapter.swapCursor(cursor);
        }
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null);
        
        EditText etName = view.findViewById(R.id.etServiceName);
        EditText etDesc = view.findViewById(R.id.etServiceDesc);
        view.findViewById(R.id.layoutPriceDuration).setVisibility(View.GONE);
        view.findViewById(R.id.layoutImagePicker).setVisibility(View.GONE);
        ((android.widget.TextView)view.findViewById(R.id.dialogTitle)).setText("Thêm danh mục mới");
        etName.setHint("Tên danh mục");

        builder.setView(view);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            if (!name.isEmpty()) {
                dbHelper.addCategory(name, desc);
                loadCategories();
                dialog.dismiss();
            }
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
        imgPreview = view.findViewById(R.id.imgPreview);
        selectedImageUri = null;

        view.findViewById(R.id.btnPickImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String durStr = etDuration.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) return;

            long serviceId = dbHelper.addService(selectedCategoryId, name, desc, 
                    Double.parseDouble(priceStr), Integer.parseInt(durStr));
            
            if (serviceId != -1 && selectedImageUri != null) {
                dbHelper.addServiceImage((int)serviceId, selectedImageUri.toString(), 1, true);
            }
            
            loadServices(selectedCategoryId);
            dialog.dismiss();
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static class CategoryItem {
        int id;
        String name;
        CategoryItem(int id, String name) { this.id = id; this.name = name; }
    }
}
