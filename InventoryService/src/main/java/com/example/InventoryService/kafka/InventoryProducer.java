package com.example.InventoryService.kafka;

import com.example.InventoryService.entity.Inventory;
import com.example.InventoryService.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class InventoryProducer {

    private static final Logger logger = Logger.getLogger(InventoryProducer.class.getName());
    private static final String UPDATE_TOPIC = "inventory-updates";
    private static final String ADD_TOPIC = "inventory-add";
    private static final String GET_TOPIC_ALL = "inventory-get-all";
    private static final String GET_TOPIC_ONE = "inventory-get-one";

    private final KafkaTemplate<String, Inventory> kafkaTemplate;
    private final InventoryService inventoryService;

    @Autowired
    public InventoryProducer(KafkaTemplate<String, Inventory> kafkaTemplate, InventoryService inventoryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryService = inventoryService;
    }

    public void sendInventoryUpdate(Inventory inventory) {
        logger.info("Sending inventory update: " + inventory);
        kafkaTemplate.send(UPDATE_TOPIC, inventory);
    }

    public void addInventory(Inventory inventory) {
        logger.info("Adding inventory: " + inventory);
        kafkaTemplate.send(ADD_TOPIC, inventory);
    }

    public void getProducts() {
        logger.info("Getting products");
        List<Inventory> inventories = inventoryService.getAllInventory();
        inventories.forEach(inventory -> kafkaTemplate.send(GET_TOPIC_ALL, inventory));
    }

    public void getProduct(Inventory product) {
        logger.info("Getting product " + product);
        kafkaTemplate.send(GET_TOPIC_ONE, product);
        logger.info("Product " + product + " sent to " + GET_TOPIC_ONE);
    }
}
