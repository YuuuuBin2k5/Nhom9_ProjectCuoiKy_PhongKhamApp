package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragment for Crown (Bọc răng sứ) service
 * Allows selecting crown type and multiple teeth
 */
public class FragmentCrownService extends Fragment {

    private Spinner spinnerCrownType;
    private TextView tvSelectedTeeth;
    private MaterialButton btnSelectTeeth;
    private EditText etCrownNotes;
    private Button btnEditMode;
    private MaterialButton btnAddCrownService;
    
    private Set<Integer> selectedTeeth = new HashSet<>();
    private boolean isReadOnly = false;
    private boolean isEditMode = false;
    private boolean isSelectingTeeth = false;
    
    // Crown types with prices (using single service "Bọc răng sứ" with customPrice)
    private static class CrownType {
        String name;
        double price;  // Price in VND
        
        CrownType(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
    
    private static final CrownType[] CROWN_TYPES = {
        new CrownType("Sứ Titan (3,000,000đ)", 3000000),
        new CrownType("Sứ Zirconia (5,000,000đ)", 5000000),
        new CrownType("Sứ Emax (7,000,000đ)", 7000000),
        new CrownType("Sứ Cercon (6,000,000đ)", 6000000),
        new CrownType("Dán sứ Veneer (8,000,000đ)", 8000000)
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crown_service, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        spinnerCrownType = view.findViewById(R.id.spinnerCrownType);
        tvSelectedTeeth = view.findViewById(R.id.tvSelectedTeeth);
        btnSelectTeeth = view.findViewById(R.id.btnSelectTeeth);
        etCrownNotes = view.findViewById(R.id.etCrownNotes);
        btnEditMode = view.findViewById(R.id.btnEditMode);
        btnAddCrownService = view.findViewById(R.id.btnAddCrownService);
        
        // Setup crown type spinner
        String[] crownTypeNames = new String[CROWN_TYPES.length];
        for (int i = 0; i < CROWN_TYPES.length; i++) {
            crownTypeNames[i] = CROWN_TYPES[i].name;
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            crownTypeNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCrownType.setAdapter(adapter);
        
        // Show button to select teeth
        if (btnSelectTeeth != null) {
            btnSelectTeeth.setVisibility(View.VISIBLE);
            btnSelectTeeth.setOnClickListener(v -> {
                isSelectingTeeth = true;
                Toast.makeText(getContext(), "Nhấn vào răng trên sơ đồ để chọn", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Show button to add crown service
        if (btnAddCrownService != null) {
            btnAddCrownService.setVisibility(View.VISIBLE);
            btnAddCrownService.setOnClickListener(v -> onAddCrownService());
        }
        
        // Setup edit mode button
        if (btnEditMode != null) {
            btnEditMode.setOnClickListener(v -> toggleEditMode());
            btnEditMode.setVisibility(View.GONE);
        }
        
        // Restore state if available
        if (savedInstanceState != null) {
            isReadOnly = savedInstanceState.getBoolean("isReadOnly", false);
            isEditMode = savedInstanceState.getBoolean("isEditMode", false);
            
            int[] teethArray = savedInstanceState.getIntArray("selectedTeeth");
            if (teethArray != null) {
                selectedTeeth.clear();
                for (int tooth : teethArray) {
                    selectedTeeth.add(tooth);
                }
            }
            
            int crownTypeIndex = savedInstanceState.getInt("crownTypeIndex", 0);
            spinnerCrownType.setSelection(crownTypeIndex);
            
            String notes = savedInstanceState.getString("notes");
            if (notes != null) {
                etCrownNotes.setText(notes);
            }
        }
        
        updateSelectedTeethDisplay();
        updateEditableState();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isReadOnly", isReadOnly);
        outState.putBoolean("isEditMode", isEditMode);
        
        int[] teethArray = new int[selectedTeeth.size()];
        int i = 0;
        for (int tooth : selectedTeeth) {
            teethArray[i++] = tooth;
        }
        outState.putIntArray("selectedTeeth", teethArray);
        
        outState.putInt("crownTypeIndex", spinnerCrownType.getSelectedItemPosition());
        outState.putString("notes", etCrownNotes.getText().toString());
    }

    /**
     * Called when a tooth is clicked on the odontogram
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
        
        // Notify activity to update odontogram
        if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) {
            ((com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) getActivity())
                .updateOdontogramSelection(selectedTeeth);
        }
    }

    /**
     * Stop tooth selection mode
     */
    public void stopToothSelection() {
        isSelectingTeeth = false;
    }

    /**
     * Get selected teeth as sorted list
     */
    public List<Integer> getSelectedTeeth() {
        List<Integer> list = new ArrayList<>(selectedTeeth);
        Collections.sort(list);
        return list;
    }

    /**
     * Set selected teeth (when loading existing data)
     */
    public void setSelectedTeeth(List<Integer> teeth) {
        selectedTeeth.clear();
        if (teeth != null) {
            selectedTeeth.addAll(teeth);
        }
        updateSelectedTeethDisplay();
    }

    private void updateSelectedTeethDisplay() {
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

    /**
     * Get form data as formatted notes string
     */
    public String getFormDataNotes() {
        StringBuilder sb = new StringBuilder();
        
        // Crown type name
        String crownTypeName = getSelectedCrownTypeName();
        sb.append("Loại răng sứ: ").append(crownTypeName).append("\n");
        
        // Selected teeth
        if (!selectedTeeth.isEmpty()) {
            List<Integer> sortedTeeth = new ArrayList<>(selectedTeeth);
            Collections.sort(sortedTeeth);
            
            sb.append("Răng bọc sứ: ");
            for (int i = 0; i < sortedTeeth.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("R").append(sortedTeeth.get(i));
            }
            sb.append("\n");
        }
        
        // Notes
        String notes = etCrownNotes.getText().toString().trim();
        if (!notes.isEmpty()) {
            sb.append("Ghi chú: ").append(notes);
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Get selected crown type name
     */
    public String getSelectedCrownTypeName() {
        int selectedIndex = spinnerCrownType.getSelectedItemPosition();
        if (selectedIndex >= 0 && selectedIndex < CROWN_TYPES.length) {
            return CROWN_TYPES[selectedIndex].name;
        }
        return CROWN_TYPES[0].name;
    }
    
    /**
     * Get selected crown price for API call
     * Returns price as BigDecimal for backend
     */
    public java.math.BigDecimal getSelectedCrownPrice() {
        int selectedIndex = spinnerCrownType.getSelectedItemPosition();
        if (selectedIndex >= 0 && selectedIndex < CROWN_TYPES.length) {
            return java.math.BigDecimal.valueOf(CROWN_TYPES[selectedIndex].price);
        }
        return java.math.BigDecimal.valueOf(CROWN_TYPES[0].price);
    }

    /**
     * Validate form before completion
     */
    public boolean validateForm() {
        if (selectedTeeth.isEmpty()) {
            Toast.makeText(getContext(), 
                "Vui lòng chọn ít nhất một răng để bọc sứ", 
                Toast.LENGTH_LONG).show();
            return false;
        }
        
        return true;
    }

    /**
     * Load data from doctorConclusion string
     */
    public void setData(String doctorConclusion) {
        if (doctorConclusion == null || doctorConclusion.trim().isEmpty()) {
            return;
        }
        
        selectedTeeth.clear();
        
        String[] lines = doctorConclusion.split("\n");
        for (String line : lines) {
            line = line.trim();
            
            if (line.startsWith("Loại răng sứ: ")) {
                String typeName = line.substring("Loại răng sứ: ".length()).trim();
                // Find matching crown type by name
                for (int i = 0; i < CROWN_TYPES.length; i++) {
                    if (CROWN_TYPES[i].name.equals(typeName) || 
                        typeName.contains(CROWN_TYPES[i].name.split(" \\(")[0])) {
                        spinnerCrownType.setSelection(i);
                        break;
                    }
                }
            } else if (line.startsWith("Răng bọc sứ: ")) {
                String teethStr = line.substring("Răng bọc sứ: ".length()).trim();
                // Parse teeth: "R11, R12, R13"
                String[] teethParts = teethStr.split(",");
                for (String toothStr : teethParts) {
                    toothStr = toothStr.trim();
                    if (toothStr.startsWith("R")) {
                        try {
                            int toothNumber = Integer.parseInt(toothStr.substring(1));
                            selectedTeeth.add(toothNumber);
                        } catch (NumberFormatException e) {
                            // Ignore invalid tooth numbers
                        }
                    }
                }
            } else if (line.startsWith("Ghi chú: ")) {
                String notes = line.substring("Ghi chú: ".length()).trim();
                etCrownNotes.setText(notes);
            }
        }
        
        updateSelectedTeethDisplay();
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        updateEditableState();
        
        if (btnEditMode != null) {
            btnEditMode.setText(isEditMode ? "Lưu" : "Chỉnh sửa");
        }
        
        if (!isEditMode) {
            Toast.makeText(getContext(), "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEditableState() {
        boolean canEdit = !isReadOnly || isEditMode;
        
        spinnerCrownType.setEnabled(canEdit);
        btnSelectTeeth.setEnabled(canEdit);
        btnSelectTeeth.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        
        if (etCrownNotes != null) {
            etCrownNotes.setEnabled(canEdit);
            etCrownNotes.setFocusable(canEdit);
            etCrownNotes.setFocusableInTouchMode(canEdit);
            etCrownNotes.setTextColor(canEdit ? 0xFF000000 : 0xFF757575);
        }
    }

    public void setReadOnlyMode(boolean readOnly) {
        this.isReadOnly = readOnly;
        this.isEditMode = false;
        updateEditableState();
        
        if (btnEditMode != null) {
            btnEditMode.setVisibility(readOnly ? View.VISIBLE : View.GONE);
            btnEditMode.setText("Chỉnh sửa");
        }
    }
    
    /**
     * Handle add crown service button click
     */
    private void onAddCrownService() {
        // Validate form
        if (!validateForm()) {
            return;
        }
        
        // Get activity and call the method to add crown service
        if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) {
            com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity activity = 
                (com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) getActivity();
            
            // Get selected data
            List<Integer> teeth = getSelectedTeeth();
            java.math.BigDecimal price = getSelectedCrownPrice();
            String notes = etCrownNotes.getText().toString().trim();
            
            // Call activity method to add crown service
            activity.addCrownServiceToMultipleTeeth(teeth, price, notes);
        }
    }
}
