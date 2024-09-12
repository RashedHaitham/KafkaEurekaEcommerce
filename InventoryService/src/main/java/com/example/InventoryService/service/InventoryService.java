package com.example.InventoryService.service;

import com.example.InventoryService.entity.Inventory;
import com.example.InventoryService.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class InventoryService {

    private static final Logger logger = Logger.getLogger(InventoryService.class.getName());

    @Autowired
    private InventoryRepository inventoryRepository;


    public boolean reduceStock(String productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);

        if (inventory != null && inventory.getAvailableStock() >= quantity) {
            // Synchronization or optimistic locking should be considered here to avoid race conditions
            inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
            inventoryRepository.save(inventory);
            logger.info("Reduced stock for product: " + productId + " by " + quantity);
            return true;
        }

        logger.warning("Failed to reduce stock for product: " + productId + ". Available stock: " + (inventory != null ? inventory.getAvailableStock() : "N/A"));
        return false;
    }


    public boolean addStock(Inventory inventory) {
        try {
            Inventory savedInventory = inventoryRepository.save(inventory);
            logger.info("Added stock for product: " + savedInventory.getProductId());
            return true;
        } catch (Exception e) {
            logger.severe("Failed to add stock: " + e.getMessage());
        }
        return false;
    }


    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory getInventory(String productId) {
        return inventoryRepository.findByProductId(productId);
    }
}
