package com.asg.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


// Invoice Details DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long stockPoid;
    private Long stockUnitPoid;
    private Long quantity;
    private BigDecimal price;
    private Long discount;
    private Long amount;
    private Long baseAmt;
    private Long taxPoid;
    private Long taxPercentage;
    private Long taxAmount;
    private Long netSales;
    private Long netDiscount;
    private Long incentive;
    private Long itemGp;
    private Long itemGpPer;
    private Long costAmt;
    private Long purCost;
    private Long purchasePrice;
    private Long purchaseQty;
    private String itemType;
    private Long costCenterPoid;
    private Long dnPoidLinkFk;
    private Long detRowIdChrgFk;
    private Long quotationPoid;
    private Long quotationDetRowId;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
    
    // LOV Details
    private LovDetailDto stockDetails;
    private LovDetailDto costCenterDetails;
    private LovDetailDto taxDetails;
    
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
