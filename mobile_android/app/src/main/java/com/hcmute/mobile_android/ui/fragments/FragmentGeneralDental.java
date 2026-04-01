package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.ImagePreviewAdapter;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentGeneralDental extends Fragment {

    private TextView tvToothNotes;
    private EditText etReason, etDiagnosis;
    private Button btnEditMode;
    
    // NEW: Image upload components
    private MaterialButton btnUploadGeneralImage;
    private View layoutImagePreview;
    private RecyclerView rvGeneralImages;
    private TextView tvImageCount;
    private List<String> imageUrls = new ArrayList<>();
    private ImagePreviewAdapter imageAdapter;
    private Map<Integer, String> toothCustomNotesMap = new HashMap<>();
    
    // Edit mode state
    private boolean isReadOnly = false;
    private boolean isEditMode = false;

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
        
        tvToothNotes = view.findViewById(R.id.tvToothNotes);
        etReason = view.findViewById(R.id.etReason);
        etDiagnosis = view.findViewById(R.id.etDiagnosis);
        btnEditMode = view.findViewById(R.id.btnEditMode);
        
        // NEW: Image upload views
        btnUploadGeneralImage = view.findViewById(R.id.btnUploadGeneralImage);
        layoutImagePreview = view.findViewById(R.id.layoutImagePreview);
        rvGeneralImages = view.findViewById(R.id.rvGeneralImages);
        tvImageCount = view.findViewById(R.id.tvImageCount);

        setupImageRecyclerView();
        setupListeners();
        
        // Setup edit mode button
        if (btnEditMode != null) {
            btnEditMode.setOnClickListener(v -> toggleEditMode());
            btnEditMode.setVisibility(View.GONE); // Hidden by default
        }
    }

    private void setupListeners() {
        if (btnUploadGeneralImage != null) {
            btnUploadGeneralImage.setOnClickListener(v -> {
                if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) {
                    ((com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) getActivity()).launchImagePicker();
                }
            });
        }
    }

    private void setupImageRecyclerView() {
        imageAdapter = new ImagePreviewAdapter(imageUrls, position -> {
            String removedUrl = imageUrls.get(position);
            imageUrls.remove(position);
            updateImagePreview();
            
            // Notify activity
            if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) {
                ((com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) getActivity()).onImageDeleted(removedUrl);
            }
        });
        
        if (rvGeneralImages != null) {
            rvGeneralImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvGeneralImages.setAdapter(imageAdapter);
        }
    }

    public void onImageUploaded(String url) {
        if (url != null && !imageUrls.contains(url)) {
            imageUrls.add(url);
            updateImagePreview();
        }
    }
    
    public void setImageUrls(List<String> urls) {
        if (urls != null) {
            imageUrls.clear();
            imageUrls.addAll(urls);
            updateImagePreview();
        }
    }
    
    public List<String> getImageUrls() {
        return new ArrayList<>(imageUrls);
    }
    
    private void updateImagePreview() {
        if (layoutImagePreview == null || imageAdapter == null) return;
        
        if (imageUrls.isEmpty()) {
            layoutImagePreview.setVisibility(View.GONE);
        } else {
            layoutImagePreview.setVisibility(View.VISIBLE);
            if (tvImageCount != null) {
                tvImageCount.setText(imageUrls.size() + " ảnh");
            }
            imageAdapter.notifyDataSetChanged();
        }
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
        
        // Get existing note from map
        if (toothCustomNotesMap.containsKey(toothNumber)) {
            String note = toothCustomNotesMap.get(toothNumber);
            // Parse status from note
            if (note.contains("Sâu răng")) rgStatus.check(R.id.rbCaries);
            else if (note.contains("Đã trám")) rgStatus.check(R.id.rbFilled);
            else if (note.contains("BN yêu cầu")) rgStatus.check(R.id.rbRequested);
            else if (note.contains("Cần chữa tủy")) rgStatus.check(R.id.rbRct);
            else rgStatus.check(R.id.rbHealthy);
            
            etNote.setText(note);
        } else {
            rgStatus.check(R.id.rbHealthy);
        }
        
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            int checkedId = rgStatus.getCheckedRadioButtonId();
            String status = "healthy", statusText = "Bình thường";
            
            if (checkedId == R.id.rbCaries) { status = "caries"; statusText = "Sâu răng"; }
            else if (checkedId == R.id.rbFilled) { status = "filled"; statusText = "Đã trám"; }
            else if (checkedId == R.id.rbRequested) { status = "requested"; statusText = "BN yêu cầu"; }
            else if (checkedId == R.id.rbRct) { status = "rct"; statusText = "Cần chữa tủy"; }
            
            String customNote = etNote.getText().toString().trim();
            if (status.equals("healthy") && customNote.isEmpty()) {
                toothCustomNotesMap.remove(toothNumber);
            } else {
                toothCustomNotesMap.put(toothNumber, statusText + (customNote.isEmpty() ? "" : " - " + customNote));
            }
            
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
        StringBuilder sb = new StringBuilder();
        String reason = etReason.getText().toString().trim();
        String diagnosis = etDiagnosis.getText().toString().trim();
        
        if (!reason.isEmpty()) sb.append("Lý do: ").append(reason).append("\n");
        if (!diagnosis.isEmpty()) sb.append("Chẩn đoán: ").append(diagnosis).append("\n");
        if (!toothCustomNotesMap.isEmpty()) {
            sb.append("Tình trạng răng:\n");
            for (Map.Entry<Integer, String> entry : toothCustomNotesMap.entrySet()) {
                sb.append("- R").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        return sb.toString().trim();
    }

    public void setData(String doctorConclusion) {
        if (doctorConclusion == null || doctorConclusion.isEmpty()) {
            return;
        }
        
        toothCustomNotesMap.clear();
        
        String[] lines = doctorConclusion.split("\n");
        boolean inToothSection = false;
        
        for (String line : lines) {
            if (line.startsWith("Lý do: ")) {
                if (etReason != null) etReason.setText(line.substring(7).trim());
            } else if (line.startsWith("Chẩn đoán: ")) {
                if (etDiagnosis != null) etDiagnosis.setText(line.substring(11).trim());
            } else if (line.startsWith("Tình trạng răng:")) {
                inToothSection = true;
            } else if (inToothSection && line.trim().startsWith("- R")) {
                try {
                    String toothData = line.trim().substring(2);
                    int colonIndex = toothData.indexOf(":");
                    if (colonIndex > 0) {
                        String toothNumStr = toothData.substring(1, colonIndex).trim();
                        String toothNote = toothData.substring(colonIndex + 1).trim();
                        int toothNumber = Integer.parseInt(toothNumStr);
                        toothCustomNotesMap.put(toothNumber, toothNote);
                    }
                } catch (Exception e) {
                    android.util.Log.e("FragmentGeneralDental", "Error parsing tooth line: " + line, e);
                }
            }
        }
        updateToothNotesDisplay();
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        updateEditableState();
        
        if (btnEditMode != null) {
            btnEditMode.setText(isEditMode ? "Lưu" : "Chỉnh sửa");
        }
        
        if (!isEditMode) {
            android.widget.Toast.makeText(getContext(), "Đã lưu thay đổi", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateEditableState() {
        boolean canEdit = !isReadOnly || isEditMode;
        
        if (etReason != null) {
            etReason.setEnabled(canEdit);
            etReason.setFocusable(canEdit);
            etReason.setFocusableInTouchMode(canEdit);
            etReason.setTextColor(canEdit ? 0xFF000000 : 0xFF757575);
        }
        
        if (etDiagnosis != null) {
            etDiagnosis.setEnabled(canEdit);
            etDiagnosis.setFocusable(canEdit);
            etDiagnosis.setFocusableInTouchMode(canEdit);
            etDiagnosis.setTextColor(canEdit ? 0xFF000000 : 0xFF757575);
        }
    }
    
    public void setReadOnlyMode(boolean readOnly) {
        this.isReadOnly = readOnly;
        this.isEditMode = false;
        
        boolean canEdit = !readOnly;
        if (etReason != null) etReason.setEnabled(canEdit);
        if (etDiagnosis != null) etDiagnosis.setEnabled(canEdit);
        if (btnUploadGeneralImage != null) {
            btnUploadGeneralImage.setEnabled(canEdit);
            btnUploadGeneralImage.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        }
        
        if (btnEditMode != null) {
            btnEditMode.setVisibility(readOnly ? View.VISIBLE : View.GONE);
            btnEditMode.setText("Chỉnh sửa");
        }
    }
}
