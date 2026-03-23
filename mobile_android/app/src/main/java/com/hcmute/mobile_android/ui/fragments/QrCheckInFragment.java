package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.CheckInMyStatusResponse;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.network.models.QrTokenResponse;
import com.hcmute.mobile_android.util.QrCodeGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrCheckInFragment extends Fragment {

    private ImageView ivQrCode;
    private TextView tvStatus;
    private ProgressBar progress;
    private TextView tvError;
    private View scrollContent;
    private MaterialCardView cardQueueStatus;
    private TextView tvQueueNumber;
    private TextView tvQueueRoom;
    private TextView tvQueueState;
    private TextView tvQueueHint;
    private TextView tvUserInfo;
    private Bitmap latestQrBitmap;
    private final Handler qrRefreshHandler = new Handler(Looper.getMainLooper());
    private Runnable qrRefreshRunnable;
    private final Handler queuePollHandler = new Handler(Looper.getMainLooper());
    private Runnable queuePollRunnable;
    private static final int QUEUE_POLL_INTERVAL_MS = 12000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_checkin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivQrCode = view.findViewById(R.id.ivQrCode);
        tvStatus = view.findViewById(R.id.tvStatus);
        progress = view.findViewById(R.id.progress);
        tvError = view.findViewById(R.id.tvError);
        scrollContent = view.findViewById(R.id.scrollContent);
        cardQueueStatus = view.findViewById(R.id.cardQueueStatus);
        tvQueueNumber = view.findViewById(R.id.tvQueueNumber);
        tvQueueRoom = view.findViewById(R.id.tvQueueRoom);
        tvQueueState = view.findViewById(R.id.tvQueueState);
        tvQueueHint = view.findViewById(R.id.tvQueueHint);
        tvUserInfo = view.findViewById(R.id.tvUserInfo);
        view.findViewById(R.id.btnDownload).setOnClickListener(v -> downloadQr());
        loadScreen();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            loadScreen();
        }
    }

    private void loadScreen() {
        progress.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        cardQueueStatus.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getQrToken().enqueue(new Callback<QrTokenResponse>() {
            @Override
            public void onResponse(Call<QrTokenResponse> call, Response<QrTokenResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                    String qrData = response.body().getToken();
                    try {
                        latestQrBitmap = QrCodeGenerator.encodeToBitmap(qrData, 512);
                        ivQrCode.setImageBitmap(latestQrBitmap);
                        scrollContent.setVisibility(View.VISIBLE);
                        fetchCheckInStatus(api);
                        loadUserInfo(api);
                        scheduleQrRefresh(response.body().getExpiresIn() > 0 ? response.body().getExpiresIn() : 150);
                    } catch (Exception e) {
                        fallbackToStaticQr(api);
                    }
                } else {
                    fallbackToStaticQr(api);
                }
            }

            @Override
            public void onFailure(Call<QrTokenResponse> call, Throwable t) {
                fallbackToStaticQr(api);
            }
        });
    }

    private void fallbackToStaticQr(ApiService api) {
        api.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                progress.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    tvError.setText("Vui lòng đăng nhập để sử dụng tính năng này");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                String qrData = response.body().getQrCodeData();
                if (qrData == null || qrData.isEmpty()) {
                    tvError.setText("Chưa có mã QR. Vui lòng liên hệ phòng khám.");
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                try {
                    latestQrBitmap = QrCodeGenerator.encodeToBitmap(qrData, 512);
                    ivQrCode.setImageBitmap(latestQrBitmap);
                    scrollContent.setVisibility(View.VISIBLE);
                    fetchCheckInStatus(api);
                    loadUserInfo(api);
                } catch (Exception e) {
                    tvError.setText("Không thể tạo mã QR");
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvError.setText(t.getMessage() != null ? t.getMessage() : "Lỗi kết nối");
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadUserInfo(ApiService api) {
        api.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientMeResponse p = response.body();
                    String fullName = ((p.getFirstName() != null ? p.getFirstName() : "") + " "
                            + (p.getLastName() != null ? p.getLastName() : "")).trim();
                    if (fullName.isEmpty()) fullName = "Bạn";
                    tvUserInfo.setText("Họ tên: " + fullName);
                }
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) { }
        });
    }

    private void downloadQr() {
        if (latestQrBitmap == null) {
            Toast.makeText(requireContext(), "QR chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String url = MediaStore.Images.Media.insertImage(
                    requireContext().getContentResolver(),
                    latestQrBitmap,
                    "qr_checkin_" + System.currentTimeMillis(),
                    "QR check-in"
            );
            if (url != null) {
                Toast.makeText(requireContext(), "Đã lưu QR vào thư viện", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Không thể lưu QR", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi khi lưu QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleQrRefresh(int seconds) {
        qrRefreshHandler.removeCallbacks(qrRefreshRunnable);
        qrRefreshRunnable = () -> {
            if (isAdded()) loadScreen();
        };
        qrRefreshHandler.postDelayed(qrRefreshRunnable, Math.max(seconds - 30, 60) * 1000L);
    }

    private void fetchCheckInStatus(ApiService api) {
        api.getMyCheckInStatus().enqueue(new Callback<CheckInMyStatusResponse>() {
            @Override
            public void onResponse(Call<CheckInMyStatusResponse> call, Response<CheckInMyStatusResponse> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    applyCheckInStatus(response.body());
                } else {
                    tvStatus.setText("Chưa check-in");
                    cardQueueStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<CheckInMyStatusResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvStatus.setText("Chưa check-in");
                cardQueueStatus.setVisibility(View.GONE);
                Toast.makeText(requireContext(), t.getMessage() != null ? t.getMessage() : "Không tải được trạng thái hàng đợi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyCheckInStatus(CheckInMyStatusResponse s) {
        queuePollHandler.removeCallbacks(queuePollRunnable);
        if (s.isCheckedIn()) {
            scheduleQueuePoll();
            tvStatus.setText("Đã check-in");
            cardQueueStatus.setVisibility(View.VISIBLE);
            Integer num = s.getQueueNumber();
            tvQueueNumber.setText(num != null ? String.valueOf(num) : "—");
            String room = s.getRoomName() != null ? s.getRoomName() : "";
            String loc = s.getRoomLocation() != null ? s.getRoomLocation() : "";
            if (!room.isEmpty() && !loc.isEmpty()) {
                tvQueueRoom.setText(room + " — " + loc);
            } else if (!room.isEmpty()) {
                tvQueueRoom.setText(room);
            } else {
                tvQueueRoom.setText("");
            }
            tvQueueRoom.setVisibility(room.isEmpty() && loc.isEmpty() ? View.GONE : View.VISIBLE);
            tvQueueState.setText(s.getStatusLabel() != null ? s.getStatusLabel() : "");
            tvQueueHint.setText(s.getHint() != null ? s.getHint() : "");
        } else {
            tvStatus.setText("Chưa check-in");
            cardQueueStatus.setVisibility(View.GONE);
        }
    }

    private void scheduleQueuePoll() {
        queuePollRunnable = () -> {
            if (!isAdded()) return;
            ApiService api = RetrofitClient.getApiService(requireContext());
            api.getMyCheckInStatus().enqueue(new Callback<CheckInMyStatusResponse>() {
                @Override
                public void onResponse(Call<CheckInMyStatusResponse> call, Response<CheckInMyStatusResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isCheckedIn()) {
                        applyCheckInStatus(response.body());
                    }
                }
                @Override
                public void onFailure(Call<CheckInMyStatusResponse> call, Throwable t) { }
            });
        };
        queuePollHandler.postDelayed(queuePollRunnable, QUEUE_POLL_INTERVAL_MS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        qrRefreshHandler.removeCallbacks(qrRefreshRunnable);
        queuePollHandler.removeCallbacks(queuePollRunnable);
    }
}
