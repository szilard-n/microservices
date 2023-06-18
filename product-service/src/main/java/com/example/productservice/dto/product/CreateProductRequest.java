package com.example.productservice.dto.product;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @Size(min = 3, max = 100)
        String name,

        @Size(min = 10, max = 1000)
        String description,

        @Positive
        Double price,

        @Positive
        Integer quantity) {
}
