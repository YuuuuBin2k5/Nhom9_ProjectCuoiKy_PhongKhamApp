package com.example.phongkham_app.ui.doctor;

import com.example.phongkham_app.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class UpdatePrescriptionActivity extends AppCompatActivity {

    public static final String EXTRA_PRESCRIPTION_POSITION = "extra_prescription_position";
    public static final String EXTRA_PRESCRIPTION_DRUG_NAME = "extra_prescription_drug_name";
    public static final String EXTRA_PRESCRIPTION_DOSAGE = "extra_prescription_dosage";
    public static final String EXTRA_PRESCRIPTION_FREQUENCY = "extra_prescription_frequency";
    public static final String EXTRA_PRESCRIPTION_INSTRUCTION = "extra_prescription_instruction";
    public static final String EXTRA_PRESCRIPTION_QUANTITY = "extra_prescription_quantity";
    public static final String EXTRA_PRESCRIPTION_DAYS = "extra_prescription_days";

    private TextInputEditText etDrugName, etDosage, etQuantity, etDays;
    private AutoCompleteTextView actvFrequency, actvInstruction;
    private MaterialButton btnSavePrescription;

    private final String[] frequencyOptions = {"1 lần / ngày", "2 lần / ngày (sáng, tối)", "3 lần / ngày", "Khi cần"};
    private final String[] instructionOptions = {"Trước ăn", "Sau ăn", "Trong bữa ăn"};

    private int prescriptionPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_activity_update_prescription);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etDrugName = findViewById(R.id.etDrugName);
        etDosage = findViewById(R.id.etDosage);
        actvFrequency = findViewById(R.id.actvFrequency);
        actvInstruction = findViewById(R.id.actvInstruction);
        etQuantity = findViewById(R.id.etQuantity);
        etDays = findViewById(R.id.etDays);
        btnSavePrescription = findViewById(R.id.btnSavePrescription);

        // Set up adapters for AutoCompleteTextViews
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, frequencyOptions);
        actvFrequency.setAdapter(frequencyAdapter);

        ArrayAdapter<String> instructionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, instructionOptions);
        actvInstruction.setAdapter(instructionAdapter);

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            prescriptionPosition = intent.getIntExtra(EXTRA_PRESCRIPTION_POSITION, -1);
            etDrugName.setText(intent.getStringExtra(EXTRA_PRESCRIPTION_DRUG_NAME));
            etDosage.setText(intent.getStringExtra(EXTRA_PRESCRIPTION_DOSAGE));
            actvFrequency.setText(intent.getStringExtra(EXTRA_PRESCRIPTION_FREQUENCY));
            actvInstruction.setText(intent.getStringExtra(EXTRA_PRESCRIPTION_INSTRUCTION));
            etQuantity.setText(intent.getStringExtra(EXTRA_PRESCRIPTION_QUANTITY));
            etDays.setText(intent.getStringExtra(EXTRA_PRESCRIPTION_DAYS));
        }

        btnSavePrescription.setOnClickListener(v -> {
            savePrescription();
        });
    }

    private void savePrescription() {
        String drugName = etDrugName.getText().toString();
        String dosage = etDosage.getText().toString();
        String frequency = actvFrequency.getText().toString();
        String instruction = actvInstruction.getText().toString();
        String quantity = etQuantity.getText().toString();
        String days = etDays.getText().toString();

        if (drugName.isEmpty() || dosage.isEmpty() || frequency.isEmpty() || instruction.isEmpty() || quantity.isEmpty() || days.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_PRESCRIPTION_POSITION, prescriptionPosition);
        resultIntent.putExtra(EXTRA_PRESCRIPTION_DRUG_NAME, drugName);
        resultIntent.putExtra(EXTRA_PRESCRIPTION_DOSAGE, dosage);
        resultIntent.putExtra(EXTRA_PRESCRIPTION_FREQUENCY, frequency);
        resultIntent.putExtra(EXTRA_PRESCRIPTION_INSTRUCTION, instruction);
        resultIntent.putExtra(EXTRA_PRESCRIPTION_QUANTITY, quantity);
        resultIntent.putExtra(EXTRA_PRESCRIPTION_DAYS, days);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
