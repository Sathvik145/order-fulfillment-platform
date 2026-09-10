package com.sathvik.inventoryservice.controller;

import com.sathvik.inventoryservice.dto.InventoryResponse;
import com.sathvik.inventoryservice.dto.StockRequest;
import com.sathvik.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }

    @PutMapping("/stock")
    public InventoryResponse addOrUpdateStock(
            @Valid @RequestBody StockRequest request
    ) {
        return inventoryService.addOrUpdateStock(request);
    }
}