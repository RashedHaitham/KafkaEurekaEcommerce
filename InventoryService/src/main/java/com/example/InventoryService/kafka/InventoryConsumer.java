package com.example.InventoryService.kafka;

import com.example.InventoryService.entity.Inventory;
import com.example.InventoryService.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class InventoryConsumer {

    private static final Logger logger = Logger.getLogger(InventoryConsumer.class.getName());
    @Autowired
    InventoryService inventoryService;

    @KafkaListener(topics = "inventory-updates", groupId = "inventoryService")
    public void consumeInventoryUpdate(Inventory inventory) {
        logger.info("Received inventory update: " + inventory);
        logger.info("Stock updated successfully for product: " + inventory.getProductId());

    }

    @KafkaListener(topics = "inventory-add", groupId = "inventoryService")
    public void consumeInventoryAdd(Inventory inventory) {
        logger.info("Received inventory add: " + inventory);
        boolean stockAdded = inventoryService.addStock(inventory);

        if (stockAdded) {
            logger.info(inventory + " added successfully");
        } else {
            logger.warning("Failed to add product: " + inventory);
        }
    }

    @KafkaListener(topics = "inventory-get-all", groupId = "inventoryService")
    public void consumeInventoryGetAll() {
        List<Inventory> inventories = inventoryService.getAllInventory();
        logger.info("Received request to get all inventory: " + inventories);

        if (inventories.isEmpty()) {
            logger.info("No inventory found");
        } else {
            logger.info("Inventory found: " + inventories);
        }
    }

    @KafkaListener(topics = "inventory-get-one", groupId = "inventoryService")
    public void consumeInventoryGetOne(Inventory inventory) {
        Inventory product = inventoryService.getInventory(inventory.getProductId());
        logger.info("Received request to process product with ID: " + inventory.getProductId());

        if (product == null) {
            logger.warning("No inventory found for product ID: " + inventory.getProductId() +
                    ". Message will be discarded.");
            return;
        }

        logger.info("Product found and will be processed: " + product);
    }


}
