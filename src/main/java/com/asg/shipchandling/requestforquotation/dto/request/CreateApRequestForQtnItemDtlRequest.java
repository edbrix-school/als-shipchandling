package com.asg.shipchandling.requestforquotation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApRequestForQtnItemDtlRequest {
    @NotNull(message = "Stock is required")
    private Long stockPoid;

    private Long stockUnitPoid;

    @NotNull(message = "Quantity is required")
    private BigDecimal qty;

    private Long supplierPoid;
    private BigDecimal price;
    private BigDecimal purchaseQty;
    private BigDecimal purchasePrice;
    private Long taxPoid;
    private String remarks;
    
    // Action field for CRUD operations: "isCreated", "isUpdated", "isDeleted", "noChange"
    private String action;
    
    // Optional detRowId for identifying existing items during updates/deletes
    private Long detRowId;
}