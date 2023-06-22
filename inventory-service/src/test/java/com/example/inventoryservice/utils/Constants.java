package com.example.inventoryservice.utils;

import com.example.inventoryservice.model.Inventory;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class Constants {

    public static final Inventory INVENTORY_1 = Inventory.builder()
            .id(UUID.fromString("a07102fc-dae4-4891-92d9-3ce7e5a44714"))
            .productId(UUID.fromString("029e11f8-4a88-4231-8d77-f72170df3afb"))
            .quantity(250)
            .build();

    public static final Inventory INVENTORY_2 = Inventory.builder()
            .id(UUID.fromString("1e2096bd-d92b-4df4-aeb2-b1443aae81b7"))
            .productId(UUID.fromString("91a1d3dd-4c74-43a5-ae6c-de8e0495b06c"))
            .quantity(20)
            .build();
}
