package com.hcmute.mobile_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;

import java.util.List;

public class ServiceImageAdapter extends RecyclerView.Adapter<ServiceImageAdapter.ViewHolder> {

    private final List<String> imageUrls;
    private final Context context;

    public ServiceImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String originalUrl = imageUrls.get(position);
        String imageName = originalUrl;
        
        // Remove .png extension if present for getIdentifier
        if (imageName.endsWith(".png")) {
            imageName = imageName.substring(0, imageName.length() - 4);
        } else if (imageName.endsWith(".jpg") || imageName.endsWith(".jpeg")) {
            imageName = imageName.substring(0, imageName.lastIndexOf("."));
        }
        
        // Also handle the case where it might be a full URL but we want the filename part
        if (imageName.contains("/")) {
            imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
        }

        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        if (resId != 0) {
            // Found local resource matching the seed name
            holder.ivServiceImage.setImageResource(resId);
        } else {
            // Not a local resource (likely a newly uploaded image), use Glide
            com.bumptech.glide.Glide.with(context)
                    .load(originalUrl)
                    .placeholder(R.drawable.ic_tooth)
                    .error(R.drawable.ic_tooth)
                    .into(holder.ivServiceImage);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivServiceImage;

        ViewHolder(View itemView) {
            super(itemView);
            ivServiceImage = itemView.findViewById(R.id.ivServiceImage);
        }
    }
}
