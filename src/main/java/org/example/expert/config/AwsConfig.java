package org.example.expert.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    // DefaultCredentialsProvider를 별도의 Bean으로 등록
    // 최신 Docs의 내용처럼 builder().build()를 사용하여 매번 새로운 인스턴스를 생성하도록 구현
    @Bean
    public DefaultCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

    @Bean
    public S3Presigner s3Presigner(DefaultCredentialsProvider credentialsProvider) {
        return S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .build();
    }

    @Bean
    public S3Client s3Client(DefaultCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .build();
    }
}
