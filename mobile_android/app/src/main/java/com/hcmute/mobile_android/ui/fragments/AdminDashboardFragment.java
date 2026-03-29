package com.hcmute.mobile_android.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.DoctorStatsAdapter;
import com.hcmute.mobile_android.adapters.ServiceStatsAdapter;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.models.DoctorStats;
import com.hcmute.mobile_android.network.models.RevenueReport;
import com.hcmute.mobile_android.network.models.ServiceStats;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {
    
    private Button btnStartDate, btnEndDate, btnLoadReport;
    private TextView tvTotalRevenue, tvTotalAppointments, tvAvgRevenue;
    private RecyclerView recyclerViewTopServices, recyclerViewDoctorPerformance;
    private ProgressBar progressBar;
    
    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        
        setupViews(view);
        initializeDates();
        loadAllReports();
        
        return view;
    }
    
    private void setupViews(View view) {
        btnStartDate = view.findViewById(R.id.btnStartDate);
        btnEndDate = view.findViewById(R.id.btnEndDate);
        btnLoadReport = view.findViewById(R.id.btnLoadReport);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTotalAppointments = view.findViewById(R.id.tvTotalAppointments);
        tvAvgRevenue = view.findViewById(R.id.tvAvgRevenue);
        recyclerViewTopServices = view.findViewById(R.id.recyclerViewTopServices);
        recyclerViewDoctorPerformance = view.findViewById(R.id.recyclerViewDoctorPerformance);
        progressBar = view.findViewById(R.id.progressBar);
        
        recyclerViewTopServices.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDoctorPerformance.setLayoutManager(new LinearLayoutManager(getContext()));
        
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
        btnLoadReport.setOnClickListener(v -> loadAllReports());
    }
    
    private void initializeDates() {
        endDate = Calendar.getInstance();
        startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_MONTH, -30);
        
        updateDateButtons();
    }
    
    private void updateDateButtons() {
        btnStartDate.setText(displayFormat.format(startDate.getTime()));
        btnEndDate.setText(displayFormat.format(endDate.getTime()));
    }
    
    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;
        
        DatePickerDialog picker = new DatePickerDialog(
            getContext(),
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                updateDateButtons();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        picker.show();
    }
    
    private void loadAllReports() {
        progressBar.setVisibility(View.VISIBLE);
        
        String start = dateFormat.format(startDate.getTime());
        String end = dateFormat.format(endDate.getTime());
        
        loadRevenueReport(start, end);
        loadTopServices();
        loadDoctorPerformance(start, end);
    }
    
    private void loadRevenueReport(String startDate, String endDate) {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        Call<RevenueReport> call = apiService.getRevenueReport(startDate, endDate);
        
        call.enqueue(new Callback<RevenueReport>() {
            @Override
            public void onResponse(Call<RevenueReport> call, Response<RevenueReport> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayRevenueReport(response.body());
                }
            }
            
            @Override
            public void onFailure(Call<RevenueReport> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải báo cáo doanh thu", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadTopServices() {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        Call<List<ServiceStats>> call = apiService.getTopServices(10);
        
        call.enqueue(new Callback<List<ServiceStats>>() {
            @Override
            public void onResponse(Call<List<ServiceStats>> call, Response<List<ServiceStats>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayTopServices(response.body());
                }
            }
            
            @Override
            public void onFailure(Call<List<ServiceStats>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải top dịch vụ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadDoctorPerformance(String startDate, String endDate) {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        Call<List<DoctorStats>> call = apiService.getDoctorPerformance(startDate, endDate);
        
        call.enqueue(new Callback<List<DoctorStats>>() {
            @Override
            public void onResponse(Call<List<DoctorStats>> call, Response<List<DoctorStats>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    displayDoctorPerformance(response.body());
                }
            }
            
            @Override
            public void onFailure(Call<List<DoctorStats>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi tải hiệu suất bác sĩ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayRevenueReport(RevenueReport report) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        tvTotalRevenue.setText(formatter.format(report.getTotalRevenue()));
        tvTotalAppointments.setText(String.valueOf(report.getTotalAppointments()));
        tvAvgRevenue.setText(formatter.format(report.getAverageRevenuePerAppointment()));
    }
    
    private void displayTopServices(List<ServiceStats> services) {
        ServiceStatsAdapter adapter = new ServiceStatsAdapter(getContext(), services);
        recyclerViewTopServices.setAdapter(adapter);
    }
    
    private void displayDoctorPerformance(List<DoctorStats> doctors) {
        DoctorStatsAdapter adapter = new DoctorStatsAdapter(getContext(), doctors);
        recyclerViewDoctorPerformance.setAdapter(adapter);
    }
}
