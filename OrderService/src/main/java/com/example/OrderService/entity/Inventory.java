package com.example.OrderService.entity;

public class Inventory {

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
