package com.shopsphere.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.dto.OrderResponse;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderMessageConsumerService {
    private final ObjectMapper objectMapper;
    // private final SnsMessagePublisherService snsMessagePublisherService; // Will be injected later for SNS

    /**
     * Listens to messages from the SQS order processing queue.
     * @param message The raw message string from SQS (expected to be JSON).
     */
    @SqsListener("${aws.sqs.order-processing-queue-name}") // Listens to the queue defined in properties
    public void receiveOrderMessage(String message) {
        log.info("Received SQS message: {}", message);
        try {
            // Parse the JSON message back into an OrderResponse DTO
            OrderResponse orderResponse = objectMapper.readValue(message, OrderResponse.class);
            log.info("Successfully parsed order message for Order ID: {}", orderResponse.getOrderId());

            //TODO: implement SNS for email notifications

        } catch (JsonProcessingException e) {
            log.error("Failed to parse SQS message JSON for order confirmation: {}", e.getMessage(), e);
            // TODO: implement moving to dead letter queue
        } catch (Exception e) {
            log.error("Error processing SQS message for order confirmation: {}", e.getMessage(), e);
        }
    }
}
