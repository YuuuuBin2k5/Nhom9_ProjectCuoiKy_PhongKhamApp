package com.hcmute.mobile_android.ui.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.AdminRoomAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.RoomItem;
import com.hcmute.mobile_android.network.models.RoomRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRoomActivity extends BaseAdminActivity implements AdminRoomAdapter.OnRoomActionListener {

    private RecyclerView rvRooms;
    private AdminRoomAdapter adapter;
    private List<RoomItem> roomList = new ArrayList<>();
    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private View emptyStateView;
    private FloatingActionButton fabAddRoom;

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
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyStateView = findViewById(R.id.emptyStateView);
        fabAddRoom = findViewById(R.id.fabAddRoom);
        
        rvRooms = findViewById(R.id.rvRooms);
        rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
        rvRooms.setPadding(8, 8, 8, 80);
        rvRooms.setClipToPadding(false);
        
        adapter = new AdminRoomAdapter(roomList, this);
        rvRooms.setAdapter(adapter);
        
        setupSearch(toolbar, adapter);
        
        // Setup listeners
        fabAddRoom.setOnClickListener(v -> showAddRoomDialog());
        swipeRefreshLayout.setOnRefreshListener(this::loadRooms);
    }

    private void loadRooms() {
        showLoading(true);
        
        apiService.getRooms().enqueue(new Callback<List<RoomItem>>() {
            @Override
            public void onResponse(Call<List<RoomItem>> call, Response<List<RoomItem>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    roomList.clear();
                    roomList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState(roomList.isEmpty(), "Chưa có phòng khám", "Nhấn nút + để thêm mới");
                } else {
                    showError("Lỗi tải danh sách: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RoomItem>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                showError("Lỗi kết nối: " + t.getMessage());
                updateEmptyState(roomList.isEmpty(), "Lỗi kết nối", "Vui lòng kiểm tra mạng và thử lại", AdminRoomActivity.this::loadRooms);
            }
        });
    }

    private void showAddRoomDialog() {
        showRoomDialog(null);
    }

    private void showEditRoomDialog(RoomItem room) {
        showRoomDialog(room);
    }

    private void showRoomDialog(RoomItem existingRoom) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
        
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etName = view.findViewById(R.id.etRoomName);
        EditText etDesc = view.findViewById(R.id.etRoomDescription);
        
        // Pre-fill data if editing
        if (existingRoom != null) {
            tvTitle.setText("Sửa Phòng Khám");
            etName.setText(existingRoom.getName());
        }
        
        builder.setView(view);
        AlertDialog dialog = builder.create();
        
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            
            // Validation
            if (name.isEmpty()) {
                etName.setError("Vui lòng nhập tên phòng");
                etName.requestFocus();
                return;
            }
            
            if (name.length() < 3) {
                etName.setError("Tên phòng phải có ít nhất 3 ký tự");
                etName.requestFocus();
                return;
            }
            
            if (name.length() > 50) {
                etName.setError("Tên phòng không được quá 50 ký tự");
                etName.requestFocus();
                return;
            }
            
            if (existingRoom != null) {
                updateRoom(existingRoom.getId(), name, desc, dialog);
            } else {
                createRoom(name, desc, dialog);
            }
        });
        
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void createRoom(String name, String description, AlertDialog dialog) {
        showLoading(true, "Đang thêm phòng...");
        RoomRequest request = new RoomRequest(name, description);
        
        apiService.createRoom(request).enqueue(new Callback<RoomItem>() {
            @Override
            public void onResponse(Call<RoomItem> call, Response<RoomItem> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    showSuccess("Thêm phòng thành công");
                    loadRooms();
                    dialog.dismiss();
                } else {
                    showError("Lỗi khi thêm phòng: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<RoomItem> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void updateRoom(Long id, String name, String description, AlertDialog dialog) {
        showLoading(true, "Đang cập nhật...");
        RoomRequest request = new RoomRequest(name, description);
        
        apiService.updateRoom(id, request).enqueue(new Callback<RoomItem>() {
            @Override
            public void onResponse(Call<RoomItem> call, Response<RoomItem> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    showSuccess("Cập nhật thành công");
                    loadRooms();
                    dialog.dismiss();
                } else {
                    showError("Lỗi khi cập nhật: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<RoomItem> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void deleteRoom(RoomItem room) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa phòng \"" + room.getName() + "\"?\n\nLưu ý: Phòng sẽ bị vô hiệu hóa, không bị xóa vĩnh viễn.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                showLoading(true, "Đang xóa phòng...");
                apiService.deleteRoom(room.getId()).enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                        showLoading(false);
                        if (response.isSuccessful()) {
                            showSuccess("Đã xóa phòng");
                            loadRooms();
                        } else {
                            showError("Lỗi khi xóa: " + response.code());
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi kết nối: " + t.getMessage());
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void toggleRoomStatus(RoomItem room) {
        boolean newStatus = !room.isActive();
        showLoading(true, "Đang cập nhật trạng thái...");
        
        apiService.updateRoomStatus(room.getId(), newStatus).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    String message = newStatus ? "Đã kích hoạt phòng" : "Đã vô hiệu hóa phòng";
                    showSuccess(message);
                    loadRooms();
                } else {
                    showError("Lỗi khi cập nhật: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    public void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (!show) {
            super.showLoading(false, "");
        }
    }



    // Implement OnRoomActionListener
    @Override
    public void onRoomClick(RoomItem room) {
        showEditRoomDialog(room);
    }

    @Override
    public void onRoomEdit(RoomItem room) {
        showEditRoomDialog(room);
    }

    @Override
    public void onRoomDelete(RoomItem room) {
        deleteRoom(room);
    }

    @Override
    public void onRoomToggleStatus(RoomItem room) {
        toggleRoomStatus(room);
    }
}
