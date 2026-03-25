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

public class FragmentOrthodontics extends Fragment {

    private EditText etOrthoNotes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orthodontics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etOrthoNotes = view.findViewById(R.id.etOrthoNotes);

        view.findViewById(R.id.btnUploadBefore).setOnClickListener(v -> 
            android.widget.Toast.makeText(getContext(), "Máy ảnh/Chọn ảnh trước điều trị", android.widget.Toast.LENGTH_SHORT).show());
        
        view.findViewById(R.id.btnUploadAfter).setOnClickListener(v -> 
            android.widget.Toast.makeText(getContext(), "Máy ảnh/Chọn ảnh cận hàm", android.widget.Toast.LENGTH_SHORT).show());
    }

    public String getFormDataNotes() {
        return "[Niềng răng] " + etOrthoNotes.getText().toString().trim();
    }
}
