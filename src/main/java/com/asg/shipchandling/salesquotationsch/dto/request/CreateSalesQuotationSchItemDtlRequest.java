package com.asg.shipchandling.salesquotationsch.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSalesQuotationSchItemDtlRequest {
    private Long detRowId; // Required for UPDATE and DELETE actions
    
    private Long stockPoid;
    
    private BigDecimal quantity;
    
    private BigDecimal price;
    
    private BigDecimal discount;
    
    private BigDecimal amount;
    
    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
    
    private Long stockUnitPoid;
    
    private BigDecimal adjQuantity;
    
    private BigDecimal cost;
    
    private BigDecimal lastRate1;
    
    private BigDecimal lastRate2;
    
    @Size(max = 1, message = "Delivery select must not exceed 1 character")
    private String deliverySelect;
    
    @Size(max = 50, message = "DN reference number must not exceed 50 characters")
    private String dnRefNo;
    
    private BigDecimal gpAmount;
    
    private BigDecimal gpPercentage;
    
    private BigDecimal totCost;
    
    private BigDecimal purchasePrice;
    
    private BigDecimal purchaseQty;
    
    @Size(max = 50, message = "Item type must not exceed 50 characters")
    private String itemType;
    
    @Size(max = 50, message = "Reference document ID must not exceed 50 characters")
    private String refDocId;
    
    private Long refPoid;
    
    private Long taxPoid;
    
    private BigDecimal taxAmount;
    
    private BigDecimal taxPercentage;
    
    @Size(max = 1, message = "VAT modified must not exceed 1 character")
    private String vatModified;
    
    /**
     * Item detail action type.
     *
     * PUT (/v1/sales-quotations/{transactionPoid}):
     * - noChange/noChanges or blank: skip (no DB change)
     * - isCreated (or CREATE): create (detRowId not required)
     * - isUpdated (or UPDATE): update (detRowId required)
     * - isDeleted (or DELETE): delete (detRowId required)
     *
     * POST (/v1/sales-quotations):
     * - all rows are created regardless of actionType (current implementation ignores actionType on create)
     */
    private String actionType;
}
