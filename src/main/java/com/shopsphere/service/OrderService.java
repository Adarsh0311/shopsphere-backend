package com.shopsphere.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.dto.OrderItemResponse;
import com.shopsphere.dto.OrderResponse;
import com.shopsphere.dto.PlaceOrderRequest;
import com.shopsphere.model.*;
import com.shopsphere.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for creating orders, updating stocks and publishing messages to SQS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final SqsMessageSenderService sqsMessageSenderService;
    private final ObjectMapper objectMapper; // To convert objects to JSON for SQS


    /**
     * Place a new order from the user's cart.
     * @param userId The UUID of the authenticated user.
     * @param placeOrderRequest The PlaceOrderRequest DTO.
     * @return The created OrderResponse DTO.
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public OrderResponse placeOrder(String userId, PlaceOrderRequest placeOrderRequest) {
        log.info("place order request {}", placeOrderRequest);
        User user = userService.findById(userId);
        Cart cart = getUserCart(user);

        Order newOrder = new Order();
        newOrder.setUser(user);
        newOrder.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<CartItem> cartItems = cart.getCartItems();

        for (CartItem cartItem : cartItems) {
            Product product = productService.getProductEntityById(cartItem.getProduct().getProductId());
            validateStock(cartItem, product);

            //create order item (snapshot of product details at time or order)
            OrderItem orderItem = getOrderItem(cartItem, product);
            newOrder.addOrderItem(orderItem);

            totalAmount = totalAmount.add(cartItem.getPriceAtAddition().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            //Deduct stock from product
            final int updatedQuantity = product.getStockQuantity() - cartItem.getQuantity();
            productService.updateProductStockQuantity(product.getProductId(), updatedQuantity);

        }

        newOrder.setTotalAmount(totalAmount);

        //Address handling for order (shipping)
        Address shippingAddress = getShippingAddress(placeOrderRequest);
        newOrder.setShippingAddress(shippingAddress); //CascadeType.ALL on Order ensures it's saved

        //Handle Payment Details
        Payment payment = getPayment(placeOrderRequest, totalAmount);

        newOrder.setPayment(payment);

        //all changes within this transaction will be commited
        Order savedOrder = orderRepository.save(newOrder);

        //clear the user's cart
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        //sending the message to SQS queue for async processing
        try {
            String orderJson = objectMapper.writeValueAsString(convertToOrderDto(newOrder));
            //this is async processing
            sqsMessageSenderService.sendOrderProcessingMessage(orderJson);
            log.info("sent order processing message {}", orderJson);
        } catch (Exception e) {
            log.error("sent order processing error {}", e.getMessage());
            //TODO: implement retry mechanism
            //throw new RuntimeException(e);
        }

        return convertToOrderDto(savedOrder);

    }

    /**
     * Get a user's orders.
     * @param userId The UUID of the authenticated user.
     * @return List of OrderResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String userId) {
        User user = userService.findById(userId);
        return orderRepository.findByUser(user)
                .stream()
                .map(this::convertToOrderDto)
                .toList();
    }

    /**
     * Get a specific order by its ID.
     * @param orderId The ID of the order.
     * @return The OrderResponse DTO.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id " + orderId));

        return convertToOrderDto(order);
    }

    /**
     * Update the status of an order.
     * @param orderId The ID of the order.
     * @param newStatus The new status string.
     * @return The updated OrderResponse DTO.
     */
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with ID: " + orderId));

        if (newStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New status cannot be empty.");
        }
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order ID {} status updated to: {}", orderId, newStatus);
        return convertToOrderDto(updatedOrder);
    }




    private Cart getUserCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("cart not found {}", user.getUserId());
                   return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not place order. No active cart found");
                });

        if (cart.getCartItems().isEmpty()) {
            log.warn("no cart items found for user {}", user.getUserId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No cart items found for user " + user.getUserId());
        }
        return cart;
    }

    private static OrderItem getOrderItem(CartItem cartItem, Product product) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPriceAtPurchase(cartItem.getPriceAtAddition());
        return orderItem;
    }

    private static Payment getPayment(PlaceOrderRequest placeOrderRequest, BigDecimal totalAmount) {
        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(placeOrderRequest.getPaymentMethod());
        //TODO: implement payment gateway and fetch transaction id from there
        payment.setTransactionId(placeOrderRequest.getTransactionId()); //it would come from payment gateway
        payment.setStatus(PaymentStatus.COMPLETED); //for now
        return payment;
    }

    private static Address getShippingAddress(PlaceOrderRequest placeOrderRequest) {
        Address shippingAddress = new Address();
        shippingAddress.setStreet(placeOrderRequest.getStreet());
        shippingAddress.setCity(placeOrderRequest.getCity());
        shippingAddress.setState(placeOrderRequest.getState());
        shippingAddress.setPostalCode(placeOrderRequest.getPostalCode());
        shippingAddress.setCountry(placeOrderRequest.getCountry());
        shippingAddress.setAddressType("SHIPPING_ORDER");
        return shippingAddress;
    }

    private void validateStock(CartItem cartItem, Product product) {
        if (cartItem.getQuantity() > product.getStockQuantity()) {
            log.warn("Insufficient stock for product ID {}", product.getProductId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient stock for product:  " + product.getName() + ". Available stock: " + product.getStockQuantity());
        }
    }

    /**
     * Helper method to convert OrderItem entity to OrderItemResponse DTO.
     */
    private OrderItemResponse convertToOrderItemDto(OrderItem orderItem) {
        OrderItemResponse dto = new OrderItemResponse();
        dto.setOrderItemId(orderItem.getOrderItemId());
        dto.setProductId(orderItem.getProduct().getProductId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setProductImageUrl(orderItem.getProduct().getImageUrl());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        dto.setItemTotal(orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return dto;
    }

    /**
     * Helper method to convert Order entity to OrderResponse DTO.
     */
    private OrderResponse convertToOrderDto(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUser().getUserId());
        dto.setUsername(order.getUser().getUsername());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());

        // Shipping Address details
        if (order.getShippingAddress() != null) {
            dto.setShippingStreet(order.getShippingAddress().getStreet());
            dto.setShippingCity(order.getShippingAddress().getCity());
            dto.setShippingState(order.getShippingAddress().getState());
            dto.setShippingPostalCode(order.getShippingAddress().getPostalCode());
            dto.setShippingCountry(order.getShippingAddress().getCountry());
        }

        // Payment details
        if (order.getPayment() != null) {
            dto.setPaymentMethod(order.getPayment().getPaymentMethod());
            dto.setPaymentTransactionId(order.getPayment().getTransactionId());
            dto.setPaymentStatus(order.getPayment().getStatus());
            dto.setPaymentDate(order.getPayment().getPaymentDate());
        }

        List<OrderItemResponse> itemDtos = order.getOrderItems()
                .stream()
                .map(this::convertToOrderItemDto)
                .toList();
        dto.setItems(itemDtos);

        return dto;
    }

}
