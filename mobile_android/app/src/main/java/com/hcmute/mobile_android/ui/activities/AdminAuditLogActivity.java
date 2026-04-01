package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.AuditLogResponse;
import com.hcmute.mobile_android.ui.adapters.AuditLogAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAuditLogActivity extends BaseAdminActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private AuditLogAdapter adapter;
    private ApiService apiService;
    private int currentPage = 0;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_audit_log);

        apiService = RetrofitClient.getApiService(this);
        initViews();
        loadAuditLogs(true);
    }

    private void initViews() {
        setupToolbar();
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        
        adapter = new AuditLogAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> loadAuditLogs(true));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadAuditLogs(false);
                    }
                }
            }
        });
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void loadAuditLogs(boolean refresh) {
        if (refresh) {
            currentPage = 0;
            isLastPage = false;
        }

        isLoading = true;
        if (refresh) swipeRefresh.setRefreshing(true);

        apiService.getAuditLogs(currentPage, pageSize).enqueue(new Callback<AuditLogResponse>() {
            @Override
            public void onResponse(Call<AuditLogResponse> call, Response<AuditLogResponse> response) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    AuditLogResponse data = response.body();
                    if (refresh) {
                        adapter.setLogs(data.getContent());
                    } else {
                        adapter.addLogs(data.getContent());
                    }
                    
                    currentPage++;
                    isLastPage = data.isLast();
                    updateEmptyState(adapter.getItemCount() == 0, "Trống", "Không có hoạt động nào được ghi lại", () -> loadAuditLogs(true));
                } else {
                    showError("Lỗi tải nhật ký");
                    updateEmptyState(adapter.getItemCount() == 0, "Lỗi", "Không thể tải nhật ký hoạt động", () -> loadAuditLogs(true));
                }
            }

            @Override
            public void onFailure(Call<AuditLogResponse> call, Throwable t) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);
                showError("Lỗi kết nối");
                updateEmptyState(adapter.getItemCount() == 0, "Lỗi kết nối", "Vui lòng kiểm tra lại mạng", () -> loadAuditLogs(true));
            }
        });
    }
}
