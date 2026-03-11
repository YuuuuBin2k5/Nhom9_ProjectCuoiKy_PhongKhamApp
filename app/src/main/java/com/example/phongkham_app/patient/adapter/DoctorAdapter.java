package com.example.phongkham_app.patient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Doctor;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors = new ArrayList<>();
    private final OnDoctorActionListener listener;

    public interface OnDoctorActionListener {
        void onViewDetailClick(Doctor doctor);
        void onAddReviewClick(Doctor doctor);
        void onFavoriteClick(Doctor doctor);
    }

    public DoctorAdapter(OnDoctorActionListener listener) {
        this.listener = listener;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
        notifyDataSetChanged();
    }

    public void updateData(List<Doctor> newDoctors) {
        this.doctors = newDoctors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item_doctor_record, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.bind(doctor);
        
        // Item Animation: fade in + translate
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(50f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(100L * position)
                .start();
    }

    @Override
    public int getItemCount() {
        return doctors != null ? doctors.size() : 0;
    }

    class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivAvatar;
        private final TextView tvName, tvSpecialty, tvRating;
        private final ImageView ivFavorite, ivStar1, ivStar2, ivStar3, ivStar4, ivStar5;
        private final Button btnViewDetail, btnAddReview;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivDoctorAvatar);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvRating = itemView.findViewById(R.id.tvRating);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnAddReview = itemView.findViewById(R.id.btnAddReview);

            ivStar1 = itemView.findViewById(R.id.ivStar1);
            ivStar2 = itemView.findViewById(R.id.ivStar2);
            ivStar3 = itemView.findViewById(R.id.ivStar3);
            ivStar4 = itemView.findViewById(R.id.ivStar4);
            ivStar5 = itemView.findViewById(R.id.ivStar5);
        }

        public void bind(final Doctor doctor) {
            tvName.setText(doctor.getName());
            tvSpecialty.setText(doctor.getSpecialty());
            tvRating.setText(String.valueOf(doctor.getRating()));

            // Set favorite icon color
            if (doctor.isFavorite()) {
                ivFavorite.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.red_heart));
            } else {
                ivFavorite.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary)); // Gray
            }

            // Bind rating stars
            ImageView[] stars = {ivStar1, ivStar2, ivStar3, ivStar4, ivStar5};
            int filledStars = (int) Math.round(doctor.getRating());
            for (int i = 0; i < 5; i++) {
                if (i < filledStars) {
                    stars[i].setImageResource(R.drawable.ic_star_filled);
                } else {
                    stars[i].setImageResource(R.drawable.ic_star_empty);
                }
            }

            // Click Listeners
            btnViewDetail.setOnClickListener(v -> listener.onViewDetailClick(doctor));
            btnAddReview.setOnClickListener(v -> listener.onAddReviewClick(doctor));

            ivFavorite.setOnClickListener(v -> {
                listener.onFavoriteClick(doctor);
                // Heart scale animation
                v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150)
                 .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
            });
        }
    }
}
