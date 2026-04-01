package com.hcmute.mobile_android.ui.activities.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import com.hcmute.mobile_android.ui.activities.BaseAdminActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.QueueAdapter;
import com.hcmute.mobile_android.adapters.QueuePagerAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.QueueItem;
import com.hcmute.mobile_android.network.models.RoomItem;
import com.hcmute.mobile_android.services.FirebaseQueueManager;
import com.hcmute.mobile_android.ui.fragments.QueueListFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QueueManagementActivity extends BaseAdminActivity implements QueueAdapter.OnQueueActionListener, QueueListFragment.OnRefreshRequestedListener {

    private Spinner spinnerRooms;
    private MaterialButton btnRefresh;
    private com.google.android.material.textfield.TextInputEditText etSearch;
    private TextView tvTotalWaiting, tvAverageWaitTime, tvPriorityCount;
    private View connectionStatus;
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private QueuePagerAdapter pagerAdapter;
    private FirebaseQueueManager firebaseQueueManager;
    private List<RoomItem> roomList = new ArrayList<>();
    private List<QueueItem> allQueueItems = new ArrayList<>();
    
    private ApiService apiService;
    private Long selectedRoomId;
    
    // Auto-refresh
    private android.os.Handler refreshHandler = new android.os.Handler();
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL = 30000; // 30 seconds
    private boolean autoRefreshEnabled = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_queue_management);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        loadRooms();
        setupAutoRefresh();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        spinnerRooms = findViewById(R.id.spinnerRooms);
        btnRefresh = findViewById(R.id.btnRefresh);
        etSearch = findViewById(R.id.etSearch);
        tvTotalWaiting = findViewById(R.id.tvTotalWaiting);
        tvAverageWaitTime = findViewById(R.id.tvAverageWaitTime);
        tvPriorityCount = findViewById(R.id.tvPriorityCount);
        
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        
        // Setup Tabs and ViewPager
        pagerAdapter = new QueuePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Explicitly pass listener so fragments have it regardless of attach timing
        pagerAdapter.getWaitingFragment().setListener(this);
        pagerAdapter.getSubClinicalFragment().setListener(this);
        pagerAdapter.getPriorityFragment().setListener(this);
        

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Đang chờ"); break;
                case 1: tab.setText("Cận lâm sàng"); break;
                case 2: tab.setText("Ưu tiên"); break;
            }
        }).attach();
        
        btnRefresh.setOnClickListener(v -> loadQueue());
        
        // Setup search
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterQueue(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // Setup room selection
        spinnerRooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < roomList.size()) {
                    selectedRoomId = roomList.get(position).getId();
                    setupFirebaseListener();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadRooms() {
        apiService.getRooms().enqueue(new Callback<List<RoomItem>>() {
            @Override
            public void onResponse(Call<List<RoomItem>> call, Response<List<RoomItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomList.clear();
                    roomList.addAll(response.body());
                    
                    List<String> roomNames = new ArrayList<>();
                    for (RoomItem room : roomList) {
                        roomNames.add(room.getName());
                    }
                    
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            QueueManagementActivity.this,
                            android.R.layout.simple_spinner_item,
                            roomNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRooms.setAdapter(adapter);
                    
                    if (!roomList.isEmpty()) {
                        selectedRoomId = roomList.get(0).getId();
                        setupFirebaseListener();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RoomItem>> call, Throwable t) {
                showError("Lỗi tải danh sách phòng: " + t.getMessage());
            }
        });
    }

    @Override
    public void onRefreshRequested() {
        loadQueue();
    }
    
    private void setupFirebaseListener() {
        if (selectedRoomId == null) return;
        
        if (firebaseQueueManager != null) {
            firebaseQueueManager.stopListening();
        }
        
        firebaseQueueManager = new FirebaseQueueManager(
            selectedRoomId, 
            this::filterAndDistributeQueue
        );
        firebaseQueueManager.startListening();
    }
    
    private void filterAndDistributeQueue(List<QueueItem> allItems) {
        // Store all items for search
        allQueueItems.clear();
        allQueueItems.addAll(allItems);
        
        // Apply search filter if needed
        String searchQuery = etSearch != null ? etSearch.getText().toString().trim() : "";
        if (!searchQuery.isEmpty()) {
            filterQueue(searchQuery);
            return;
        }
        
        List<QueueItem> waiting = new ArrayList<>();
        List<QueueItem> subClinical = new ArrayList<>();
        List<QueueItem> priority = new ArrayList<>();
        
        for (QueueItem item : allItems) {
            String status = item.getStatus();
            if (item.isPriority() || item.isReturnedPriority()) {
                priority.add(item);
            } else if ("PAUSED_FOR_TEST".equals(status)) {
                subClinical.add(item);
            } else if ("WAITING".equals(status) || "IN_PROGRESS".equals(status)) {
                if ("IN_PROGRESS".equals(status)) {
                    waiting.add(0, item);
                } else {
                    waiting.add(item);
                }
            }
        }
        
        pagerAdapter.getWaitingFragment().updateList(waiting);
        pagerAdapter.getSubClinicalFragment().updateList(subClinical);
        pagerAdapter.getPriorityFragment().updateList(priority);
        
        // Update statistics
        updateStatistics(waiting, subClinical, priority);
    }
    
    private void filterQueue(String query) {
        if (query.isEmpty()) {
            filterAndDistributeQueue(allQueueItems);
            return;
        }
        
        String lowerQuery = query.toLowerCase();
        List<QueueItem> filtered = new ArrayList<>();
        
        for (QueueItem item : allQueueItems) {
            if (item.getPatientName().toLowerCase().contains(lowerQuery) ||
                item.getPatientPhone().contains(query) ||
                String.valueOf(item.getQueueNumber()).contains(query)) {
                filtered.add(item);
            }
        }
        
        // Distribute filtered results
        List<QueueItem> waiting = new ArrayList<>();
        List<QueueItem> subClinical = new ArrayList<>();
        List<QueueItem> priority = new ArrayList<>();
        
        for (QueueItem item : filtered) {
            String status = item.getStatus();
            if (item.isPriority() || item.isReturnedPriority()) {
                priority.add(item);
            } else if ("PAUSED_FOR_TEST".equals(status)) {
                subClinical.add(item);
            } else if ("WAITING".equals(status) || "IN_PROGRESS".equals(status)) {
                if ("IN_PROGRESS".equals(status)) {
                    waiting.add(0, item);
                } else {
                    waiting.add(item);
                }
            }
        }
        
        pagerAdapter.getWaitingFragment().updateList(waiting);
        pagerAdapter.getSubClinicalFragment().updateList(subClinical);
        pagerAdapter.getPriorityFragment().updateList(priority);
        
        // Update statistics with filtered data
        updateStatistics(waiting, subClinical, priority);
    }
    
    private void updateStatistics(List<QueueItem> waiting, List<QueueItem> subClinical, List<QueueItem> priority) {
        // Total waiting (waiting + subclinical)
        int totalWaiting = waiting.size() + subClinical.size();
        tvTotalWaiting.setText(String.valueOf(totalWaiting));
        
        // Priority count
        tvPriorityCount.setText(String.valueOf(priority.size()));
        
        // Average wait time (estimate: 15 minutes per patient)
        int avgWaitTime = waiting.size() > 0 ? (waiting.size() * 15) / 2 : 0;
        tvAverageWaitTime.setText(String.valueOf(avgWaitTime));
    }

    private void loadQueue() {
        if (selectedRoomId == null) return;
        
        apiService.getQueueByRoom(selectedRoomId).enqueue(new Callback<List<QueueItem>>() {
            @Override
            public void onResponse(Call<List<QueueItem>> call, Response<List<QueueItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterAndDistributeQueue(response.body());
                } else {
                    showError("Lỗi tải hàng đợi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<QueueItem>> call, Throwable t) {
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firebaseQueueManager != null) {
            firebaseQueueManager.stopListening();
        }
        stopAutoRefresh();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (autoRefreshEnabled) {
            startAutoRefresh();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }
    
    private void setupAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (autoRefreshEnabled && selectedRoomId != null) {
                    loadQueue();
                    refreshHandler.postDelayed(this, REFRESH_INTERVAL);
                }
            }
        };
    }
    
    private void startAutoRefresh() {
        stopAutoRefresh(); // Stop any existing refresh
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }
    
    private void stopAutoRefresh() {
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onCallPatient(QueueItem item) {
        showLoading(true, "Đang gọi bệnh nhân...");
        apiService.callPatientToRoom(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Đã gọi bệnh nhân: " + item.getPatientName());
                    loadQueue();
                } else {
                    showError("Lỗi gọi bệnh nhân: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    public void onTransferToXRay(QueueItem item) {
        showLoading(true, "Đang chuyển X-Quang...");
        apiService.transferToXRay(item.getId(), new java.util.HashMap<>()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Đã chuyển " + item.getPatientName() + " đi chụp X-Quang");
                    loadQueue();
                } else {
                    showError("Lỗi chuyển X-Quang: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    public void onCompletePatient(QueueItem item) {
        showLoading(true, "Đang hoàn thành...");
        apiService.completePatient(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Đã hoàn thành khám cho: " + item.getPatientName());
                    loadQueue();
                } else {
                    showError("Lỗi hoàn thành: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    public void onSkipPatient(QueueItem item) {
        // Show confirmation dialog
        new android.app.AlertDialog.Builder(this)
            .setTitle("Lùi 1 người")
            .setMessage("Bệnh nhân " + item.getPatientName() + " sẽ quay lại hàng đợi với độ ưu tiên cao.\n\nNgười tiếp theo sẽ được gọi vào phòng.\n\nXác nhận?")
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                showLoading(true, "Đang xử lý...");
                apiService.skipPatient(item.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        showLoading(false);
                        if (response.isSuccessful()) {
                            showSuccess("Đã lùi " + item.getPatientName() + " và gọi người tiếp theo");
                            loadQueue();
                        } else {
                            showError("Lỗi: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi kết nối: " + t.getMessage());
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    public void onExaminePatient(QueueItem item) {
        if (item.getPatientId() == null) {
            showError("Lỗi: Không tìm thấy ID bệnh nhân");
            return;
        }
        
        android.content.Intent intent = new android.content.Intent(this, DoctorWorkflowActivity.class);
        intent.putExtra(DoctorWorkflowActivity.EXTRA_INITIAL_QR, "patient:" + item.getPatientId());
        
        // Pass Room Info to avoid X-Ray transfer loops
        if (selectedRoomId != null) {
            intent.putExtra("EXTRA_ROOM_ID", selectedRoomId);
            String roomName = "";
            for (com.hcmute.mobile_android.network.models.RoomItem r : roomList) {
                if (r.getId().equals(selectedRoomId)) {
                    roomName = r.getName();
                    break;
                }
            }
            intent.putExtra("EXTRA_ROOM_NAME", roomName);
        }
        
        showSuccess("Đang mở hồ sơ: " + item.getPatientName());
        startActivity(intent);
    }
}
