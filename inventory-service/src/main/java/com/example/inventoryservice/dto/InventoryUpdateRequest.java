package com.example.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record InventoryUpdateRequest(
        @NotNull
        UUID productId,

        @Positive
        Integer quantity) {
}
