package com.example.phongkham_app.ui.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phongkham_app.data.model.Medicine;
import com.example.phongkham_app.data.model.Patient;
import com.example.phongkham_app.data.model.VisitHistory;
import com.example.phongkham_app.data.repository.PatientRepository;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import android.app.Application;
import androidx.annotation.NonNull;

public class MedicalDetailViewModel extends AndroidViewModel {

    private final PatientRepository repository;
    private final MutableLiveData<Patient> patient = new MutableLiveData<>();
    private final MutableLiveData<List<Medicine>> medicines = new MutableLiveData<>();
    private final MutableLiveData<List<VisitHistory>> visitHistory = new MutableLiveData<>();

    public MedicalDetailViewModel(@NonNull Application application) {
        super(application);
        repository = PatientRepository.getInstance(application);
        loadData();
    }

    private void loadData() {
        patient.setValue(repository.getPatientProfile());

        List<Medicine> mockMeds = new ArrayList<>();
        mockMeds.add(new Medicine(1, "Paracetamol 500mg", "1 viên / ngày (Sáng, Tối)", ""));
        mockMeds.add(new Medicine(2, "Amoxicillin 250mg", "1 viên / ngày (Sáng, Tối)", ""));
        mockMeds.add(new Medicine(3, "Vitamin C 1000mg", "1 viên / ngày (Sáng)", ""));
        medicines.setValue(mockMeds);

        List<VisitHistory> mockHistory = new ArrayList<>();
        mockHistory.add(new VisitHistory(1, "15/01/2026", "Khám Tổng Quát", "BS. Trần Hoàng Nam", "Hoàn thành"));
        mockHistory.add(new VisitHistory(2, "02/12/2025", "Tái Khám", "BS. Lê Thị Mai Anh", "Hoàn thành"));
        mockHistory.add(new VisitHistory(3, "18/10/2025", "Khám Chuyên Khoa", "BS. Phạm Minh Tuấn", "Hoàn thành"));
        visitHistory.setValue(mockHistory);
    }

    public LiveData<Patient> getPatient() { return patient; }
    public LiveData<List<Medicine>> getMedicines() { return medicines; }
    public LiveData<List<VisitHistory>> getVisitHistory() { return visitHistory; }
}
