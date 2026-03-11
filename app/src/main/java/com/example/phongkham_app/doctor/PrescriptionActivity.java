package com.example.phongkham_app.doctor;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Prescription;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionActivity extends AppCompatActivity {

    private RecyclerView rvPrescribedDrugs;
    private PrescriptionAdapter adapter;
    private List<Prescription> prescriptionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_activity_prescription);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rvPrescribedDrugs = findViewById(R.id.rvPrescribedDrugs);
        rvPrescribedDrugs.setLayoutManager(new LinearLayoutManager(this));

        prescriptionList = new ArrayList<>();
        // Add some dummy data for demonstration
        prescriptionList.add(new Prescription("Paracetamol 500mg", "Uống 1 viên / lần", "2 lần / ngày (sáng, tối)", "Sau ăn", "10", "5"));
        prescriptionList.add(new Prescription("Amoxicillin 250mg", "Uống 1 viên / lần", "3 lần / ngày", "Sau ăn", "15", "5"));

        adapter = new PrescriptionAdapter(this, prescriptionList);
        rvPrescribedDrugs.setAdapter(adapter);

        MaterialButton btnAddNewDrug = findViewById(R.id.btnAddNewDrug);
        btnAddNewDrug.setOnClickListener(v -> {
            // Logic to add a new drug (you might want to open an AddNewDrugActivity here)
            prescriptionList.add(new Prescription("Thuốc Mới", "", "", "", "", "")); // Placeholder for new drug
            adapter.notifyItemInserted(prescriptionList.size() - 1);
            rvPrescribedDrugs.scrollToPosition(prescriptionList.size() - 1);
        });

        MaterialButton btnFinishPrescription = findViewById(R.id.btnFinishPrescription);
        btnFinishPrescription.setOnClickListener(v -> {
            Toast.makeText(this, "Đơn thuốc đã được gửi.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PrescriptionAdapter.EDIT_PRESCRIPTION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_POSITION, -1);
            if (position != -1) {
                String drugName = data.getStringExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_DRUG_NAME);
                String dosage = data.getStringExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_DOSAGE);
                String frequency = data.getStringExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_FREQUENCY);
                String instruction = data.getStringExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_INSTRUCTION);
                String quantity = data.getStringExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_QUANTITY);
                String days = data.getStringExtra(UpdatePrescriptionActivity.EXTRA_PRESCRIPTION_DAYS);

                Prescription updatedPrescription = new Prescription(drugName, dosage, frequency, instruction, quantity, days);
                prescriptionList.set(position, updatedPrescription);
                adapter.notifyItemChanged(position);
                Toast.makeText(this, "Đã cập nhật đơn thuốc.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
