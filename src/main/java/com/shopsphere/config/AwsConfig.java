package com.shopsphere.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsConfig {
    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Bean
    public SqsClient sqsClient() {
         return SqsClient.builder()
                .region(Region.of(region))
//                .credentialsProvider(StaticCredentialsProvider.create(getCredentials()))
                .build();
//        SqsClient.builder()
//                .region(Region.of(region))
        // This will tell AWS to look for AWS_ACCESS_KEY_ID and SECRET in your environment
//                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
//                .build();


    }

    @Bean
    public SnsClient snsClient() {

        return SnsClient.builder()
                .region(Region.of(region))
//                .credentialsProvider(StaticCredentialsProvider.create(getCredentials()))
                .build();
    }

    private AwsBasicCredentials getCredentials() {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }
}
