package com.example.phongkham_app.ui.patient.adapter;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item_doctor, parent, false);
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
        private final ImageView ivAvatar;
        private final TextView tvName, tvSpecialty, tvExp;
        private final Button btnBook;
        private final ImageView ivPerson;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.imgDoctor);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialty = itemView.findViewById(R.id.tvMajor);
            tvExp = itemView.findViewById(R.id.tvExp);
            btnBook = itemView.findViewById(R.id.btnBook);
            ivPerson = itemView.findViewById(R.id.ivPerson);
        }

        public void bind(final Doctor doctor) {
            if (tvName != null) tvName.setText(doctor.getName());
            if (tvSpecialty != null) tvSpecialty.setText(doctor.getSpecialty());
            
            // Experience is not in basic Doctor model constructor, setting static text for demo if available
            if (tvExp != null) tvExp.setText("Kinh nghiệm: 10 năm"); 

            // Click Listeners
            if (ivPerson != null) {
                ivPerson.setOnClickListener(v -> {
                    if (listener != null) listener.onViewDetailClick(doctor);
                });
            }
            if (btnBook != null) {
                btnBook.setOnClickListener(v -> {
                    // booking logic could go here, or just redirect
                    if (listener != null) listener.onViewDetailClick(doctor);
                });
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetailClick(doctor);
            });
        }
    }
}
