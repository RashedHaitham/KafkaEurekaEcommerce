package com.example.InventoryService.controller;

import com.example.InventoryService.entity.Inventory;
import com.example.InventoryService.kafka.InventoryProducer;
import com.example.InventoryService.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/update")
    public ResponseEntity<String> updateInventory(@RequestBody Inventory inventory) {
        inventoryProducer.sendInventoryUpdate(inventory);
        return ResponseEntity.ok("Inventory updated successfully");
    }

    @PostMapping("/add")
    public ResponseEntity<String> addInventory(@RequestBody Inventory inventory) {
        inventoryProducer.addInventory(inventory);
        return ResponseEntity.ok("Inventory added successfully");
    }

    @GetMapping("/products")
    public ResponseEntity<List<Inventory>> getInventory() {
        List<Inventory> inventories = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventories);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Inventory> checkProduct(@PathVariable String id) {
        Inventory product = inventoryService.getInventory(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        inventoryProducer.getProduct(product);
        return ResponseEntity.ok(product);
    }
}
