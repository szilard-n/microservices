package com.example.productservice.client;

import com.example.productservice.configuration.FeignClientConfiguration;
import com.example.productservice.dto.inventory.InventoryDto;
import com.example.productservice.dto.inventory.InventoryUpdateRequest;
import com.example.productservice.handler.FeignClientErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "inventoryClient",
        url = "${clients.inventory}",
        configuration = {FeignClientErrorDecoder.class, FeignClientConfiguration.class}
)
public interface InventoryClient {

    @GetMapping("/{productId}")
    InventoryDto getInventoryForProduct(@PathVariable UUID productId);

    @PutMapping("/purchase")
    void updateInventoryForPurchase(@RequestBody InventoryUpdateRequest inventoryUpdateRequest);

    @PutMapping("/restock")
    void updateInventoryForRestock(@RequestBody InventoryUpdateRequest inventoryUpdateRequest);

    @PostMapping("/add-stock")
    void addNewItemToInventory(@RequestBody InventoryUpdateRequest inventoryUpdateRequest);

    @DeleteMapping("/{productId}")
    void removeProductFromInventory(@PathVariable UUID productId);

}
