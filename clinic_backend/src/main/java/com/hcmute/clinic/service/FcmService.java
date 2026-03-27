package com.hcmute.clinic.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Slf4j
public class FcmService {

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (resource.exists()) {
                InputStream serviceAccount = resource.getInputStream();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK initialized");
                }
            } else {
                log.warn("firebase-service-account.json not found in resources. Push notifications will be mocked.");
            }
        } catch (Exception e) {
            log.error("Error initializing Firebase Admin SDK", e);
        }
    }

    public void sendNotification(String token, String title, String body) {
        if (token == null || token.isBlank()) {
            return;
        }
        
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("[MOCK PUSH] Sending push to {}: {} - {}", token, title, body);
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);
        } catch (Exception e) {
            log.error("Failed to send FCM message", e);
        }
    }
}
