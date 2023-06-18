package com.example.productservice.dto.inventory;

import java.util.List;
import java.util.UUID;

public record InventoryForProductsRequest(List<UUID> productIds) {
}
