package com.flashsale.repository;

import com.flashsale.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);

    @Modifying
    @Query("UPDATE Product p SET p.stockCount = :stockCount WHERE p.id = :productId")
    void updateStockCount(@Param("productId") Long productId, @Param("stockCount") Integer stockCount);
}
