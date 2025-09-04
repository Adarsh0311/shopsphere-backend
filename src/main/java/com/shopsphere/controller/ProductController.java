package com.shopsphere.controller;

import com.shopsphere.dto.ProductResponse;
import com.shopsphere.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }


    @GetMapping("/search/name")
    public ResponseEntity<ProductResponse> getProductByName(@RequestParam String name) {
        ProductResponse product = productService.getProductByName(name);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsInPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<ProductResponse> products = productService.getProductsInPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts(@RequestParam Integer threshold) {
        List<ProductResponse> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String categoryId) {
        List<ProductResponse> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }
}