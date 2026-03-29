package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.hcmute.mobile_android.R;

import android.widget.EditText;
import android.widget.Button;

public class FragmentOrthodontics extends Fragment {

    private EditText etOrthoNotes;
    private Button btnEditMode;
    private boolean isReadOnly = false;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orthodontics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etOrthoNotes = view.findViewById(R.id.etOrthoNotes);
        btnEditMode = view.findViewById(R.id.btnEditMode);

        // Edit mode toggle button
        if (btnEditMode != null) {
            btnEditMode.setOnClickListener(v -> toggleEditMode());
            btnEditMode.setVisibility(View.GONE); // Hidden by default
        }

        // Upload buttons - integrate with parent activity
        view.findViewById(R.id.btnUploadBefore).setOnClickListener(v -> {
            if (isReadOnly && !isEditMode) {
                android.widget.Toast.makeText(getContext(), "Nhấn 'Chỉnh sửa' để thay đổi dữ liệu", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            android.widget.Toast.makeText(getContext(), "Sử dụng nút 'Tải ảnh' ở trên", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        view.findViewById(R.id.btnUploadAfter).setOnClickListener(v -> {
            if (isReadOnly && !isEditMode) {
                android.widget.Toast.makeText(getContext(), "Nhấn 'Chỉnh sửa' để thay đổi dữ liệu", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            android.widget.Toast.makeText(getContext(), "Sử dụng nút 'Tải ảnh' ở trên", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        // Restore state if available
        if (savedInstanceState != null) {
            isReadOnly = savedInstanceState.getBoolean("isReadOnly", false);
            isEditMode = savedInstanceState.getBoolean("isEditMode", false);
        }
        
        updateEditableState();
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isReadOnly", isReadOnly);
        outState.putBoolean("isEditMode", isEditMode);
        if (etOrthoNotes != null) {
            outState.putString("orthoNotes", etOrthoNotes.getText().toString());
        }
    }
    
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && etOrthoNotes != null) {
            String notes = savedInstanceState.getString("orthoNotes");
            if (notes != null) {
                etOrthoNotes.setText(notes);
            }
        }
    }
    
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        updateEditableState();
        
        if (btnEditMode != null) {
            btnEditMode.setText(isEditMode ? "Lưu" : "Chỉnh sửa");
        }
        
        if (!isEditMode) {
            // Save mode - notify parent activity
            if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) {
                android.widget.Toast.makeText(getContext(), "Đã lưu thay đổi", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updateEditableState() {
        boolean canEdit = !isReadOnly || isEditMode;
        if (etOrthoNotes != null) {
            etOrthoNotes.setEnabled(canEdit);
            etOrthoNotes.setBackgroundResource(canEdit ? 
                R.drawable.bg_card_white_rounded : 
                android.R.color.transparent);
            etOrthoNotes.setTextColor(canEdit ? 
                0xFF000000 : // Black
                0xFF757575); // Gray
        }
    }
    
    public void setReadOnlyMode(boolean readOnly) {
        this.isReadOnly = readOnly;
        this.isEditMode = false; // Reset edit mode
        updateEditableState();
        
        // Show/hide edit button
        if (btnEditMode != null) {
            btnEditMode.setVisibility(readOnly ? View.VISIBLE : View.GONE);
            btnEditMode.setText("Chỉnh sửa");
        }
    }

    public String getFormDataNotes() {
        // Null safety check
        if (etOrthoNotes == null) {
            return "[Niềng răng] ";
        }
        String notes = etOrthoNotes.getText().toString().trim();
        if (notes.isEmpty()) {
            return "[Niềng răng] (Chưa có ghi chú)";
        }
        return "[Niềng răng] " + notes;
    }
    
    public boolean validateForm() {
        // Notes must not be empty
        boolean hasNotes = etOrthoNotes != null && !etOrthoNotes.getText().toString().trim().isEmpty();
        
        if (!hasNotes) {
            android.widget.Toast.makeText(getContext(), 
                "Vui lòng nhập ghi chú về tình trạng niềng răng\n(VD: Thay dây cung số 3, lực kéo 150g, nướu hồng khỏe)", 
                android.widget.Toast.LENGTH_LONG).show();
            if (etOrthoNotes != null) {
                etOrthoNotes.requestFocus();
            }
            return false;
        }
        
        // Check minimum length
        String notes = etOrthoNotes.getText().toString().trim();
        if (notes.length() < 10) {
            android.widget.Toast.makeText(getContext(), 
                "Ghi chú quá ngắn. Vui lòng mô tả chi tiết hơn về tình trạng niềng răng", 
                android.widget.Toast.LENGTH_LONG).show();
            if (etOrthoNotes != null) {
                etOrthoNotes.requestFocus();
            }
            return false;
        }
        
        return true;
    }

    public void setData(String doctorConclusion) {
        if (doctorConclusion == null || doctorConclusion.trim().isEmpty()) {
            return;
        }
        
        // Parse: "[Niềng răng] notes here"
        String notes = doctorConclusion;
        if (doctorConclusion.startsWith("[Niềng răng]")) {
            notes = doctorConclusion.substring("[Niềng răng]".length()).trim();
        }
        
        // Remove "(Chưa có ghi chú)" if present
        if (notes.equals("(Chưa có ghi chú)")) {
            notes = "";
        }
        
        if (etOrthoNotes != null && !notes.isEmpty()) {
            etOrthoNotes.setText(notes);
        }
    }
}
