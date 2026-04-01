package com.hcmute.mobile_android.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.models.ImageSource;
import java.util.List;

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ViewHolder> {

    private final List<ImageSource> imageSources;
    private final OnImageRemoveListener listener;

    public interface OnImageRemoveListener {
        void onRemove(int position);
    }

    public SelectedImageAdapter(List<ImageSource> imageSources, OnImageRemoveListener listener) {
        this.imageSources = imageSources;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageSource imageSource = imageSources.get(position);
        
        Glide.with(holder.itemView.getContext())
                .load(imageSource.getSource())
                .centerCrop()
                .placeholder(R.drawable.background)
                .into(holder.ivSelectedImage);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageSources.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSelectedImage;
        View btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSelectedImage = itemView.findViewById(R.id.ivSelectedImage);
            btnRemove = itemView.findViewById(R.id.btnRemoveImage);
        }
    }
}
