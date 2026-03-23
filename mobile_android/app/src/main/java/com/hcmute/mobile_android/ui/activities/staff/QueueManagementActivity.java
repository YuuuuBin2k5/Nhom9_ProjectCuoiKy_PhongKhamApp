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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.QueueAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.QueueItem;
import com.hcmute.mobile_android.network.models.RoomItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QueueManagementActivity extends AppCompatActivity implements QueueAdapter.OnQueueActionListener {

    private Spinner spinnerRooms;
    private RecyclerView rvQueue;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnRefresh;
    
    private QueueAdapter queueAdapter;
    private List<QueueItem> queueList = new ArrayList<>();
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
        rvQueue = findViewById(R.id.rvQueue);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnRefresh = findViewById(R.id.btnRefresh);
        
        // Setup RecyclerView
        rvQueue.setLayoutManager(new LinearLayoutManager(this));
        queueAdapter = new QueueAdapter(queueList, this);
        rvQueue.setAdapter(queueAdapter);
        
        // Setup refresh
        swipeRefresh.setOnRefreshListener(this::loadQueue);
        btnRefresh.setOnClickListener(v -> loadQueue());
        
        // Setup room selection
        spinnerRooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < roomList.size()) {
                    selectedRoomId = roomList.get(position).getId();
                    loadQueue();
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
                        loadQueue();
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

    private void loadQueue() {
        if (selectedRoomId == null) return;
        
        swipeRefresh.setRefreshing(true);
        
        apiService.getQueueByRoom(selectedRoomId).enqueue(new Callback<List<QueueItem>>() {
            @Override
            public void onResponse(Call<List<QueueItem>> call, Response<List<QueueItem>> response) {
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    queueList.clear();
                    queueList.addAll(response.body());
                    queueAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Lỗi tải hàng đợi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QueueItem>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(QueueManagementActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCallPatient(QueueItem item) {
        apiService.callPatient(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Đã gọi bệnh nhân: " + item.getPatientName(), Toast.LENGTH_SHORT).show();
                    loadQueue(); // Refresh queue
                } else {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Lỗi gọi bệnh nhân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QueueManagementActivity.this, 
                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTransferToXRay(QueueItem item) {
        apiService.transferToXRay(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Đã chuyển " + item.getPatientName() + " đi chụp X-Quang", Toast.LENGTH_SHORT).show();
                    loadQueue(); // Refresh queue
                } else {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Lỗi chuyển X-Quang", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QueueManagementActivity.this, 
                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
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
                    loadQueue(); // Refresh queue
                } else {
                    Toast.makeText(QueueManagementActivity.this, 
                        "Lỗi hoàn thành", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QueueManagementActivity.this, 
                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}