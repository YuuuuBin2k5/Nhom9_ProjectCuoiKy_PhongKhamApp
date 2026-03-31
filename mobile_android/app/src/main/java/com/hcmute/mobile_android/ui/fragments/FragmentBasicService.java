package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FragmentBasicService extends Fragment {

    private TextView tvServiceNameHeader;
    private TextView tvSelectedTeeth;
    private MaterialButton btnSelectTeeth;
    private EditText etBasicDiagnosis;
    private EditText etBasicNotes;
    // Data
    private Set<Integer> selectedTeeth = new HashSet<>();
    private boolean isSelectingTeeth = false;
    private Long currentStepId;
    private String serviceName;

    // State modes
    private boolean isReadOnly = false;
    private boolean isEditMode = false;

    public FragmentBasicService() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(com.hcmute.mobile_android.R.layout.fragment_basic_service, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        tvServiceNameHeader = view.findViewById(com.hcmute.mobile_android.R.id.tvServiceNameHeader);
        tvSelectedTeeth = view.findViewById(com.hcmute.mobile_android.R.id.tvSelectedTeeth);
        btnSelectTeeth = view.findViewById(com.hcmute.mobile_android.R.id.btnSelectTeeth);
        etBasicDiagnosis = view.findViewById(com.hcmute.mobile_android.R.id.etBasicDiagnosis);
        etBasicNotes = view.findViewById(com.hcmute.mobile_android.R.id.etBasicNotes);

        updateSelectedTeethDisplay();
    }

    private void setupListeners() {
        btnSelectTeeth.setOnClickListener(v -> {
            isSelectingTeeth = !isSelectingTeeth;
            if (isSelectingTeeth) {
                btnSelectTeeth.setText("Đang chọn... (Nhấn để dừn)");
                Toast.makeText(getContext(), "Vui lòng chọn răng trên sơ đồ", Toast.LENGTH_SHORT).show();
            } else {
                btnSelectTeeth.setText("Chọn răng trên sơ đồ");
            }
        });
    }

    /**
     * Set the current service being edited/executed
     */
    public void loadStepData(Long stepId, String name, String diagnosis, String notes, List<Integer> teeth) {
        this.currentStepId = stepId;
        this.serviceName = name;
        this.selectedTeeth.clear();
        
        if (tvServiceNameHeader != null) {
            tvServiceNameHeader.setText(name != null ? name : "Chi tiết dịch vụ");
        }
        
        if (teeth != null) {
            this.selectedTeeth.addAll(teeth);
        }
        
        if (etBasicDiagnosis != null && diagnosis != null) {
            etBasicDiagnosis.setText(diagnosis);
        } else if (etBasicDiagnosis != null) {
            etBasicDiagnosis.setText("");
        }

        if (etBasicNotes != null && notes != null) {
            etBasicNotes.setText(notes);
        } else if (etBasicNotes != null) {
            etBasicNotes.setText("");
        }

        updateSelectedTeethDisplay();
    }

    /**
     * Called when a tooth is clicked on Odontogram
     */
    public void onToothClicked(int toothNumber) {
        if (selectedTeeth.contains(toothNumber)) {
            selectedTeeth.remove(toothNumber);
            Toast.makeText(getContext(), "Đã bỏ chọn răng " + toothNumber, Toast.LENGTH_SHORT).show();
        } else {
            selectedTeeth.add(toothNumber);
            Toast.makeText(getContext(), "Đã chọn răng " + toothNumber, Toast.LENGTH_SHORT).show();
        }
        
        updateSelectedTeethDisplay();
        
        // Cập nhật lên Sơ đồ
        if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) {
            ((com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) getActivity())
                .updateOdontogramSelection(selectedTeeth);
        }
    }

    public List<Integer> getSelectedTeeth() {
        List<Integer> list = new ArrayList<>(selectedTeeth);
        Collections.sort(list);
        return list;
    }

    private void updateSelectedTeethDisplay() {
        if (tvSelectedTeeth == null) return;
        
        if (selectedTeeth.isEmpty()) {
            tvSelectedTeeth.setText("Chưa chọn răng nào. Nhấn vào răng trên sơ đồ để chọn.");
        } else {
            List<Integer> sortedTeeth = new ArrayList<>(selectedTeeth);
            Collections.sort(sortedTeeth);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Đã chọn ").append(selectedTeeth.size()).append(" răng: ");
            for (int i = 0; i < sortedTeeth.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("R").append(sortedTeeth.get(i));
            }
            tvSelectedTeeth.setText(sb.toString());
        }
    }
    
    public String getFormDataNotes() {
        StringBuilder sb = new StringBuilder();
        String diagnosis = etBasicDiagnosis.getText().toString().trim();
        String notes = etBasicNotes.getText().toString().trim();
        
        if (!diagnosis.isEmpty()) {
            sb.append("Chẩn đoán: ").append(diagnosis).append("\n");
        }
        if (!notes.isEmpty()) {
            sb.append("Ghi chú thực hiện: ").append(notes);
        }
        return sb.toString();
    }
    


    public void setReadOnlyMode(boolean readOnly) {
        this.isReadOnly = readOnly;
        this.isEditMode = false;
        
        boolean canEdit = !readOnly;
        if (etBasicDiagnosis != null) {
            etBasicDiagnosis.setEnabled(canEdit);
        }
        if (etBasicNotes != null) {
            etBasicNotes.setEnabled(canEdit);
        }
        if (btnSelectTeeth != null) {
            btnSelectTeeth.setEnabled(canEdit);
            btnSelectTeeth.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        }
    }
    /**
     * Restore data from conclusion string
     */
    public void setData(String data) {
        if (data == null || data.isEmpty()) return;
        
        String diagnosisPrefix = "Chẩn đoán: ";
        String notesPrefix = "Ghi chú thực hiện: ";
        
        String diagnosis = "";
        String notes = "";
        
        if (data.contains(diagnosisPrefix)) {
            int start = data.indexOf(diagnosisPrefix) + diagnosisPrefix.length();
            int end = data.indexOf("\n", start);
            if (end == -1) end = data.length();
            diagnosis = data.substring(start, end).trim();
        }
        
        if (data.contains(notesPrefix)) {
            int start = data.indexOf(notesPrefix) + notesPrefix.length();
            notes = data.substring(start).trim();
        }
        
        if (etBasicDiagnosis != null) etBasicDiagnosis.setText(diagnosis);
        if (etBasicNotes != null) etBasicNotes.setText(notes);
    }
}
