package com.shopsphere.controller;

import com.shopsphere.config.security.CustomUserDetails;
import com.shopsphere.dto.AddToCartRequest;
import com.shopsphere.dto.CartResponse;
import com.shopsphere.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/cart : Get the authenticated user's cart.
     * @param userDetails The authenticated user's details.
     * @return The CartResponse DTO.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getUserCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse cart = cartService.getCartByUserId(userDetails.getUserId());
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /api/cart/add : Add a product to the authenticated user's cart.
     * @param userDetails The authenticated user's details.
     * @param request The AddToCartRequest DTO.
     * @return The updated CartResponse DTO.
     */
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addProductToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addProductToCart(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.OK).body(cart); // Use OK for updates/adds to existing resource
    }

    /**
     * PUT /api/cart/update-quantity/{productId} : Update quantity of a product in the authenticated user's cart.
     * @param userDetails The authenticated user's details.
     * @param productId The ID of the product.
     * @param quantity The new quantity.
     * @return The updated CartResponse DTO.
     */
    @PutMapping("/update-quantity/{productId}")
    public ResponseEntity<CartResponse> updateProductQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        CartResponse cart = cartService.updateProductQuantityInCart(userDetails.getUserId(), productId, quantity);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart/remove/{productId} : Remove a product from the authenticated user's cart.
     * @param userDetails The authenticated user's details.
     * @param productId The ID of the product to remove.
     * @return The updated CartResponse DTO.
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeProductFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String productId) {
        CartResponse cart = cartService.removeProductFromCart(userDetails.getUserId(), productId);
        return ResponseEntity.ok(cart); // Return updated cart for client to refresh
    }

    /**
     * DELETE /api/cart/clear : Clear all items from the authenticated user's cart.
     * @param userDetails The authenticated user's details.
     * @return The cleared CartResponse DTO.
     */
    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse cart = cartService.clearCart(userDetails.getUserId());
        return ResponseEntity.ok(cart);
    }
}