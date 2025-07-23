package com.shopsphere.controller;

import com.shopsphere.config.CustomUserDetails;
import com.shopsphere.dto.OrderResponse;
import com.shopsphere.dto.PlaceOrderRequest;
import com.shopsphere.model.OrderStatus;
import com.shopsphere.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders/place : Place a new order from the authenticated user's cart.
     * @param userDetails The authenticated user's details.
     * @param request The PlaceOrderRequest DTO.
     * @return ResponseEntity with the created OrderResponse DTO and HTTP status 201 Created.
     */
    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PlaceOrderRequest request) {
        OrderResponse createdOrder = orderService.placeOrder(userDetails.getUserId(), request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(createdOrder.getOrderId())
                .toUri();

        return ResponseEntity.created(location).body(createdOrder);
    }

    /**
     * GET /api/orders : Get all orders for the authenticated user.
     * @param userDetails The authenticated user's details.
     * @return ResponseEntity with a list of OrderResponse DTOs and HTTP status 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrderResponse> orders = orderService.getUserOrders(userDetails.getUserId());
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/{id} : Get a specific order by its ID.
     * @param id The ID of the order.
     * @return ResponseEntity with the OrderResponse DTO and HTTP status 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // TODO: Admin endpoint for updating order status
    // We will make this protected with roles later
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderResponse updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

}
