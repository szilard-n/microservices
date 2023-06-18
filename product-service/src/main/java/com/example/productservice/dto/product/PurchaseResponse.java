package com.example.productservice.dto.product;

import java.sql.Timestamp;

public record PurchaseResponse(
        String productName,
        double price,
        int quantity,
        Timestamp date) {
}
