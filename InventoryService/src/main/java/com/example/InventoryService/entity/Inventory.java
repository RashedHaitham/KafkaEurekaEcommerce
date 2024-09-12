package com.example.InventoryService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Inventory {

    @Id
    private String productId;
    private int availableStock;

    public Inventory() {}

    public Inventory(String productId, int availableStock) {
        this.productId = productId;
        this.availableStock = availableStock;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "productId='" + productId + '\'' +
                ", availableStock=" + availableStock +
                '}';
    }
}
