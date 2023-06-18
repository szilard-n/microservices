package com.example.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record InventoryUpdateRequest(
        @NotNull
        UUID productId,

        @PositiveOrZero
        Integer quantity) {
}
