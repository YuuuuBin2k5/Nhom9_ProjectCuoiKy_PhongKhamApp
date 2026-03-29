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
        
        android.util.Log.d("FragmentGeneralDental", "=== onViewCreated ===");
        android.util.Log.d("FragmentGeneralDental", "Fragment instance: " + this.hashCode());
        
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
            
            android.util.Log.d("FragmentGeneralDental", "=== Saved tooth note ===");
            android.util.Log.d("FragmentGeneralDental", "Fragment instance: " + FragmentGeneralDental.this.hashCode());
            android.util.Log.d("FragmentGeneralDental", "Tooth: " + toothNumber);
            android.util.Log.d("FragmentGeneralDental", "Status: " + statusText);
            android.util.Log.d("FragmentGeneralDental", "Custom note: '" + customNote + "'");
            android.util.Log.d("FragmentGeneralDental", "toothCustomNotesMap size after save: " + toothCustomNotesMap.size());
            android.util.Log.d("FragmentGeneralDental", "toothCustomNotesMap content: " + toothCustomNotesMap.toString());
            
            updateToothNotesDisplay();
            dialog.dismiss();
        });
        
        // Add service button inside dialog
        view.findViewById(R.id.btnAssignService).setOnClickListener(v -> {
            dialog.dismiss();
            if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) {
                ((com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) getActivity()).onToothSelected(toothNumber);
            }
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

    public String getFormDataNotes() {
        android.util.Log.d("FragmentGeneralDental", "=== getFormDataNotes() called ===");
        android.util.Log.d("FragmentGeneralDental", "Fragment instance: " + this.hashCode());
        android.util.Log.d("FragmentGeneralDental", "toothCustomNotesMap size: " + toothCustomNotesMap.size());
        android.util.Log.d("FragmentGeneralDental", "toothCustomNotesMap content: " + toothCustomNotesMap.toString());
        
        StringBuilder sb = new StringBuilder();
        String reason = etReason.getText().toString().trim();
        String diagnosis = etDiagnosis.getText().toString().trim();
        
        android.util.Log.d("FragmentGeneralDental", "reason: '" + reason + "'");
        android.util.Log.d("FragmentGeneralDental", "diagnosis: '" + diagnosis + "'");
        
        if (!reason.isEmpty()) sb.append("Lý do: ").append(reason).append("\n");
        if (!diagnosis.isEmpty()) sb.append("Chẩn đoán: ").append(diagnosis).append("\n");
        if (!toothCustomNotesMap.isEmpty()) {
            sb.append("Tình trạng răng:\n");
            for (Map.Entry<Integer, String> entry : toothCustomNotesMap.entrySet()) {
                sb.append("- R").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        String result = sb.toString().trim();
        android.util.Log.d("FragmentGeneralDental", "Final result: '" + result + "'");
        android.util.Log.d("FragmentGeneralDental", "=== getFormDataNotes() end ===");
        return result;
    }

    public void setData(String doctorConclusion) {
        if (doctorConclusion == null || doctorConclusion.isEmpty()) {
            return;
        }
        
        android.util.Log.d("FragmentGeneralDental", "=== setData() called ===");
        android.util.Log.d("FragmentGeneralDental", "Fragment instance: " + this.hashCode());
        android.util.Log.d("FragmentGeneralDental", "doctorConclusion: " + doctorConclusion);
        
        // Clear existing data first
        toothCustomNotesMap.clear();
        
        // Parse the conclusion and populate fields
        String[] lines = doctorConclusion.split("\n");
        boolean inToothSection = false;
        
        for (String line : lines) {
            if (line.startsWith("Lý do: ")) {
                if (etReason != null) {
                    etReason.setText(line.substring(7).trim());
                }
            } else if (line.startsWith("Chẩn đoán: ")) {
                if (etDiagnosis != null) {
                    etDiagnosis.setText(line.substring(11).trim());
                }
            } else if (line.startsWith("Tình trạng răng:")) {
                inToothSection = true;
            } else if (inToothSection && line.trim().startsWith("- R")) {
                // Parse tooth data: "- R12: Sâu răng - note text"
                try {
                    String toothData = line.trim().substring(2); // Remove "- "
                    int colonIndex = toothData.indexOf(":");
                    if (colonIndex > 0) {
                        String toothNumStr = toothData.substring(1, colonIndex).trim(); // Remove "R"
                        String toothNote = toothData.substring(colonIndex + 1).trim();
                        
                        int toothNumber = Integer.parseInt(toothNumStr);
                        toothCustomNotesMap.put(toothNumber, toothNote);
                        
                        // Parse status from note to update odontogram visual
                        String status = "healthy";
                        if (toothNote.contains("Sâu răng")) {
                            status = "caries";
                        } else if (toothNote.contains("Đã trám")) {
                            status = "filled";
                        } else if (toothNote.contains("BN yêu cầu")) {
                            status = "requested";
                        } else if (toothNote.contains("Cần chữa tủy")) {
                            status = "rct";
                        }
                        
                        if (odontogramView != null) {
                            odontogramView.setToothStatus(toothNumber, status);
                        }
                        
                        android.util.Log.d("FragmentGeneralDental", "Loaded tooth R" + toothNumber + ": " + toothNote + " (status: " + status + ")");
                    }
                } catch (Exception e) {
                    android.util.Log.e("FragmentGeneralDental", "Error parsing tooth line: " + line, e);
                }
            }
        }
        
        // Update display
        updateToothNotesDisplay();
        
        android.util.Log.d("FragmentGeneralDental", "toothCustomNotesMap size after setData: " + toothCustomNotesMap.size());
        android.util.Log.d("FragmentGeneralDental", "toothCustomNotesMap content: " + toothCustomNotesMap.toString());
        android.util.Log.d("FragmentGeneralDental", "=== setData() end ===");
    }

    public void setReadOnlyMode(boolean readOnly) {
        if (etReason != null) {
            etReason.setEnabled(!readOnly);
            etReason.setFocusable(!readOnly);
        }
        if (etDiagnosis != null) {
            etDiagnosis.setEnabled(!readOnly);
            etDiagnosis.setFocusable(!readOnly);
        }
        if (odontogramView != null) {
            odontogramView.setEnabled(!readOnly);
        }
    }
}
