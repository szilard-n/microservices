package com.example.productservice.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record RestockRequest(
        @NotNull
        UUID productId,

        @Positive
        Integer quantity) {
}
