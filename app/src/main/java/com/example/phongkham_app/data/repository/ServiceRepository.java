package com.example.phongkham_app.data.repository;

import com.example.phongkham_app.data.model.Service;

import java.util.ArrayList;
import java.util.List;

import com.example.phongkham_app.data.local.DatabaseHelper;
import android.content.Context;
import android.database.Cursor;

public class ServiceRepository {
    private static ServiceRepository instance;
    private DatabaseHelper dbHelper;

    private ServiceRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public static synchronized ServiceRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ServiceRepository(context.getApplicationContext());
        }
        return instance;
    }

    public List<Service> getServices() {
        List<Service> services = new ArrayList<>();
        Cursor cursor = dbHelper.getAllServicesWithImages();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("id");
                int nameIndex = cursor.getColumnIndex("name");
                int priceIndex = cursor.getColumnIndex("price");
                int imageIndex = cursor.getColumnIndex("image_url");
                int durationIndex = cursor.getColumnIndex("duration_minutes");
                
                int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                String name = nameIndex != -1 ? cursor.getString(nameIndex) : "Unknown";
                double priceVal = priceIndex != -1 ? cursor.getDouble(priceIndex) : 0.0;
                String priceStr = String.format("%,.0f VNĐ", priceVal);
                String imageUrl = imageIndex != -1 && !cursor.isNull(imageIndex) ? cursor.getString(imageIndex) : null;
                int duration = durationIndex != -1 ? cursor.getInt(durationIndex) : 15;
                
                Service svc = new Service(id, name, true, priceStr);
                svc.setImageUrl(imageUrl);
                svc.setDuration(duration);
                services.add(svc);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return services;
    }
}
