package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.AdminRoomAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.RoomItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRoomActivity extends AppCompatActivity {

    private RecyclerView rvRooms;
    private AdminRoomAdapter adapter;
    private List<RoomItem> roomList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_room);

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
        
        rvRooms = findViewById(R.id.rvRooms);
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AdminRoomAdapter(roomList);
        rvRooms.setAdapter(adapter);
    }

    private void loadRooms() {
        apiService.getRooms().enqueue(new Callback<List<RoomItem>>() {
            @Override
            public void onResponse(Call<List<RoomItem>> call, Response<List<RoomItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomList.clear();
                    roomList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<RoomItem>> call, Throwable t) {
                Toast.makeText(AdminRoomActivity.this, "Lỗi tải danh sách phòng khám", Toast.LENGTH_SHORT).show();
            }
        });
    }
}