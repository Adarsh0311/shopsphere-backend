package com.shopsphere.service;

import com.shopsphere.model.Product;
import com.shopsphere.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * Fetches all products from the database.
     * @return A list of all products.
     */
    @Transactional(readOnly = true) // Read-only transaction, optimizes database read operations
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Fetches product by ID;
     * Throws ResponseStatusException if product not found
     * @param productId The id of the product
     * @return The found product
     */
    @Transactional(readOnly = true)
    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id " + productId));
    }

    /**
     * Creates a new product.
     * @param product The product object to save.
     * @return The saved product with generated ID and timestamps.
     */
    @Transactional
    public Product createProduct(Product product) {
        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price cannot be negative");
        }
        if (product.getStockQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock quantity cannot be negative");
        }

        //here @prePersist in the product entity will be called to handle createAt and updateAt
        return productRepository.save(product);
    }

    /**
     * Updates an existing product.
     * Throws ResponseStatusException if product not found.
     * @param productId The ID of the product to update.
     * @param productDetails The updated product details.
     * @return The updated product.
     */
    @Transactional
    public Product updateProduct(String productId, Product productDetails) {
        Product existingProduct = getProductById(productId);

        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStockQuantity(productDetails.getStockQuantity());
        existingProduct.setImageUrl(productDetails.getImageUrl());

        // The @PreUpdate in Product entity will handle updatedAt
        if (existingProduct.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Updated product price cannot be negative.");
        }
        if (existingProduct.getStockQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Updated stock quantity cannot be negative.");
        }

        return productRepository.save(existingProduct);
    }

    /**
     * Deletes a product by its ID.
     * Throws ResponseStatusException if product not found.
     * @param productId The ID of the product to delete.
     */
    @Transactional
    public void deleteProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId);
        }
        productRepository.deleteById(productId);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(maxPrice) > 0 || minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid price range");
        }

        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Transactional(readOnly = true)
    public Product getProductByName(String name) {
        return productRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with name: " + name));
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        if (threshold < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock threshold cannot be negative.");
        }
        return productRepository.findByStockQuantityLessThanEqual(threshold).stream()
                .filter(p -> p.getStockQuantity() <= threshold) //defensive filtering
                .toList();
    }

}
