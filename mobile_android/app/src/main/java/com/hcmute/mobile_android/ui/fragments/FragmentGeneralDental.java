package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.ui.views.OdontogramView;

import java.util.HashMap;
import java.util.Map;

public class FragmentGeneralDental extends Fragment {

    private OdontogramView odontogramView;
    private TextView tvToothNotes;
    public EditText etReason, etDiagnosis;
    private Map<Integer, String> toothCustomNotesMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_general_dental, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        odontogramView = view.findViewById(R.id.odontogramView);
        tvToothNotes = view.findViewById(R.id.tvToothNotes);
        etReason = view.findViewById(R.id.etReason);
        etDiagnosis = view.findViewById(R.id.etDiagnosis);
        
        odontogramView.setOnToothSelectedListener(this::showToothNoteDialog);
    }

    public void onToothSelected(int toothNumber) {
        showToothNoteDialog(toothNumber);
    }

    private void showToothNoteDialog(int toothNumber) {
        if (getContext() == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_tooth_note, null);
        dialog.setContentView(view);
        
        TextView tvTitle = view.findViewById(R.id.tvToothTitle);
        tvTitle.setText("Ghi chú Răng R" + toothNumber);
        
        android.widget.RadioGroup rgStatus = view.findViewById(R.id.rgToothStatus);
        EditText etNote = view.findViewById(R.id.etToothNote);
        
        String existingStatus = odontogramView.getToothStatus(toothNumber);
        if (existingStatus != null) {
            switch (existingStatus) {
                case "caries": rgStatus.check(R.id.rbCaries); break;
                case "filled": rgStatus.check(R.id.rbFilled); break;
                case "requested": rgStatus.check(R.id.rbRequested); break;
                case "rct": rgStatus.check(R.id.rbRct); break;
                default: rgStatus.check(R.id.rbHealthy); break;
            }
        } else {
            rgStatus.check(R.id.rbHealthy);
        }
        
        if (toothCustomNotesMap.containsKey(toothNumber)) {
            etNote.setText(toothCustomNotesMap.get(toothNumber));
        }
        
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            int checkedId = rgStatus.getCheckedRadioButtonId();
            String status = "healthy", statusText = "Bình thường";
            
            if (checkedId == R.id.rbCaries) { status = "caries"; statusText = "Sâu răng"; }
            else if (checkedId == R.id.rbFilled) { status = "filled"; statusText = "Đã trám"; }
            else if (checkedId == R.id.rbRequested) { status = "requested"; statusText = "BN yêu cầu"; }
            else if (checkedId == R.id.rbRct) { status = "rct"; statusText = "Cần chữa tủy"; }
            
            odontogramView.setToothStatus(toothNumber, status);
            
            String customNote = etNote.getText().toString().trim();
            if (status.equals("healthy") && customNote.isEmpty()) {
                toothCustomNotesMap.remove(toothNumber);
            } else {
                toothCustomNotesMap.put(toothNumber, statusText + (customNote.isEmpty() ? "" : " - " + customNote));
            }
            updateToothNotesDisplay();
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void updateToothNotesDisplay() {
        if (toothCustomNotesMap.isEmpty()) {
            tvToothNotes.setText("Chưa có ghi chú nào. Nhấn vào răng trên sơ đồ để thêm ghi chú.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String> entry : toothCustomNotesMap.entrySet()) {
            sb.append("• R").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        tvToothNotes.setText(sb.toString().trim());
    }

    public java.util.List<com.hcmute.mobile_android.network.models.request.ToothTreatmentDTO> getToothTreatments() {
        java.util.List<com.hcmute.mobile_android.network.models.request.ToothTreatmentDTO> list = new java.util.ArrayList<>();
        for (Map.Entry<Integer, String> entry : toothCustomNotesMap.entrySet()) {
            list.add(new com.hcmute.mobile_android.network.models.request.ToothTreatmentDTO(
                String.valueOf(entry.getKey()), 
                entry.getValue()
            ));
        }
        return list;
    }
}
