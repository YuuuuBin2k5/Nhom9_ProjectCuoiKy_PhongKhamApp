package com.hcmute.clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Lớp khởi chạy ứng dụng Spring Boot cho hệ thống quản lý phòng khám nha khoa.
 */
@SpringBootApplication
@EnableScheduling
public class ClinicApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicApplication.class, args);
	}

}
