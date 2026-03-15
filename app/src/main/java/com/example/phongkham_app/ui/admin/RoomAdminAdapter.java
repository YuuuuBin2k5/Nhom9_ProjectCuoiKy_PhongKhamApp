package com.example.phongkham_app.ui.admin;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;

public class RoomAdminAdapter extends RecyclerView.Adapter<RoomAdminAdapter.ViewHolder> {

    private Cursor cursor;

    public RoomAdminAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int count = cursor.getInt(cursor.getColumnIndexOrThrow("waiting_count"));

            holder.tvRoomName.setText(name);
            holder.tvWaitingCount.setText(String.valueOf(count));
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvWaitingCount;

        ViewHolder(View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvWaitingCount = itemView.findViewById(R.id.tvWaitingCount);
        }
    }
}
