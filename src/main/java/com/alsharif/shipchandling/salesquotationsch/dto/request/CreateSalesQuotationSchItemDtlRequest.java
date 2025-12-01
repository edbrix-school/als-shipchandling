package com.alsharif.shipchandling.salesquotationsch.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSalesQuotationSchItemDtlRequest {
    private Long detRowId; // Required for UPDATE and DELETE actions
    
    private Long stockPoid;
    
    private Long quantity;
    
    private Long price;
    
    private Long discount;
    
    private Long amount;
    
    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
    
    private Long stockUnitPoid;
    
    private Long adjQuantity;
    
    private Long cost;
    
    private Long lastRate1;
    
    private Long lastRate2;
    
    @Size(max = 1, message = "Delivery select must not exceed 1 character")
    private String deliverySelect;
    
    @Size(max = 50, message = "DN reference number must not exceed 50 characters")
    private String dnRefNo;
    
    private Long gpAmount;
    
    private Long gpPercentage;
    
    private Long totCost;
    
    private Long purchasePrice;
    
    private Long purchaseQty;
    
    @Size(max = 50, message = "Item type must not exceed 50 characters")
    private String itemType;
    
    @Size(max = 50, message = "Reference document ID must not exceed 50 characters")
    private String refDocId;
    
    private Long refPoid;
    
    private Long taxPoid;
    
    private Long taxAmount;
    
    private Long taxPercentage;
    
    @Size(max = 1, message = "VAT modified must not exceed 1 character")
    private String vatModified;
    
    private String actionType; // "UPDATE", "DELETE", or null/"CREATE" for new items
}
