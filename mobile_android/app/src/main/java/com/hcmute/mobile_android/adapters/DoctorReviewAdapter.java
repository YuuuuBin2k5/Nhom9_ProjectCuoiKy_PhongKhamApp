package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.Review;

import java.util.ArrayList;
import java.util.List;

public class DoctorReviewAdapter extends RecyclerView.Adapter<DoctorReviewAdapter.Holder> {

    private final List<Review> items = new ArrayList<>();

    public void setItems(List<Review> reviews) {
        items.clear();
        if (reviews != null) {
            items.addAll(reviews);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_doctor, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Review r = items.get(position);
        String name = r.getPatientName() != null && !r.getPatientName().isEmpty()
                ? r.getPatientName()
                : "Bệnh nhân";
        holder.tvName.setText(name);
        int stars = r.getRating() != null ? Math.min(5, Math.max(0, r.getRating())) : 0;
        holder.tvStars.setText(starsRow(stars));
        holder.tvComment.setText(r.getComment() != null ? r.getComment() : "");
    }

    private static String starsRow(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("★");
        }
        for (int i = n; i < 5; i++) {
            sb.append("☆");
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvStars;
        final TextView tvComment;

        Holder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvReviewerName);
            tvStars = v.findViewById(R.id.tvReviewStars);
            tvComment = v.findViewById(R.id.tvReviewComment);
        }
    }
}
