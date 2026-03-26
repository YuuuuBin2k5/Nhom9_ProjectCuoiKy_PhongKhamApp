package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MedicalRecordResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetMedicalHistory extends BottomSheetDialogFragment {

    private static final String ARG_PATIENT_ID = "patient_id";
    private Long patientId;

    private RecyclerView rvMedicalHistory;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MedicalHistoryAdapter adapter;
    private List<MedicalRecordResponse> recordList = new ArrayList<>();
    
    private ApiService apiService;

    public static BottomSheetMedicalHistory newInstance(Long patientId) {
        BottomSheetMedicalHistory fragment = new BottomSheetMedicalHistory();
        Bundle args = new Bundle();
        args.putLong(ARG_PATIENT_ID, patientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patientId = getArguments().getLong(ARG_PATIENT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_medical_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvMedicalHistory = view.findViewById(R.id.rvMedicalHistory);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        
        rvMedicalHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicalHistoryAdapter(recordList);
        rvMedicalHistory.setAdapter(adapter);
        
        apiService = RetrofitClient.getApiService(requireContext());
        loadMedicalHistory();
    }

    private void loadMedicalHistory() {
        if (patientId == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        rvMedicalHistory.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        
        apiService.getPatientMedicalRecords(patientId).enqueue(new Callback<List<MedicalRecordResponse>>() {
            @Override
            public void onResponse(Call<List<MedicalRecordResponse>> call, Response<List<MedicalRecordResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    recordList.clear();
                    recordList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    if (recordList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvMedicalHistory.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải lịch sử bệnh án", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MedicalRecordResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Inner Adapter
    private class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.ViewHolder> {
        private List<MedicalRecordResponse> records;

        public MedicalHistoryAdapter(List<MedicalRecordResponse> records) {
            this.records = records;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Sử dụng layout mặc định của Android cho nhanh
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MedicalRecordResponse record = records.get(position);
            holder.text1.setText("Ngày khám: " + record.getDate() + " - " + record.getDoctorName());
            holder.text2.setText("Chẩn đoán: " + record.getDiagnosis());
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
