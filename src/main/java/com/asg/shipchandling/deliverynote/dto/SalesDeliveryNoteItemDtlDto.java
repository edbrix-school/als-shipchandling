package com.asg.shipchandling.deliverynote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private BigDecimal price;
    private Long discount;
    private Long amount;
    private String remarks;
    private String checkAll; // Y = included, N = excluded
    private Long qtnDetRowId; // Links to quotation detail
    private Long totCost;
    private String itemType;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
    
    // LOV Details
    private LovDetailDto stockDetails;
    private LovDetailDto stockUnitDetails;
    
    // Inner class for LOV details
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LovDetailDto {
        private Long poid;
        private String code;
        private String description;
    }
}
