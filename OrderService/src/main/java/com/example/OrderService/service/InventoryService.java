package com.example.OrderService.service;

import com.example.OrderService.entity.Inventory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "InventoryService")
public interface InventoryService {

    @PostMapping("/api/inventory/{id}")
    public ResponseEntity<Inventory> checkProduct(@PathVariable String id);
}
