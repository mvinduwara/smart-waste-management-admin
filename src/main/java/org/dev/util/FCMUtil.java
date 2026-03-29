package org.dev.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import java.io.InputStream;

public class FCMUtil {

    public static void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = FCMUtil.class.getClassLoader()
                        .getResourceAsStream("serviceAccountKey.json");

                if (serviceAccount == null) {
                    System.err.println("Firebase service account key not found!");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK initialized successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendNotification(String targetToken, String title, String body) {
        if (targetToken == null || targetToken.isEmpty()) return;

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
        } catch (Exception e) {
            System.err.println("Error sending FCM notification: " + e.getMessage());
        }
    }
}