package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.ImagePreviewAdapter;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FragmentSurgeryChecklist extends Fragment {

    private EditText etBloodPressure, etHeartRate, etSurgeryNotes;
    private CheckBox cbCoagulation, cbAllergy;
    private Button btnEditMode;
    private boolean isReadOnly = false;
    private boolean isEditMode = false;
    
    // NEW: Image upload components
    private MaterialButton btnUploadSurgeryImage;
    private LinearLayout layoutImagePreview;
    private RecyclerView rvSurgeryImages;
    private TextView tvImageCount;
    private List<String> surgeryImageUrls = new ArrayList<>();
    private ImagePreviewAdapter imageAdapter;

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
        btnEditMode = view.findViewById(R.id.btnEditMode);
        
        // NEW: Image upload views
        btnUploadSurgeryImage = view.findViewById(R.id.btnUploadSurgeryImage);
        layoutImagePreview = view.findViewById(R.id.layoutImagePreview);
        rvSurgeryImages = view.findViewById(R.id.rvSurgeryImages);
        tvImageCount = view.findViewById(R.id.tvImageCount);
        
        // Edit mode toggle button
        if (btnEditMode != null) {
            btnEditMode.setOnClickListener(v -> toggleEditMode());
            btnEditMode.setVisibility(View.GONE); // Hidden by default
        }
        
        // Restore state if available
        if (savedInstanceState != null) {
            isReadOnly = savedInstanceState.getBoolean("isReadOnly", false);
            isEditMode = savedInstanceState.getBoolean("isEditMode", false);
            ArrayList<String> savedImages = savedInstanceState.getStringArrayList("surgeryImageUrls");
            if (savedImages != null) {
                surgeryImageUrls.clear();
                surgeryImageUrls.addAll(savedImages);
            }
        }
        
        // Setup image RecyclerView
        setupImageRecyclerView();
        
        // Update preview after restoring state
        updateImagePreview();
        
        // Setup upload button
        if (btnUploadSurgeryImage != null) {
            btnUploadSurgeryImage.setOnClickListener(v -> {
                if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) {
                    ((com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) getActivity()).launchImagePicker();
                } else {
                    Toast.makeText(getContext(), "Tính năng tải ảnh chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        updateEditableState();
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isReadOnly", isReadOnly);
        outState.putBoolean("isEditMode", isEditMode);
        outState.putStringArrayList("surgeryImageUrls", new ArrayList<>(surgeryImageUrls));
        if (etBloodPressure != null) {
            outState.putString("bloodPressure", etBloodPressure.getText().toString());
        }
        if (etHeartRate != null) {
            outState.putString("heartRate", etHeartRate.getText().toString());
        }
        if (etSurgeryNotes != null) {
            outState.putString("surgeryNotes", etSurgeryNotes.getText().toString());
        }
        if (cbCoagulation != null) {
            outState.putBoolean("coagulation", cbCoagulation.isChecked());
        }
        if (cbAllergy != null) {
            outState.putBoolean("allergy", cbAllergy.isChecked());
        }
    }
    
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            if (etBloodPressure != null) {
                String bp = savedInstanceState.getString("bloodPressure");
                if (bp != null) etBloodPressure.setText(bp);
            }
            if (etHeartRate != null) {
                String hr = savedInstanceState.getString("heartRate");
                if (hr != null) etHeartRate.setText(hr);
            }
            if (etSurgeryNotes != null) {
                String notes = savedInstanceState.getString("surgeryNotes");
                if (notes != null) etSurgeryNotes.setText(notes);
            }
            if (cbCoagulation != null) {
                cbCoagulation.setChecked(savedInstanceState.getBoolean("coagulation", false));
            }
            if (cbAllergy != null) {
                cbAllergy.setChecked(savedInstanceState.getBoolean("allergy", false));
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup to prevent memory leaks
        if (imageAdapter != null) {
            imageAdapter = null;
        }
        if (rvSurgeryImages != null) {
            rvSurgeryImages.setAdapter(null);
        }
    }
    
    private void setupImageRecyclerView() {
        imageAdapter = new ImagePreviewAdapter(surgeryImageUrls, position -> {
            // On delete click
            showDeleteImageDialog(position);
        });
        
        if (rvSurgeryImages != null) {
            rvSurgeryImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvSurgeryImages.setAdapter(imageAdapter);
        }
    }
    
    private void showDeleteImageDialog(int position) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa ảnh")
            .setMessage("Bạn có chắc muốn xóa ảnh này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                surgeryImageUrls.remove(position);
                updateImagePreview();
                Toast.makeText(getContext(), "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Called by DoctorWorkflowActivity when an image is uploaded successfully
     */
    public void onImageUploaded(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            surgeryImageUrls.add(imageUrl);
            updateImagePreview();
            Toast.makeText(getContext(), "Đã thêm ảnh phẫu thuật", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Get all uploaded image URLs
     */
    public List<String> getImageUrls() {
        return new ArrayList<>(surgeryImageUrls);
    }
    
    /**
     * Set image URLs (when loading existing data)
     */
    public void setImageUrls(List<String> urls) {
        android.util.Log.d("FragmentSurgeryChecklist", "setImageUrls called with " + (urls != null ? urls.size() : 0) + " images");
        
        if (urls != null) {
            surgeryImageUrls.clear();
            surgeryImageUrls.addAll(urls);
            
            android.util.Log.d("FragmentSurgeryChecklist", "Images added to list. Current size: " + surgeryImageUrls.size());
            
            // Always schedule update to ensure view and adapter are ready
            if (getView() != null) {
                getView().post(() -> {
                    // Ensure adapter is initialized
                    if (imageAdapter == null) {
                        android.util.Log.d("FragmentSurgeryChecklist", "Adapter was null, setting up RecyclerView");
                        setupImageRecyclerView();
                    }
                    updateImagePreview();
                    android.util.Log.d("FragmentSurgeryChecklist", "Image preview updated");
                });
            } else {
                android.util.Log.w("FragmentSurgeryChecklist", "View is null, cannot update preview");
            }
        }
    }
    
    private void updateImagePreview() {
        // Safety check: ensure adapter is initialized
        if (imageAdapter == null || rvSurgeryImages == null || layoutImagePreview == null) {
            return;
        }
        
        if (surgeryImageUrls.isEmpty()) {
            layoutImagePreview.setVisibility(View.GONE);
        } else {
            layoutImagePreview.setVisibility(View.VISIBLE);
            if (tvImageCount != null) {
                tvImageCount.setText(surgeryImageUrls.size() + " ảnh");
            }
            imageAdapter.notifyDataSetChanged();
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
            android.widget.Toast.makeText(getContext(), "Đã lưu thay đổi", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateEditableState() {
        boolean canEdit = !isReadOnly || isEditMode;
        
        // EditText fields
        if (etBloodPressure != null) {
            etBloodPressure.setEnabled(canEdit);
            etBloodPressure.setBackgroundResource(canEdit ? 
                R.drawable.bg_card_white_rounded : 
                android.R.color.transparent);
            etBloodPressure.setTextColor(canEdit ? 0xFF000000 : 0xFF757575);
        }
        if (etHeartRate != null) {
            etHeartRate.setEnabled(canEdit);
            etHeartRate.setBackgroundResource(canEdit ? 
                R.drawable.bg_card_white_rounded : 
                android.R.color.transparent);
            etHeartRate.setTextColor(canEdit ? 0xFF000000 : 0xFF757575);
        }
        if (etSurgeryNotes != null) {
            etSurgeryNotes.setEnabled(canEdit);
            etSurgeryNotes.setBackgroundResource(canEdit ? 
                R.drawable.bg_card_white_rounded : 
                android.R.color.transparent);
            etSurgeryNotes.setTextColor(canEdit ? 0xFF000000 : 0xFF757575);
        }
        
        // CheckBox fields - disable but keep visual
        if (cbCoagulation != null) {
            cbCoagulation.setEnabled(canEdit);
            cbCoagulation.setAlpha(canEdit ? 1.0f : 0.6f);
        }
        if (cbAllergy != null) {
            cbAllergy.setEnabled(canEdit);
            cbAllergy.setAlpha(canEdit ? 1.0f : 0.6f);
        }
        
        // NEW: Upload button
        if (btnUploadSurgeryImage != null) {
            btnUploadSurgeryImage.setEnabled(canEdit);
            btnUploadSurgeryImage.setVisibility(canEdit ? View.VISIBLE : View.GONE);
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
        // Null safety checks
        if (etBloodPressure == null || etHeartRate == null || etSurgeryNotes == null || 
            cbCoagulation == null || cbAllergy == null) {
            return "[Tiểu phẫu] (Chưa có dữ liệu)";
        }
        
        String bp = etBloodPressure.getText().toString().trim();
        String hr = etHeartRate.getText().toString().trim();
        String notes = etSurgeryNotes.getText().toString().trim();
        
        // Check if all fields are empty
        if (bp.isEmpty() && hr.isEmpty() && !cbCoagulation.isChecked() && 
            !cbAllergy.isChecked() && notes.isEmpty()) {
            return "[Tiểu phẫu] (Chưa có dữ liệu)";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[Sinh hiệu] ");
        
        if (!bp.isEmpty()) {
            sb.append("BP: ").append(bp);
        } else {
            sb.append("BP: (chưa đo)");
        }
        
        sb.append(", ");
        
        if (!hr.isEmpty()) {
            sb.append("HR: ").append(hr);
        } else {
            sb.append("HR: (chưa đo)");
        }
        
        sb.append("\n");
        
        if (cbCoagulation.isChecked()) sb.append("- Máu khó đông\n");
        if (cbAllergy.isChecked()) sb.append("- Dị ứng thuốc tê\n");
        
        if (!notes.isEmpty()) {
            sb.append("Ghi chú: ").append(notes);
        }
        
        // NEW: Image count
        if (!surgeryImageUrls.isEmpty()) {
            sb.append("\nSố ảnh phẫu thuật: ").append(surgeryImageUrls.size());
        }
        
        return sb.toString().trim();
    }
    
    public boolean validateForm() {
        // At least BP or HR must have value
        boolean hasBP = etBloodPressure != null && !etBloodPressure.getText().toString().trim().isEmpty();
        boolean hasHR = etHeartRate != null && !etHeartRate.getText().toString().trim().isEmpty();
        
        if (!hasBP && !hasHR) {
            android.widget.Toast.makeText(getContext(), 
                "Vui lòng nhập ít nhất Huyết áp hoặc Nhịp tim\n(Đây là thông tin bắt buộc trước phẫu thuật)", 
                android.widget.Toast.LENGTH_LONG).show();
            return false;
        }
        
        // Validate BP format if provided
        if (hasBP) {
            String bp = etBloodPressure.getText().toString().trim();
            if (!bp.matches("\\d+/\\d+")) {
                android.widget.Toast.makeText(getContext(), 
                    "Huyết áp phải có định dạng: xxx/yyy (VD: 120/80)", 
                    android.widget.Toast.LENGTH_LONG).show();
                etBloodPressure.requestFocus();
                return false;
            }
            
            // Validate BP range
            try {
                String[] parts = bp.split("/");
                int systolic = Integer.parseInt(parts[0]);
                int diastolic = Integer.parseInt(parts[1]);
                
                if (systolic < 70 || systolic > 250) {
                    android.widget.Toast.makeText(getContext(), 
                        "Huyết áp tâm thu (số trên) phải trong khoảng 70-250 mmHg", 
                        android.widget.Toast.LENGTH_LONG).show();
                    etBloodPressure.requestFocus();
                    return false;
                }
                
                if (diastolic < 40 || diastolic > 150) {
                    android.widget.Toast.makeText(getContext(), 
                        "Huyết áp tâm trương (số dưới) phải trong khoảng 40-150 mmHg", 
                        android.widget.Toast.LENGTH_LONG).show();
                    etBloodPressure.requestFocus();
                    return false;
                }
                
                if (systolic <= diastolic) {
                    android.widget.Toast.makeText(getContext(), 
                        "Huyết áp tâm thu phải lớn hơn huyết áp tâm trương", 
                        android.widget.Toast.LENGTH_LONG).show();
                    etBloodPressure.requestFocus();
                    return false;
                }
            } catch (Exception e) {
                // Already validated format above
            }
        }
        
        // Validate HR if provided
        if (hasHR) {
            String hr = etHeartRate.getText().toString().trim();
            try {
                int heartRate = Integer.parseInt(hr);
                if (heartRate < 40 || heartRate > 200) {
                    android.widget.Toast.makeText(getContext(), 
                        "Nhịp tim phải trong khoảng 40-200 lần/phút", 
                        android.widget.Toast.LENGTH_LONG).show();
                    etHeartRate.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                android.widget.Toast.makeText(getContext(), 
                    "Nhịp tim phải là số nguyên", 
                    android.widget.Toast.LENGTH_SHORT).show();
                etHeartRate.requestFocus();
                return false;
            }
        }
        
        // Warning for risk combination (non-blocking)
        if (cbCoagulation != null && cbAllergy != null && 
            cbCoagulation.isChecked() && cbAllergy.isChecked()) {
            // Show warning but allow to proceed
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("⚠️ Cảnh báo")
                .setMessage("Bệnh nhân có cả máu khó đông VÀ dị ứng thuốc tê.\n\nĐây là tình trạng nguy hiểm, cần đặc biệt thận trọng trong quá trình phẫu thuật!")
                .setPositiveButton("Đã hiểu", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        }
        
        return true;
    }

    public void setData(String doctorConclusion) {
        if (doctorConclusion == null || doctorConclusion.trim().isEmpty()) {
            return;
        }
        
        // Handle empty data marker
        if (doctorConclusion.contains("(Chưa có dữ liệu)")) {
            return;
        }
        
        // Parse the doctorConclusion string to populate fields
        String[] lines = doctorConclusion.split("\n");
        String currentSection = "";
        StringBuilder currentContent = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.startsWith("[Sinh hiệu]") || line.startsWith("[Tiểu phẫu]")) {
                // Parse: "[Sinh hiệu] BP: 120/80, HR: 75"
                try {
                    int startIndex = line.indexOf("]") + 1;
                    if (startIndex > 0 && startIndex < line.length()) {
                        String data = line.substring(startIndex).trim();
                        String[] parts = data.split(",");
                        for (String part : parts) {
                            part = part.trim();
                            if (part.startsWith("BP:")) {
                                String bp = part.substring("BP:".length()).trim();
                                if (!bp.equals("(chưa đo)") && etBloodPressure != null) {
                                    etBloodPressure.setText(bp);
                                }
                            } else if (part.startsWith("HR:")) {
                                String hr = part.substring("HR:".length()).trim();
                                if (!hr.equals("(chưa đo)") && etHeartRate != null) {
                                    etHeartRate.setText(hr);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
                currentSection = "";
            } else if (line.toLowerCase().contains("máu khó đông")) {
                if (cbCoagulation != null) {
                    cbCoagulation.setChecked(true);
                }
            } else if (line.toLowerCase().contains("dị ứng thuốc tê") || 
                       line.toLowerCase().contains("dị ứng thuốc mê")) {
                if (cbAllergy != null) {
                    cbAllergy.setChecked(true);
                }
            } else if (line.startsWith("Ghi chú:") || line.startsWith("- Ghi chú:")) {
                // Save previous section
                saveSection(currentSection, currentContent.toString().trim());
                currentContent = new StringBuilder();
                
                // Start new section
                currentSection = "notes";
                String note = line.replaceFirst("^-?\\s*Ghi chú:\\s*", "").trim();
                if (!note.isEmpty()) {
                    currentContent.append(note);
                }
            } else if (!line.isEmpty() && currentSection.equals("notes")) {
                // Continue notes section (multi-line)
                if (currentContent.length() > 0) {
                    currentContent.append("\n");
                }
                currentContent.append(line);
            }
        }
        
        // Save last section
        saveSection(currentSection, currentContent.toString().trim());
    }
    
    private void saveSection(String section, String content) {
        if (content.isEmpty()) {
            return;
        }
        
        if (section.equals("notes")) {
            if (etSurgeryNotes != null) {
                etSurgeryNotes.setText(content);
            }
        }
    }
}
