package com.example.inventoryservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "t_inventory")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(
            name = "id",
            updatable = false
    )
    private UUID id;

    @Column(
            name = "product_id",
            nullable = false
    )
    private UUID productId;

    @Column(
            name = "quantity",
            nullable = false
    )
    private int quantity;
}
