package com.flashsalex.inventory.repository;

import com.flashsalex.inventory.entity.InventoryStock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stock from InventoryStock stock where stock.productId = :productId")
    Optional<InventoryStock> findByProductIdForUpdate(@Param("productId") String productId);
}
