package com.example.productservice.dto.product;

import java.util.UUID;

public record ProductDto(
        UUID id,
        String name,
        double price) {
}
