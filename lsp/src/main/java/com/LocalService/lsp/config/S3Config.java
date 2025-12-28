package com.LocalService.lsp.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    private static final Logger logger = LoggerFactory.getLogger(S3Config.class);

    /**
     * Maps to aws.access-key in your application.properties.
     * The ':' ensures that if the property is missing, it defaults to an empty string.
     */
    @Value("${aws.access-key:}")
    private String accessKey;

    /**
     * Maps to aws.secret-key in your application.properties.
     */
    @Value("${aws.secret-key:}")
    private String secretKey;

    /**
     * Maps to aws.s3.region in your application.properties.
     */
    @Value("${aws.s3.region:us-east-1}")
    private String region;

    /**
     * Diagnostic check to verify properties are loaded from application.properties.
     */
    @PostConstruct
    public void diagnosticCheck() {
        logger.info("======= AWS S3 Property Check =======");

        boolean isAccessKeyPresent = (accessKey != null && !accessKey.isBlank());
        boolean isSecretKeyPresent = (secretKey != null && !secretKey.isBlank());

        logger.info("Property [aws.access-key]: {}", isAccessKeyPresent ? "LOADED" : "MISSING");
        logger.info("Property [aws.secret-key]: {}", isSecretKeyPresent ? "LOADED" : "MISSING");
        logger.info("Property [aws.s3.region]: {}", region);

        if (!isAccessKeyPresent || !isSecretKeyPresent) {
            logger.error("!!! ALERT: AWS Credentials are not loaded. Portfolio photo features will fail.");
        } else {
            logger.info("Status: SUCCESS - S3 Configuration initialized.");
        }
        logger.info("=====================================");
    }

    /**
     * Factory method for S3Client.
     * Uses AnonymousCredentialsProvider as a fallback to prevent startup crashes
     * if the keys in application.properties are blank.
     */
    @Bean
    public S3Client s3Client() {
        try {
            if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
                logger.warn("S3 Credentials are blank. Returning unauthenticated S3Client.");
                return S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(AnonymousCredentialsProvider.create())
                        .build();
            }

            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        } catch (Exception e) {
            logger.error("Error creating S3Client bean: {}", e.getMessage());
            // Fallback to allow context to load
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(AnonymousCredentialsProvider.create())
                    .build();
        }
    }
}