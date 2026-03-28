package com.hcmute.mobile_android.ui.activities.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

public class QueueManagementActivity extends AppCompatActivity implements QueueAdapter.OnQueueActionListener, QueueListFragment.OnRefreshRequestedListener {

    private Spinner spinnerRooms;
    private MaterialButton btnRefresh;
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private QueuePagerAdapter pagerAdapter;
    private FirebaseQueueManager firebaseQueueManager;
    private List<RoomItem> roomList = new ArrayList<>();
    
    private ApiService apiService;
    private Long selectedRoomId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_queue_management);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        loadRooms();

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
                Toast.makeText(QueueManagementActivity.this, 
                    "Lỗi tải danh sách phòng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
    }

    private void loadQueue() {
        if (selectedRoomId == null) return;
        
        apiService.getQueueByRoom(selectedRoomId).enqueue(new Callback<List<QueueItem>>() {
            @Override
            public void onResponse(Call<List<QueueItem>> call, Response<List<QueueItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterAndDistributeQueue(response.body());
                } else {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Lỗi tải hàng đợi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QueueItem>> call, Throwable t) {
                Toast.makeText(QueueManagementActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firebaseQueueManager != null) {
            firebaseQueueManager.stopListening();
        }
    }

    @Override
    public void onCallPatient(QueueItem item) {
        apiService.callPatientToRoom(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Đã gọi bệnh nhân: " + item.getPatientName(), Toast.LENGTH_SHORT).show();
                    loadQueue();
                } else {
                    Toast.makeText(QueueManagementActivity.this, "Lỗi gọi bệnh nhân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QueueManagementActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTransferToXRay(QueueItem item) {
        apiService.transferToXRay(item.getId(), new java.util.HashMap<>()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Đã chuyển " + item.getPatientName() + " đi chụp X-Quang", Toast.LENGTH_SHORT).show();
                    loadQueue();
                } else {
                    Toast.makeText(QueueManagementActivity.this, "Lỗi chuyển X-Quang", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QueueManagementActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onCompletePatient(QueueItem item) {
        apiService.completePatient(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Đã hoàn thành khám cho: " + item.getPatientName(), Toast.LENGTH_SHORT).show();
                    loadQueue();
                } else {
                    Toast.makeText(QueueManagementActivity.this, "Lỗi hoàn thành", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QueueManagementActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onExaminePatient(QueueItem item) {
        if (item.getPatientId() == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID bệnh nhân", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.content.Intent intent = new android.content.Intent(this, DoctorWorkflowActivity.class);
        intent.putExtra(DoctorWorkflowActivity.EXTRA_INITIAL_QR, "patient:" + item.getPatientId());
        Toast.makeText(this, "Đang mở hồ sơ: " + item.getPatientName(), Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}
