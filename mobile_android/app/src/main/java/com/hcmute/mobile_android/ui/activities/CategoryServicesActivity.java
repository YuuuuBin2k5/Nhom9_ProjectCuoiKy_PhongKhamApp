package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.PatientServiceAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.ServiceItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryServicesActivity extends AppCompatActivity {

    private RecyclerView rvServices;
    private PatientServiceAdapter adapter;
    private List<ServiceItem> serviceList = new ArrayList<>();
    private ApiService apiService;
    private int categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_services);

        String categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        categoryId = getIntent().getIntExtra("CATEGORY_ID", -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(categoryName != null ? categoryName : "Dịch vụ");
        toolbar.setNavigationOnClickListener(v -> finish());

        rvServices = findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientServiceAdapter(serviceList, this::onServiceClick);
        rvServices.setAdapter(adapter);

        apiService = RetrofitClient.getApiService(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        loadServices();
    }

    private void loadServices() {
        apiService.getServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serviceList.clear();
                    // Just take all for now, assuming mock category filter if needed
                    serviceList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CategoryServicesActivity.this, "Failed to load", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                Toast.makeText(CategoryServicesActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onServiceClick(ServiceItem item) {
        Intent intent = new Intent(this, ServiceDetailActivity.class);
        intent.putExtra("id", item.getId());
        intent.putExtra("name", item.getName());
        intent.putExtra("price", item.getPrice());
        intent.putExtra("description", item.getDescription());
        intent.putExtra("duration", item.getDurationMinutes());
        intent.putExtra("category", item.getCategoryName());
        if (item.getImageUrls() != null) {
            intent.putStringArrayListExtra("imageUrls", new java.util.ArrayList<>(item.getImageUrls()));
        }
        startActivity(intent);
    }
}
