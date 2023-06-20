package com.example.inventoryservice.service;


import com.example.inventoryservice.dto.InventoryDto;
import com.example.inventoryservice.dto.InventoryUpdateRequest;
import com.example.inventoryservice.exception.InsufficientQuantityException;
import com.example.inventoryservice.exception.NotFoundException;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryDto getInventoryForProduct(UUID productId) {
        return new InventoryDto(findInventoryLocked(productId).getQuantity());
    }

    /**
     * This method will verify that the quantity of a product in stock
     * is greater than the amount purchased. If that's the case, then the
     * quantity will be decremented, otherwise an exception is thrown.
     *
     * @param inventoryUpdateRequest contains the product id the customer
     *                               wants to purchase and the quantity
     */
    @Transactional
    public void updateQuantityForPurchase(InventoryUpdateRequest inventoryUpdateRequest) {
        Inventory inventory = findInventoryLocked(inventoryUpdateRequest.productId());

        if (inventory.getQuantity() - inventoryUpdateRequest.quantity() < 0) {
            throw new InsufficientQuantityException("Not enough products in inventory!");
        }

        inventory.setQuantity(inventory.getQuantity() - inventoryUpdateRequest.quantity());
    }

    /**
     * This method increments the quantity of a product when the seller restocks.
     *
     * @param inventoryUpdateRequest contains the product id the seller
     *                               wants to restock and the quantity
     */
    @Transactional
    public void updateQuantityForRestock(InventoryUpdateRequest inventoryUpdateRequest) {
        Inventory inventory = findInventoryLocked(inventoryUpdateRequest.productId());
        inventory.setQuantity(inventory.getQuantity() + inventoryUpdateRequest.quantity());
        log.info("Product with id {} restocked with quantity {}", inventoryUpdateRequest.productId(), inventoryUpdateRequest.quantity());
    }

    /**
     * Adds previously non-existent items to the inventory.
     *
     * @param inventoryUpdateRequest contains the product id and the quantity
     */
    @Transactional
    public void addNewItemToInventory(InventoryUpdateRequest inventoryUpdateRequest) {
        Inventory inventory = Inventory.builder()
                .productId(inventoryUpdateRequest.productId())
                .quantity(inventoryUpdateRequest.quantity())
                .build();

        inventoryRepository.save(inventory);
        log.info("New product was added to the inventory with id {} and quantity {}", inventory.getId(), inventory.getQuantity());
    }

    @Transactional
    public void removeProductFromInventory(UUID productId) {
        inventoryRepository.delete(findInventoryLocked(productId));
        log.info("Product with id {} removed from inventory", productId);
    }

    /**
     * Finds a product based on the id. The method called from the repository uses
     * a pessimistic lock to ensure that the product's quantity is accurate between multiple
     * requests.
     */
    private Inventory findInventoryLocked(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException(MessageFormat.format("Product with id {} not found in inventory!", productId)));
    }
}
