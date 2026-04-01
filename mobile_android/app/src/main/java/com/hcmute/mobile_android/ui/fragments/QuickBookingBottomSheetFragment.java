package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CreateAppointmentRequest;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;
import com.hcmute.mobile_android.util.TokenManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuickBookingBottomSheetFragment extends BottomSheetDialogFragment {

    private RadioGroup rgServices, rgTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_quick_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rgServices = view.findViewById(R.id.rgServices);
        rgTime = view.findViewById(R.id.rgTime);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(v -> handleQuickBooking());
    }

    private void handleQuickBooking() {
        TokenManager tm = new TokenManager(requireContext());
        Long patientId = tm.getPatientId();

        if (patientId == null || patientId == -1L) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin bệnh nhân. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // --- Determine Service ID ---
        Long serviceId = 1L; // Fallback "Khám tổng quát"
        int checkedService = rgServices.getCheckedRadioButtonId();
        if (checkedService == R.id.rbService2) {
            serviceId = 2L; 
        } else if (checkedService == R.id.rbService3) {
            serviceId = 3L;
        }

        // --- Determine Datetime ---
        Calendar cal = Calendar.getInstance();
        int checkedTime = rgTime.getCheckedRadioButtonId();

        if (checkedTime == R.id.rbTime1) {
            // Hôm nay 14:00
            cal.set(Calendar.HOUR_OF_DAY, 14);
            cal.set(Calendar.MINUTE, 0);
        } else if (checkedTime == R.id.rbTime2) {
            // Sáng mai 09:00
            cal.add(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 9);
            cal.set(Calendar.MINUTE, 0);
        } else if (checkedTime == R.id.rbTime3) {
            // Tuần tới 10:00
            cal.add(Calendar.DAY_OF_YEAR, 7);
            cal.set(Calendar.HOUR_OF_DAY, 10);
            cal.set(Calendar.MINUTE, 0);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String datetimeStr = sdf.format(cal.getTime());

        // Quick Booking uses generic Doctor ID 1 as standard auto-assignment for demo
        CreateAppointmentRequest request = new CreateAppointmentRequest(serviceId, 1L, patientId, datetimeStr);

        ApiService api = RetrofitClient.getApiService(requireContext());
        api.createAppointment(request).enqueue(new Callback<UpcomingAppointment>() {
            @Override
            public void onResponse(Call<UpcomingAppointment> call, Response<UpcomingAppointment> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đặt lịch thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Có lỗi xảy ra: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }

            @Override
            public void onFailure(Call<UpcomingAppointment> call, Throwable t) {
                Toast.makeText(requireContext(), "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }
}
