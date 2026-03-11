package com.example.phongkham_app.doctor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Medicine;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private List<Medicine> medicineList;
    private OnRemoveClickListener listener;

    // *** SỬA LỖI LOGIC: Đổi tên interface cho rõ ràng ***
    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.listener = listener;
    }

    public MedicineAdapter(List<Medicine> medicineList) {
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_item_medicine, parent, false);
        // *** SỬA LỖI LOGIC: Truyền listener vào ViewHolder ***
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);
        holder.tvMedicineName.setText(medicine.getName());
        holder.tvMedicineDosage.setText(medicine.getDosage());
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName;
        TextView tvMedicineDosage;
        ImageView btnRemoveMedicine;

        // *** SỬA LỖI LOGIC: Sửa lại hàm khởi tạo của ViewHolder ***
        public ViewHolder(@NonNull View itemView, final OnRemoveClickListener listener) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvMedicineDosage = itemView.findViewById(R.id.tvMedicineDosage);
            btnRemoveMedicine = itemView.findViewById(R.id.btnRemoveMedicine);

            // Chỉ gán sự kiện click cho NÚT XÓA
            btnRemoveMedicine.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRemoveClick(position);
                    }
                }
            });
        }
    }
}
