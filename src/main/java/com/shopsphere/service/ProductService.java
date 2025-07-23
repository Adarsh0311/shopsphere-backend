package com.shopsphere.service;

import com.shopsphere.dto.ProductRequest; // New import
import com.shopsphere.dto.ProductResponse; // New import
import com.shopsphere.model.Category; // New import
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
    private final CategoryService categoryService;

    /**
     * Converts Product entity to ProductResponse DTO.
     * [NOTE] Use a dedicated mapper class (like MapStruct or ModelMapper) for complex mappings,
     * but for simple cases, direct conversion methods are fine.
     */
    private ProductResponse convertToDto(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getCategoryId());
            dto.setCategoryName(product.getCategory().getName());
        }
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    /**
     * Fetches all products and converts to DTOs.
     * @return A list of ProductResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto) // Use method reference for mapping
                .toList();
    }

    /**
     * Fetches a product by its ID and converts to DTO.
     * @param productId The ID of the product to find.
     * @return The found ProductResponse DTO.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));
        return convertToDto(product);
    }

    // --- for internal use ---
    @Transactional(readOnly = true)
    Product getProductEntityById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));
    }

    /**
     * Creates a new product from DTO.
     * @param request The ProductRequest DTO.
     * @return The created ProductResponse DTO.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(request.getCategoryId());
            product.setCategory(category);
            // category.addProduct(product); // Not needed here as product.setCategory handles the relationship
        }


        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product price cannot be negative.");
        }
        if (product.getStockQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock quantity cannot be negative.");
        }

        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    /**
     * Updates an existing product from DTO.
     * @param productId The ID of the product to update.
     * @param request The ProductRequest DTO with updated details.
     * @return The updated ProductResponse DTO.
     */
    @Transactional
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(request.getCategoryId());
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null); // Allow unsetting category
        }

        // Re-validate updated product details
        if (existingProduct.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Updated product price cannot be negative.");
        }
        if (existingProduct.getStockQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Updated stock quantity cannot be negative.");
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    /**
     * Deletes a product by its ID.
     */
    @Transactional
    public void deleteProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId);
        }
        productRepository.deleteById(productId);
    }

    // --- Custom query methods (updated to return DTOs) ---
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0 || minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid price range.");
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByName(String name) {
        Product product = productRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with name: " + name));
        return convertToDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(Integer threshold) {
        if (threshold < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock threshold cannot be negative.");
        }
        return productRepository.findByStockQuantityLessThanEqual(threshold).stream()
                .filter(p -> p.getStockQuantity() <= threshold)
                .map(this::convertToDto)
                .toList();
    }

    @Transactional
    public int updateProductStockQuantity(String productId, Integer stockQuantity) {
        return productRepository.updateProductStockQuantity(productId, stockQuantity);
    }
}