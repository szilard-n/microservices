package com.example.inventoryservice.repository;

import com.example.inventoryservice.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findByProductId(UUID productId);
}
