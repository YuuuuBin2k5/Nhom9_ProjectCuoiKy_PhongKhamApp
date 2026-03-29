package com.hcmute.mobile_android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.ImagePreviewAdapter;
import com.hcmute.mobile_android.ui.activities.ImageViewerActivity;

import java.util.ArrayList;
import java.util.List;

public class FragmentXray extends Fragment {

    private RadioGroup rgImageType;
    private RadioButton rbPanoramic, rbPeriapical, rbCephalometric, rbCTScan, rbOther;
    private EditText etXrayFindings, etXrayDiagnosis, etXrayRecommendations, etOtherType;
    private LinearLayout layoutOtherType, layoutImagePreview;
    private RecyclerView rvXrayImages;
    private TextView tvImageCount;
    private MaterialButton btnUploadXrayImage;
    
    // Image management
    private List<String> xrayImageUrls = new ArrayList<>();
    private ImagePreviewAdapter imageAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_xray, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        rgImageType = view.findViewById(R.id.rgImageType);
        rbPanoramic = view.findViewById(R.id.rbPanoramic);
        rbPeriapical = view.findViewById(R.id.rbPeriapical);
        rbCephalometric = view.findViewById(R.id.rbCephalometric);
        rbCTScan = view.findViewById(R.id.rbCTScan);
        rbOther = view.findViewById(R.id.rbOther);
        
        etXrayFindings = view.findViewById(R.id.etXrayFindings);
        etXrayDiagnosis = view.findViewById(R.id.etXrayDiagnosis);
        etXrayRecommendations = view.findViewById(R.id.etXrayRecommendations);
        etOtherType = view.findViewById(R.id.etOtherType);
        layoutOtherType = view.findViewById(R.id.layoutOtherType);
        
        // Image upload views
        btnUploadXrayImage = view.findViewById(R.id.btnUploadXrayImage);
        layoutImagePreview = view.findViewById(R.id.layoutImagePreview);
        rvXrayImages = view.findViewById(R.id.rvXrayImages);
        tvImageCount = view.findViewById(R.id.tvImageCount);
        
        // Restore state if available
        if (savedInstanceState != null) {
            ArrayList<String> savedImages = savedInstanceState.getStringArrayList("xrayImageUrls");
            if (savedImages != null) {
                xrayImageUrls.clear();
                xrayImageUrls.addAll(savedImages);
            }
        }
        
        // Setup image RecyclerView
        setupImageRecyclerView();
        
        // Update preview after restoring state
        updateImagePreview();
        
        // Show/hide "Other type" field based on radio selection
        rgImageType.setOnCheckedChangeListener((group, checkedId) -> {
            if (layoutOtherType != null) {
                layoutOtherType.setVisibility(checkedId == R.id.rbOther ? View.VISIBLE : View.GONE);
            }
        });
        
        // Setup upload button
        btnUploadXrayImage.setOnClickListener(v -> {
            if (getActivity() instanceof com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) {
                ((com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity) getActivity()).launchImagePicker();
            } else {
                Toast.makeText(getContext(), "Tính năng tải ảnh chưa được hỗ trợ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save image URLs to survive configuration changes
        outState.putStringArrayList("xrayImageUrls", new ArrayList<>(xrayImageUrls));
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup to prevent memory leaks
        if (imageAdapter != null) {
            imageAdapter = null;
        }
        if (rvXrayImages != null) {
            rvXrayImages.setAdapter(null);
        }
    }
    
    private void setupImageRecyclerView() {
        imageAdapter = new ImagePreviewAdapter(xrayImageUrls, position -> {
            // On delete click
            showDeleteImageDialog(position);
        });
        
        rvXrayImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvXrayImages.setAdapter(imageAdapter);
    }
    
    private void showDeleteImageDialog(int position) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa ảnh")
            .setMessage("Bạn có chắc muốn xóa ảnh này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                xrayImageUrls.remove(position);
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
            xrayImageUrls.add(imageUrl);
            updateImagePreview();
            Toast.makeText(getContext(), "Đã thêm ảnh X-quang", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Get all uploaded image URLs
     */
    public List<String> getImageUrls() {
        return new ArrayList<>(xrayImageUrls);
    }
    
    /**
     * Set image URLs (when loading existing data)
     * SAFE: Checks if adapter is ready before updating
     */
    public void setImageUrls(List<String> urls) {
        android.util.Log.d("FragmentXray", "setImageUrls called with " + (urls != null ? urls.size() : 0) + " images");
        
        if (urls != null) {
            xrayImageUrls.clear();
            xrayImageUrls.addAll(urls);
            
            android.util.Log.d("FragmentXray", "Images added to list. Current size: " + xrayImageUrls.size());
            
            // Always schedule update to ensure view and adapter are ready
            if (getView() != null) {
                getView().post(() -> {
                    // Ensure adapter is initialized
                    if (imageAdapter == null) {
                        android.util.Log.d("FragmentXray", "Adapter was null, setting up RecyclerView");
                        setupImageRecyclerView();
                    }
                    updateImagePreview();
                    android.util.Log.d("FragmentXray", "Image preview updated");
                });
            } else {
                android.util.Log.w("FragmentXray", "View is null, cannot update preview");
            }
        }
    }
    
    private void updateImagePreview() {
        // Safety check: ensure adapter is initialized
        if (imageAdapter == null || rvXrayImages == null || layoutImagePreview == null) {
            return;
        }
        
        if (xrayImageUrls.isEmpty()) {
            layoutImagePreview.setVisibility(View.GONE);
        } else {
            layoutImagePreview.setVisibility(View.VISIBLE);
            if (tvImageCount != null) {
                tvImageCount.setText(xrayImageUrls.size() + " ảnh");
            }
            imageAdapter.notifyDataSetChanged();
        }
    }

    public String getFormDataNotes() {
        // Null safety checks
        if (etXrayFindings == null || etXrayDiagnosis == null || etXrayRecommendations == null || rgImageType == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Image type
        String imageType = getSelectedImageType();
        sb.append("[X-quang] Loại: ").append(imageType).append("\n");
        
        // Findings
        String findings = etXrayFindings.getText().toString().trim();
        if (!findings.isEmpty()) {
            sb.append("Kết quả đọc phim: ").append(findings).append("\n");
        }
        
        // Diagnosis
        String diagnosis = etXrayDiagnosis.getText().toString().trim();
        if (!diagnosis.isEmpty()) {
            sb.append("Chẩn đoán: ").append(diagnosis).append("\n");
        }
        
        // Recommendations
        String recommendations = etXrayRecommendations.getText().toString().trim();
        if (!recommendations.isEmpty()) {
            sb.append("Khuyến nghị: ").append(recommendations).append("\n");
        }
        
        // Image count
        if (!xrayImageUrls.isEmpty()) {
            sb.append("Số ảnh X-quang: ").append(xrayImageUrls.size());
        }
        
        return sb.toString().trim();
    }

    private String getSelectedImageType() {
        if (rgImageType == null) {
            return "Panoramic (Toàn cảnh)"; // Default
        }
        
        int checkedId = rgImageType.getCheckedRadioButtonId();
        if (checkedId == R.id.rbPanoramic) {
            return "Panoramic (Toàn cảnh)";
        } else if (checkedId == R.id.rbPeriapical) {
            return "Periapical (Chóp răng)";
        } else if (checkedId == R.id.rbCephalometric) {
            return "Cephalometric (Đo sọ)";
        } else if (checkedId == R.id.rbCTScan) {
            return "CT Scan / CBCT";
        } else if (checkedId == R.id.rbOther) {
            // Get custom type from EditText
            if (etOtherType != null && !etOtherType.getText().toString().trim().isEmpty()) {
                return "Khác: " + etOtherType.getText().toString().trim();
            }
            return "Khác";
        }
        return "Panoramic (Toàn cảnh)"; // Default if nothing selected
    }
    
    public boolean validateForm() {
        // At least one field must have ACTUAL data (not just whitespace)
        boolean hasFindings = etXrayFindings != null && !etXrayFindings.getText().toString().trim().isEmpty();
        boolean hasDiagnosis = etXrayDiagnosis != null && !etXrayDiagnosis.getText().toString().trim().isEmpty();
        boolean hasRecommendations = etXrayRecommendations != null && !etXrayRecommendations.getText().toString().trim().isEmpty();
        boolean hasImages = !xrayImageUrls.isEmpty();
        
        if (!hasFindings && !hasDiagnosis && !hasRecommendations && !hasImages) {
            Toast.makeText(getContext(), "Vui lòng nhập ít nhất một trong các trường hoặc tải lên ảnh X-quang", Toast.LENGTH_LONG).show();
            return false;
        }
        
        // If "Other" is selected, must specify type
        if (rgImageType != null && rgImageType.getCheckedRadioButtonId() == R.id.rbOther) {
            if (etOtherType == null || etOtherType.getText().toString().trim().isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập loại X-quang khác", Toast.LENGTH_SHORT).show();
                if (etOtherType != null) {
                    etOtherType.requestFocus();
                }
                return false;
            }
        }
        
        // CRITICAL: If user has images, must have at least diagnosis or findings
        if (hasImages && !hasFindings && !hasDiagnosis) {
            Toast.makeText(getContext(), "Bạn đã tải ảnh X-quang nhưng chưa nhập kết quả đọc phim hoặc chẩn đoán", Toast.LENGTH_LONG).show();
            if (etXrayFindings != null) {
                etXrayFindings.requestFocus();
            }
            return false;
        }
        
        return true;
    }

    public void setData(String doctorConclusion) {
        android.util.Log.d("FragmentXray", "setData called with: " + (doctorConclusion != null ? doctorConclusion.substring(0, Math.min(100, doctorConclusion.length())) : "null"));
        
        if (doctorConclusion == null || doctorConclusion.trim().isEmpty()) {
            android.util.Log.w("FragmentXray", "doctorConclusion is null or empty");
            return;
        }
        
        // Ensure view is ready before setting data
        if (getView() == null) {
            android.util.Log.w("FragmentXray", "View is null, scheduling setData for later");
            // Schedule for when view is ready
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getView() != null) {
                        setData(doctorConclusion);
                    }
                });
            }
            return;
        }
        
        // Parse the doctorConclusion string to populate fields
        // Use state machine to handle multi-line content
        String[] lines = doctorConclusion.split("\n");
        String currentSection = "";
        StringBuilder currentContent = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            
            android.util.Log.d("FragmentXray", "Parsing line: " + line);
            
            if (line.startsWith("[X-quang] Loại:")) {
                // Save previous section
                saveSection(currentSection, currentContent.toString().trim());
                currentContent = new StringBuilder();
                
                // Parse image type
                String type = line.substring("[X-quang] Loại:".length()).trim();
                setImageType(type);
                android.util.Log.d("FragmentXray", "✓ Set image type: " + type);
                currentSection = "";
            } else if (line.startsWith("Kết quả đọc phim:")) {
                // Save previous section
                saveSection(currentSection, currentContent.toString().trim());
                currentContent = new StringBuilder();
                
                // Start new section
                currentSection = "findings";
                String findings = line.substring("Kết quả đọc phim:".length()).trim();
                if (!findings.isEmpty()) {
                    currentContent.append(findings);
                }
                android.util.Log.d("FragmentXray", "Started findings section");
            } else if (line.startsWith("Chẩn đoán:")) {
                // Save previous section
                saveSection(currentSection, currentContent.toString().trim());
                currentContent = new StringBuilder();
                
                // Start new section
                currentSection = "diagnosis";
                String diagnosis = line.substring("Chẩn đoán:".length()).trim();
                if (!diagnosis.isEmpty()) {
                    currentContent.append(diagnosis);
                }
                android.util.Log.d("FragmentXray", "Started diagnosis section");
            } else if (line.startsWith("Khuyến nghị:")) {
                // Save previous section
                saveSection(currentSection, currentContent.toString().trim());
                currentContent = new StringBuilder();
                
                // Start new section
                currentSection = "recommendations";
                String recommendations = line.substring("Khuyến nghị:".length()).trim();
                if (!recommendations.isEmpty()) {
                    currentContent.append(recommendations);
                }
                android.util.Log.d("FragmentXray", "Started recommendations section");
            } else if (line.startsWith("Số ảnh X-quang:")) {
                // Save previous section
                saveSection(currentSection, currentContent.toString().trim());
                currentContent = new StringBuilder();
                currentSection = "";
                // Image count is just informational, actual images loaded separately
                android.util.Log.d("FragmentXray", "Found image count line");
            } else if (!line.isEmpty() && !currentSection.isEmpty()) {
                // Continue current section (multi-line content)
                if (currentContent.length() > 0) {
                    currentContent.append("\n");
                }
                currentContent.append(line);
            }
        }
        
        // Save last section
        saveSection(currentSection, currentContent.toString().trim());
        android.util.Log.d("FragmentXray", "✓ setData completed");
    }
    
    private void saveSection(String section, String content) {
        if (content.isEmpty()) {
            return;
        }
        
        android.util.Log.d("FragmentXray", "saveSection: " + section + " = " + content.substring(0, Math.min(50, content.length())));
        
        switch (section) {
            case "findings":
                if (etXrayFindings != null) {
                    etXrayFindings.setText(content);
                    android.util.Log.d("FragmentXray", "✓ Set findings");
                }
                break;
            case "diagnosis":
                if (etXrayDiagnosis != null) {
                    etXrayDiagnosis.setText(content);
                    android.util.Log.d("FragmentXray", "✓ Set diagnosis");
                }
                break;
            case "recommendations":
                if (etXrayRecommendations != null) {
                    etXrayRecommendations.setText(content);
                    android.util.Log.d("FragmentXray", "✓ Set recommendations");
                }
                break;
        }
    }

    public void setReadOnlyMode(boolean readOnly) {
        android.util.Log.d("FragmentXray", "setReadOnlyMode: " + readOnly);
        
        // Disable all input fields
        if (etXrayFindings != null) {
            etXrayFindings.setEnabled(!readOnly);
            etXrayFindings.setFocusable(!readOnly);
        }
        
        if (etXrayDiagnosis != null) {
            etXrayDiagnosis.setEnabled(!readOnly);
            etXrayDiagnosis.setFocusable(!readOnly);
        }
        
        if (etXrayRecommendations != null) {
            etXrayRecommendations.setEnabled(!readOnly);
            etXrayRecommendations.setFocusable(!readOnly);
        }
        
        if (etOtherType != null) {
            etOtherType.setEnabled(!readOnly);
            etOtherType.setFocusable(!readOnly);
        }
        
        // Disable image type selection
        if (rgImageType != null) {
            rgImageType.setEnabled(!readOnly);
            for (int i = 0; i < rgImageType.getChildCount(); i++) {
                rgImageType.getChildAt(i).setEnabled(!readOnly);
            }
        }
        
        // Disable upload button
        if (btnUploadXrayImage != null) {
            btnUploadXrayImage.setEnabled(!readOnly);
            btnUploadXrayImage.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        }
    }

    private void setImageType(String type) {
        if (type == null || type.isEmpty() || rgImageType == null) {
            return;
        }
        
        if (type.contains("Panoramic") || type.contains("Toàn cảnh")) {
            rgImageType.check(R.id.rbPanoramic);
        } else if (type.contains("Periapical") || type.contains("Chóp răng")) {
            rgImageType.check(R.id.rbPeriapical);
        } else if (type.contains("Cephalometric") || type.contains("Đo sọ")) {
            rgImageType.check(R.id.rbCephalometric);
        } else if (type.contains("CT Scan") || type.contains("CBCT")) {
            rgImageType.check(R.id.rbCTScan);
        } else if (type.startsWith("Khác")) {
            rgImageType.check(R.id.rbOther);
            // Extract custom type after "Khác: "
            if (type.contains(": ") && etOtherType != null) {
                String customType = type.substring(type.indexOf(": ") + 2).trim();
                etOtherType.setText(customType);
            }
            // Show the other type field
            if (layoutOtherType != null) {
                layoutOtherType.setVisibility(View.VISIBLE);
            }
        }
    }
}
