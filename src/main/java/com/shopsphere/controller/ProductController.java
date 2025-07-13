package com.shopsphere.controller;

import com.shopsphere.model.Product;
import com.shopsphere.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products : Get all products.
     * @return ResponseEntity with a list of products and HTTP status 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id} : Get a product by ID.
     * @param id The ID of the product to retrieve.
     * @return ResponseEntity with the product and HTTP status 200 OK.
     * @throws ResponseStatusException 404 Not Found if product not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * POST /api/products : Create a new product.
     * @param product The product object to create.
     * @return ResponseEntity with the created product and HTTP status 201 Created.
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);

        // Build the location URI for the newly created product
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.getProductId())
                .toUri();

        // Return 201 Created with Location header and the created product in body
        return ResponseEntity.created(location).body(createdProduct);
//        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct)
    }

    /**
     * PUT /api/products/{id} : Update an existing product.
     * @param id The ID of the product to update.
     * @param productDetails The updated product details.
     * @return ResponseEntity with the updated product and HTTP status 200 OK.
     * @throws ResponseStatusException 404 Not Found if product not found, 400 Bad Request for validation.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * DELETE /api/products/{id} : Delete a product by ID.
     * @param id The ID of the product to delete.
     * @return ResponseEntity with HTTP status 204 No Content.
     * @throws ResponseStatusException 404 Not Found if product not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * GET /api/products/search/name?name={productName} : Find product by name.
     * @param name The name of the product.
     * @return ResponseEntity with the product and HTTP status 200 OK.
     */
    @GetMapping("/search/name")
    public ResponseEntity<Product> getProductByName(@RequestParam String name) {
        Product product = productService.getProductByName(name);
        return ResponseEntity.ok(product);
    }

    /**
     * GET /api/products/search/price-range?min={minPrice}&max={maxPrice} : Find products in a price range.
     * @param minPrice The minimum price.
     * @param maxPrice The maximum price.
     * @return ResponseEntity with a list of products.
     */
    @GetMapping("/search/price-range")
    public ResponseEntity<List<Product>> getProductsInPriceRange(
            @RequestParam java.math.BigDecimal minPrice,
            @RequestParam java.math.BigDecimal maxPrice) {
        List<Product> products = productService.getProductsInPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/search/low-stock?threshold={quantity} : Find products with low stock.
     * @param threshold The stock quantity threshold.
     * @return ResponseEntity with a list of products.
     */
    @GetMapping("/search/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam Integer threshold) {
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
}
