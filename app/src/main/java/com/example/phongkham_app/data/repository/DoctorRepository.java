package com.example.phongkham_app.data.repository;

import com.example.phongkham_app.data.model.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {
    private static DoctorRepository instance;

    private DoctorRepository() {}

    public static synchronized DoctorRepository getInstance() {
        if (instance == null) {
            instance = new DoctorRepository();
        }
        return instance;
    }

    public List<Doctor> getDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        doctors.add(new Doctor("DOC_1", "BS. Trần Hoàng Nam", "Nội tổng quát", "0912345678", 4.5, 120, 0, false));
        doctors.add(new Doctor("DOC_2", "BS. Lê Thị Mai Anh", "Tim mạch", "0923456789", 4.8, 250, 0, true));
        doctors.add(new Doctor("DOC_3", "BS. Phạm Minh Tuấn", "Thần kinh", "0934567890", 4.2, 85, 0, false));
        doctors.add(new Doctor("DOC_4", "BS. Nguyễn Thùy Linh", "Da liễu", "0945678901", 4.9, 310, 0, true));
        doctors.add(new Doctor("DOC_5", "BS. Võ Đức Hải", "Ngoại khoa", "0956789012", 4.6, 175, 0, false));
        return doctors;
    }

    public Doctor getDoctorById(String id) {
        for (Doctor doctor : getDoctors()) {
            if (doctor.getId().equals(id)) return doctor;
        }
        return null;
    }
}
