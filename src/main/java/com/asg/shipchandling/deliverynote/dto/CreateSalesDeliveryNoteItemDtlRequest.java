package com.asg.shipchandling.deliverynote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public
class CreateSalesDeliveryNoteItemDtlRequest {
    private Long detRowId; // Required for UPDATE and DELETE actions
    private Long stockPoid;
    private Long stockUnitPoid;
    private Long quantity;
    private Long price;
    private Long discount;
    private Long amount;
    private String remarks;
    private String checkAll = "Y"; // Y = included, N = excluded
    private Long qtnDetRowId; // Set when loading from quotation
    private Long totCost;
    private String itemType;
    private String actionType; // "isCreated", "isUpdated", "isDeleted", "noChange", or null (defaults to isCreated)
}
