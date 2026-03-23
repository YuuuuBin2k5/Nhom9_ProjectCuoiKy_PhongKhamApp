package com.hcmute.mobile_android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.UpcomingAppointmentAdapter;
import com.hcmute.mobile_android.adapters.TreatmentProgressAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.network.models.CheckInMyStatusResponse;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;
import com.hcmute.mobile_android.network.models.TreatmentPlanSummary;
import com.hcmute.mobile_android.ui.activities.PatientQueueActivity;
import com.hcmute.mobile_android.ui.activities.QRCheckInActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDashboardFragment extends Fragment {

    // Views
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvGreeting, tvPatientName;
    private MaterialCardView cardCheckInStatus, cardNextAppointment, cardTreatmentProgress;
    private TextView tvCheckInStatus, tvQueuePosition, tvEstimatedTime;
    private TextView tvNextAppointmentDate, tvNextAppointmentDoctor, tvNextAppointmentService;
    private MaterialButton btnCheckIn, btnViewQueue, btnViewAppointment;
    private RecyclerView rvUpcomingAppointments, rvTreatmentProgress;
    
    // Adapters
    private UpcomingAppointmentAdapter appointmentAdapter;
    private TreatmentProgressAdapter treatmentAdapter;
    
    // Data
    private List<UpcomingAppointment> upcomingAppointments = new ArrayList<>();
    private List<TreatmentPlanSummary> treatmentPlans = new ArrayList<>();
    
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
        loadPatientData();
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvPatientName = view.findViewById(R.id.tvPatientName);
        
        // Check-in status card
        cardCheckInStatus = view.findViewById(R.id.cardCheckInStatus);
        tvCheckInStatus = view.findViewById(R.id.tvCheckInStatus);
        tvQueuePosition = view.findViewById(R.id.tvQueuePosition);
        tvEstimatedTime = view.findViewById(R.id.tvEstimatedTime);
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        btnViewQueue = view.findViewById(R.id.btnViewQueue);
        
        // Next appointment card
        cardNextAppointment = view.findViewById(R.id.cardNextAppointment);
        tvNextAppointmentDate = view.findViewById(R.id.tvNextAppointmentDate);
        tvNextAppointmentDoctor = view.findViewById(R.id.tvNextAppointmentDoctor);
        tvNextAppointmentService = view.findViewById(R.id.tvNextAppointmentService);
        btnViewAppointment = view.findViewById(R.id.btnViewAppointment);
        
        // Treatment progress card
        cardTreatmentProgress = view.findViewById(R.id.cardTreatmentProgress);
        
        // RecyclerViews
        rvUpcomingAppointments = view.findViewById(R.id.rvUpcomingAppointments);
        rvTreatmentProgress = view.findViewById(R.id.rvTreatmentProgress);
        
        // Set greeting
        tvGreeting.setText(getGreeting());
        
        // Setup click listeners
        swipeRefresh.setOnRefreshListener(this::loadPatientData);
        btnCheckIn.setOnClickListener(v -> openCheckIn());
        btnViewQueue.setOnClickListener(v -> openQueueStatus());
        btnViewAppointment.setOnClickListener(v -> openAppointmentDetail());
    }

    private void setupAdapters() {
        // Upcoming appointments
        rvUpcomingAppointments.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        appointmentAdapter = new UpcomingAppointmentAdapter(upcomingAppointments, this::onAppointmentClick);
        rvUpcomingAppointments.setAdapter(appointmentAdapter);
        
        // Treatment progress
        rvTreatmentProgress.setLayoutManager(new LinearLayoutManager(requireContext()));
        treatmentAdapter = new TreatmentProgressAdapter(treatmentPlans, this::onTreatmentPlanClick);
        rvTreatmentProgress.setAdapter(treatmentAdapter);
    }

    private void loadPatientData() {
        swipeRefresh.setRefreshing(true);
        
        // Load patient info
        loadPatientInfo();
        
        // Load check-in status
        loadCheckInStatus();
        
        // Load upcoming appointments
        loadUpcomingAppointments();
        
        // Load treatment plans
        loadTreatmentPlans();
    }

    private void loadPatientInfo() {
        apiService.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPatient = response.body();
                    updatePatientInfo(currentPatient);
                }
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                showError("Lỗi tải thông tin bệnh nhân: " + t.getMessage());
            }
        });
    }

    private void loadCheckInStatus() {
        apiService.getMyCheckInStatus().enqueue(new Callback<CheckInMyStatusResponse>() {
            @Override
            public void onResponse(Call<CheckInMyStatusResponse> call, Response<CheckInMyStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateCheckInStatus(response.body());
                } else {
                    // No active check-in
                    updateCheckInStatus(null);
                }
            }

            @Override
            public void onFailure(Call<CheckInMyStatusResponse> call, Throwable t) {
                updateCheckInStatus(null);
            }
        });
    }

    private void loadUpcomingAppointments() {
        apiService.getUpcomingAppointments().enqueue(new Callback<List<UpcomingAppointment>>() {
            @Override
            public void onResponse(Call<List<UpcomingAppointment>> call, Response<List<UpcomingAppointment>> response) {
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    upcomingAppointments.clear();
                    upcomingAppointments.addAll(response.body());
                    appointmentAdapter.notifyDataSetChanged();
                    
                    updateNextAppointmentCard();
                }
            }

            @Override
            public void onFailure(Call<List<UpcomingAppointment>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                showError("Lỗi tải lịch hẹn: " + t.getMessage());
            }
        });
    }

    private void loadTreatmentPlans() {
        apiService.getMyTreatmentPlans().enqueue(new Callback<List<TreatmentPlanSummary>>() {
            @Override
            public void onResponse(Call<List<TreatmentPlanSummary>> call, Response<List<TreatmentPlanSummary>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    treatmentPlans.clear();
                    treatmentPlans.addAll(response.body());
                    treatmentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<TreatmentPlanSummary>> call, Throwable t) {
                showError("Lỗi tải phác đồ điều trị: " + t.getMessage());
            }
        });
    }

    private void updatePatientInfo(PatientMeResponse patient) {
        if (patient != null) {
            String fullName = (patient.getLastName() + " " + patient.getFirstName()).trim();
            tvPatientName.setText(fullName);
        }
    }

    private void updateCheckInStatus(CheckInMyStatusResponse status) {
        if (status != null && status.isCheckedIn()) {
            // Patient is checked in
            cardCheckInStatus.setVisibility(View.VISIBLE);
            cardCheckInStatus.setCardBackgroundColor(requireContext().getColor(R.color.success_background));
            
            tvCheckInStatus.setText("Đã check-in");
            tvQueuePosition.setText("Số thứ tự: " + status.getQueueNumber());
            tvEstimatedTime.setText("Ước tính: " + status.getEstimatedWaitTime() + " phút");
            
            btnCheckIn.setVisibility(View.GONE);
            btnViewQueue.setVisibility(View.VISIBLE);
        } else {
            // Patient not checked in
            cardCheckInStatus.setVisibility(View.VISIBLE);
            cardCheckInStatus.setCardBackgroundColor(requireContext().getColor(android.R.color.white));
            
            tvCheckInStatus.setText("Chưa check-in");
            tvQueuePosition.setVisibility(View.GONE);
            tvEstimatedTime.setVisibility(View.GONE);
            
            btnCheckIn.setVisibility(View.VISIBLE);
            btnViewQueue.setVisibility(View.GONE);
        }
    }

    private void updateNextAppointmentCard() {
        if (!upcomingAppointments.isEmpty()) {
            UpcomingAppointment next = upcomingAppointments.get(0);
            
            cardNextAppointment.setVisibility(View.VISIBLE);
            tvNextAppointmentDate.setText(formatAppointmentDate(next.getAppointmentTime()));
            tvNextAppointmentDoctor.setText("BS. " + next.getDoctorName());
            tvNextAppointmentService.setText(next.getServiceName());
        } else {
            cardNextAppointment.setVisibility(View.GONE);
        }
    }

    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            return "Chào buổi sáng";
        } else if (hour < 18) {
            return "Chào buổi chiều";
        } else {
            return "Chào buổi tối";
        }
    }

    private String formatAppointmentDate(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateTime));
        } catch (Exception e) {
            return dateTime;
        }
    }

    private void openCheckIn() {
        startActivity(new Intent(requireContext(), QRCheckInActivity.class));
    }

    private void openQueueStatus() {
        startActivity(new Intent(requireContext(), PatientQueueActivity.class));
    }

    private void openAppointmentDetail() {
        // TODO: Open appointment detail activity
        Toast.makeText(requireContext(), "Chi tiết lịch hẹn", Toast.LENGTH_SHORT).show();
    }

    private void onAppointmentClick(UpcomingAppointment appointment) {
        // TODO: Open appointment detail
        Toast.makeText(requireContext(), "Lịch hẹn: " + appointment.getServiceName(), Toast.LENGTH_SHORT).show();
    }

    private void onTreatmentPlanClick(TreatmentPlanSummary plan) {
        // TODO: Open treatment plan detail
        Toast.makeText(requireContext(), "Phác đồ: " + plan.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}