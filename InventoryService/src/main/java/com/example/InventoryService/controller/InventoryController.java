package com.example.InventoryService.controller;

import com.example.InventoryService.entity.Inventory;
import com.example.InventoryService.kafka.InventoryProducer;
import com.example.InventoryService.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryProducer inventoryProducer;
    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryProducer inventoryProducer, InventoryService inventoryService) {
        this.inventoryProducer = inventoryProducer;
        this.inventoryService = inventoryService;
    }

    // Update existing inventory item
    @PutMapping("/{id}")
    public ResponseEntity<String> updateInventory(@PathVariable String id, @Valid @RequestBody Inventory inventory) {
        inventory.setProductId(id); // Ensure the ID in the path is the same as in the request body
        boolean updated = inventoryService.updateInventory(inventory);
        if (updated) {
            inventoryProducer.sendInventoryUpdate(inventory);
            return ResponseEntity.ok("Inventory updated successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventory not found");
    }

    // Add new inventory item
    @PostMapping
    public ResponseEntity<String> addInventory(@Valid @RequestBody Inventory inventory) {
        boolean created = inventoryService.addStock(inventory);
        if (created) {
            inventoryProducer.addInventory(inventory);
            return ResponseEntity.status(HttpStatus.CREATED).body("Inventory added successfully");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Inventory already exists");
    }

    // Retrieve all inventory items
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> inventories = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventories);
    }

    // Retrieve a specific product by ID
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventory(@PathVariable String id) {
        Inventory product = inventoryService.getInventory(id);
        if (product != null) {
            inventoryProducer.getProduct(product);
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Delete an inventory item
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInventory(@PathVariable String id) {
        boolean deleted = inventoryService.deleteInventory(id);
        if (deleted) {
            return ResponseEntity.ok("Inventory deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventory not found");
    }
}
