package com.example.phongkham_app.ui.admin;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.InputStream;

public class ServiceAdminAdapter extends RecyclerView.Adapter<ServiceAdminAdapter.ViewHolder> {

    private Cursor cursor;
    private final OnServiceStatusChangeListener listener;

    public interface OnServiceStatusChangeListener {
        void onStatusChanged(int serviceId, boolean isActive);
    }

    public ServiceAdminAdapter(Cursor cursor, OnServiceStatusChangeListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration_minutes"));
            boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1;
            String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));

            holder.tvName.setText(name);
            holder.tvPrice.setText(String.format("%,.0fđ", price).replace(",", "."));
            holder.tvDuration.setText(duration + " phút");
            
            holder.switchStatus.setOnCheckedChangeListener(null);
            holder.switchStatus.setChecked(isActive);
            holder.switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onStatusChanged(id, isChecked);
            });

            loadImage(holder.imgService, imageUrl);
        }
    }

    private void loadImage(ImageView imageView, String url) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            return;
        }

        Context context = imageView.getContext();
        try {
            if (url.startsWith("content://") || url.startsWith("file://")) {
                Uri uri = Uri.parse(url);
                InputStream is = context.getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                imageView.setImageBitmap(bitmap);
                if (is != null) is.close();
            } else if (url.startsWith("images/")) {
                InputStream is = context.getAssets().open(url);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                imageView.setImageBitmap(bitmap);
                is.close();
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } catch (Exception e) {
            Log.e("ServiceAdminAdapter", "Error loading image: " + url, e);
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvDuration;
        ImageView imgService;
        SwitchMaterial switchStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            imgService = itemView.findViewById(R.id.imgService);
            switchStatus = itemView.findViewById(R.id.switchStatus);
        }
    }
}
