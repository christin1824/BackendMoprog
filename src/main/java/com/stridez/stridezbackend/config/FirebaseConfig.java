package com.stridez.stridezbackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Initialize Firebase Admin SDK using service account JSON placed in resources.
 *
 * Add your service account JSON to: src/main/resources/firebase-service-account.json
 * Do NOT commit that file to version control.
 */
@Component
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (!resource.exists()) {
                log.warn("Firebase service account json not found on classpath (firebase-service-account.json). Firebase verification disabled. Place the file at src/main/resources/firebase-service-account.json to enable verification.");
                return;
            }

            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin initialized from service account JSON.");
                }
            }
        } catch (Exception e) {
            // If something unexpected happens, log and continue without failing startup
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
        }
    }
}
