package com.shopsphere.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class SqsMessageSenderService {
    private final SqsClient sqsClient;

    @Value("${aws.sqs.order-processing-queue-name}")
    private String orderProcessingQueueName;

    /**
     * Sends a message to the SQS order processing queue.
     * @param messageBody The content of the message (e.g., JSON representation of an order).
     */
    public void sendOrderProcessingMessage(String messageBody) {
        try {
            //get Queue URL
            GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                    .queueName(orderProcessingQueueName)
                    .build());
            String queueUrl = getQueueUrlResponse.queueUrl();

            //build and send the message
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(sendMessageRequest);
            log.info("Successfully sent message to SQS queue '{}': {}", orderProcessingQueueName, messageBody);

        } catch (Exception e) {
            log.error("Failed to send message to SQS queue '{}': {}", orderProcessingQueueName, e.getMessage());
            //throw e;
        }
    }

}
