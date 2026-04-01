package com.hcmute.mobile_android.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hcmute.mobile_android.R;

import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {

    private List<String> imageUrls;
    private OnImageRemoveListener removeListener;

    public interface OnImageRemoveListener {
        void onRemove(int position);
    }

    public ImagePreviewAdapter(List<String> imageUrls, OnImageRemoveListener removeListener) {
        this.imageUrls = imageUrls;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        
        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(imageUrl.startsWith("http") ? imageUrl : Uri.parse(imageUrl))
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .centerCrop()
                .into(holder.ivPreview);

        // Click to view full screen
        holder.ivPreview.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(holder.itemView.getContext(), 
                com.hcmute.mobile_android.ui.activities.ImageViewerActivity.class);
            intent.putStringArrayListExtra("images", new java.util.ArrayList<>(imageUrls));
            intent.putExtra("position", position);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPreview;
        ImageButton btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.ivPreview);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
