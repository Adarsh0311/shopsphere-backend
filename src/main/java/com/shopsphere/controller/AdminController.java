package com.shopsphere.controller;

import com.shopsphere.dto.AdminDashboardStats;
import com.shopsphere.dto.CategoryRequest;
import com.shopsphere.dto.CategoryResponse;
import com.shopsphere.dto.OrderResponse;
import com.shopsphere.dto.ProductRequest;
import com.shopsphere.dto.ProductResponse;
import com.shopsphere.model.User;
import com.shopsphere.model.enums.OrderStatus;
import com.shopsphere.service.AdminService;
import com.shopsphere.service.CategoryService;
import com.shopsphere.service.OrderService;
import com.shopsphere.service.ProductService;
import com.shopsphere.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;

    /**
     * GET /api/admin/dashboard/stats : Get admin dashboard statistics.
     * @return ResponseEntity with {@link AdminDashboardStats} and HTTP status 200 OK.
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStats> getDashboardStats() {
        AdminDashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // Product Management Methods

    /**
     * GET /api/admin/products : Get all products.
     * @return ResponseEntity with list of all products and HTTP status 200 OK.
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/admin/products/{id} : Get product by ID.
     * @param id The product ID.
     * @return ResponseEntity with the product and HTTP status 200 OK.
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * GET /api/admin/products/search/name : Get product by name.
     * @param name The product name.
     * @return ResponseEntity with the product and HTTP status 200 OK.
     */
    @GetMapping("/products/search/name")
    public ResponseEntity<ProductResponse> getProductByName(@RequestParam String name) {
        ProductResponse product = productService.getProductByName(name);
        return ResponseEntity.ok(product);
    }

    /**
     * GET /api/admin/products/search/price-range : Get products in price range.
     * @param minPrice The minimum price.
     * @param maxPrice The maximum price.
     * @return ResponseEntity with list of products and HTTP status 200 OK.
     */
    @GetMapping("/products/search/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsInPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<ProductResponse> products = productService.getProductsInPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/admin/products/search/low-stock : Get low stock products.
     * @param threshold The stock threshold.
     * @return ResponseEntity with list of low stock products and HTTP status 200 OK.
     */
    @GetMapping("/products/search/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts(@RequestParam Integer threshold) {
        List<ProductResponse> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/admin/products/category/{categoryId} : Get products by category.
     * @param categoryId The category ID.
     * @return ResponseEntity with list of products and HTTP status 200 OK.
     */
    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String categoryId) {
        List<ProductResponse> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * POST /api/admin/products : Create a new product.
     * @param productRequest The product request DTO.
     * @return ResponseEntity with the created product and HTTP status 201 Created.
     */
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
        ProductResponse createdProduct = productService.createProduct(productRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.getProductId())
                .toUri();
        return ResponseEntity.created(location).body(createdProduct);
    }

    /**
     * PUT /api/admin/products/{id} : Update an existing product.
     * @param id The product ID.
     * @param productRequest The product request DTO.
     * @return ResponseEntity with the updated product and HTTP status 200 OK.
     */
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id, @RequestBody ProductRequest productRequest) {
        ProductResponse updatedProduct = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * DELETE /api/admin/products/{id} : Delete a product.
     * @param id The product ID.
     * @return ResponseEntity with HTTP status 204 No Content.
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Category Management Methods

    /**
     * GET /api/admin/categories : Get all categories.
     * @return ResponseEntity with list of all categories and HTTP status 200 OK.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * GET /api/admin/categories/{id} : Get category by ID.
     * @param id The category ID.
     * @return ResponseEntity with the category and HTTP status 200 OK.
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable String id) {
        CategoryResponse category = categoryService.getCategoryResponseById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * GET /api/admin/categories/search/name : Get category by name.
     * @param name The category name.
     * @return ResponseEntity with the category and HTTP status 200 OK.
     */
    @GetMapping("/categories/search/name")
    public ResponseEntity<CategoryResponse> getCategoryByName(@RequestParam String name) {
        CategoryResponse category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }

    /**
     * POST /api/admin/categories : Create a new category.
     * @param categoryRequest The category request DTO.
     * @return ResponseEntity with the created category and HTTP status 201 Created.
     */
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryResponse createdCategory = categoryService.createCategory(categoryRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCategory.getCategoryId())
                .toUri();
        return ResponseEntity.created(location).body(createdCategory);
    }

    /**
     * PUT /api/admin/categories/{id} : Update an existing category.
     * @param id The category ID.
     * @param categoryRequest The category request DTO.
     * @return ResponseEntity with the updated category and HTTP status 200 OK.
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable String id, @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse updatedCategory = categoryService.updateCategory(id, categoryRequest);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * DELETE /api/admin/categories/{id} : Delete a category.
     * @param id The category ID.
     * @return ResponseEntity with HTTP status 204 No Content.
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Order Management Methods

    /**
     * GET /api/admin/orders : Get all orders.
     * @return ResponseEntity with list of all orders and HTTP status 200 OK.
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/admin/orders/{id} : Get order by ID.
     * @param id The order ID.
     * @return ResponseEntity with the order and HTTP status 200 OK.
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * PUT /api/admin/orders/{id}/status : Update order status.
     * @param id The order ID.
     * @param status The new order status.
     * @return ResponseEntity with the updated order and HTTP status 200 OK.
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status) {
        OrderResponse updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    // User Management Methods

    /**
     * GET /api/admin/users : Get all users.
     * @return ResponseEntity with list of all users and HTTP status 200 OK.
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/admin/users/{id} : Get user by ID.
     * @param id The user ID.
     * @return ResponseEntity with the user and HTTP status 200 OK.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
