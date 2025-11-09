package com.alsharif.shipchandling.deliverynote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public
class SalesDeliveryNoteItemDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long stockPoid;
    private Long stockUnitPoid;
    private Long quantity;
    private Long price;
    private Long discount;
    private Long amount;
    private String remarks;
    private String checkAll; // Y = included, N = excluded
    private Long qtnDetRowId; // Links to quotation detail
    private Long totCost;
    private String itemType;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
}
