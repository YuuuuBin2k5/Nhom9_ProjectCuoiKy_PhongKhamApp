package com.example.phongkham_app.data.repository;

import com.example.phongkham_app.data.model.Doctor;

import java.util.ArrayList;
import java.util.List;

import com.example.phongkham_app.data.local.DatabaseHelper;
import android.content.Context;
import android.database.Cursor;

public class DoctorRepository {
    private static DoctorRepository instance;
    private DatabaseHelper dbHelper;

    private DoctorRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public static synchronized DoctorRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DoctorRepository(context.getApplicationContext());
        }
        return instance;
    }

    public List<Doctor> getDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        Cursor cursor = dbHelper.getAllDoctors();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("id");
                int nameIndex = cursor.getColumnIndex("name");
                int specialtyIndex = cursor.getColumnIndex("specialty");
                
                String id = idIndex != -1 ? String.valueOf(cursor.getInt(idIndex)) : "0";
                String name = nameIndex != -1 ? cursor.getString(nameIndex) : "Unknown";
                String specialty = specialtyIndex != -1 ? cursor.getString(specialtyIndex) : "General";
                
                doctors.add(new Doctor(id, "BS. " + name, specialty, "", 4.5, 100, 0, false));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return doctors;
    }

    public Doctor getDoctorById(String id) {
        for (Doctor doctor : getDoctors()) {
            if (doctor.getId().equals(id)) return doctor;
        }
        return null;
    }
}
