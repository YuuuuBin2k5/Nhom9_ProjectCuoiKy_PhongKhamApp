package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.hcmute.mobile_android.R;

import android.widget.CheckBox;
import android.widget.EditText;

public class FragmentSurgeryChecklist extends Fragment {

    private EditText etBloodPressure, etHeartRate, etSurgeryNotes;
    private CheckBox cbCoagulation, cbAllergy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_surgery_checklist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etBloodPressure = view.findViewById(R.id.etBloodPressure);
        etHeartRate = view.findViewById(R.id.etHeartRate);
        etSurgeryNotes = view.findViewById(R.id.etSurgeryNotes);
        cbCoagulation = view.findViewById(R.id.cbCoagulation);
        cbAllergy = view.findViewById(R.id.cbAllergy);
    }

    public String getFormDataNotes() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Sinh hiệu] BP: ").append(etBloodPressure.getText().toString().trim())
          .append(", HR: ").append(etHeartRate.getText().toString().trim()).append("\n");
        if (cbCoagulation.isChecked()) sb.append("- Máu khó đông\n");
        if (cbAllergy.isChecked()) sb.append("- Dị ứng thuốc tê\n");
        String notes = etSurgeryNotes.getText().toString().trim();
        if (!notes.isEmpty()) sb.append("- Ghi chú: ").append(notes);
        return sb.toString().trim();
    }
}
