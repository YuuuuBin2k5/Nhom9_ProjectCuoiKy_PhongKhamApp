package com.hcmute.mobile_android.ui.fragments;

import com.hcmute.mobile_android.ui.activities.AppointmentDetailActivity;
import com.hcmute.mobile_android.util.TokenManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.UpcomingAppointmentAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CheckInMyStatusResponse;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.network.models.ServiceItem;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;
import com.hcmute.mobile_android.ui.activities.GenericListActivity;
import com.hcmute.mobile_android.ui.activities.PatientQueueActivity;
import com.hcmute.mobile_android.ui.activities.QRCheckInActivity;
import com.hcmute.mobile_android.ui.activities.ServiceDetailActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDashboardFragment extends Fragment {

    // Views
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvPatientName;
    private ImageView ivAvatar;

    // Header Actions
    private MaterialButton btnEmergency;

    // Grid / Slide lists
    private RecyclerView rvCategories, rvServices, rvDoctors;
    private CategoryAdapter categoryAdapterPremium;
    private ServiceAdapter serviceAdapter;
    private DoctorAdapter doctorAdapter;
    private List<ServiceItem> allServices = new ArrayList<>();

    // Check-in
    private MaterialCardView cardCheckInStatus;
    private TextView tvCheckInStatus, tvQueuePosition;
    private MaterialButton btnViewQueue;

    // Appointments
    private RecyclerView rvUpcomingAppointments;
    private UpcomingAppointmentAdapter appointmentAdapter;
    private List<UpcomingAppointment> upcomingAppointments = new ArrayList<>();

    // Data
    private ApiService apiService;
    private PatientMeResponse currentPatient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        apiService = RetrofitClient.getApiService(requireContext());
        
        initViews(view);
        setupAdapters();
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvPatientName = view.findViewById(R.id.tvPatientName);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        
        btnEmergency = view.findViewById(R.id.btnEmergency); 
        
        View btnQrScan = view.findViewById(R.id.btn_qr_scan); 
        if (btnQrScan != null) btnQrScan.setOnClickListener(v -> openCheckIn());
        
        rvCategories = view.findViewById(R.id.rvCategories);
        rvServices = view.findViewById(R.id.rv_services);
        rvDoctors = view.findViewById(R.id.rv_doctors);
        
        cardCheckInStatus = view.findViewById(R.id.cardCheckInStatus);
        tvCheckInStatus = view.findViewById(R.id.tvCheckInStatus);
        tvQueuePosition = view.findViewById(R.id.tvQueuePosition);
        btnViewQueue = view.findViewById(R.id.btnViewQueue);
        
        rvUpcomingAppointments = view.findViewById(R.id.rvUpcomingAppointments);
        
        swipeRefresh.setOnRefreshListener(this::loadPatientData);
        btnViewQueue.setOnClickListener(v -> openQueueStatus());
        btnEmergency.setOnClickListener(v -> Toast.makeText(requireContext(), "Call Emergency", Toast.LENGTH_SHORT).show());
        
        View btnAllDv = view.findViewById(R.id.all_dv);
        if (btnAllDv != null) btnAllDv.setOnClickListener(v -> openList(GenericListActivity.MODE_SERVICES));

        View btnAllBs = view.findViewById(R.id.all_bs);
        if (btnAllBs != null) btnAllBs.setOnClickListener(v -> openList(GenericListActivity.MODE_DOCTORS));
    }

    private void setupAdapters() {
        if (!isAdded()) return;

        // Categories
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapterPremium = new CategoryAdapter();
        rvCategories.setAdapter(categoryAdapterPremium);
        
        // Services
        rvServices.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        serviceAdapter = new ServiceAdapter();
        rvServices.setAdapter(serviceAdapter);
        
        // Doctors
        rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        doctorAdapter = new DoctorAdapter();
        rvDoctors.setAdapter(doctorAdapter);

        // Upcoming appointments
        rvUpcomingAppointments.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        appointmentAdapter = new UpcomingAppointmentAdapter(upcomingAppointments, this::onAppointmentClick);
        rvUpcomingAppointments.setAdapter(appointmentAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load data on resume to ensure it's fresh after returning from other activities
        if (apiService != null) {
            loadPatientData();
        }
    }

    private void loadPatientData() {
        if (!isAdded()) return;
        swipeRefresh.setRefreshing(true);
        loadPatientInfo();
        loadCheckInStatus();
        loadUpcomingAppointments();
        loadServices();
        loadDoctors();
    }

    private void loadPatientInfo() {
        apiService.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (!isAdded()) return;

                // Always ensure click listener is set even if data fails to load
                View avatarContainer = getView() != null ? getView().findViewById(R.id.rlAvatarContainer) : null;
                if (avatarContainer != null) {
                    avatarContainer.setOnClickListener(v -> {
                        startActivity(new Intent(requireContext(), com.hcmute.mobile_android.ui.activities.ProfileActivity.class));
                    });
                }

                if (response.isSuccessful() && response.body() != null) {
                    currentPatient = response.body();
                    if (currentPatient.getId() != null) {
                        new TokenManager(requireContext()).savePatientId(currentPatient.getId());
                    }
                    String fullName = (currentPatient.getLastName() + " " + currentPatient.getFirstName()).trim();
                    tvPatientName.setText("Hi " + fullName + " \uD83D\uDC4B");
                    
                    // Check missing profile info
                    boolean isMissingInfo = currentPatient.getPhone() == null || currentPatient.getPhone().isEmpty() ||
                            currentPatient.getEmail() == null || currentPatient.getEmail().isEmpty() ||
                            currentPatient.getAddress() == null || currentPatient.getAddress().isEmpty();
                    
                    View warningIcon = getView() != null ? getView().findViewById(R.id.ivWarningProfile) : null;
                    if (warningIcon != null) {
                        warningIcon.setVisibility(isMissingInfo ? View.VISIBLE : View.GONE);
                    }
                } else if (response.code() == 404 || response.code() == 401) {
                    // Patient not found in this database instance
                    Toast.makeText(requireContext(), "Phiên làm việc hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    logout();
                }
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                if (!isAdded()) return;
                // Still ensure click listener is set on network failure
                View avatarContainer = getView() != null ? getView().findViewById(R.id.rlAvatarContainer) : null;
                if (avatarContainer != null) {
                    avatarContainer.setOnClickListener(v -> {
                        startActivity(new Intent(requireContext(), com.hcmute.mobile_android.ui.activities.ProfileActivity.class));
                    });
                }
                Toast.makeText(requireContext(), "Lỗi tải thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void loadCheckInStatus() {
        apiService.getMyCheckInStatus().enqueue(new Callback<CheckInMyStatusResponse>() {
            @Override
            public void onResponse(Call<CheckInMyStatusResponse> call, Response<CheckInMyStatusResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    CheckInMyStatusResponse status = response.body();
                    if (status.isCheckedIn()) {
                        cardCheckInStatus.setVisibility(View.VISIBLE);
                        tvCheckInStatus.setText("Đã nhận Check-in");
                        tvQueuePosition.setText("Số thứ tự chờ: " + status.getQueueNumber() + " (Khoảng " + status.getEstimatedWaitTime() + " phút)");
                    } else {
                        cardCheckInStatus.setVisibility(View.GONE);
                    }
                } else {
                    cardCheckInStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<CheckInMyStatusResponse> call, Throwable t) {
                if (!isAdded()) return;
                cardCheckInStatus.setVisibility(View.GONE);
            }
        });
    }

    private void loadUpcomingAppointments() {
        apiService.getUpcomingAppointments().enqueue(new Callback<List<UpcomingAppointment>>() {
            @Override
            public void onResponse(Call<List<UpcomingAppointment>> call, Response<List<UpcomingAppointment>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    upcomingAppointments.clear();
                    upcomingAppointments.addAll(response.body());
                    appointmentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<UpcomingAppointment>> call, Throwable t) {}
        });
    }

    private void loadServices() {
        apiService.getServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    allServices = response.body();
                    serviceAdapter.updateItems(allServices);
                    extractCategories(allServices);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                if (!isAdded()) return;
                allServices = new ArrayList<>();
                serviceAdapter.updateItems(allServices);
            }
        });
    }

    private void loadDoctors() {
        apiService.getDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    doctorAdapter.updateItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                if (!isAdded()) return;
                doctorAdapter.updateItems(new ArrayList<>());
            }
        });
    }

    private void openCheckIn() {
        if (!isAdded()) return;
        
        String[] options = {"Quét mã QR Check-in", "Đặt lịch hẹn khám"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Bạn muốn làm gì?");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Lựa chọn: Điểm danh / Check-in
                if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.MainActivity) {
                    ((com.hcmute.mobile_android.ui.activities.MainActivity) getActivity()).onNavigateToQr();
                }
            } else {
                // Lựa chọn: Đặt lịch hẹn
                try {
                    startActivity(new Intent(requireContext(), com.hcmute.mobile_android.ui.activities.BookAppointmentActivity.class));
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Không thể mở màn hình đặt lịch", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void openQueueStatus() {
        if (isAdded()) {
            startActivity(new Intent(requireContext(), PatientQueueActivity.class));
        }
    }

    private void onAppointmentClick(UpcomingAppointment appointment) {
        if (isAdded()) {
            Intent intent = new Intent(requireContext(), AppointmentDetailActivity.class);
            intent.putExtra("appointmentId", appointment.getId());
            intent.putExtra("datetime", appointment.getAppointmentTime());
            intent.putExtra("serviceName", appointment.getServiceName());
            intent.putExtra("doctorName", appointment.getDoctorName());
            intent.putExtra("status", appointment.getStatus());
            startActivity(intent);
        }
    }

    private void logout() {
        if (!isAdded()) return;
        new TokenManager(requireContext()).clearToken();
        startActivity(new Intent(requireContext(), com.hcmute.mobile_android.ui.activities.LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void openList(String mode) {
        Intent i = new Intent(requireContext(), GenericListActivity.class);
        i.putExtra(GenericListActivity.EXTRA_MODE, mode);
        startActivity(i);
    }
    
    private void filterServices(String category) {
        if (category == null || category.equals("All")) {
            serviceAdapter.updateItems(allServices);
            return;
        }
        List<ServiceItem> filtered = new ArrayList<>();
        for (ServiceItem s : allServices) {
            if (category.equals(s.getCategoryName())) {
                filtered.add(s);
            }
        }
        serviceAdapter.updateItems(filtered);
    }

    private void extractCategories(List<ServiceItem> list) {
        List<String> cats = new ArrayList<>();
        for (ServiceItem s : list) {
            String c = s.getCategoryName();
            if (c != null && !c.isEmpty() && !cats.contains(c)) {
                cats.add(c);
            }
        }
        if (categoryAdapterPremium != null) {
            categoryAdapterPremium.updateItems(cats);
        }
    }

    private static class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.Holder> {
        private List<ServiceItem> items = new ArrayList<>();

        ServiceAdapter() {}

        void updateItems(List<ServiceItem> list) {
            items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_premium, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ServiceItem s = items.get(position);
            holder.tvName.setText(s.getName() != null ? s.getName() : "Dịch vụ");
            holder.tvPrice.setText(formatPrice(s.getPrice()));
            int dur = s.getDurationMinutes() != null ? s.getDurationMinutes() : 0;
            holder.tvDuration.setText(dur + " phút •");
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ServiceDetailActivity.class);
                intent.putExtra("id", s.getId());
                intent.putExtra("name", s.getName());
                intent.putExtra("price", s.getPrice());
                intent.putExtra("duration", s.getDurationMinutes() != null ? s.getDurationMinutes() : 0);
                intent.putExtra("description", s.getDescription());
                intent.putExtra("category", s.getCategoryName());
                if (s.getImageUrls() != null) {
                    intent.putStringArrayListExtra("imageUrls", new java.util.ArrayList<>(s.getImageUrls()));
                }
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String formatPrice(double p) {
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format((long) p) + "đ";
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice, tvDuration;

            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvServiceName);
                tvPrice = v.findViewById(R.id.tvServicePrice);
                tvDuration = v.findViewById(R.id.tvDuration);
            }
        }
    }

    private static class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.Holder> {
        private List<DoctorItem> items = new ArrayList<>();

        DoctorAdapter() {}

        void updateItems(List<DoctorItem> list) {
            items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_suggested_premium, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            DoctorItem d = items.get(position);
            holder.tvName.setText("BS. " + d.getFullName());
            holder.tvSpecialization.setText(d.getSpecialization() != null && !d.getSpecialization().isEmpty()
                    ? d.getSpecialization() : "Bác sĩ Gia đình");
            holder.tvRating.setText("4." + (8 - (position % 4))); // Mock rating

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), com.hcmute.mobile_android.ui.activities.DoctorDetailActivity.class);
                intent.putExtra("doctorId", d.getId());
                intent.putExtra("doctorName", "BS. " + d.getFullName());
                intent.putExtra("specialization", d.getSpecialization() != null && !d.getSpecialization().isEmpty()
                        ? d.getSpecialization() : "Bác sĩ Gia đình");
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvSpecialization, tvRating;

            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvDoctorName);
                tvSpecialization = v.findViewById(R.id.tvSpecialization);
                tvRating = v.findViewById(R.id.tvRating);
            }
        }
    }
    
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Holder> {
        private List<String> items = new ArrayList<>();
        private int selectedPosition = 0;

        CategoryAdapter() {}

        void updateItems(List<String> list) {
            items = new ArrayList<>(list != null ? list : new ArrayList<>());
            if (!items.contains("All")) {
                items.add(0, "All");
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_premium, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            String cat = items.get(position);
            holder.tvName.setText(cat.equals("All") ? "Tất cả" : cat);

            if (position == selectedPosition) {
                holder.flBg.setBackgroundResource(R.drawable.bg_category_icon_premium);
                holder.tvName.setTextColor(android.graphics.Color.parseColor("#1CB1A6"));
            } else {
                holder.flBg.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                holder.tvName.setTextColor(android.graphics.Color.parseColor("#757575"));
            }

            holder.itemView.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return;
                
                int oldPos = selectedPosition;
                selectedPosition = currentPos;
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                filterServices(items.get(currentPos));
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvName;
            View flBg;

            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvCategoryName);
                flBg = v.findViewById(R.id.flCategoryBg);
            }
        }
    }
}