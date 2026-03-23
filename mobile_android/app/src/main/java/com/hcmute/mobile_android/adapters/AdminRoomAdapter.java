package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.RoomItem;

import java.util.List;

public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.ViewHolder> {

    private List<RoomItem> roomList;

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoomName, tvStatus, tvWaitingCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvWaitingCount = itemView.findViewById(R.id.tvWaitingCount);
        }

        public void bind(RoomItem room) {
            tvRoomName.setText(room.getName());
            tvStatus.setText(room.getStatus());
            tvWaitingCount.setText(room.getWaitingCount() + " người chờ");
        }
    }
}