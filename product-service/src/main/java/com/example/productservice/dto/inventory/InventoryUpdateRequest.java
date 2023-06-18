package com.example.productservice.dto.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record InventoryUpdateRequest(
        @NotNull
        UUID productId,

        @Positive
        Integer quantity) {
}
