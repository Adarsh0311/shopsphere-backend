package com.shopsphere.service;

import com.shopsphere.dto.PlaceOrderRequest;
import com.shopsphere.model.Order;
import com.shopsphere.model.Payment;
import com.shopsphere.model.User;
import com.shopsphere.model.enums.OrderStatus;
import com.shopsphere.model.enums.PaymentStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@Slf4j
public class StripePaymentService {

    public void processPayment(User user, Order newOrder, PlaceOrderRequest placeOrderRequest, BigDecimal totalAmount) {
        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(placeOrderRequest.getPaymentMethod());

        try {
            PaymentIntent paymentIntent = createPaymentIntent(user, placeOrderRequest, totalAmount);
            payment.setTransactionId(paymentIntent.getId());


            if ("succeeded".equalsIgnoreCase(paymentIntent.getStatus())) {
                payment.setStatus(PaymentStatus.COMPLETED);
                newOrder.setStatus(OrderStatus.PROCESSING); //update order status on successful payment

            } else if ("requires_payment_method".equalsIgnoreCase(paymentIntent.getStatus()) || "requires_action".equalsIgnoreCase(paymentIntent.getStatus())) {
                payment.setStatus(PaymentStatus.PENDING);

            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.error("Stripe charge failed or has unexpected status for order {}: Status - {}", newOrder.getOrderId(), paymentIntent.getStatus());
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Payment failed with status" + payment.getStatus());
            }

            log.info("Stripe charge processed for order {}. Charge ID: {}, Status: {}", newOrder.getOrderId(), paymentIntent.getId(), paymentIntent.getStatus());

        } catch (StripeException e) {
            log.error("Stripe API call error during order {}: {}", newOrder.getOrderId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Payment processing failed: " + e.getMessage());
        }  catch (ArithmeticException e) { // Catch if totalAmount has too many decimal places for longValueExact
            log.error("Error converting total amount to cents for order {}: {}", newOrder.getOrderId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount for payment.");
        }
        newOrder.setPayment(payment);
    }

    private PaymentIntent createPaymentIntent(User user, PlaceOrderRequest placeOrderRequest, BigDecimal totalAmount) throws StripeException {
        long amountInCents = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .addPaymentMethodType("card")
                .setPaymentMethod(placeOrderRequest.getPaymentMethodToken())
                .setConfirm(true)
                .setReceiptEmail(user.getEmail())
                .build();

        return PaymentIntent.create(params);
    }

}
