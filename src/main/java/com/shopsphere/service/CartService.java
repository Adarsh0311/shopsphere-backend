package com.shopsphere.service;

import com.shopsphere.dto.AddToCartRequest;
import com.shopsphere.dto.CartItemResponse;
import com.shopsphere.dto.CartResponse;
import com.shopsphere.model.Cart;
import com.shopsphere.model.CartItem;
import com.shopsphere.model.Product;
import com.shopsphere.model.User;
import com.shopsphere.repository.CartItemRepository;
import com.shopsphere.repository.CartRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;

    /**
     * Helper method to convert CartItem entity to CartItemResponse DTO.
     */
    private CartItemResponse convertToCartItemDto(CartItem cartItem) {
        CartItemResponse dto = new CartItemResponse();
        dto.setCartItemId(cartItem.getCartItemId());
        dto.setProductId(cartItem.getProduct().getProductId());
        dto.setProductName(cartItem.getProduct().getName()); // Access product name
        dto.setProductImageUrl(cartItem.getProduct().getImageUrl()); // Access product image URL
        dto.setQuantity(cartItem.getQuantity());
        dto.setPriceAtAddition(cartItem.getPriceAtAddition());
        dto.setItemTotal(cartItem.getPriceAtAddition().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        dto.setAddedAt(cartItem.getAddedAt());
        return dto;
    }

    /**
     * Helper method to convert Cart to CartResponse
     */
    private CartResponse convertToCartResponse(Cart cart) {
        CartResponse dto = new CartResponse();
        dto.setCartId(cart.getCartId());

        List<CartItemResponse> cartItemResponseList = cart
                .getCartItems()
                .stream()
                .map(this::convertToCartItemDto)
                .toList();

        dto.setItems(cartItemResponseList);
        //calculate total
        BigDecimal total  = cartItemResponseList
                .stream()
                .map(CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setTotalAmount(total);

        if (cart.getUser() != null) {
            dto.setUserId(cart.getUser().getUserId());
            dto.setUsername(cart.getUser().getUsername());
        }

        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        return dto;
    }

    /**
     * Get or create a cart for a specific user.
     * @param userId The ID of the authenticated user.
     * @return The user's cart entity.
     */
    @Transactional
    public Cart getOrCreateCart(String userId) {
        //User user = userService.findByUsername(userId); // Assuming userId here is actually the username from SecurityContextHolder
        // In a real app, you'd likely fetch by userId, not username for cart linking.
        // If the principal is the actual userId (UUID string), use userRepository.findById(userId) instead.
        // For now, let's assume `userId` passed here is the username.

        User user = new User();
        user.setUserId(userId);

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Get a cart response for a specific user.
     * @param userId The ID of the authenticated user.
     * @return The user's cart as a DTO.
     */
    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(String userId) {
        Cart cart = getOrCreateCart(userId); // Reuse logic to get the cart entity
        return convertToCartResponse(cart);
    }

    /**
     * Adds a product to the cart or updates its quantity.
     * @param userId The ID of the authenticated user.
     * @param request The AddToCartRequest DTO containing productId and quantity.
     * @return The updated cart as a DTO.
     */
    @Transactional
    public CartResponse addProductToCart(String userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId); // Get or create user's cart

        Product actualProduct = productService.getProductEntityById(request.getProductId());

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, actualProduct);

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart); // Set bidirectional relationship
            newItem.setProduct(actualProduct);
            newItem.setQuantity(request.getQuantity());
            newItem.setPriceAtAddition(actualProduct.getPrice()); // Set price at time of addition
            cartItemRepository.save(newItem);
            cart.addCartItem(newItem); // Also add to cart's collection
        }

        return convertToCartResponse(cart);
    }

    /**
     * Updates the quantity of a product in the cart.
     * @param userId The ID of the authenticated user.
     * @param productId The ID of the product to update.
     * @param quantity The new quantity.
     * @return The updated cart as a DTO.
     */
    @Transactional
    public CartResponse updateProductQuantityInCart(String userId, String productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productService.getProductEntityById(productId);

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in cart."));

        if (quantity <= 0) {
            // If quantity is 0 or less, remove item from cart
            cartItemRepository.delete(item);
            cart.removeCartItem(item); // Remove from cart's collection as well
        } else {
            // Update quantity
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        cartRepository.save(cart);
        return convertToCartResponse(cart);
    }

    @Transactional
    public CartResponse removeProductFromCart(String userId, String productId) {
        Cart cart = getOrCreateCart(userId);
        Product product = productService.getProductEntityById(productId);

       CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in cart."));

       cartItemRepository.delete(item);
       cart.removeCartItem(item);

       cartRepository.save(cart);
       return convertToCartResponse(cart);
    }

    /**
     * Clears all items from a user's cart.
     * @param userId The ID of the authenticated user.
     * @return The cleared cart as a DTO.
     */
    @Transactional
    public CartResponse clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteAll(cart.getCartItems()); // Delete all items associated with this cart
        cart.getCartItems().clear(); // Clear the collection in memory

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        return convertToCartResponse(cart);
    }

}
