package com.shopnow.shopnow;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@SpringBootApplication
public class ShopNowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopNowApplication.class, args);
    }

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("firebase-service-account.json").getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions
                .builder()
                .setCredentials(googleCredentials)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, "my-app");
        return FirebaseMessaging.getInstance(app);
    }

    @Bean
    BraintreeGateway getGateway() {
        return new BraintreeGateway(
                Environment.SANDBOX,
                "hbwcs3cttj8zwxy3",
                "53q6w6bxpyxj6qfz",
                "839b3524f96f770d93bedded80219ae3"
        );
    }
}
