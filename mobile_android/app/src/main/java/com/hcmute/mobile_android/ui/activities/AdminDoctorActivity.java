package com.hcmute.mobile_android.ui.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.AdminDoctorAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CreateDoctorRequest;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.RoomItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDoctorActivity extends BaseAdminActivity implements AdminDoctorAdapter.OnDoctorActionListener {

    private RecyclerView rvDoctors;
    private AdminDoctorAdapter adapter;
    private List<DoctorItem> doctorList = new ArrayList<>();
    private List<RoomItem> roomList = new ArrayList<>();
    private ApiService apiService;
    private DoctorItem editingDoctor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_doctor);

        apiService = RetrofitClient.getApiService(this);
        
        initViews();
        loadRooms();
        loadDoctors();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        rvDoctors = findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        
        adapter = new AdminDoctorAdapter(doctorList, this);
        rvDoctors.setAdapter(adapter);

        setupSearch(toolbar, adapter);

        FloatingActionButton fabAddDoctor = findViewById(R.id.fabAddDoctor);
        fabAddDoctor.setOnClickListener(v -> showAddDoctorDialog());
    }

    private void loadRooms() {
        apiService.getRooms().enqueue(new Callback<List<RoomItem>>() {
            @Override
            public void onResponse(Call<List<RoomItem>> call, Response<List<RoomItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    roomList.clear();
                    roomList.addAll(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<RoomItem>> call, Throwable t) {
                showError("Lỗi tải danh sách phòng: " + t.getMessage());
            }
        });
    }

    private void loadDoctors() {
        showLoading(true);
        apiService.getAdminDoctors().enqueue(new Callback<com.hcmute.mobile_android.network.models.PagedResponse<DoctorItem>>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.PagedResponse<DoctorItem>> call, Response<com.hcmute.mobile_android.network.models.PagedResponse<DoctorItem>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getContent() != null) {
                    doctorList.clear();
                    doctorList.addAll(response.body().getContent());
                    adapter.notifyDataSetChanged();
                    updateEmptyState(doctorList.isEmpty(), "Chưa có bác sĩ", "Nhấn nút + để thêm mới");
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.PagedResponse<DoctorItem>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi tải danh sách bác sĩ: " + t.getMessage());
                updateEmptyState(doctorList.isEmpty(), "Lỗi kết nối", "Vui lòng kiểm tra mạng và thử lại", AdminDoctorActivity.this::loadDoctors);
            }
        });
    }



    private void showAddDoctorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null);
        
        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etSpecialty = view.findViewById(R.id.etSpecialty);
        EditText etExperience = view.findViewById(R.id.etExperience);
        EditText etBio = view.findViewById(R.id.etBio);
        Spinner spinnerRoom = view.findViewById(R.id.spinnerRoom);

        // Setup room spinner
        setupRoomSpinner(spinnerRoom, null);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String specialty = etSpecialty.getText().toString().trim();
            String expStr = etExperience.getText().toString().trim();
            String bio = etBio.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || 
                password.isEmpty() || specialty.isEmpty() || expStr.isEmpty()) {
                showError("Vui lòng nhập đầy đủ thông tin");
                return;
            }

            try {
                int experience = Integer.parseInt(expStr);
                Long roomId = getSelectedRoomId(spinnerRoom);
                
                CreateDoctorRequest request = new CreateDoctorRequest(
                    firstName, lastName, email, password, specialty, experience, bio, roomId
                );
                
                createDoctor(request, dialog);
            } catch (NumberFormatException e) {
                showError("Kinh nghiệm phải là số");
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupRoomSpinner(Spinner spinner, String currentRoomName) {
        List<String> roomNames = new ArrayList<>();
        roomNames.add("Không gán phòng");
        for (RoomItem room : roomList) {
            roomNames.add(room.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, roomNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Select current room if editing
        if (currentRoomName != null && !currentRoomName.isEmpty()) {
            for (int i = 0; i < roomNames.size(); i++) {
                if (roomNames.get(i).equals(currentRoomName)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private Long getSelectedRoomId(Spinner spinner) {
        int position = spinner.getSelectedItemPosition();
        if (position == 0) {
            return null; // "Không gán phòng"
        }
        return roomList.get(position - 1).getId();
    }

    private void createDoctor(CreateDoctorRequest request, AlertDialog dialog) {
        showLoading(true, "Đang thêm bác sĩ...");
        apiService.createDoctor(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Thêm bác sĩ thành công");
                    loadDoctors();
                    dialog.dismiss();
                } else {
                    showError("Lỗi khi thêm bác sĩ: " + response.code());
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
    public void onEditDoctor(DoctorItem doctor) {
        editingDoctor = doctor;
        showEditDoctorDialog(doctor);
    }

    @Override
    public void onDeleteDoctor(DoctorItem doctor) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa bác sĩ \"" + doctor.getFullName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteDoctor(doctor))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditDoctorDialog(DoctorItem doctor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null);
        
        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etSpecialty = view.findViewById(R.id.etSpecialty);
        EditText etExperience = view.findViewById(R.id.etExperience);
        EditText etBio = view.findViewById(R.id.etBio);
        Spinner spinnerRoom = view.findViewById(R.id.spinnerRoom);

        // Pre-fill existing data
        String[] nameParts = doctor.getFullName().split(" ", 2);
        etFirstName.setText(nameParts.length > 0 ? nameParts[0] : "");
        etLastName.setText(nameParts.length > 1 ? nameParts[1] : "");
        etEmail.setText(doctor.getEmail());
        etSpecialty.setText(doctor.getSpecialization());
        etExperience.setText(String.valueOf(doctor.getExperienceYears() != null ? doctor.getExperienceYears() : 0));
        etBio.setText("");
        
        // Setup room spinner with current room
        setupRoomSpinner(spinnerRoom, doctor.getRoomName());
        
        // Password is optional for edit
        etPassword.setHint("Để trống nếu không đổi mật khẩu");

        builder.setView(view);
        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String specialty = etSpecialty.getText().toString().trim();
            String expStr = etExperience.getText().toString().trim();
            String bio = etBio.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || 
                specialty.isEmpty() || expStr.isEmpty()) {
                showError("Vui lòng nhập đầy đủ thông tin");
                return;
            }

            try {
                int experience = Integer.parseInt(expStr);
                Long roomId = getSelectedRoomId(spinnerRoom);
                
                CreateDoctorRequest request = new CreateDoctorRequest(
                    firstName, lastName, email, password.isEmpty() ? null : password, 
                    specialty, experience, bio, roomId
                );
                
                updateDoctor(doctor.getId(), request, dialog);
            } catch (NumberFormatException e) {
                showError("Kinh nghiệm phải là số");
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            editingDoctor = null;
            dialog.dismiss();
        });
        
        dialog.setOnDismissListener(d -> editingDoctor = null);
        dialog.show();
    }

    private void updateDoctor(Long doctorId, CreateDoctorRequest request, AlertDialog dialog) {
        showLoading(true, "Đang cập nhật bác sĩ...");
        apiService.updateDoctor(doctorId, request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Cập nhật bác sĩ thành công");
                    editingDoctor = null;
                    loadDoctors();
                    dialog.dismiss();
                } else {
                    showError("Lỗi khi cập nhật bác sĩ: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void deleteDoctor(DoctorItem doctor) {
        showLoading(true, "Đang xóa bác sĩ...");
        apiService.deleteDoctor(doctor.getId()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccess("Xóa bác sĩ thành công");
                    loadDoctors();
                } else {
                    showError("Lỗi khi xóa bác sĩ: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
