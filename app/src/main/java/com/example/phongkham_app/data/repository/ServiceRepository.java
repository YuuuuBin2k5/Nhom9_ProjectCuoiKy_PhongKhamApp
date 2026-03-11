package com.example.phongkham_app.data.repository;

import com.example.phongkham_app.data.model.Service;

import java.util.ArrayList;
import java.util.List;

public class ServiceRepository {
    private static ServiceRepository instance;

    private ServiceRepository() {}

    public static synchronized ServiceRepository getInstance() {
        if (instance == null) {
            instance = new ServiceRepository();
        }
        return instance;
    }

    public List<Service> getServices() {
        List<Service> services = new ArrayList<>();
        services.add(new Service("Khám tổng quát", true, "200,000 VNĐ"));
        services.add(new Service("Khám chuyên khoa", true, "500,000 VNĐ"));
        services.add(new Service("Xét nghiệm máu", true, "300,000 VNĐ"));
        services.add(new Service("Siêu âm", true, "400,000 VNĐ"));
        services.add(new Service("Chụp X-Quang", true, "350,000 VNĐ"));
        return services;
    }
}
