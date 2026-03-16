package com.example.movra.config.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${cloud.file.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.file.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.file.seaweedfs.region}")
    private String region;

    @Value("${cloud.file.seaweedfs.endpoint}")
    private String endpoint;

    @Bean
    public S3Client s3Client(){
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .build();
    }
}
