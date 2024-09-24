package com.example.InventoryService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Inventory {

    @Id
    private String productId;
    private int availableStock;

    public Inventory() {}

    public Inventory(String productId, int availableStock) {
        this.productId = productId;
        this.availableStock = availableStock;
    }
}
