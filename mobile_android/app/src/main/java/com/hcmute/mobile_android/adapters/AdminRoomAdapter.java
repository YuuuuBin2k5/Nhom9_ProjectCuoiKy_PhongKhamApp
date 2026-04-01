package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.RoomItem;

import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.ViewHolder> implements Filterable {

    private List<RoomItem> roomList;
    private List<RoomItem> roomListFull;
    private OnRoomActionListener listener;

    public interface OnRoomActionListener {
        void onRoomClick(RoomItem room);
        void onRoomEdit(RoomItem room);
        void onRoomDelete(RoomItem room);
        void onRoomToggleStatus(RoomItem room);
    }

    public AdminRoomAdapter(List<RoomItem> roomList, OnRoomActionListener listener) {
        this.roomList = roomList;
        this.roomListFull = new ArrayList<>(roomList);
        this.listener = listener;
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
        private MaterialCardView cardRoom;
        private TextView tvRoomName, tvWaitingCount, tvStatus, tvDescription;
        private ImageButton btnMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoom = itemView.findViewById(R.id.cardRoom);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvWaitingCount = itemView.findViewById(R.id.tvWaitingCount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }

        public void bind(RoomItem room) {
            tvRoomName.setText(room.getName());
            tvWaitingCount.setText(room.getWaitingCount() + " người chờ");
            
            // Hide description since it's removed from model
            tvDescription.setVisibility(View.GONE);
            
            // Set status
            if (room.isActive()) {
                tvStatus.setText("Hoạt động");
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                tvStatus.setBackgroundResource(R.drawable.bg_status_active);
            } else {
                tvStatus.setText("Vô hiệu");
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
            }

            // Click listeners
            cardRoom.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoomClick(room);
                }
            });

            btnMenu.setOnClickListener(v -> showPopupMenu(v, room));
        }

        private void showPopupMenu(View view, RoomItem room) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.inflate(R.menu.menu_admin_room);
            
            // Update menu items based on room status
            if (room.isActive()) {
                popup.getMenu().findItem(R.id.action_toggle_status).setTitle("Vô hiệu hóa");
            } else {
                popup.getMenu().findItem(R.id.action_toggle_status).setTitle("Kích hoạt");
            }
            
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) {
                        listener.onRoomEdit(room);
                    }
                    return true;
                } else if (id == R.id.action_toggle_status) {
                    if (listener != null) {
                        listener.onRoomToggleStatus(room);
                    }
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) {
                        listener.onRoomDelete(room);
                    }
                    return true;
                }
                return false;
            });
            
            popup.show();
        }
    }

    public void updateList(List<RoomItem> newList) {
        this.roomListFull = new ArrayList<>(newList);
        this.roomList = newList;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<RoomItem> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(roomListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (RoomItem item : roomListFull) {
                        if (item.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                roomList = (List<RoomItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
