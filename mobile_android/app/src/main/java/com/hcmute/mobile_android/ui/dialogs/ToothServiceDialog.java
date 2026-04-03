package com.hcmute.mobile_android.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.ToothServiceAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.AddToothServiceRequest;
import com.hcmute.mobile_android.network.models.ServiceItem;
import com.hcmute.mobile_android.network.models.ToothServiceResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dialog hiển thị danh sách các dịch vụ nha khoa có thể áp dụng cho một răng cụ thể.
 * Giúp bác sĩ nhanh chóng gán dịch vụ trong quá trình khám.
 */
public class ToothServiceDialog extends DialogFragment {
    
    private Long planId;
    private String toothNumber;
    private Integer nextSequenceOrder = 1;
    private OnServiceSelectedListener listener;
    
    private RecyclerView rvServices;
    private ToothServiceAdapter adapter;
    private ApiService apiService;
    
    public interface OnServiceSelectedListener {
        void onServiceSelected(ToothServiceResponse response);
        void onError(String message);
    }
    
    public ToothServiceDialog() {
        // Required empty public constructor
    }
    
    public static ToothServiceDialog newInstance(Long planId, String toothNumber, Integer sequenceOrder) {
        ToothServiceDialog dialog = new ToothServiceDialog();
        Bundle args = new Bundle();
        args.putLong("planId", planId);
        args.putString("toothNumber", toothNumber);
        args.putInt("sequenceOrder", sequenceOrder);
        dialog.setArguments(args);
        return dialog;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            planId = getArguments().getLong("planId");
            toothNumber = getArguments().getString("toothNumber");
            nextSequenceOrder = getArguments().getInt("sequenceOrder", 1);
        }
        apiService = RetrofitClient.getApiService(requireContext());
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        // Inflate custom layout
        android.view.View view = requireActivity().getLayoutInflater()
            .inflate(R.layout.dialog_tooth_service, null);
        
        // Setup views
        TextView tvToothInfo = view.findViewById(R.id.tvToothInfo);
        tvToothInfo.setText("Răng số: " + toothNumber);
        
        rvServices = view.findViewById(R.id.rvToothServices);
        rvServices.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> dismiss());
        
        // Setup adapter with tooth-specific services
        List<ServiceItem> services = getToothSpecificServices();
        adapter = new ToothServiceAdapter(services, service -> onServiceSelected(service));
        rvServices.setAdapter(adapter);
        
        builder.setView(view);
        return builder.create();
    }
    
    /**
     * Get the 4 tooth-specific services
     * These must match the backend DataSeed.java service IDs
     */
    private List<ServiceItem> getToothSpecificServices() {
        List<ServiceItem> services = new ArrayList<>();
        
        // Service IDs from backend DataSeed.java
        services.add(new ServiceItem(4L, "Trám răng sâu", 300000.0));
        services.add(new ServiceItem(6L, "Nhổ răng thường", 300000.0));
        services.add(new ServiceItem(7L, "Nhổ răng khôn", 2000000.0));
        services.add(new ServiceItem(9L, "Bọc răng sứ", 5000000.0));
        
        return services;
    }
    
    /**
     * Handle service selection
     */
    private void onServiceSelected(ServiceItem service) {
        if (planId == null || toothNumber == null) {
            Toast.makeText(requireContext(), "Lỗi: Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        Toast.makeText(requireContext(), "Đang thêm dịch vụ...", Toast.LENGTH_SHORT).show();
        
        // Create request
        AddToothServiceRequest request = new AddToothServiceRequest(
            service.getId(),
            nextSequenceOrder
        );
        
        // Call API
        apiService.addServiceToTooth(planId, toothNumber, request)
            .enqueue(new Callback<ToothServiceResponse>() {
                @Override
                public void onResponse(Call<ToothServiceResponse> call, Response<ToothServiceResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ToothServiceResponse result = response.body();
                        Toast.makeText(requireContext(), 
                            "Đã thêm: " + result.getServiceName(), 
                            Toast.LENGTH_SHORT).show();
                        
                        // Notify listener
                        if (listener != null) {
                            listener.onServiceSelected(result);
                        }
                        
                        dismiss();
                    } else {
                        String error = "Lỗi thêm dịch vụ: " + response.code();
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<ToothServiceResponse> call, Throwable t) {
                    String error = "Lỗi kết nối: " + t.getMessage();
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onError(error);
                    }
                }
            });
    }
    
    public void setOnServiceSelectedListener(OnServiceSelectedListener listener) {
        this.listener = listener;
    }
}
