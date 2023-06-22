package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryDto;
import com.example.inventoryservice.dto.InventoryUpdateRequest;
import com.example.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public InventoryDto getInventory(@PathVariable UUID productId) {
        return inventoryService.getInventoryForProduct(productId);
    }

    @PutMapping("/purchase")
    @ResponseStatus(HttpStatus.OK)
    public void updateInventoryForPurchase(@RequestBody @Valid InventoryUpdateRequest inventoryUpdateRequest) {
        inventoryService.updateQuantityForPurchase(inventoryUpdateRequest);
    }

    @PutMapping("/restock")
    @ResponseStatus(HttpStatus.OK)
    public void updateInventoryForRestock(@RequestBody @Valid InventoryUpdateRequest inventoryUpdateRequest) {
        inventoryService.updateQuantityForRestock(inventoryUpdateRequest);
    }

    @PostMapping("/add-stock")
    @ResponseStatus(HttpStatus.CREATED)
    public void addNewItemToInventory(@RequestBody @Valid InventoryUpdateRequest inventoryUpdateRequest) {
        inventoryService.addNewItemToInventory(inventoryUpdateRequest);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeProductFromInventory(@PathVariable UUID productId) {
        inventoryService.removeProductFromInventory(productId);
    }
}
