package com.example.productservice.dto.product;

import java.util.UUID;

public record ProductPreviewDto(
        UUID id,
        String name,
        String description,
        double price,
        int quantity) {
}
