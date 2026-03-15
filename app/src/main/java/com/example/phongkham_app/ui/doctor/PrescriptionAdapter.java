package com.example.phongkham_app.ui.doctor;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Prescription;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder> {

    private final Context context;
    private final List<Prescription> prescriptionList;
    private final String[] frequencyOptions = {"1 lần / ngày", "2 lần / ngày (sáng, tối)", "3 lần / ngày", "Khi cần"};
    private final String[] instructionOptions = {"Trước ăn", "Sau ăn", "Trong bữa ăn"};

    public static final int EDIT_PRESCRIPTION_REQUEST_CODE = 1;

    public PrescriptionAdapter(Context context, List<Prescription> prescriptionList) {
        this.context = context;
        this.prescriptionList = prescriptionList;
    }

    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doctor_item_prescription_drug, parent, false);
        return new PrescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionViewHolder holder, int position) {
        Prescription prescription = prescriptionList.get(position);
        holder.tvDrugName.setText(prescription.getDrugName());
        holder.etDosage.setText(prescription.getDosage());
        holder.actvFrequency.setText(prescription.getFrequency());
        holder.actvInstruction.setText(prescription.getInstruction());
        holder.etQuantity.setText(prescription.getQuantity());
        holder.etDays.setText(prescription.getDays());

        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, frequencyOptions);
        holder.actvFrequency.setAdapter(frequencyAdapter);

        ArrayAdapter<String> instructionAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, instructionOptions);
        holder.actvInstruction.setAdapter(instructionAdapter);

        holder.ivDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                prescriptionList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, prescriptionList.size());
            }
        });

        holder.ivEdit.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Prescription currentPrescription = prescriptionList.get(currentPosition);

                Intent intent = new Intent(context, UpdatePrescriptionActivity.class);
                intent.putExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_POSITION, currentPosition);
                intent.putExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_DRUG_NAME, currentPrescription.getDrugName());
                intent.putExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_DOSAGE, currentPrescription.getDosage());
                intent.putExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_FREQUENCY, currentPrescription.getFrequency());
                intent.putExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_INSTRUCTION, currentPrescription.getInstruction());
                intent.putExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_QUANTITY, currentPrescription.getQuantity());
                intent.putExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_DAYS, currentPrescription.getDays());

                ((Activity) context).startActivityForResult(intent, EDIT_PRESCRIPTION_REQUEST_CODE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return prescriptionList.size();
    }

    public static class PrescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDrugName;
        TextInputEditText etDosage, etQuantity, etDays;
        AutoCompleteTextView actvFrequency, actvInstruction;
        ImageView ivEdit, ivDelete;

        public PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDrugName = itemView.findViewById(R.id.tvDrugName);
            etDosage = (TextInputEditText) ((TextInputLayout) itemView.findViewById(R.id.tilDosage)).getEditText();
            actvFrequency = itemView.findViewById(R.id.actvFrequency);
            actvInstruction = itemView.findViewById(R.id.actvInstruction);
            etQuantity = (TextInputEditText) ((TextInputLayout) itemView.findViewById(R.id.tilQuantity)).getEditText();
            etDays = (TextInputEditText) ((TextInputLayout) itemView.findViewById(R.id.tilDays)).getEditText();
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
