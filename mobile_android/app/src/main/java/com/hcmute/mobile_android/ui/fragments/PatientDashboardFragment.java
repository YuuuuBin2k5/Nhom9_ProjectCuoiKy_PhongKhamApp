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
import com.hcmute.mobile_android.util.ToastUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
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
import com.hcmute.mobile_android.ui.activities.MainActivity;
import com.hcmute.mobile_android.ui.activities.PatientQueueActivity;
import com.hcmute.mobile_android.ui.activities.PatientQRScannerActivity;
import com.hcmute.mobile_android.ui.activities.ServiceDetailActivity;

import com.bumptech.glide.Glide;

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

    // Grid / Slide lists
    private RecyclerView rvServices, rvDoctors;
    private TabLayout tabsCategories;
    private ServiceAdapter serviceAdapter;
    private DoctorAdapter doctorAdapter;
    private List<ServiceItem> allServices = new ArrayList<>();

    // Check-in
    private MaterialCardView cardCheckInStatus;
    private TextView tvCheckInStatus, tvRoomInfo, tvDoctorInfo, tvStepInfo, tvQueueNumberBig, tvEstimatedTime;
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
        
        tabsCategories = view.findViewById(R.id.tabs_categories);
        rvServices = view.findViewById(R.id.rv_services);
        rvDoctors = view.findViewById(R.id.rv_doctors);
        
        cardCheckInStatus = view.findViewById(R.id.cardCheckInStatus);
        tvCheckInStatus = view.findViewById(R.id.tvCheckInStatus);
        tvRoomInfo = view.findViewById(R.id.tvRoomInfo);
        tvDoctorInfo = view.findViewById(R.id.tvDoctorInfo);
        tvStepInfo = view.findViewById(R.id.tvStepInfo);
        tvQueueNumberBig = view.findViewById(R.id.tvQueueNumberBig);
        tvEstimatedTime = view.findViewById(R.id.tvEstimatedTime);
        btnViewQueue = view.findViewById(R.id.btnViewQueue);
        
        rvUpcomingAppointments = view.findViewById(R.id.rvUpcomingAppointments);
        
        swipeRefresh.setOnRefreshListener(this::loadPatientData);
        btnViewQueue.setOnClickListener(v -> openQueueStatus());
        
        View btnAllDv = view.findViewById(R.id.all_dv);
        if (btnAllDv != null) btnAllDv.setOnClickListener(v -> openList(GenericListActivity.MODE_SERVICES));

        View btnViewAllDoctors = view.findViewById(R.id.btnViewAllDoctors);
        if (btnViewAllDoctors != null) {
            btnViewAllDoctors.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onNavigateToDoctors();
                }
            });
        }

        View ivNotifications = view.findViewById(R.id.iv_notifications);
        if (ivNotifications != null) {
            ivNotifications.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onNavigateToNotifications();
                }
            });
        }

        View btnBanner = view.findViewById(R.id.btnBannerAction);
        if (btnBanner != null) {
            btnBanner.setOnClickListener(v -> {
                // Open Dental Scaling or similar service
                // Attempt to find a service with "Scaling" in name from loaded list
                ServiceItem scalingService = null;
                for (ServiceItem s : allServices) {
                    if (s.getName() != null && s.getName().toLowerCase().contains("scaling")) {
                        scalingService = s;
                        break;
                    }
                }
                
                if (scalingService != null) {
                    Intent intent = new Intent(requireContext(), ServiceDetailActivity.class);
                    intent.putExtra("id", scalingService.getId());
                    intent.putExtra("name", scalingService.getName());
                    intent.putExtra("price", scalingService.getPrice());
                    intent.putExtra("description", scalingService.getDescription());
                    startActivity(intent);
                } else {
                    // Fallback: search for all services
                    openList(GenericListActivity.MODE_SERVICES);
                }
            });
        }

        View headerQr = view.findViewById(R.id.ivHeaderQrScan);
        if (headerQr != null) {
            headerQr.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), PatientQRScannerActivity.class));
            });
        }

        View avatar = view.findViewById(R.id.ivAvatar);
        if (avatar != null) {
            avatar.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), com.hcmute.mobile_android.ui.activities.MedicalRecordActivity.class));
            });
        }
    }

    private void setupAdapters() {
        if (!isAdded()) return;

        // Services (2-column grid)
        rvServices.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        serviceAdapter = new ServiceAdapter();
        rvServices.setAdapter(serviceAdapter);
        
        // Doctors (Vertical)
        rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
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

                if (response.isSuccessful() && response.body() != null) {
                    currentPatient = response.body();
                    if (currentPatient.getId() != null) {
                        new TokenManager(requireContext()).savePatientId(currentPatient.getId());
                    }
                    String fn = currentPatient.getFirstName() != null ? currentPatient.getFirstName() : "";
                    String ln = currentPatient.getLastName() != null ? currentPatient.getLastName() : "";
                    String fullName = (fn + " " + ln).trim();
                    tvPatientName.setText(fullName.isEmpty() ? "Bệnh nhân" : fullName);
                    
                    boolean isMissingInfo = currentPatient.getPhone() == null || currentPatient.getPhone().isEmpty() ||
                            currentPatient.getEmail() == null || currentPatient.getEmail().isEmpty() ||
                            currentPatient.getAddress() == null || currentPatient.getAddress().isEmpty();
                    
                    View warningIcon = getView() != null ? getView().findViewById(R.id.ivWarningProfile) : null;
                    if (warningIcon != null) {
                        warningIcon.setVisibility(isMissingInfo ? View.VISIBLE : View.GONE);
                    }
                } else if (response.code() == 404 || response.code() == 401) {
                    ToastUtils.showCenteredToastLong(requireContext(), "Phiên làm việc hết hạn. Vui lòng đăng nhập lại.");
                    logout();
                }
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                if (!isAdded()) return;
                ToastUtils.showCenteredToast(requireContext(), "Lỗi tải thông tin bệnh nhân");
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
                        updateCheckInStatusUI(status);
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
    
    private void updateCheckInStatusUI(CheckInMyStatusResponse status) {
        // Status badge setup
        String statusText = "Đã Check-in";
        int statusColor = ContextCompat.getColor(requireContext(), R.color.primary_trust_blue);
        
        if ("IN_PROGRESS".equals(status.getStatus())) {
            statusText = "Đang điều trị";
            statusColor = ContextCompat.getColor(requireContext(), R.color.success_green);
        } else if ("WAITING".equals(status.getStatus()) || "RETURNED_PRIORITY".equals(status.getStatus())) {
            statusText = "Đang chờ";
            statusColor = ContextCompat.getColor(requireContext(), R.color.warning_orange);
        } else if ("PAUSED_FOR_TEST".equals(status.getStatus())) {
            statusText = "Đang chụp phim";
            statusColor = ContextCompat.getColor(requireContext(), R.color.info_blue);
        }
        
        tvCheckInStatus.setText(statusText);
        tvCheckInStatus.setTextColor(statusColor);
        tvCheckInStatus.getBackground().setTint(statusColor & 0x22FFFFFF | 0x11000000); // Subtle background tint
        
        // Room & Doctor
        String room = status.getRoomName() != null ? status.getRoomName() : "Phòng khám";
        tvRoomInfo.setText(room);
        
        String doc = (status.getDoctorName() != null && !status.getDoctorName().isEmpty()) 
            ? "BS. " + status.getDoctorName() : "Đội ngũ chuyên gia";
        tvDoctorInfo.setText(doc);
        
        // Step info
        if (status.getCurrentStepName() != null && status.getTotalSteps() != null && status.getCurrentStepNumber() != null) {
            tvStepInfo.setText("Bước " + status.getCurrentStepNumber() + "/" + status.getTotalSteps() + ": " + status.getCurrentStepName());
            tvStepInfo.setVisibility(View.VISIBLE);
        } else if (status.getServiceName() != null) {
            tvStepInfo.setText(status.getServiceName());
            tvStepInfo.setVisibility(View.VISIBLE);
        } else {
            tvStepInfo.setVisibility(View.GONE);
        }
        
        // Queue Number
        int qNum = status.getQueueNumber() != null ? status.getQueueNumber() : 0;
        tvQueueNumberBig.setText(String.format(Locale.getDefault(), "%02d", qNum));
        
        // Estimation
        if (status.getEstimatedWaitTime() != null && status.getEstimatedWaitTime() > 0) {
            tvEstimatedTime.setText("~" + status.getEstimatedWaitTime() + " phút nữa");
            tvEstimatedTime.setVisibility(View.VISIBLE);
        } else {
            tvEstimatedTime.setVisibility(View.GONE);
        }
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
                    List<DoctorItem> generalDoctors = new ArrayList<>();
                    for (DoctorItem d : response.body()) {
                        if (!d.isSpecialist() || "Khám tổng quát".equalsIgnoreCase(d.getSpecialization()) || "Nha khoa tổng quát".equalsIgnoreCase(d.getSpecialization())) {
                            generalDoctors.add(d);
                        }
                    }
                    doctorAdapter.updateItems(generalDoctors);
                }
            }

            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                if (!isAdded()) return;
                doctorAdapter.updateItems(new ArrayList<>());
            }
        });
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
        if (GenericListActivity.MODE_DOCTORS.equals(mode)) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onNavigateToDoctors();
            }
            return;
        }
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
        if (tabsCategories == null) return;
        List<String> cats = new ArrayList<>();
        cats.add("All");
        for (ServiceItem s : list) {
            String c = s.getCategoryName();
            if (c != null && !c.isEmpty() && !cats.contains(c)) {
                cats.add(c);
            }
        }
        
        tabsCategories.removeAllTabs();
        for (String cat : cats) {
            tabsCategories.addTab(tabsCategories.newTab().setText(cat.equals("All") ? "Tất cả" : cat).setTag(cat));
        }

        tabsCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getTag() != null) {
                    filterServices((String) tab.getTag());
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
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
            
            // Map Premium 3D Icons
            String name = s.getName().toLowerCase();
            if (name.contains("niềng") || name.contains("braces")) {
                holder.ivIcon.setImageResource(R.drawable.ic_3d_braces_premium);
            } else if (name.contains("sứ") || name.contains("crown")) {
                holder.ivIcon.setImageResource(R.drawable.ic_3d_crown);
            } else if (name.contains("cao răng") || name.contains("scaling") || name.contains("quang")) {
                holder.ivIcon.setImageResource(R.drawable.ic_3d_xray_premium);
            } else if (name.contains("tủy") || name.contains("root canal")) {
                holder.ivIcon.setImageResource(R.drawable.ic_3d_root_canal_premium);
            } else if (name.contains("implant")) {
                holder.ivIcon.setImageResource(R.drawable.ic_3d_implant_premium);
            } else if (name.contains("phẫu thuật") || name.contains("nhổ")) {
                holder.ivIcon.setImageResource(R.drawable.ic_3d_scaling_tools);
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_3d_whitening_premium);
            }

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

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName;
            ImageView ivIcon;

            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvServiceName);
                ivIcon = v.findViewById(R.id.ivServiceIcon);
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
            String rawName = d.getFullName();
            String displayName = rawName.startsWith("BS.") ? rawName : "BS. " + rawName;
            holder.tvName.setText(displayName);

            String spec = d.getSpecialization();
            holder.tvSpecialty.setText(spec != null && !spec.isEmpty() ? spec : "Nha khoa đa khoa");

            String room = d.getRoomName();
            holder.tvLocation.setText(room != null && !room.isEmpty() ? room : "Phòng khám");

            Integer years = d.getExperienceYears();
            if (years != null && years > 0) {
                holder.tvTrusted.setText(years + " năm KN");
            } else if (d.getAppointmentCount() > 0) {
                holder.tvTrusted.setText(d.getAppointmentCount() + "+ lượt khám");
            } else {
                holder.tvTrusted.setText("Đội ngũ giàu kinh nghiệm");
            }

            // Gender heuristic for fallback avatars - Fix Unicode boundary issue
            String nameL = d.getFullName() != null ? d.getFullName().toLowerCase() : "";
            boolean isFemale = nameL.contains("hà") || nameL.contains("thu") || nameL.contains("mai") || 
                               nameL.contains("trang") || nameL.contains("lan") || nameL.contains("thị") || 
                               nameL.contains("hạnh") || nameL.contains("ngọc") || nameL.contains("tuyết");
            
            int[] maleAvatars = { R.drawable.doc1, R.drawable.doc3, R.drawable.doc5 };
            int[] femaleAvatars = { R.drawable.doc2, R.drawable.doc4 };

            // Use doctor ID to deterministically assign a fallback avatar based on gender
            int fallbackIndex = R.drawable.ic_doctor;
            if (d.getId() != null) {
                fallbackIndex = isFemale 
                    ? femaleAvatars[(int) (Math.abs(d.getId()) % femaleAvatars.length)]
                    : maleAvatars[(int) (Math.abs(d.getId()) % maleAvatars.length)];
            } else {
                fallbackIndex = isFemale ? femaleAvatars[position % femaleAvatars.length] : maleAvatars[position % maleAvatars.length];
            }

            String avatarUrl = d.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                String finalUrl = avatarUrl;
                if (!avatarUrl.startsWith("http")) {
                    String base = com.hcmute.mobile_android.BuildConfig.API_BASE_URL;
                    if (!base.endsWith("/")) base = base + "/";
                    String p = avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl;
                    finalUrl = base + "uploads/" + p;
                }
                Glide.with(holder.imgDoctor.getContext())
                        .load(finalUrl)
                        .centerCrop()
                        .placeholder(fallbackIndex)
                        .error(fallbackIndex)
                        .into(holder.imgDoctor);
            } else {
                holder.imgDoctor.setImageResource(fallbackIndex);
            }

            // Handle Clicks
            View.OnClickListener bookingAction = v -> {
                Intent intent = new Intent(v.getContext(), com.hcmute.mobile_android.ui.activities.BookAppointmentActivity.class);
                intent.putExtra("selectedAutoDoctorId", d.getId());
                v.getContext().startActivity(intent);
            };

            holder.itemView.setOnClickListener(bookingAction);
            if (holder.btnBook != null) {
                holder.btnBook.setOnClickListener(bookingAction);
            }
        }

        @Override
        public int getItemCount() {
            return Math.min(items.size(), 3);
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvSpecialty, tvLocation, tvTrusted;
            ImageView imgDoctor;
            View btnBook;

            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_doctor_name);
                tvSpecialty = v.findViewById(R.id.tv_specialty);
                tvLocation = v.findViewById(R.id.tv_location);
                tvTrusted = v.findViewById(R.id.tv_trusted);
                imgDoctor = v.findViewById(R.id.img_doctor);
                btnBook = v.findViewById(R.id.btn_swipe_to_book);
            }
        }
    }
}