package com.shopsphere.repository;

import com.shopsphere.model.Product;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByNameIgnoreCase(String name);

    //List<Product> findByCategoryId(String categoryId);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByStockQuantityLessThanEqual(Integer stockQuantity);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:searchTerm%")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    @Modifying
    @Query("UPDATE Product p set p.stockQuantity = :stockQuantity WHERE p.productId = :productId")
    int updateProductStockQuantity(String productId, Integer stockQuantity);

    List<Product> findAllByCategoryCategoryId(String categoryId);

}
