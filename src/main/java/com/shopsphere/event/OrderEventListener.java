package com.shopsphere.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.service.SqsMessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    private final SqsMessageSenderService sqsMessageSenderService;
    private final ObjectMapper objectMapper;

    /**
     * Listens for OrderPlacedEvent and sends an SQS message after the transaction commits.
     * @param event The OrderPlacedEvent.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Handling OrderPlacedEvent for order ID: {}. Transaction phase: {}",
                event.getOrderResponse().getOrderId(), TransactionPhase.AFTER_COMMIT.name());

        try {
            String orderJson = objectMapper.writeValueAsString(event.getOrderResponse());
            sqsMessageSenderService.sendOrderProcessingMessage(orderJson);
            log.info("SQS message sent successfully for order ID: {}", event.getOrderResponse().getOrderId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing OrderResponse to JSON for order ID {}: {}", event.getOrderResponse().getOrderId(), e.getMessage());
            // Handle serialization error: e.g., send to dead-letter queue, log for manual inspection

        } catch (Exception e) {
            log.error("Error sending SQS message for order ID {}: {}", event.getOrderResponse().getOrderId(), e.getMessage());
            // TODO: implement retry mechanism
        }
    }
}
