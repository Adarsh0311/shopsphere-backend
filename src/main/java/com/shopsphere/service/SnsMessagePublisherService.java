package com.shopsphere.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

/**
 * Service class for handling send messages to SNS topic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SnsMessagePublisherService {
    private final SnsClient snsClient;

    @Value("${aws.sns.order-confirmation-topic-arn}")
    private String orderConfirmationTopicArn;

    /**
     * publishes an order confirmation message to the SNS topic
     * @param subject : The subject of email (e.g. Order confirmation)
     * @param messageBody : The body of the email (JSON representation)
     */
    public void publishOrderConfirmation(String subject, String messageBody) {
        try {
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(orderConfirmationTopicArn)
                    .subject(subject)
                    .message(messageBody)
                    .build();

            PublishResponse publishResponse = snsClient.publish(publishRequest);
            log.info("Successfully published message to SNS topic '{}'. Message ID: {}", orderConfirmationTopicArn, publishResponse.messageId());
        } catch (Exception e) {
            //TODO: implement retry mechanism or send to Dead letter Queue
            throw new RuntimeException("Failed to publish SNS message: " + e.getMessage(), e);
        }
    }
}
