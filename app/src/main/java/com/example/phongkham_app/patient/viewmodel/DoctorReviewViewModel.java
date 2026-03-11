package com.example.phongkham_app.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DoctorReviewViewModel extends ViewModel {

    private final MutableLiveData<Integer> doctorId = new MutableLiveData<>();
    private final MutableLiveData<String> doctorName = new MutableLiveData<>();
    private final MutableLiveData<String> specialty = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedRating = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> submitSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public void setDoctorInfo(int id, String name, String spec) {
        doctorId.setValue(id);
        doctorName.setValue(name);
        specialty.setValue(spec);
    }

    public void selectRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            selectedRating.setValue(rating);
        }
    }

    public void submitReview(String reviewText) {
        Integer rating = selectedRating.getValue();
        if (rating == null || rating == 0) {
            errorMessage.setValue("Vui lòng chọn số sao đánh giá.");
        } else {
            // Processing review logic here (API call etc)
            submitSuccess.setValue(true);
        }
    }

    // Getters
    public LiveData<String> getDoctorName() { return doctorName; }
    public LiveData<String> getSpecialty() { return specialty; }
    public LiveData<Integer> getSelectedRating() { return selectedRating; }
    public LiveData<Boolean> getSubmitSuccess() { return submitSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
