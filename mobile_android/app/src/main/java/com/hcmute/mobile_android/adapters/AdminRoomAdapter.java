package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.RoomItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.ViewHolder> {

    private List<RoomItem> roomList;
    private ApiService apiService;

    public AdminRoomAdapter(List<RoomItem> roomList) {
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomItem room = roomList.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoomName, tvWaitingCount;
        private MaterialButton btnActive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvWaitingCount = itemView.findViewById(R.id.tvWaitingCount);
            btnActive = itemView.findViewById(R.id.btnActive);
            
            if (apiService == null) {
                apiService = RetrofitClient.getApiService(itemView.getContext());
            }
        }

        public void bind(RoomItem room) {
            tvRoomName.setText(room.getName());
            tvWaitingCount.setText(room.getWaitingCount() + " người chờ");
            
            updateActiveButton(room.isActive());

            btnActive.setOnClickListener(v -> toggleRoomStatus(room));
        }

        private void updateActiveButton(boolean isActive) {
            if (isActive) {
                btnActive.setText("Hoạt động");
                btnActive.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                btnActive.setText("Chặn");
                btnActive.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
            }
        }

        private void toggleRoomStatus(RoomItem room) {
            boolean newStatus = !room.isActive();
            btnActive.setEnabled(false);
            
            apiService.updateRoomStatus(room.getId(), newStatus).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    btnActive.setEnabled(true);
                    if (response.isSuccessful()) {
                        room.setActive(newStatus);
                        updateActiveButton(newStatus);
                        Toast.makeText(itemView.getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(itemView.getContext(), "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    btnActive.setEnabled(true);
                    Toast.makeText(itemView.getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}